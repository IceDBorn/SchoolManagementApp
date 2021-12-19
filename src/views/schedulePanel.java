package views;

import controllers.databaseController;
import controllers.panelController;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;

public class schedulePanel extends JFrame {
    private JTable scheduleTable;
    private JPanel schedulePanel;
    private JScrollPane scrollPane;
    private JButton backButton;

    public schedulePanel() {
        add(schedulePanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        backButton.addActionListener(action -> {
            panelController.createMainPanel();
            this.setVisible(false);
        });
    }

    private void createUIComponents() {
        // Add columns
        String[] scheduleTableColumns = {"Classroom", "Subject", "Day", "Time"};
        DefaultTableModel scheduleTableModel = new DefaultTableModel(scheduleTableColumns, 0);
        scheduleTable = new JTable(scheduleTableModel);
        // Stop users from interacting with the table
        scheduleTable.getTableHeader().setReorderingAllowed(false);
        scheduleTable.setEnabled(false);

        try {
            ResultSet lessons = databaseController.selectQuery(String.format("""
                    SELECT "Lessons".name, "Classrooms".name, "Courses".day, "Courses".time
                    FROM "StudentLessons"
                    INNER JOIN "Courses" ON "StudentLessons"."lessonId" = "Courses"."lessonId"
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id
                    INNER JOIN "Classrooms" ON "Classrooms".id = "Courses"."classroomId"
                    WHERE "StudentLessons"."studentId" = %d""", User.getId()));

            // Add rows
            Object[] row = new Object[4];

            while (lessons.next()) {
                row[0] = lessons.getString("\"Lessons\".name");
                row[1] = lessons.getString("\"Classrooms\".name");
                row[2] = lessons.getString("\"Courses\".day");
                row[3] = lessons.getString("\"Courses\".time");

                scheduleTableModel.addRow(row);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
            panelController.createErrorPanel("Something went wrong.", this);
        } finally {
            panelController.fillEmptyRows(scheduleTableModel);
            scheduleTable.setModel(scheduleTableModel);
        }
    }
}
