package controllers;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import views.*;

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
}
