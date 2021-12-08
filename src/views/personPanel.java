package views;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class personPanel extends JFrame {
    private JPanel personPanel;
    private JTextField usernameTextField;
    private JButton addButton;
    private JComboBox<String> userDetailsComboBox;
    private JComboBox<String> userTypeComboBox;
    private JTextField emailTextField;
    private JPasswordField passwordField;
    private JLabel userDetailsLabel;
    private JComboBox<String> genderComboBox;
    private JCheckBox adminCheckBox;
    private JXDatePicker userBirthDayPicker;

    private final int userId;
    private final ArrayList<String> subjectList;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";

    private Connection dbConnection;
    private Statement dbStatement;
    private PreparedStatement dbPreparedStatement;
    private ResultSet dbResult;

    public personPanel(int userId) {
        this.userId = userId;
        this.subjectList = new ArrayList<>();

        add(personPanel);
        setSize(400, 320);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        addSubjects();

        userTypeComboBox.addItem("Teacher");
        userTypeComboBox.addItem("Student");
        userDetailsLabel.setText("Profession");

        genderComboBox.addItem("Male");
        genderComboBox.addItem("Female");

        userTypeComboBox.addItemListener(item -> {
            userDetailsComboBox.removeAllItems();

            if (Objects.requireNonNull(userTypeComboBox.getSelectedItem()).toString().equals("Teacher")) {
                userDetailsLabel.setText("Profession");
                addSubjects();
                adminCheckBox.setEnabled(true);
            } else {
                userDetailsLabel.setText("School Year");
                userDetailsComboBox.addItem("1η Γυμνασίου");
                userDetailsComboBox.addItem("2α Γυμνασίου");
                userDetailsComboBox.addItem("3η Γυμνασίου");
                userDetailsComboBox.addItem("1η Λυκείου");
                userDetailsComboBox.addItem("2α Λυκείου");
                userDetailsComboBox.addItem("3η Λυκείου");
                adminCheckBox.setEnabled(false);
                adminCheckBox.setSelected(false);
            }
        });

        addButton.addActionListener(action -> {
            String userName = usernameTextField.getText();
            String userEmail = emailTextField.getText();
            String userPassword = String.valueOf(passwordField.getPassword());
            Date userBirthday = new Date(userBirthDayPicker.getDate().getTime());
            int userGender = genderComboBox.getSelectedIndex();
            int userPhoneNumber = 1234567890;
            boolean isTeacher = Objects.requireNonNull(userTypeComboBox.getSelectedItem()).toString().equals("Teacher");
            boolean isAdmin = adminCheckBox.isSelected();

            String userSubject = Objects.requireNonNull(userDetailsComboBox.getSelectedItem()).toString();
            int userYear = userDetailsComboBox.getSelectedIndex() + 1;

            try {
                dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
                dbPreparedStatement = dbConnection.prepareStatement("INSERT INTO \"Users\"(name, gender, birthday, phone, email, password, \"isAdmin\") VALUES (?, ?, ?, ?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);

                dbPreparedStatement.setString(1, userName);
                dbPreparedStatement.setInt(2, userGender);
                dbPreparedStatement.setDate(3, userBirthday);
                dbPreparedStatement.setInt(4, userPhoneNumber);
                dbPreparedStatement.setString(5, userEmail);
                dbPreparedStatement.setString(6, userPassword);
                dbPreparedStatement.setBoolean(7, isAdmin);
                dbPreparedStatement.executeUpdate();

                dbResult = dbPreparedStatement.getGeneratedKeys();
                dbResult.next();
                int personId = dbResult.getInt(1);

                // Check whether the user is a student or a teacher and import into the corresponding table
                if (isTeacher) {
                    dbPreparedStatement = dbConnection.prepareStatement("INSERT INTO \"Teachers\"(id, subject) VALUES (?, ?)");
                    dbPreparedStatement.setInt(1, personId);
                    dbPreparedStatement.setString(2, userSubject);
                } else {
                    dbPreparedStatement = dbConnection.prepareStatement("INSERT INTO \"Students\"(id, year) VALUES (?, ?)");
                    dbPreparedStatement.setInt(1, personId);
                    dbPreparedStatement.setInt(2, userYear);
                }

                dbPreparedStatement.executeUpdate();
                dbPreparedStatement.close();
                dbConnection.close();

                System.out.printf("userId %d created %s: %s (gender: %s, birthday: %s, phone: %d, email: %s, admin: %s%n",
                        userId, isTeacher ? "teacher" : "student", userName, userGender, userBirthday, userPhoneNumber, userEmail, isAdmin ? "Yes" : "No");
            } catch (SQLException err) {
                System.out.println("SQL Exception:");
                err.printStackTrace();
            }
            addSubjects();
        });
    }

    /**
     * Get all subjects, add any new ones into subjectList and update userDetailsComboBox
     */
    private void addSubjects() {
        try {
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery("SELECT DISTINCT(subject) FROM \"Teachers\"");

            while (dbResult.next()) {
                String subjectName = dbResult.getString(1);

                if (subjectList.contains(subjectName))
                    continue;

                subjectList.add(subjectName);
            }

            dbStatement.close();
            dbConnection.close();
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }

        userDetailsComboBox.removeAllItems();
        userDetailsComboBox.addItem("Add New Subject");

        for (String subject : subjectList)
            userDetailsComboBox.addItem(subject);
    }
}
