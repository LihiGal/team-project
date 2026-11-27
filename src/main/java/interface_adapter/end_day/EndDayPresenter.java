package interface_adapter.end_day;

import entity.GameState;
import entity.GameStatus;
import interface_adapter.hud.HudState;
import interface_adapter.hud.HudViewModel;
import interface_adapter.ViewManagerModel;
import interface_adapter.result.ResultState;
import interface_adapter.result.ResultViewModel;
import use_case.end_day.EndDayOutputBoundary;
import use_case.end_day.EndDayOutputData;

public class EndDayPresenter implements EndDayOutputBoundary {

    private final HudViewModel hudViewModel;
    private final ResultViewModel resultViewModel;
    private final ViewManagerModel viewManagerModel;

    public EndDayPresenter(HudViewModel hudViewModel, ResultViewModel resultViewModel, ViewManagerModel viewManagerModel){
        this.hudViewModel = hudViewModel;
        this.resultViewModel = resultViewModel;
        this.viewManagerModel = viewManagerModel;
    }

    @Override
    public void prepareSuccessView(EndDayOutputData outputData){
        HudState state = new HudState(hudViewModel.getState());

        state.setDay(outputData.getDay());
        state.setTotalDays(GameState.TOTAL_DAYS);
        state.setEnergy(outputData.getEnergy());
        state.setSanity(outputData.getSanity());
        state.setAcademicSurvival(outputData.getAcademicSurvival());
        state.setMoney(outputData.getMoney());
        state.setRemainingTimeBlocks(outputData.getRemainingTimeBlocks());

        hudViewModel.setState(state);
        hudViewModel.firePropertyChanged();

        if (outputData.getStatus() != GameStatus.IN_PROGRESS){
            ResultState resultState = new ResultState();

            boolean won = outputData.getStatus() == GameStatus.WON;
            resultState.setWon(won);

            if (won){
                resultState.setMessage("You survived the week at UofT!");
            }else if (outputData.getEnergy() == 0 || outputData.getSanity() == 0 || outputData.getAcademicSurvival() == 0){
                resultState.setMessage("One of your stats has reached 0");
            }else{
                resultState.setMessage("Your Academic Survival is below 60.");
            }

            resultViewModel.setState(resultState);
            resultViewModel.firePropertyChanged();
            viewManagerModel.setState(resultViewModel.getViewName());
            viewManagerModel.firePropertyChanged();
        }
    }

    @Override
    public void prepareFailView(String error){
        hudViewModel.firePropertyChanged("error", error);
    }
}
