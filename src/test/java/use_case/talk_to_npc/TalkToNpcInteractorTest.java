package use_case.talk_to_npc;

import entity.DialogueNode;
import entity.DialogueOption;
import entity.GameState;
import entity.GameStatus;
import entity.NPC;
import entity.Outcome;
import entity.Player;
import entity.StatEffects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.GameStateDataAccessInterface;
import use_case.AutoSave;
import use_case.SaveGameDataAccessInterface;
import use_case.GamePersistenceException;
import use_case.SavedGameSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TalkToNpcInteractorTest {

    /** Fake game store: holds whatever game the test gives it. */
    private static class FakeGameStateDataAccess implements GameStateDataAccessInterface {
        private GameState gameState;
        private int timesSaved = 0;

        @Override
        public void saveGameState(GameState gameState) {
            this.gameState = gameState;
            timesSaved = timesSaved + 1;
        }

        @Override
        public GameState getGameState() {
            return gameState;
        }
    }

    private static class FakeSaveDao implements SaveGameDataAccessInterface {

        private GameState savedGame;
        private String savedUsername;
        private int saveCount;

        @Override
        public void save(String username, GameState game, String name) throws GamePersistenceException {
            savedUsername = username;
            savedGame = game;
            saveCount++;
        }

        @Override
        public Optional<GameState> loadLatest(String username) {
            return Optional.ofNullable(savedGame);
        }

        @Override
        public Optional<GameState> load(String username, UUID runId) {
            return Optional.ofNullable(savedGame);
        }

        @Override
        public List<SavedGameSummary> list(String username) {
            return List.of();
        }

        @Override
        public boolean delete(String username, UUID runId) {
            return false;
        }
    }

    /** Fake dialogue store: knows about exactly one character. */
    private static class FakeDialogueDataAccess implements DialogueDataAccessInterface {
        private final NPC npc;

        FakeDialogueDataAccess(NPC npc) {
            this.npc = npc;
        }

        @Override
        public NPC getNpc(String npcId) {
            if (npc.getId().equals(npcId)) {
                return npc;
            }
            return null;
        }
    }

    /** Fake presenter: remembers what the interactor told it. */
    private static class FakePresenter implements TalkToNpcOutputBoundary {
        private StartTalkOutputData dialogueData;
        private ChooseOptionOutputData resultData;
        private String errorMessage;
        private boolean wentBackToGame = false;

        @Override
        public void prepareDialogueView(StartTalkOutputData outputData) {
            dialogueData = outputData;
        }

        @Override
        public void prepareResultView(ChooseOptionOutputData outputData) {
            resultData = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public void prepareGameView() {
            wentBackToGame = true;
        }
    }

    private FakeGameStateDataAccess gameStateDataAccess;
    private FakePresenter presenter;
    private TalkToNpcInteractor interactor;
    private FakeSaveDao saveDao;

    /**
     * Every test starts with a new game and a vendor who has one greeting
     * with two responses: a nice one (Energy -5, Sanity +10) and a rude
     * one (Sanity -10).
     */
    @BeforeEach
    void setUp() {
        Outcome niceOutcome = new Outcome.Builder()
                .effects(new StatEffects(-5, 10, 0, 0))
                .timeBlocksUsed(1)
                .message("She smiles.")
                .build();
        Outcome rudeOutcome = new Outcome.Builder()
                .effects(new StatEffects(0, -10, 0, 0))
                .timeBlocksUsed(1)
                .message("She glares.")
                .build();

        List<DialogueOption> options = new ArrayList<DialogueOption>();
        options.add(new DialogueOption("be_nice", "Be nice", niceOutcome));
        options.add(new DialogueOption("be_rude", "Be rude", rudeOutcome));

        List<DialogueNode> nodes = new ArrayList<DialogueNode>();
        nodes.add(new DialogueNode("greeting", "What'll it be?", options));
        NPC vendor = new NPC("vendor", "Vendor", "greeting", nodes);

        gameStateDataAccess = new FakeGameStateDataAccess();
        gameStateDataAccess.gameState = GameState.startNew();
        presenter = new FakePresenter();
        saveDao = new FakeSaveDao();
        AutoSave autoSave = new AutoSave(saveDao, () -> "testUser");

        interactor = new TalkToNpcInteractor(
                gameStateDataAccess, new FakeDialogueDataAccess(vendor), presenter, autoSave);
    }

    // ---------- start ----------

    @Test
    void startShowsTheGreetingAndBothResponses() {
        interactor.start(new StartTalkInputData("vendor"));

        StartTalkOutputData output = presenter.dialogueData;
        assertNotNull(output);
        assertEquals("vendor", output.getNpcId());
        assertEquals("Vendor", output.getNpcName());
        assertEquals("greeting", output.getNodeId());
        assertEquals("What'll it be?", output.getText());
        assertEquals(2, output.getOptionIds().size());
        assertEquals("be_nice", output.getOptionIds().get(0));
        assertEquals("Be nice", output.getOptionTexts().get(0));
        assertEquals("be_rude", output.getOptionIds().get(1));
        assertEquals("Be rude", output.getOptionTexts().get(1));
        assertNull(presenter.errorMessage);
    }

    @Test
    void startDoesNotChangeAnyStats() {
        interactor.start(new StartTalkInputData("vendor"));

        Player player = gameStateDataAccess.getGameState().getPlayer();
        assertEquals(100, player.getEnergy());
        assertEquals(100, player.getSanity());
        assertEquals(50, player.getAcademicSurvival());
        assertEquals(50, player.getMoney());
    }

    @Test
    void startFailsWhenThereIsNoGame() {
        gameStateDataAccess.gameState = null;

        interactor.start(new StartTalkInputData("vendor"));

        assertNotNull(presenter.errorMessage);
        assertNull(presenter.dialogueData);
    }

    @Test
    void startFailsWhenTheGameIsAlreadyLost() {
        // Energy starts at 5, and losing 5 makes it 0, which loses the game.
        GameState lostGame = new GameState(1, new Player(5, 50, 50, 50), "residence");
        lostGame.apply(new Outcome.Builder().effects(new StatEffects(-5, 0, 0, 0)).build());
        gameStateDataAccess.gameState = lostGame;

        interactor.start(new StartTalkInputData("vendor"));

        assertNotNull(presenter.errorMessage);
        assertNull(presenter.dialogueData);
    }

    @Test
    void startFailsForACharacterThatDoesNotExist() {
        interactor.start(new StartTalkInputData("nobody"));

        assertNotNull(presenter.errorMessage);
        assertNull(presenter.dialogueData);
    }

    @Test
    void startFailsWhenTheCharacterHasNoStartNode() {
        NPC brokenNpc = new NPC("broken", "Broken", "missing_node", new ArrayList<DialogueNode>());
        interactor = new TalkToNpcInteractor(
                gameStateDataAccess, new FakeDialogueDataAccess(brokenNpc), presenter);

        interactor.start(new StartTalkInputData("broken"));

        assertNotNull(presenter.errorMessage);
        assertNull(presenter.dialogueData);
    }

    // ---------- choose ----------

    @Test
    void chooseAppliesTheStatChangeToTheGame() {
        interactor.choose(new ChooseOptionInputData("vendor", "greeting", "be_nice"));

        Player player = gameStateDataAccess.getGameState().getPlayer();
        assertEquals(95, player.getEnergy());
        assertEquals(100, player.getSanity());   // 100 + 10 is capped at 100
        assertEquals(50, player.getAcademicSurvival());
        assertEquals(50, player.getMoney());
    }

    @Test
    void chooseTellsThePresenterTheMessageAndTheNewStats() {
        interactor.choose(new ChooseOptionInputData("vendor", "greeting", "be_rude"));

        ChooseOptionOutputData output = presenter.resultData;
        assertNotNull(output);
        assertEquals("She glares.", output.getMessage());
        assertEquals(100, output.getEnergy());
        assertEquals(90, output.getSanity());
        assertEquals(50, output.getAcademicSurvival());
        assertEquals(50, output.getMoney());
        assertFalse(output.isGameOver());
        assertNull(presenter.errorMessage);
    }

    @Test
    void chooseSavesTheChangedGame() {
        GameState gameBefore = gameStateDataAccess.getGameState();

        interactor.choose(new ChooseOptionInputData("vendor", "greeting", "be_nice"));

        assertEquals(1, gameStateDataAccess.timesSaved);
        assertSame(gameBefore, gameStateDataAccess.getGameState());
    }

    @Test
    void chooseReportsGameOverWhenAStatHitsZero() {
        // Sanity starts at 10, and the rude response costs exactly 10.
        gameStateDataAccess.gameState = new GameState(1, new Player(50, 10, 50, 50), "residence");

        interactor.choose(new ChooseOptionInputData("vendor", "greeting", "be_rude"));

        assertTrue(presenter.resultData.isGameOver());
        assertEquals(0, presenter.resultData.getSanity());
        assertEquals(GameStatus.LOST, gameStateDataAccess.getGameState().getStatus());
    }

    @Test
    void chooseFailsWhenThereIsNoGame() {
        gameStateDataAccess.gameState = null;

        interactor.choose(new ChooseOptionInputData("vendor", "greeting", "be_nice"));

        assertNotNull(presenter.errorMessage);
        assertNull(presenter.resultData);
    }

    @Test
    void chooseFailsWhenTheGameIsAlreadyLost() {
        GameState lostGame = new GameState(1, new Player(5, 50, 50, 50), "residence");
        lostGame.apply(new Outcome.Builder().effects(new StatEffects(-5, 0, 0, 0)).build());
        gameStateDataAccess.gameState = lostGame;

        interactor.choose(new ChooseOptionInputData("vendor", "greeting", "be_nice"));

        assertNotNull(presenter.errorMessage);
        assertNull(presenter.resultData);
    }

    @Test
    void chooseFailsForACharacterThatDoesNotExist() {
        interactor.choose(new ChooseOptionInputData("nobody", "greeting", "be_nice"));

        assertNotNull(presenter.errorMessage);
        assertNull(presenter.resultData);
    }

    @Test
    void chooseFailsForANodeThatDoesNotExist() {
        interactor.choose(new ChooseOptionInputData("vendor", "missing_node", "be_nice"));

        assertNotNull(presenter.errorMessage);
        assertNull(presenter.resultData);
    }

    @Test
    void chooseFailsForAnOptionThatDoesNotExistAndChangesNothing() {
        interactor.choose(new ChooseOptionInputData("vendor", "greeting", "sing_a_song"));

        assertNotNull(presenter.errorMessage);
        assertNull(presenter.resultData);
        assertEquals(100, gameStateDataAccess.getGameState().getPlayer().getEnergy());
        assertEquals(100, gameStateDataAccess.getGameState().getPlayer().getSanity());
        assertEquals(0, gameStateDataAccess.timesSaved);
    }

    // ---------- leave ----------

    @Test
    void leaveGoesBackToTheGameScreen() {
        interactor.leave();

        assertTrue(presenter.wentBackToGame);
    }

    @Test
    void autosaveOccursOnlyWhenDayEnds() {
        ChooseOptionInputData input =
                new ChooseOptionInputData("vendor", "greeting", "be_nice");

        interactor.choose(input);
        assertEquals(0, saveDao.saveCount);

        interactor.choose(input);
        assertEquals(0, saveDao.saveCount);

        interactor.choose(input);
        assertEquals(1, saveDao.saveCount);

        assertEquals("testUser", saveDao.savedUsername);
        assertNotNull(saveDao.savedGame);
        assertEquals(2, saveDao.savedGame.getDay());
        assertEquals(3, saveDao.savedGame.getRemainingTimeBlocks());
        assertNull(presenter.errorMessage);
    }

    @Test
    void autosaveStoresWinningGame() {
        gameStateDataAccess.gameState =
                new GameState(7, new Player(100, 100, 60, 50), "residence");

        ChooseOptionInputData input =
                new ChooseOptionInputData("vendor", "greeting", "be_nice");

        interactor.choose(input);
        interactor.choose(input);
        interactor.choose(input);

        assertEquals(1, saveDao.saveCount);
        assertEquals(GameStatus.WON, saveDao.savedGame.getStatus());
        assertTrue(presenter.resultData.isGameOver());
    }

    @Test
    void autosaveStoresLosingGame() {
        gameStateDataAccess.gameState =
                new GameState(7, new Player(100, 100, 59, 50), "residence");

        ChooseOptionInputData input =
                new ChooseOptionInputData("vendor", "greeting", "be_nice");

        interactor.choose(input);
        interactor.choose(input);
        interactor.choose(input);

        assertEquals(1, saveDao.saveCount);
        assertEquals(GameStatus.LOST, saveDao.savedGame.getStatus());
        assertTrue(presenter.resultData.isGameOver());
    }

    @Test
    void immediateLossDoesNotTriggerDayEndAutosave() {
        gameStateDataAccess.gameState =
                new GameState(1, new Player(5, 100, 50, 50), "residence");

        interactor.choose(
                new ChooseOptionInputData("vendor", "greeting", "be_nice"));

        assertEquals(0, saveDao.saveCount);
        assertEquals(GameStatus.LOST,
                gameStateDataAccess.getGameState().getStatus());
        assertTrue(presenter.resultData.isGameOver());
    }

    @Test
    void autosaveFailureDoesNotPreventDayTransition() {
        FakeSaveDao failingSaveDao = new FakeSaveDao() {
            @Override
            public void save(String username, GameState game, String name) throws GamePersistenceException {
                throw new GamePersistenceException("Test save failure", new RuntimeException("Simulated failure"));
            }
        };

        Outcome outcome = new Outcome.Builder()
                .timeBlocksUsed(1)
                .build();

        DialogueOption option = new DialogueOption("be_nice", "Be nice", outcome);
        DialogueNode node = new DialogueNode("greeting", "Hello", List.of(option));
        NPC vendor = new NPC("vendor", "Vendor", "greeting", List.of(node));
        AutoSave autoSave = new AutoSave(failingSaveDao, () -> "testUser");

        interactor = new TalkToNpcInteractor(gameStateDataAccess,
                new FakeDialogueDataAccess(vendor), presenter, autoSave);

        ChooseOptionInputData input = new ChooseOptionInputData("vendor", "greeting", "be_nice");

        interactor.choose(input);
        interactor.choose(input);
        interactor.choose(input);

        assertEquals(2, gameStateDataAccess.getGameState().getDay());
        assertEquals(3, gameStateDataAccess.getGameState()
                .getRemainingTimeBlocks());

        assertNotNull(presenter.resultData);
        assertEquals("Autosave failed. Please save manually.",
                presenter.errorMessage);
    }
}
