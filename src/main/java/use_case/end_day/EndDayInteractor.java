package use_case.end_day;

import entity.GameState;
import entity.GameStatus;
import entity.Player;
import use_case.GameStateDataAccessInterface;
import use_case.AutoSave;
import use_case.GamePersistenceException;

public class EndDayInteractor implements EndDayInputBoundary {

    private final GameStateDataAccessInterface gameStateDataAccess;
    private final EndDayOutputBoundary presenter;
    private final AutoSave autoSave;

    public EndDayInteractor(GameStateDataAccessInterface gameStateDataAccess, EndDayOutputBoundary presenter){
        this(gameStateDataAccess, presenter, null);
    }

    public EndDayInteractor(GameStateDataAccessInterface gameStateDataAccess, EndDayOutputBoundary presenter, AutoSave autoSave){
        this.gameStateDataAccess = gameStateDataAccess;
        this.presenter = presenter;
        this.autoSave = autoSave;
    }

    @Override
    public void execute(){
        GameState gameState = gameStateDataAccess.getGameState();

        if (gameState == null){
            presenter.prepareFailView("Start a new game first.");
            return;
        }

        if (gameState.getStatus() != GameStatus.IN_PROGRESS){
            presenter.prepareFailView("This game is already over.");
            return;
        }

        GameStatus status = gameState.startNextDay();
        gameStateDataAccess.saveGameState(gameState);
        String autoSaveError = null;

        if(autoSave != null){
            try {
                autoSave.save(gameState);
            } catch (GamePersistenceException e) {
                autoSaveError = "Autosave failed. Please save manually.";
            }
        }

        Player player = gameState.getPlayer();
        EndDayOutputData outputData = new EndDayOutputData(
                gameState.getDay(),
                player.getEnergy(),
                player.getSanity(),
                player.getAcademicSurvival(),
                player.getMoney(),
                gameState.getRemainingTimeBlocks(),
                gameState.getCurrentLocationId(),
                status
        );

        presenter.prepareSuccessView(outputData);

        if (autoSaveError != null) {
            presenter.prepareFailView(autoSaveError);
        }
    }
}
