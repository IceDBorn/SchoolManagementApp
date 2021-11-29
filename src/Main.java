import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;
import views.*;

import java.sql.*;

public class Main {
    private static final int userId = 6;
    private static String userName;

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialOceanicTheme()));

        try {
            String dbURL = "jdbc:postgresql://localhost:5432/postgres";
            String dbUser = "postgres";
            String dbPass = "kekw123";

            Connection dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            Statement dbStatement = dbConnection.createStatement();
            ResultSet dbResult = dbStatement.executeQuery(String.format("SELECT name FROM \"Users\" WHERE id = %d", userId));

            if (dbResult.next()) userName = dbResult.getString(1);
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            //schedulePanel schedule = new schedulePanel(userId, userName);
            //schedule.setVisible(true);

            gradesPanel grades = new gradesPanel(userId, userName);
            grades.setVisible(true);
        });
    }
}
