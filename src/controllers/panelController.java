package controllers;

import javax.swing.table.DefaultTableModel;
import java.util.Arrays;
import java.util.stream.IntStream;

public class panelController {
    /**
     * Fill missing rows to fix white space
     */
    public static void fillEmptyRows(Object[] row, DefaultTableModel tableModel) {
        int rowCount = tableModel.getRowCount();

        if (rowCount < 16) IntStream.range(0, 16 - rowCount).forEach(i -> {
            Arrays.fill(row, "");
            tableModel.addRow(row);
        });
    }

    /**
     * Gets the year and returns the school year name.
     */
    public static String getYear(int year) {
        return switch (year) {
            case 1 -> "1η Γυμνασίου";
            case 2 -> "2α Γυμνασίου";
            case 3 -> "3η Γυμνασίου";
            case 4 -> "1η Λυκείου";
            case 5 -> "2α Λυκείου";
            case 6 -> "3η Λυκείου";
            default -> throw new IllegalStateException("Unexpected value: " + year);
        };
    }
}
