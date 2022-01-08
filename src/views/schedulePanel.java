package views;

import controllers.databaseController;
import controllers.fileController;
import controllers.panelController;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class schedulePanel extends JFrame {
    private JTable scheduleTable;
    private JPanel schedulePanel;
    private JScrollPane scrollPane;
    private JButton backButton;

    public schedulePanel(Point location) {
        add(schedulePanel);
        setTitle("Schedule");
        setSize(1280, 720);
        setResizable(false);
        setLocation(location);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("res/school.png").getImage());

        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        backButton.addActionListener(action -> {
            panelController.createMainPanel(this.getLocation());
            this.setVisible(false);
        });
    }

    private void createUIComponents() throws IOException {
        // Add columns
        String[] scheduleTableColumns = {"Classroom", "Subject", "Day", "Starts", "Ends"};

        DefaultTableModel scheduleTableModel = new DefaultTableModel(scheduleTableColumns, 0);
        scheduleTable = new JTable(scheduleTableModel);
        // Stop users from interacting with the table
        scheduleTable.getTableHeader().setReorderingAllowed(false);
        scheduleTable.setEnabled(false);

        try {
            String query;

            if (User.isTeacher())
                query = String.format("""
                        SELECT "Classrooms".name AS classroom, "Lessons".name AS lesson, day, "startTime", "endTime"
                        FROM "Courses"
                            INNER JOIN "Classrooms" on "Courses"."classroomId" = "Classrooms".id
                            INNER JOIN "Lessons" on "Courses"."lessonId" = "Lessons".id
                        WHERE "teacherId" = '%d'""", User.getId());
            else
                query = String.format("""
                        SELECT "Classrooms".name AS classroom, "Lessons".name AS lesson, day, "startTime", "endTime"
                        FROM "StudentLessons"
                            INNER JOIN "Courses" ON "StudentLessons"."courseId" = "Courses".id
                            INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id
                            INNER JOIN "Classrooms" ON "Classrooms".id = "Courses"."classroomId"
                        WHERE "StudentLessons"."studentId" = %d""", User.getId());

            ResultSet lessons = databaseController.selectQuery(query);

            // Add rows
            Object[] row = new Object[5];

            while (lessons.next()) {
                row[0] = lessons.getString("classroom");
                row[1] = lessons.getString("lesson");
                row[2] = lessons.getString("day");
                row[3] = lessons.getString("startTime");
                row[4] = lessons.getString("endTime");

                scheduleTableModel.addRow(row);
            }
        } catch (SQLException err) {
            StringWriter errors = new StringWriter();
            err.printStackTrace(new PrintWriter(errors));
            String message = errors.toString();
            fileController.saveFile("SQL Exception: " + message);

            panelController.createErrorPanel("Something went wrong.", this, 220);
        } finally {
            panelController.fillEmptyRows(scheduleTableModel);
            scheduleTable.setModel(scheduleTableModel);
        }
    }
}
