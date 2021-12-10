package views;

import controllers.userController;

import javax.swing.*;

public class loginPanel extends JFrame {
    private JPanel loginPanel;
    private JTextField usernameTextField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public loginPanel() {
        add(loginPanel);
        setSize(400, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        loginButton.addActionListener(action -> {
            String email = usernameTextField.getText();
            String password = String.valueOf(passwordField.getPassword());

            // Check if the email or the password are blank
            if (email.equals("") || password.equals(""))
                System.out.println("You can not have a blank email or password.");
            else
                userController.Login(email, password);
        });
    }
}
