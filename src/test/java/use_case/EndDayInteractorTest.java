package use_case;

import data_access.InMemoryGameStateDataAccessObject;
import entity.GameState;
import entity.GameStatus;
import entity.Player;
import org.junit.jupiter.api.Test;
import use_case.end_day.EndDayInteractor;
import use_case.end_day.EndDayOutputBoundary;
import use_case.end_day.EndDayOutputData;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EndDayInteractorTest {

    private static class TestPresenter implements EndDayOutputBoundary {

        private EndDayOutputData outputData;
        private String error;

        @Override
        public void prepareSuccessView(EndDayOutputData outputData) {
            this.outputData = outputData;
        }

        @Override
        public void prepareFailView(String error){
            this.error = error;
        }
    }

    private static class TestSaveDao implements SaveGameDataAccessInterface {

        private GameState savedGame;
        private String savedUsername;
        private int saveCount;

        @Override
        public void save(String username, GameState game, String name) throws GamePersistenceException {
            savedUsername = username;
            savedGame = game;
            saveCount ++;
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

    @Test
    void endDayAdvancesToNextDay(){
        InMemoryGameStateDataAccessObject dao = new InMemoryGameStateDataAccessObject();
        Player player = new Player(50, 100, 50, 50);
        GameState game = new GameState(1, player, "bahen");

        dao.saveGameState(game);

        TestPresenter presenter = new TestPresenter();
        EndDayInteractor interactor = new EndDayInteractor(dao, presenter);

        interactor.execute();

        assertNotNull(presenter.outputData);
        assertNull(presenter.error);
        assertEquals(2, presenter.outputData.getDay());
        assertEquals(70, presenter.outputData.getEnergy());
        assertEquals(3, presenter.outputData.getRemainingTimeBlocks());
        assertEquals("residence", presenter.outputData.getCurrentLocationId());
        assertEquals(GameStatus.IN_PROGRESS, presenter.outputData.getStatus());
        assertEquals(2, dao.getGameState().getDay());
    }

    @Test
    void endDaySevenCanWin(){
        InMemoryGameStateDataAccessObject dao = new InMemoryGameStateDataAccessObject();
        Player player = new Player(80, 100, 60, 50);
        dao.saveGameState(new GameState(7, player, "residence"));
        TestPresenter presenter = new TestPresenter();
        EndDayInteractor interactor = new EndDayInteractor(dao, presenter);

        interactor.execute();

        assertNotNull(presenter.outputData);
        assertEquals(GameStatus.WON, presenter.outputData.getStatus());
    }

    @Test
    void endDaySevenCanLose(){
        InMemoryGameStateDataAccessObject dao = new InMemoryGameStateDataAccessObject();
        Player player = new Player(80, 100,59, 50);
        dao.saveGameState(new GameState(7, player, "residence"));

        TestPresenter presenter = new TestPresenter();
        EndDayInteractor interactor = new EndDayInteractor(dao, presenter);

        interactor.execute();

        assertNotNull(presenter.outputData);
        assertEquals(GameStatus.LOST, presenter.outputData.getStatus());
    }

    @Test
    void cannotEndDayWithoutActiveGame(){
        InMemoryGameStateDataAccessObject dao = new InMemoryGameStateDataAccessObject();
        TestPresenter presenter = new TestPresenter();
        EndDayInteractor interactor = new EndDayInteractor(dao, presenter);

        interactor.execute();

        assertNull(presenter.outputData);
        assertEquals("Start a new game first.", presenter.error);
    }

    @Test
    void cannotEndDayAfterGameIsOver(){
        InMemoryGameStateDataAccessObject dao = new InMemoryGameStateDataAccessObject();
        Player player = new Player(80, 100, 60, 50);
        GameState game = new GameState(7, player, "residence");
        game.startNextDay();

        dao.saveGameState(game);

        TestPresenter presenter = new TestPresenter();
        EndDayInteractor interactor = new EndDayInteractor(dao, presenter);

        interactor.execute();

        assertNull(presenter.outputData);
        assertEquals("This game is already over.", presenter.error);
        assertEquals(7, game.getDay());
    }

    @Test
    void endDayAutomaticallySavesGame() {
        InMemoryGameStateDataAccessObject dao = new InMemoryGameStateDataAccessObject();
        GameState game = GameState.startNew();
        dao.saveGameState(game);

        TestSaveDao saveDao = new TestSaveDao();
        AutoSave autoSave = new AutoSave(saveDao, () -> "testUser");

        TestPresenter presenter = new TestPresenter();
        EndDayInteractor interactor = new EndDayInteractor(dao, presenter, autoSave);

        interactor.execute();

        assertEquals(1, saveDao.saveCount);
        assertEquals("testUser", saveDao.savedUsername);
        assertNotNull(saveDao.savedGame);
        assertEquals(2, saveDao.savedGame.getDay());
        assertEquals(3, saveDao.savedGame.getRemainingTimeBlocks());
        assertEquals(game.getRunId(), saveDao.savedGame.getRunId());
        assertNotNull(presenter.outputData);
        assertNull(presenter.error);
    }

    @Test
    void autosaveFailureDoesNotPreventNextDay() {
        InMemoryGameStateDataAccessObject dao = new InMemoryGameStateDataAccessObject();
        dao.saveGameState(GameState.startNew());
        TestSaveDao saveDao = new TestSaveDao() {
            @Override
            public void save(String username, GameState game, String name)
                    throws GamePersistenceException {
                throw new GamePersistenceException("Test save failure",
                        new RuntimeException("Simulated failure"));
            }
        };

        AutoSave autoSave = new AutoSave(saveDao, () -> "testUser");
        TestPresenter presenter = new TestPresenter();
        EndDayInteractor interactor = new EndDayInteractor(dao, presenter, autoSave);

        interactor.execute();

        assertEquals(2, dao.getGameState().getDay());
        assertNotNull(presenter.outputData);
        assertEquals(2, presenter.outputData.getDay());
        assertEquals(3, presenter.outputData.getRemainingTimeBlocks());
        assertEquals("Autosave failed. Please save manually.", presenter.error);
    }
}
