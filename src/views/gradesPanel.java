package views;

import controllers.databaseController;
import controllers.panelController;
import models.Database;
import models.User;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class gradesPanel extends JFrame {
    private JPanel gradesPanel;
    private JLabel usernameLabel;
    private JScrollPane infoScrollPane;
    private JScrollPane gradeScrollPane;
    private JTable infoTable;
    private JTable gradeTable;
    private JButton backButton;
    private JButton saveButton;

    public gradesPanel() {
        add(gradesPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        infoScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        gradeScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Sync tables scrolling
        infoScrollPane.getVerticalScrollBar().setModel(gradeScrollPane.getVerticalScrollBar().getModel());

        backButton.addActionListener(action -> {
            panelController.createMainPanel();
            this.setVisible(false);
        });

        saveButton.addActionListener(action -> {
            if (!User.isTeacher())
                System.out.println("You are not a teacher.");
            else {
                try {
                    // Loop through all the table rows in order to update them one by one.
                    for (int i = 0; i < infoTable.getRowCount(); i++) {
                        int studentId = Integer.parseInt(infoTable.getValueAt(i, 0).toString());
                        int studentGrade = Integer.parseInt(gradeTable.getValueAt(i, 0).toString());

                        if (studentGrade <= 20 && studentGrade >= 0) {
                            Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE \"StudentLessons\" SET grade = ? WHERE id = ?");
                            preparedStatement.setInt(1, studentGrade);
                            preparedStatement.setInt(2, studentId);
                            preparedStatement.executeUpdate();
                            preparedStatement.close();
                            connection.close();

                            System.out.printf("userId %d modified studentId: %d grade to %d%n", User.getId(), studentId, studentGrade);
                        } else System.out.println("Skipped a student, doesn't meet grade criteria.");

                        // Checks if the next row has a null id to end the loop
                        if (infoTable.getValueAt(i + 1, 0) == "")
                            break;
                    }
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            }
        });
    }

    private void createUIComponents() {
        // Add columns
        String[] infoTableColumns;
        String[] gradeTableColumns = {"Grade"};
        String query;

        // Check whether the user is a teacher and show the corresponding panel
        if (User.isTeacher()) {
            infoTableColumns = new String[]{"ID", "Student", "Subject"};
            query = String.format("""
                    SELECT DISTINCT("StudentLessons".id), "Users".name, "Lessons".name, "StudentLessons".grade
                    FROM "StudentLessons"
                    INNER JOIN "Courses" ON "StudentLessons"."lessonId" = "Courses"."lessonId"
                    INNER JOIN "Lessons" ON "StudentLessons"."lessonId" = "Lessons".id
                    INNER JOIN "Users" ON "StudentLessons"."studentId" = "Users".id
                    WHERE "Courses"."teacherId" = %d""", User.getId());
        } else {
            infoTableColumns = new String[]{"Subject"};
            query = String.format("""
                    SELECT "Lessons".name, "StudentLessons".grade
                    FROM "StudentLessons"
                    JOIN "Lessons" ON "StudentLessons"."lessonId" = "Lessons".id
                    JOIN "Users" ON "StudentLessons"."studentId" = "Users".id
                    WHERE "studentId" = %d""", User.getId());

            // Hide save button if a student account is viewing the grades
            saveButton = new JButton();
            saveButton.setVisible(false);
        }

        DefaultTableModel infoTableModel = new DefaultTableModel(infoTableColumns, 0);
        DefaultTableModel gradeTableModel = new DefaultTableModel(gradeTableColumns, 0);
        infoTable = new JTable(infoTableModel);
        gradeTable = new JTable(gradeTableModel);
        // Stop users from interacting with the table
        infoTable.getTableHeader().setReorderingAllowed(false);
        gradeTable.getTableHeader().setReorderingAllowed(false);
        infoTable.setEnabled(false);

        try {
            CachedRowSet lessons = databaseController.selectQuery(query);

            // Add rows
            Object[] infoRows = new Object[3];
            Object[] gradeRow = new Object[1];

            while (lessons.next()) {
                if (User.isTeacher()) {
                    infoRows[0] = lessons.getString("\"StudentLessons\".\"id\"");
                    infoRows[1] = lessons.getString("\"Users\".name");
                }
                infoRows[2] = lessons.getString("\"Lessons\".name");
                gradeRow[0] = lessons.getInt("\"StudentLessons\".grade");

                infoTableModel.addRow(infoRows);
                gradeTableModel.addRow(gradeRow);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        } finally {
            panelController.fillEmptyRows(infoTableModel);
            panelController.fillEmptyRows(gradeTableModel);

            infoTable.setModel(infoTableModel);
            gradeTable.setModel(gradeTableModel);
        }
    }
}