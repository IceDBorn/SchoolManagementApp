package views;

import javax.swing.*;
import java.sql.*;

public class loginPanel extends JFrame {
    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";
    private JPanel loginPanel;
    private JTextField usernameTextField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private Connection dbConnection;
    private Statement dbStatement;
    private ResultSet dbResult;

    // TODO: After a successful log in, pass the userId to the controller
    public loginPanel() {
        add(loginPanel);
        setSize(400, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setValue(1);
        model.setMinimum(1);
        model.setMaximum(99);

        loginButton.addActionListener(action -> {
            String userEmail = usernameTextField.getText();
            String userPassword = String.valueOf(passwordField.getPassword());

            // Check if the email or the password are blank
            if (userEmail.equals("") || userPassword.equals(""))
                System.out.println("You can not have a blank email or password.");
            else {
                try {
                    dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
                    dbStatement = dbConnection.createStatement();
                    dbResult = dbStatement.executeQuery(String.format("SELECT id, name FROM \"Users\" WHERE email = '%s' AND password = '%s'", userEmail, userPassword));

                    // If a user exists with the same email and password, let the user successfully log in
                    if (dbResult.next()) {
                        int userId = dbResult.getInt(1);
                        String userName = dbResult.getString(2);

                        System.out.printf("userId %d successfully logged in as %s%n", userId, userName);
                    } else System.out.println("You've specified an invalid email or password.");

                    dbStatement.close();
                    dbConnection.close();
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            }
        });
    }
}
