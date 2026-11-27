package view;

import interface_adapter.result.ResultState;
import interface_adapter.result.ResultViewModel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class ResultView extends JPanel implements PropertyChangeListener {

    public static final String VIEW_NAME = "result";

    private final JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel messageLabel = new JLabel("", SwingConstants.CENTER);
    private final JButton backButton = new JButton("Back to Main Menu");

    public ResultView(ResultViewModel resultViewModel){
        setLayout(new BorderLayout());

        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JPanel content = new JPanel(new GridLayout(2, 1, 0, 20));
        content.add(titleLabel);
        content.add(messageLabel);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.add(content);
        add(centerPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
        resultViewModel.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt){
        ResultState state = (ResultState) evt.getNewValue();

        if (state.isWon()){
            titleLabel.setText("YOU WIN!");
            titleLabel.setForeground(new Color(0, 128, 0));
        }else{
            titleLabel.setText("GAME OVER");
            titleLabel.setForeground(Color.RED);
        }

        messageLabel.setText(state.getMessage());
    }

    public void addBackAction(Runnable action){
        backButton.addActionListener(e -> action.run());
    }

    public String getViewName(){
        return VIEW_NAME;
    }
}
