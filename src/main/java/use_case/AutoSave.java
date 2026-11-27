package use_case;

import entity.GameState;
import java.util.function.Supplier;

public class AutoSave {

    private final SaveGameDataAccessInterface saves;
    private final Supplier<String> username;

    public AutoSave(SaveGameDataAccessInterface saves, Supplier<String> username){
        this.saves = saves;
        this.username = username;
    }

    public void save(GameState game) throws GamePersistenceException {
        saves.save(username.get(), game);
    }
}
