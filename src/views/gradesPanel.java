package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class gradesPanel extends JFrame {
    private JTable infoTable;
    private JPanel gradesPanel;
    private JLabel usernameLabel;
    private JButton homeButton;
    private JScrollPane infoScrollPane;
    private JScrollPane gradeScrollPane;
    private JTable gradeTable;
    private JButton saveButton;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";

    private static final int teacherId = 6;

    private Connection dbConnection;
    private Statement dbStatement;
    private ResultSet dbResult;

    public gradesPanel() {
        add(gradesPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        try {
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery(String.format("SELECT name FROM \"Users\" WHERE id = %d", teacherId));
            dbResult.next();
            usernameLabel.setText(dbResult.getString(1));
        } catch (SQLException e) {
            System.out.printf("SQL Exception:%nError: %s%n", e.getMessage());
        }
        infoScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        gradeScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        // Sync tables scrolling
        infoScrollPane.getVerticalScrollBar().setModel(gradeScrollPane.getVerticalScrollBar().getModel());
    }

    private void createUIComponents() {
        // Add columns
        String[] infoTableColumns = {"ID", "Student", "Subject"};
        String[] gradeTableColumns = {"Grade"};
        DefaultTableModel infoTableModel = new DefaultTableModel(infoTableColumns, 0);
        DefaultTableModel gradeTableModel = new DefaultTableModel(gradeTableColumns, 0);
        infoTable = new JTable(infoTableModel);
        gradeTable = new JTable(gradeTableModel);
        // Stop users from interacting with the table
        infoTable.getTableHeader().setReorderingAllowed(false);
        gradeTable.getTableHeader().setReorderingAllowed(false);
        infoTable.setEnabled(false);

        try {
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery(String.format("""
                    SELECT "Lessons".name, "Classrooms".name, "Courses".day, "Courses".time
                    FROM "StudentLessons"
                    INNER JOIN "Users" ON "StudentLessons"."studentId" = "Users".id
                    INNER JOIN "Lessons" ON "StudentLessons"."lessonId" = "Lessons".id
                    INNER Join "Classrooms" ON "StudentLessons"."lessonId" = "Classrooms"."lessonId"
                    INNER Join "Courses" ON "Classrooms".id = "Courses"."classroomId"
                    WHERE "StudentLessons"."studentId" = %d""", teacherId));

            // Add rows
            Object[] infoRows = new Object[3];
            Object[] gradeRow = new Object[1];

            while (dbResult.next()) {
                infoRows[0] = dbResult.getString(1);
                infoRows[1] = dbResult.getString(2);
                infoRows[2] = dbResult.getString(3);
                gradeRow[0] = dbResult.getString(4);

                infoTableModel.addRow(infoRows);
                gradeTableModel.addRow(gradeRow);
            }

            // Fill rows missing fixing white space
            int rowCount = infoTableModel.getRowCount();

            if (rowCount < 16) {
                for (int i = 0; i < 16 - rowCount; i++) {
                    infoRows[0] = "";
                    infoRows[1] = "";
                    infoRows[2] = "";
                    gradeRow[0] = "";

                    infoTableModel.addRow(infoRows);
                    gradeTableModel.addRow(gradeRow);
                }
            }

            dbStatement.close();
            dbConnection.close();

        } catch (SQLException e) {
            System.out.printf("SQL Exception:%nError: %s%n", e.getMessage());

            Object[] infoRows = new Object[3];
            Object[] gradeRow = new Object[1];
            for (int i = 0; i < 16; i++) {
                infoRows[0] = "";
                infoRows[1] = "";
                infoRows[2] = "";
                gradeRow[0] = "";

                infoTableModel.addRow(infoRows);
                gradeTableModel.addRow(gradeRow);
            }
        }
    }
}
