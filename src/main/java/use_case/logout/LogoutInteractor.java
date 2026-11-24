package use_case.logout;

import entity.UserSession;
import use_case.GameStateDataAccessInterface;

public class LogoutInteractor implements LogoutInputBoundary{
    private final LogoutOutputBoundary outputBoundary;
    private UserSession userSession;
    private final GameStateDataAccessInterface activeGame;

    public LogoutInteractor(LogoutOutputBoundary outputBoundary, UserSession userSession, GameStateDataAccessInterface activeGame){
        this.outputBoundary = outputBoundary;
        this.userSession = userSession;
        this.activeGame = activeGame;
    }

    @Override
    public void execute(){
        activeGame.saveGameState(null);
        userSession.logout();
        outputBoundary.prepareSuccessView();
    }
}
