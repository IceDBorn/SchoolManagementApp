package views;

import controllers.panelController;
import controllers.userController;
import models.User;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.IOException;

public class loginPanel extends JFrame {
    private JPanel loginPanel;
    private JTextField usernameTextField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public loginPanel() {
        add(loginPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        loginButton.addActionListener(action -> {
            String email = usernameTextField.getText();
            String password = String.valueOf(passwordField.getPassword());
            try {
                userController.Login(email, password, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (User.getId() != -1) {
                panelController.createMainPanel();
                this.setVisible(false);
            }
        });

        // Listen for changes in the username text
        usernameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                enableButtons();
            }

            public void removeUpdate(DocumentEvent e) {
                enableButtons();
            }

            public void insertUpdate(DocumentEvent e) {
                enableButtons();
            }
        });

        // Listen for changes in the password text
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                enableButtons();
            }

            public void removeUpdate(DocumentEvent e) {
                enableButtons();
            }

            public void insertUpdate(DocumentEvent e) {
                enableButtons();
            }
        });
    }

    private void enableButtons() {
        loginButton.setEnabled(!usernameTextField.getText().equals("") && !(passwordField.getPassword().length == 0));
    }
}
