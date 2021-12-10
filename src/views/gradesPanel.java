package views;

import models.Database;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class gradesPanel extends JFrame {
    private JPanel gradesPanel;
    private JLabel usernameLabel;
    private JScrollPane infoScrollPane;
    private JScrollPane gradeScrollPane;
    private JTable infoTable;
    private JTable gradeTable;
    private JButton homeButton;
    private JButton saveButton;

    private Connection dbConnection;
    private Statement dbStatement;
    private PreparedStatement dbPreparedStatement;
    private ResultSet dbResult;

    public gradesPanel(int userId, String userName) {

        add(gradesPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        usernameLabel.setText(userName);

        infoScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        gradeScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Sync tables scrolling
        infoScrollPane.getVerticalScrollBar().setModel(gradeScrollPane.getVerticalScrollBar().getModel());

        saveButton.addActionListener(action -> {
            if (!User.isTeacher()) return;
            try {
                dbConnection = DriverManager.getConnection(Database.getDbURL(), Database.getDbUser(), Database.getDbPass());

                // Loop through all the table rows in order to update them one by one.
                for (int i = 0; i < infoTable.getRowCount(); i++) {
                    int studentId = Integer.parseInt(infoTable.getValueAt(i, 0).toString());
                    int studentGrade = Integer.parseInt(gradeTable.getValueAt(i, 0).toString());

                    if (studentGrade <= 20 && studentGrade >= 0) {
                        dbPreparedStatement = dbConnection.prepareStatement("UPDATE \"StudentLessons\" SET grade = ? WHERE id = ?");
                        dbPreparedStatement.setInt(1, studentGrade);
                        dbPreparedStatement.setInt(2, studentId);
                        dbPreparedStatement.executeUpdate();
                        dbPreparedStatement.close();

                        System.out.printf("userId %d modified studentId: %d grade to %d%n", userId, studentId, studentGrade);
                    } else System.out.println("Skipped a student, doesn't meet grade criteria.");

                    // Checks if the next row has a null id to end the loop
                    if (infoTable.getValueAt(i + 1, 0) == "")
                        break;
                }
                dbConnection.close();
            } catch (SQLException err) {
                System.out.println("SQL Exception:");
                err.printStackTrace();
            }
        });
    }

    private void createUIComponents() {
        // Add columns
        String[] infoTableColumns;
        String dbQuery;

        // Check whether the user is a teacher and show the corresponding panel
        if (User.isTeacher()) {
            infoTableColumns = new String[]{"ID", "Student", "Subject"};
            dbQuery = String.format("""
                    SELECT DISTINCT("StudentLessons".id), "Users".name, "Lessons".name, "StudentLessons".grade
                    FROM "StudentLessons"
                    INNER JOIN "Courses" ON "StudentLessons"."lessonId" = "Courses"."lessonId"
                    INNER JOIN "Lessons" ON "StudentLessons"."lessonId" = "Lessons".id
                    INNER JOIN "Users" ON "StudentLessons"."studentId" = "Users".id
                    WHERE "Courses"."teacherId" = %d""", User.getUserId());
        } else {
            infoTableColumns = new String[]{"Subject"};
            dbQuery = String.format("""
                    SELECT "Lessons".name, "StudentLessons".grade
                    FROM "StudentLessons"
                    JOIN "Lessons" ON "StudentLessons"."lessonId" = "Lessons".id
                    JOIN "Users" ON "StudentLessons"."studentId" = "Users".id
                    WHERE "studentId" = %d""", User.getUserId());
            // Hide save button if a student account is viewing the grades
            saveButton = new JButton();
            saveButton.setVisible(false);
        }

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
            dbConnection = DriverManager.getConnection(Database.getDbURL(), Database.getDbUser(), Database.getDbPass());
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery(dbQuery);

            // Add rows
            Object[] infoRows = new Object[3];
            Object[] gradeRow = new Object[1];

            while (dbResult.next()) {
                if (User.isTeacher()) {
                    infoRows[0] = dbResult.getString(1);
                    infoRows[1] = dbResult.getString(2);
                    infoRows[2] = dbResult.getString(3);
                    gradeRow[0] = dbResult.getInt(4);
                } else {
                    infoRows[2] = dbResult.getString(1);
                    gradeRow[0] = dbResult.getInt(2);
                }

                infoTableModel.addRow(infoRows);
                gradeTableModel.addRow(gradeRow);
            }

            dbStatement.close();
            dbConnection.close();

            // Fill rows missing fixing white space
            int rowCount = infoTableModel.getRowCount();

            if (rowCount < 17) {
                for (int i = 0; i < 17 - rowCount; i++) {
                    infoRows[0] = "";
                    infoRows[1] = "";
                    infoRows[2] = "";
                    gradeRow[0] = "";

                    infoTableModel.addRow(infoRows);
                    gradeTableModel.addRow(gradeRow);
                }
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();

            Object[] infoRows = new Object[3];
            Object[] gradeRow = new Object[1];
            for (int i = 0; i < 17; i++) {
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