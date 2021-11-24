package views;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentPanel extends JFrame {

    private JTable scheduleTable;
    private JPanel studentPanel;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";
    private static Connection dbConnection;
    private static Statement dbStatement;
    private static ResultSet dbResult;

    public StudentPanel() {
        add(studentPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public String getDay(int day) {
        return switch (day) {
            case 0 -> "Monday";
            case 1 -> "Tuesday";
            case 2 -> "Wednesday";
            case 3 -> "Thursday";
            case 4 -> "Friday";
            default -> "N/A";
        };
    }

    public String getTime(int time) {
        return String.format("%d:00", time);
    }

    private void createUIComponents() {
        // Initialise & Close Connection
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }

        // Add columns
        String[] studentTableColumns = {"Classroom", "Subject", "Day", "Time"};
        DefaultTableModel studentTableModel = new DefaultTableModel(studentTableColumns, 0);
        scheduleTable = new JTable(studentTableModel);

        try {
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery("SELECT name, subject, day, time\n" +
                    "FROM \"Courses\"\n" +
                    "INNER JOIN \"Classrooms\" on \"Classrooms\".id = \"Courses\".\"classroomId\"\n" +
                    "INNER JOIN \"Teachers\" T on T.id = \"Courses\".\"teacherId\"");

            // Add rows
            while (dbResult.next()) {
                Object[] row = new Object[4];

                row[0] = dbResult.getString(1);
                row[1] = dbResult.getString(2);
                row[2] = this.getDay(dbResult.getInt(3));
                row[3] = this.getTime(dbResult.getInt(4));

                studentTableModel.addRow(row);
            }

            dbStatement.close();
            dbConnection.close();

        } catch (SQLException e) {
            System.out.printf("SQL Exception:%nError: %s%n", e.getMessage());
        }
    }
}
