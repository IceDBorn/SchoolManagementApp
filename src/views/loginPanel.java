package views;

import javax.swing.*;
import controllers.userController;

public class loginPanel extends JFrame {
    private JPanel loginPanel;
    private JTextField usernameTextField;
    private JPasswordField passwordField;
    private JButton loginButton;

    // TODO: After a successful log in, pass the userId to the controller
    public loginPanel() {
        add(loginPanel);
        setSize(400, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        loginButton.addActionListener(action -> {
            String userEmail = usernameTextField.getText();
            String userPassword = String.valueOf(passwordField.getPassword());

            // Check if the email or the password are blank
            if (userEmail.equals("") || userPassword.equals(""))
                System.out.println("You can not have a blank email or password.");
            else {
                userController.Login(userEmail, userPassword);
            }
        });
    }
}
