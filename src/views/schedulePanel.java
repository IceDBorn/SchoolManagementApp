package views;

import models.Database;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.IntStream;

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
        usernameLabel.setText(User.getName());
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

        Object[] row = new Object[4];

        try {
            ResultSet lessons = Database.selectQuery(String.format("""
                    SELECT "Lessons".name, "Classrooms".name, "Courses".day, "Courses".time
                    FROM "StudentLessons"
                    INNER JOIN "Courses" ON "StudentLessons"."lessonId" = "Courses"."lessonId"
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id
                    INNER JOIN "Classrooms" ON "Classrooms".id = "Courses"."classroomId"
                    WHERE "StudentLessons"."studentId" = %d""", User.getId()));

            // Add rows
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
        } finally {
            // Fill missing rows to fix white space
            int rowCount = scheduleTableModel.getRowCount();

            if (rowCount < 17) IntStream.range(0, 17 - rowCount).forEach(i -> {
                row[0] = "";
                row[1] = "";
                row[2] = "";
                row[3] = "";
                scheduleTableModel.addRow(row);
            });
        }
    }
}
