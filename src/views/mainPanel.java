package views;

import javax.swing.*;
import controllers.*;

public class mainPanel extends JFrame {
    private JPanel mainPanel;
    private JButton logoutButton;
    private JButton scheduleButton;
    private JButton classroomsButton;
    private JButton scheduleMakerButton;
    private JButton gradesButton;
    private JButton lessonsButton;
    private JButton usersButton;

    public mainPanel() {
        add(mainPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        logoutButton.addActionListener(action -> {
            userController.Logout();
            panelController.createLoginPanel();
            this.setVisible(false);
        });
    }
}
