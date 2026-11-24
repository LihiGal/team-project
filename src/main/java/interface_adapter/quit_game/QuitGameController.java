package interface_adapter.quit_game;

import use_case.quit_game.QuitGameInputBoundary;

/**
 * Controller for the Quit Game use case.
 */
public class QuitGameController {

    private final QuitGameInputBoundary quitGameInteractor;

    public QuitGameController(QuitGameInputBoundary quitGameInteractor) {
        this.quitGameInteractor = quitGameInteractor;
    }

    public void execute() {
        quitGameInteractor.execute();
    }
}