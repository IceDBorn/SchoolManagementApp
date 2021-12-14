package views;

import controllers.databaseController;
import models.Database;
import models.User;
import org.jdesktop.swingx.JXDatePicker;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class usersPanel extends JFrame {
    private final ArrayList<String> subjectList;
    private JPanel personPanel;
    private JTextField usernameTextField;
    private JTextField emailTextField;
    private JPasswordField passwordField;
    private JButton addButton;
    private JComboBox<String> userDetailsComboBox;
    private JComboBox<String> userTypeComboBox;
    private JComboBox<String> genderComboBox;
    private JCheckBox adminCheckBox;
    private JXDatePicker userBirthDayPicker;
    private JTable usersTable;
    private JScrollPane scrollPane;
    private JButton backButton;
    private JPanel professionPanel;

    private TitledBorder title;

    public usersPanel() {
        this.subjectList = new ArrayList<>();
        add(personPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        updateSubjects();

        // Set profession panel titled border
        title = BorderFactory.createTitledBorder(new EmptyBorder(0,0,0,0), "Profession");
        title.setTitleJustification(TitledBorder.CENTER);
        professionPanel.setBorder(title);

        userTypeComboBox.addItem("Teacher");
        userTypeComboBox.addItem("Student");

        genderComboBox.addItem("Male");
        genderComboBox.addItem("Female");

        userTypeComboBox.addItemListener(item -> {
            userDetailsComboBox.removeAllItems();

            if (Objects.requireNonNull(userTypeComboBox.getSelectedItem()).toString().equals("Teacher")) {
                title = BorderFactory.createTitledBorder(new EmptyBorder(0,0,0,0), "Profession");
                title.setTitleJustification(TitledBorder.CENTER);
                professionPanel.setBorder(title);
                adminCheckBox.setEnabled(true);
                updateSubjects();
            } else {
                title = BorderFactory.createTitledBorder(new EmptyBorder(0,0,0,0), "School Year");
                title.setTitleJustification(TitledBorder.CENTER);
                professionPanel.setBorder(title);
                adminCheckBox.setEnabled(false);
                adminCheckBox.setSelected(false);

                for (String schoolYear : Arrays.asList("1η Γυμνασίου", "2α Γυμνασίου", "3η Γυμνασίου", "1η Λυκείου", "2α Λυκείου", "3η Λυκείου"))
                    userDetailsComboBox.addItem(schoolYear);
            }
        });

        addButton.addActionListener(action -> {
            String userName = usernameTextField.getText();
            String userEmail = emailTextField.getText();
            String userPassword = String.valueOf(passwordField.getPassword());
            String userSubject = Objects.requireNonNull(userDetailsComboBox.getSelectedItem()).toString();
            Date userBirthday = new Date(userBirthDayPicker.getDate().getTime());
            int userGender = genderComboBox.getSelectedIndex();
            int userPhoneNumber = 1234567890;
            int userYear = userDetailsComboBox.getSelectedIndex() + 1;
            boolean isTeacher = Objects.requireNonNull(userTypeComboBox.getSelectedItem()).toString().equals("Teacher");
            boolean isAdmin = adminCheckBox.isSelected();

            try {
                Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO \"Users\"(name, gender, birthday, phone, email, password, \"isAdmin\") VALUES (?, ?, ?, ?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, userName);
                preparedStatement.setInt(2, userGender);
                preparedStatement.setDate(3, userBirthday);
                preparedStatement.setInt(4, userPhoneNumber);
                preparedStatement.setString(5, userEmail);
                preparedStatement.setString(6, userPassword);
                preparedStatement.setBoolean(7, isAdmin);
                preparedStatement.executeUpdate();

                // Get the userId of the newly inserted user
                int personId = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());
                preparedStatement.close();

                // Check whether the user is a student or a teacher and import into the corresponding table
                preparedStatement = connection.prepareStatement(isTeacher ? "INSERT INTO \"Teachers\"(id, subject) VALUES (?, ?)" : "INSERT INTO \"Students\"(id, year) VALUES (?, ?)");
                preparedStatement.setInt(1, personId);

                if (isTeacher)
                    preparedStatement.setString(2, userSubject);
                else
                    preparedStatement.setInt(2, userYear);

                preparedStatement.executeUpdate();
                preparedStatement.close();
                connection.close();

                updateSubjects();
                System.out.printf("userId %d created %s: %s (gender: %s, birthday: %s, phone: %d, email: %s, admin: %s%n",
                        User.getId(), isTeacher ? "teacher" : "student", userName, userGender, userBirthday, userPhoneNumber, userEmail, isAdmin ? "Yes" : "No");
            } catch (SQLException err) {
                System.out.println("SQL Exception:");
                err.printStackTrace();
            }
        });
    }

    /**
     * Get all subjects, add any new ones into subjectList and update userDetailsComboBox
     */
    private void updateSubjects() {
        try {
            CachedRowSet subjects = databaseController.selectQuery("SELECT DISTINCT(subject) FROM \"Teachers\"");

            while (subjects.next()) {
                String subjectName = subjects.getString("subject");

                if (!subjectList.contains(subjectName))
                    subjectList.add(subjectName);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        } finally {
            userDetailsComboBox.removeAllItems();
            userDetailsComboBox.addItem("Add New Subject");

            for (String subject : subjectList)
                userDetailsComboBox.addItem(subject);
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
