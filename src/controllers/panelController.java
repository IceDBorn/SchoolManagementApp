package controllers;

import views.*;

import javax.sql.rowset.CachedRowSet;
import javax.swing.table.DefaultTableModel;
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

    public static void updateList(String sql, ArrayList<String> list) {
        try {
            CachedRowSet cachedRowSet = databaseController.selectQuery(sql);

            while (cachedRowSet.next()) {
                String name = cachedRowSet.getString(1);

                if (!list.contains(name))
                    list.add(name);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
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

    public static void createScheduleMakerPanel() {
        scheduleMakerPanel courses = new scheduleMakerPanel();
        courses.setVisible(true);
    }

    public static void createLessonsPanel() {
        lessonsPanel lessons = new lessonsPanel();
        lessons.setVisible(true);
    }

    public static void createUsersPanel() {
        usersPanel users = new usersPanel();
        users.setVisible(true);
    }
}
