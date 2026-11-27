package interface_adapter.persistence;

import entity.GameStatus;
import interface_adapter.ViewManagerModel;
import interface_adapter.hud.HudState;
import interface_adapter.hud.HudViewModel;
import interface_adapter.result.ResultState;
import interface_adapter.result.ResultViewModel;
import use_case.ManageSavesInteractor;
import use_case.SavedGameSummary;
import use_case.load_game.LoadGameOutputBoundary;
import use_case.load_game.LoadGameOutputData;
import use_case.save_game.SaveGameOutputBoundary;

import javax.swing.SwingUtilities;
import java.util.List;

public final class PersistencePresenter implements SaveGameOutputBoundary, LoadGameOutputBoundary,
        ManageSavesInteractor.OutputBoundary {
    private final PersistenceViewModel feedback;
    private final HudViewModel hud;
    private final ViewManagerModel views;
    private final String gameViewName;
    private final ResultViewModel resultViewModel;

    public PersistencePresenter(PersistenceViewModel feedback, HudViewModel hud,
                                ViewManagerModel views, String gameViewName, ResultViewModel resultViewModel) {
        this.feedback = feedback;
        this.hud = hud;
        this.views = views;
        this.gameViewName = gameViewName;
        this.resultViewModel = resultViewModel;
    }

    @Override
    public void prepareSaveSuccessView() {
        showMessage("Game saved.");
    }

    @Override
    public void prepareNameView() {
        onEdt(() -> {
            updateMessage("");
            feedback.firePropertyChanged(PersistenceViewModel.REQUEST_NAME);
        });
    }

    @Override
    public void prepareSavedGamesView(List<SavedGameSummary> saves, String message) {
        onEdt(() -> {
            feedback.setSaves(saves);
            updateMessage(message);
            feedback.firePropertyChanged(PersistenceViewModel.SAVES_UPDATED);
        });
    }

    @Override
    public void prepareLoadSuccessView(LoadGameOutputData output) {
        onEdt(() -> {
            HudState state = new HudState();
            state.setDay(output.day());
            state.setTotalDays(output.totalDays());
            state.setEnergy(output.energy());
            state.setSanity(output.sanity());
            state.setAcademicSurvival(output.academicSurvival());
            state.setMoney(output.money());
            state.setRemainingTimeBlocks(output.remainingTimeBlocks());
            hud.setState(state);
            hud.firePropertyChanged();

            if (output.status() == GameStatus.IN_PROGRESS) {
                views.setState(gameViewName);
            } else {
                ResultState resultState = new ResultState();

                boolean won = output.status() == GameStatus.WON;
                resultState.setWon(won);

                if (won) {
                    resultState.setMessage("You survived the week at UofT!");
                } else if (output.energy() == 0 || output.sanity() == 0 || output.academicSurvival() == 0){
                    resultState.setMessage("One of your stats has reached 0.");
                } else {
                    resultState.setMessage("Your Academic Survival is below 60");
                }
                resultViewModel.setState(resultState);
                resultViewModel.firePropertyChanged();
                views.setState(resultViewModel.getViewName());
            }
            views.firePropertyChanged();
            updateMessage("Game loaded.");
            feedback.firePropertyChanged(PersistenceViewModel.GAME_LOADED);
        });
    }

    @Override
    public void prepareFailView(String message) {
        showMessage(message);
    }

    private void showMessage(String message) {
        onEdt(() -> updateMessage(message));
    }

    private void updateMessage(String message) {
        feedback.setState(new PersistenceViewModel.State(false, message));
        feedback.firePropertyChanged();
    }

    private void onEdt(Runnable update) {
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }
}
