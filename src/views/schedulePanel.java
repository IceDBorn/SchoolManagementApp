package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class schedulePanel extends JFrame {
    private JTable scheduleTable;
    private JPanel schedulePanel;
    private JLabel usernameLabel;
    private JScrollPane scrollPane;
    private JButton homeButton;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";

    private static int studentId;

    public schedulePanel(int studentId, String studentName) {
        this.studentId = studentId;

        add(schedulePanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        usernameLabel.setText(studentName);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    private String getDay(int day) {
        return switch (day) {
            case 0 -> "Monday";
            case 1 -> "Tuesday";
            case 2 -> "Wednesday";
            case 3 -> "Thursday";
            case 4 -> "Friday";
            default -> "N/A";
        };
    }

    private String getTime(int time) {
        return String.format("%d:00", time);
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
            Connection dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            Statement dbStatement = dbConnection.createStatement();
            ResultSet dbResult = dbStatement.executeQuery(String.format("""
                    SELECT "Lessons".name, "Classrooms".name, "Courses".day, "Courses".time
                    FROM "StudentLessons"
                    INNER JOIN "Courses" ON "StudentLessons"."lessonId" = "Courses"."lessonId"
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id
                    INNER JOIN "Classrooms" ON "Classrooms".id = "Courses"."classroomId"
                    WHERE "StudentLessons"."studentId" = %d""", studentId));

            // Add rows
            Object[] row = new Object[4];

            while (dbResult.next()) {
                row[0] = dbResult.getString(1);
                row[1] = dbResult.getString(2);
                row[2] = this.getDay(dbResult.getInt(3));
                row[3] = this.getTime(dbResult.getInt(4));

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
