package views;

import javax.swing.*;
import java.sql.*;
import java.util.Objects;

public class personPanel extends JFrame {
    private JPanel personPanel;
    private JTextField usernameTextField;
    private JButton addButton;
    private JComboBox<String> professionComboBox;
    private JComboBox<String> userDetailsComboBox;
    private JComboBox<String> userTypeComboBox;
    private JTextField emailTextField;
    private JPasswordField passwordField;
    private JLabel userDetailsLabel;
    private JComboBox<String> genderComboBox;
    private JCheckBox adminCheckBox;

    private static int userId;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";

    private Connection dbConnection;
    private Statement dbStatement;
    private PreparedStatement dbPreparedStatement;
    private ResultSet dbResult;

    public personPanel(int userId) {
        this.userId = userId;

        add(personPanel);
        setSize(400, 320);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        try {
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery("SELECT DISTINCT(subject) FROM \"Teachers\"");

            while (dbResult.next()) {
                professionComboBox.addItem(dbResult.getString(1));
            }

            dbStatement.close();
            dbConnection.close();

        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }

        userTypeComboBox.addItem("Teacher");
        userTypeComboBox.addItem("Student");
        userDetailsLabel.setText("Profession");
        userDetailsComboBox.addItem("Programmer");
        userDetailsComboBox.addItem("Physician");
        genderComboBox.addItem("Male");
        genderComboBox.addItem("Female");

        userTypeComboBox.addItemListener(item -> {
            if (Objects.equals(Objects.requireNonNull(userTypeComboBox.getSelectedItem()).toString(), "Teacher")) {
                userDetailsLabel.setText("Profession");
                userDetailsComboBox.removeAllItems();
                userDetailsComboBox.addItem("Programmer");
                userDetailsComboBox.addItem("Physician");
                adminCheckBox.setEnabled(true);
            } else {
                userDetailsLabel.setText("School Year");
                userDetailsComboBox.removeAllItems();
                userDetailsComboBox.addItem("1η Γυμνασίου");
                userDetailsComboBox.addItem("2α Γυμνασίου");
                userDetailsComboBox.addItem("3η Γυμνασίου");
                userDetailsComboBox.addItem("1η Λυκείου");
                userDetailsComboBox.addItem("2α Λυκείου");
                userDetailsComboBox.addItem("3η Λυκείου");
                adminCheckBox.setEnabled(false);
            }
        });

        addButton.addActionListener(action -> {
            String lessonName = usernameTextField.getText();

            if (!lessonName.equals("")) {
                String lessonSubject = professionComboBox.getItemAt(professionComboBox.getSelectedIndex());
                int lessonYear = userDetailsComboBox.getSelectedIndex() + 1;

                try {
                    dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
                    dbPreparedStatement = dbConnection.prepareStatement("INSERT INTO \"Lessons\"(name, subject, year) VALUES (?, ?, ?)");
                    dbPreparedStatement.setString(1, lessonName);
                    dbPreparedStatement.setString(2, lessonSubject);
                    dbPreparedStatement.setInt(3, lessonYear);
                    dbPreparedStatement.executeUpdate();

                    dbConnection.close();
                    System.out.printf("userId %d created lesson: %s with subject: %s%n", userId, lessonName, lessonSubject);
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            } else System.out.println("You can not insert a blank name");
        });
    }
}
