import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;
import views.*;

import java.sql.*;

public class Main {
    private static final int userId = 6;
    private static String username;

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialOceanicTheme()));

        try {
            String dbURL = "jdbc:postgresql://localhost:5432/postgres";
            String dbUser = "postgres";
            String dbPass = "kekw123";

            Connection dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            Statement dbStatement = dbConnection.createStatement();
            ResultSet dbResult = dbStatement.executeQuery(String.format("SELECT name FROM \"Users\" WHERE id = %d", userId));

            if (dbResult.next()) username = dbResult.getString(1);
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            int userPanel = 3;

            switch (userPanel) {
                case 1 -> {
                    schedulePanel schedule = new schedulePanel(userId, username);
                    schedule.setVisible(true);
                }
                case 2 -> {
                    gradesPanel grades = new gradesPanel(userId, username);
                    grades.setVisible(true);
                }
                case 3 -> {
                    classroomsPanel classrooms = new classroomsPanel(userId);
                    classrooms.setVisible(true);
                }
                default -> throw new IllegalStateException("Unexpected value: " + userPanel);
            }
        });
    }
}
