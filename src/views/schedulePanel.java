package views;

import models.Database;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.sql.*;

public class schedulePanel extends JFrame {
    private JTable scheduleTable;
    private JPanel schedulePanel;
    private JLabel usernameLabel;
    private JScrollPane scrollPane;
    private JButton homeButton;

    public schedulePanel() {

        add(schedulePanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        usernameLabel.setText(User.getUsername());
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
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
            Connection dbConnection = DriverManager.getConnection(Database.getDbURL(), Database.getDbUser(), Database.getDbPass());
            Statement dbStatement = dbConnection.createStatement();
            ResultSet dbResult = dbStatement.executeQuery(String.format("""
                    SELECT "Lessons".name, "Classrooms".name, "Courses".day, "Courses".time
                    FROM "StudentLessons"
                    INNER JOIN "Courses" ON "StudentLessons"."lessonId" = "Courses"."lessonId"
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id
                    INNER JOIN "Classrooms" ON "Classrooms".id = "Courses"."classroomId"
                    WHERE "StudentLessons"."studentId" = %d""", User.getUserId()));

            // Add rows
            Object[] row = new Object[4];
            while (dbResult.next()) {
                row[0] = dbResult.getString(1);
                row[1] = dbResult.getString(2);
                row[2] = dbResult.getString(3);
                row[3] = dbResult.getString(4);

                scheduleTableModel.addRow(row);
            }

            // Fill rows missing fixing white space
            int rowCount = scheduleTableModel.getRowCount();

            if (rowCount < 17) {
                for (int i = 0; i < 17 - rowCount; i++) {
                    row[0] = "";
                    row[1] = "";
                    row[2] = "";
                    row[3] = "";

                    scheduleTableModel.addRow(row);
                }
            }

            dbStatement.close();
            dbConnection.close();
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();

            Object[] row = new Object[4];
            for (int i = 0; i < 17; i++) {
                row[0] = "";
                row[1] = "";
                row[2] = "";
                row[3] = "";

                scheduleTableModel.addRow(row);
            }
        }
    }
}
