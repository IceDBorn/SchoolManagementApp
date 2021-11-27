import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;
import views.*;

import java.sql.*;

public class Main {
    private static int userId = 2;
    private static int userType;
    private static String userName;

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialOceanicTheme()));

        // Load the postgres driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }

        try {
            String dbURL = "jdbc:postgresql://localhost:5432/postgres";
            String dbUser = "postgres";
            String dbPass = "kekw123";

            Connection dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            Statement dbStatement = dbConnection.createStatement();
            ResultSet dbResult = dbStatement.executeQuery(String.format("SELECT name, type FROM \"Users\" WHERE id = %d", userId));
            dbResult.next();

            userName = dbResult.getString(1);
            userType = dbResult.getInt(2);
        } catch (SQLException e) {
            System.out.printf("SQL Exception:%nError: %s\n", e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            // Check whether the user is a student or not and show the corresponding panel
            if (userType == 0) {
                schedulePanel schedule = new schedulePanel(userId, userName);
                schedule.setVisible(true);
            } else {
                gradesPanel grades = new gradesPanel(userId, userName);
                grades.setVisible(true);
            }
        });
    }
}
