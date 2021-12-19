package views;

import controllers.panelController;
import controllers.userController;
import models.User;

import javax.swing.*;
import java.io.IOException;

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
            try {
                userController.Logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            try {
                panelController.createScheduleMakerPanel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.setVisible(false);
        });

        lessonsButton.addActionListener(action -> {
            try {
                panelController.createLessonsPanel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.setVisible(false);
        });

        usersButton.addActionListener(action -> {
            try {
                panelController.createUsersPanel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.setVisible(false);
        });
    }
}
