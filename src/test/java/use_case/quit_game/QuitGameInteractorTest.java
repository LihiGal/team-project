package use_case.quit_game;

import data_access.InMemoryGameStateDataAccessObject;
import entity.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuitGameInteractorTest {

    @Test
    void clearsActiveGameAndPreparesSuccessView() {
        InMemoryGameStateDataAccessObject activeGame =
                new InMemoryGameStateDataAccessObject();

        activeGame.saveGameState(GameState.startNew());

        assertNotNull(activeGame.getGameState());

        boolean[] successCalled = {false};

        QuitGameOutputBoundary presenter =
                () -> successCalled[0] = true;

        QuitGameInteractor interactor =
                new QuitGameInteractor(activeGame, presenter);

        interactor.execute();

        assertNull(activeGame.getGameState());
        assertTrue(successCalled[0]);
    }
}