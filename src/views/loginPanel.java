package views;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class loginPanel extends JFrame {
    private JPanel classroomsPanel;
    private JTextField usernameTextField;
    private JSpinner classCapacitySpinner;
    private JButton loginButton;
    private JPasswordField passwordField1;

    private static int userId;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";

    private Connection dbConnection;
    private PreparedStatement dbPreparedStatement;

    public loginPanel(int userId) {
        this.userId = userId;

        add(classroomsPanel);
        setSize(400, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setValue(1);
        model.setMinimum(1);
        model.setMaximum(99);

        loginButton.addActionListener(action -> {
            String classroomName = usernameTextField.getText();
            int classroomLimit = (int) classCapacitySpinner.getValue();

            // Check if the text field is blank to avoid unnecessary sql errors
            if (!classroomName.equals("")) {
                try {
                    dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
                    dbPreparedStatement = dbConnection.prepareStatement("INSERT INTO \"Classrooms\"(name, \"limit\") VALUES (?, ?)");
                    dbPreparedStatement.setString(1, classroomName);
                    dbPreparedStatement.setInt(2, classroomLimit);
                    dbPreparedStatement.executeUpdate();
                    dbPreparedStatement.close();
                    dbConnection.close();

                    System.out.printf("userId %d created classroom: %s with limit: %d%n", userId, classroomName, classroomLimit);
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            } else System.out.println("You can not insert a blank name");
        });
    }
}
