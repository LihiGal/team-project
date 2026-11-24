package use_case.quit_game;

import use_case.GameStateDataAccessInterface;

/**
 * Quits the current game without logging out the user.
 */
public class QuitGameInteractor implements QuitGameInputBoundary {

    private final GameStateDataAccessInterface activeGame;
    private final QuitGameOutputBoundary outputBoundary;

    public QuitGameInteractor(
            GameStateDataAccessInterface activeGame,
            QuitGameOutputBoundary outputBoundary) {
        this.activeGame = activeGame;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute() {
        activeGame.saveGameState(null);
        outputBoundary.prepareSuccessView();
    }
}