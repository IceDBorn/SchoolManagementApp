package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentPanel extends JFrame {
    private JTable scheduleTable;
    private JPanel studentPanel;
    private JLabel usernameLabel;
    private JButton homeButton;
    private JScrollPane scrollPane;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";

    private static int studentId = 6;

    public StudentPanel() {
      add(studentPanel);
      setSize(1280, 720);
      setResizable(false);
      setLocationRelativeTo(null);
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      usernameLabel.setText("IceDBorn");
      scrollPane.setBorder(new EmptyBorder(0,0,0,0));
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
        // Stop users from interacting with the table
        scheduleTable.getTableHeader().setReorderingAllowed(false);
        scheduleTable.setEnabled(false);


        try {
            Connection dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            Statement dbStatement = dbConnection.createStatement();
            ResultSet dbResult = dbStatement.executeQuery(String.format("SELECT \"Lessons\".name, \"Classrooms\".name, \"Courses\".day, \"Courses\".time\n" +
                    "FROM \"StudentLessons\"\n" +
                    "INNER JOIN \"Users\" on \"StudentLessons\".\"studentId\" = \"Users\".id\n" +
                    "INNER JOIN \"Lessons\" on \"StudentLessons\".\"lessonId\" = \"Lessons\".id\n" +
                    "INNER Join \"Classrooms\" on \"StudentLessons\".\"lessonId\" = \"Classrooms\".\"lessonId\"\n" +
                    "INNER Join \"Courses\" on \"Classrooms\".id = \"Courses\".\"classroomId\"\n" +
                    "WHERE \"StudentLessons\".\"studentId\" = %d", studentId));

            // Add rows
            while (dbResult.next()) {
                Object[] row = new Object[4];

                row[0] = dbResult.getString(1);
                row[1] = dbResult.getString(2);
                row[2] = this.getDay(dbResult.getInt(3));
                row[3] = this.getTime(dbResult.getInt(4));

                studentTableModel.addRow(row);
            }
          
            // Fill rows missing to fix white space
            int rowCount = studentTableModel.getRowCount();

            if (rowCount < 17) {
              for (int i = 0; i < 17 - rowCount; i++) {
                row[0] = "";
                row[1] = "";
                row[2] = "";

                studentTableModel.addRow(row);
              }
            }

            dbStatement.close();
            dbConnection.close();

        } catch (SQLException e) {
            System.out.printf("SQL Exception:%nError: %s%n", e.getMessage());
        }
    }
}
