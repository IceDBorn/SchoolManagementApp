package views;

import javax.swing.*;
import controllers.*;
import models.User;

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

        if (!User.isAdmin()) {
            classroomsButton.setVisible(false);
            scheduleMakerButton.setVisible(false);
            lessonsButton.setVisible(false);
            usersButton.setVisible(false);
        }

        logoutButton.addActionListener(action -> {
            userController.Logout();
            panelController.createLoginPanel();
            this.setVisible(false);
        });

        scheduleButton.addActionListener(action -> {
            panelController.createSchedulePanel();
            this.setVisible(false);
        });

        gradesButton.addActionListener(action -> {
            panelController.createGradesPanel();
            this.setVisible(false);
        });

        classroomsButton.addActionListener(action -> {
            panelController.createClassroomsPanel();
            this.setVisible(false);
        });

        scheduleMakerButton.addActionListener(action -> {
            panelController.createScheduleMakerPanel();
            this.setVisible(false);
        });
    }
}
