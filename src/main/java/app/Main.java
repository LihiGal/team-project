package app;

import data_access.SchemaInitializer;
import data_access.SqliteConnectionProvider;
import data_access.SqliteGameDataAccessObject;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Starts the game. All wiring lives in AppBuilder. */
public class Main {

    public static void main(String[] args) {
        SqliteConnectionProvider connections = new SqliteConnectionProvider();
        try {
            new SchemaInitializer(connections).initialize();
        } catch (SQLException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Could not initialize the game database", e);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Couldn't open the local game database.", "UofTears", JOptionPane.ERROR_MESSAGE));
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JFrame application = new AppBuilder(connections)
                    .addGameView()
                    .addDialogueView()
                    .addNewGameUseCase()
                    .addTalkToNpcUseCase()
                    .addLoginView()
                    .addSignupView()
                    .addLoginUseCase()
                    .addSignupUseCase()
                    .addLogoutUseCase()
                    .addPersistenceUseCases(new SqliteGameDataAccessObject(connections))
                    .build();
            application.setSize(480, 360);
            application.setLocationRelativeTo(null);
            application.setVisible(true);
        });
    }
}
