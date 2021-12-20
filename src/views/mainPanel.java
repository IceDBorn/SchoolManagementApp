package views;

import controllers.panelController;
import controllers.userController;
import models.User;

import javax.swing.*;
import java.awt.*;
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

    public mainPanel(Point location) {
        add(mainPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocation(location);
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
            panelController.createLoginPanel(this.getLocation());
            this.setVisible(false);
        });

        scheduleButton.addActionListener(action -> {
            panelController.createSchedulePanel(this.getLocation());
            this.setVisible(false);
        });

        gradesButton.addActionListener(action -> {
            panelController.createGradesPanel(this.getLocation());
            this.setVisible(false);
        });

        classroomsButton.addActionListener(action -> {
            panelController.createClassroomsPanel(this.getLocation());
            this.setVisible(false);
        });

        scheduleMakerButton.addActionListener(action -> {
            try {
                panelController.createScheduleMakerPanel(this.getLocation());
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.setVisible(false);
        });

        lessonsButton.addActionListener(action -> {
            try {
                panelController.createLessonsPanel(this.getLocation());
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.setVisible(false);
        });

        usersButton.addActionListener(action -> {
            try {
                panelController.createUsersPanel(this.getLocation());
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.setVisible(false);
        });
    }
}
