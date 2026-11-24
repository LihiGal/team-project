package view;

import interface_adapter.new_game.NewGameController;
import interface_adapter.logout.LogoutController;
import interface_adapter.persistence.PersistenceController;
import interface_adapter.persistence.PersistenceViewModel;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class TitleView extends JPanel {

    public static final String VIEW_NAME = "title";

    private LogoutController logoutController;

    private final JButton newGameButton = new JButton("New Game");
    private final JButton loadGameButton = new JButton("Load Game");
    private final JLabel message = new JLabel(" ");

    public TitleView(NewGameController newGameController) {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("UofTears: Survive the Semester");
        add(titleLabel, gbc);

        gbc.gridy = 1;
        newGameButton.addActionListener(
                e -> newGameController.execute()
        );
        add(newGameButton, gbc);

        gbc.gridy = 2;
        loadGameButton.setEnabled(false);
        loadGameButton.setToolTipText(
                "Available after Story 3 integration"
        );
        add(loadGameButton, gbc);

        gbc.gridy = 3;
        JButton logoutButton = new JButton("Log out");
        logoutButton.addActionListener(e -> {
            if (logoutController != null) {
                logoutController.execute();
            }
        });
        add(logoutButton, gbc);

        gbc.gridy = 4;
        add(message, gbc);
    }

    public void addLoadGame(PersistenceController controller, PersistenceViewModel feedback){
        loadGameButton.setEnabled(true);
        loadGameButton.setToolTipText(null);

        loadGameButton.addActionListener(e -> controller.load());

        feedback.addPropertyChangeListener(event -> {
            var state = feedback.getState();
            newGameButton.setEnabled(!state.busy());
            loadGameButton.setEnabled(!state.busy());
            message.setText(state.message());
        });
    }

    public void setLogoutController(
            LogoutController logoutController) {
        this.logoutController = logoutController;
    }

    public String getViewName() {
        return VIEW_NAME;
    }
}
