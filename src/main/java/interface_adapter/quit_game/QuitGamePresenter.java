package interface_adapter.quit_game;

import interface_adapter.ViewManagerModel;
import use_case.quit_game.QuitGameOutputBoundary;

/**
 * Presenter for the Quit Game use case.
 */
public class QuitGamePresenter implements QuitGameOutputBoundary {

    private final ViewManagerModel viewManagerModel;
    private final String successViewName;

    public QuitGamePresenter(ViewManagerModel viewManagerModel,
                             String successViewName) {
        this.viewManagerModel = viewManagerModel;
        this.successViewName = successViewName;
    }

    @Override
    public void prepareSuccessView() {
        viewManagerModel.setState(successViewName);
        viewManagerModel.firePropertyChanged();
    }
}