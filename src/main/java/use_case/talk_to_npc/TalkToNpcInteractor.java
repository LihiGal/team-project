package use_case.talk_to_npc;

import entity.DialogueNode;
import entity.DialogueOption;
import entity.GameState;
import entity.GameStatus;
import entity.NPC;
import entity.Player;
import use_case.GameStateDataAccessInterface;
import use_case.AutoSave;
import use_case.GamePersistenceException;

import java.util.ArrayList;
import java.util.List;

/**
 * The rules for talking to a character.
 *
 * Starting a conversation only looks things up. Choosing a response is
 * the part that changes the game: the response's Outcome is handed to
 * GameState.apply(), which is the one place where stats change. That is
 * the pattern every later action in the game follows.
 */
public class TalkToNpcInteractor implements TalkToNpcInputBoundary {

    private final GameStateDataAccessInterface gameStateDataAccess;
    private final DialogueDataAccessInterface dialogueDataAccess;
    private final TalkToNpcOutputBoundary presenter;
    private final AutoSave autoSave;

    public TalkToNpcInteractor(GameStateDataAccessInterface gameStateDataAccess,
                               DialogueDataAccessInterface dialogueDataAccess,
                               TalkToNpcOutputBoundary presenter) {
        this(gameStateDataAccess, dialogueDataAccess, presenter, null);
    }

    public TalkToNpcInteractor(GameStateDataAccessInterface gameStateDataAccess, DialogueDataAccessInterface dialogueDataAccess,
                               TalkToNpcOutputBoundary presenter, AutoSave autoSave){
        this.gameStateDataAccess = gameStateDataAccess;
        this.dialogueDataAccess = dialogueDataAccess;
        this.presenter = presenter;
        this.autoSave = autoSave;
    }

    @Override
    public void start(StartTalkInputData inputData) {
        GameState gameState = gameStateDataAccess.getGameState();
        String problem = findProblemWithGame(gameState);
        if (problem != null) {
            presenter.prepareFailView(problem);
            return;
        }

        NPC npc = dialogueDataAccess.getNpc(inputData.getNpcId());
        if (npc == null) {
            presenter.prepareFailView("There is nobody here to talk to.");
            return;
        }

        DialogueNode node = npc.getStartNode();
        if (node == null) {
            presenter.prepareFailView(npc.getName() + " has nothing to say right now.");
            return;
        }

        // Split the options into two lists of plain Strings for the presenter.
        List<String> optionIds = new ArrayList<String>();
        List<String> optionTexts = new ArrayList<String>();
        List<DialogueOption> options = node.getOptions();
        for (int i = 0; i < options.size(); i++) {
            optionIds.add(options.get(i).getId());
            optionTexts.add(options.get(i).getText());
        }

        StartTalkOutputData outputData = new StartTalkOutputData(
                npc.getId(), npc.getName(), node.getId(), node.getText(),
                optionIds, optionTexts);
        presenter.prepareDialogueView(outputData);
    }

    @Override
    public void choose(ChooseOptionInputData inputData) {
        GameState gameState = gameStateDataAccess.getGameState();
        String problem = findProblemWithGame(gameState);
        if (problem != null) {
            presenter.prepareFailView(problem);
            return;
        }

        NPC npc = dialogueDataAccess.getNpc(inputData.getNpcId());
        if (npc == null) {
            presenter.prepareFailView("There is nobody here to talk to.");
            return;
        }

        DialogueNode node = npc.getNode(inputData.getNodeId());
        if (node == null) {
            presenter.prepareFailView(npc.getName() + " has lost track of the conversation.");
            return;
        }

        DialogueOption option = node.getOption(inputData.getOptionId());
        if (option == null) {
            presenter.prepareFailView("That is not something you can say here.");
            return;
        }

        // This is where the game actually changes.
        int previousDay = gameState.getDay();
        GameStatus status = gameState.apply(option.getOutcome());
        gameStateDataAccess.saveGameState(gameState);

        boolean dayEnded = gameState.getDay() != previousDay || (previousDay == GameState.TOTAL_DAYS &&
                status != GameStatus.IN_PROGRESS && gameState.getRemainingTimeBlocks() == 0 &&
                !gameState.getPlayer().isOutOfStat());

        String autoSaveError = null;

        if (dayEnded && autoSave != null){
            try {
                autoSave.save(gameState);
            } catch (GamePersistenceException e) {
                autoSaveError = "Autosave failed. Please save manually.";
            }
        }

        Player player = gameState.getPlayer();
        ChooseOptionOutputData outputData = new ChooseOptionOutputData(
                option.getOutcome().getMessage(),
                player.getEnergy(),
                player.getSanity(),
                player.getAcademicSurvival(),
                player.getMoney(),
                gameState.getDay(),
                gameState.getRemainingTimeBlocks(),
                gameState.getCurrentLocationId(),
                status);
        presenter.prepareResultView(outputData);

        if (autoSaveError != null) {
            presenter.prepareFailView(autoSaveError);
        }
    }

    @Override
    public void leave() {
        presenter.prepareGameView();
    }

    /**
     * Checks that there is a game and that it is still being played.
     *
     * @return a message for the player if they cannot talk right now,
     *         or null if everything is fine
     */
    private String findProblemWithGame(GameState gameState) {
        if (gameState == null) {
            return "Start a new game before talking to anyone.";
        }
        if (gameState.getStatus() == GameStatus.LOST || gameState.getStatus() == GameStatus.WON) {
            return "This game is over. Start a new game to keep playing.";
        }
        return null;
    }
}
