import data_access.InMemoryUserDataAccessObject;
import data_access.InMemoryGameStateDataAccessObject;
import data_access.Pbkdf2PasswordHasher;
import entity.GameState;
import entity.User;
import entity.UserSession;
import org.junit.jupiter.api.Test;
import use_case.login.*;
import use_case.logout.*;
import use_case.user.PasswordHasher;

import static org.junit.jupiter.api.Assertions.*;

public class LoginLogoutIntegrationTest {
    @Test
    void loginThenLogout(){
        InMemoryUserDataAccessObject dao = new InMemoryUserDataAccessObject();
        InMemoryGameStateDataAccessObject activeGame = new InMemoryGameStateDataAccessObject();
        PasswordHasher hasher = new Pbkdf2PasswordHasher();
        UserSession session = new UserSession();

        dao.save(new User("Alex", hasher.hash("apple123")));

        LoginOutputBoundary loginPresenter = new LoginOutputBoundary(){
            @Override
            public void prepareSuccessView(LoginOutputData outputData){
                assertEquals("Alex", outputData.getUsername());
            }

            @Override
            public void prepareFailView(String errorMessage){
                fail("Login should succeed: " + errorMessage);
            }
        };

        LogoutOutputBoundary logoutPresenter = new LogoutOutputBoundary(){
            @Override
            public void prepareSuccessView(){
                // Logout succeeded
            }
        };

        LoginInteractor loginInteractor = new LoginInteractor(dao, loginPresenter, hasher, session);
        LogoutInteractor logoutInteractor = new LogoutInteractor(logoutPresenter, session, activeGame);

        loginInteractor.execute(new LoginInputData("Alex", "apple123"));

        assertTrue(session.isLoggedIn());
        assertEquals("Alex", session.getCurrentUsername());

        activeGame.saveGameState(GameState.startNew());
        assertNotNull(activeGame.getGameState());

        logoutInteractor.execute();

        assertFalse(session.isLoggedIn());
        assertNull(session.getCurrentUsername());
        assertNull(activeGame.getGameState());
    }
}
