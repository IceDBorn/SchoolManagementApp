package controllers;

import views.*;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class panelController {
    /**
     * Fill missing rows to fix white space
     */
    public static void fillEmptyRows(DefaultTableModel tableModel) {
        int rowCount = tableModel.getRowCount();
        Object[] row = new Object[tableModel.getColumnCount()];

        if (rowCount < 17) IntStream.range(0, 17 - rowCount).forEach(i -> {
            Arrays.fill(row, "");
            tableModel.addRow(row);
        });
    }

    public static void updateList(String sql, ArrayList<String> list, Component panel) throws IOException {
        try {
            CachedRowSet cachedRowSet = databaseController.selectQuery(sql);

            while (cachedRowSet.next()) {
                String name = cachedRowSet.getString(1);

                if (!list.contains(name))
                    list.add(name);
            }
        } catch (SQLException err) {
            StringWriter errors = new StringWriter();
            err.printStackTrace(new PrintWriter(errors));
            String message = errors.toString();
            fileController.saveFile("SQL Exception: " + message);

            panelController.createErrorPanel("Something went wrong.", panel);
        }
    }

    public static void createMainPanel() {
        mainPanel main = new mainPanel();
        main.setVisible(true);
    }

    public static void createLoginPanel() {
        loginPanel login = new loginPanel();
        login.setVisible(true);
    }

    public static void createSchedulePanel() {
        schedulePanel schedule = new schedulePanel();
        schedule.setVisible(true);
    }

    public static void createGradesPanel() {
        gradesPanel grades = new gradesPanel();
        grades.setVisible(true);
    }

    public static void createClassroomsPanel() {
        classroomsPanel classrooms = new classroomsPanel();
        classrooms.setVisible(true);
    }

    public static void createScheduleMakerPanel() throws IOException {
        scheduleMakerPanel courses = new scheduleMakerPanel();
        courses.setVisible(true);
    }

    public static void createLessonsPanel() throws IOException {
        lessonsPanel lessons = new lessonsPanel();
        lessons.setVisible(true);
    }

    public static void createUsersPanel() throws IOException {
        usersPanel users = new usersPanel();
        users.setVisible(true);
    }

    public static void createAddNewEntryPanel(boolean isProfession) {
        addNewEntryPanel add = new addNewEntryPanel(isProfession);
        add.setVisible(true);
    }

    public static int createConfirmationPanel(Component panel) {
        String message = "Any entries associated will be deleted too.";
        UIManager.put("OptionPane.minimumSize", new Dimension(325, 100));
        return JOptionPane.showConfirmDialog(panel, message, "Delete this entry?", JOptionPane.YES_NO_OPTION);
    }

    public static void createErrorPanel(String message, Component panel) {
        UIManager.put("OptionPane.minimumSize", new Dimension(350, 100));
        JOptionPane.showMessageDialog(panel, message, "", JOptionPane.ERROR_MESSAGE);
    }
}
