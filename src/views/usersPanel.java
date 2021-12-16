package views;

import controllers.databaseController;
import models.Database;
import models.User;
import org.jdesktop.swingx.JXDatePicker;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class usersPanel extends JFrame {
    private final ArrayList<String> professionList;
    DefaultTableModel usersTableModel;
    private JPanel usersPanel;
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
    private JButton cancelButton;
    private JButton editButton;
    private JButton removeButton;

    private TitledBorder title;

    public usersPanel() {
        this.professionList = new ArrayList<>();
        add(usersPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // Customize table to have no border, disable cell editing and switch single row selection
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        usersTable.setDefaultEditor(Object.class, null);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Center combobox text
        ((JLabel)userDetailsComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel)userTypeComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel)genderComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        // Set current date and custom format to birthday picker
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        userBirthDayPicker.setDate(date);
        userBirthDayPicker.setFormats(format);

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
            if (userDetailsComboBox.getSelectedIndex() == 0) {
                // TODO: (IceDBorn) Add profession addition panel
                // TODO: (Prionysis) Add new profession to the database
                System.out.println(userDetailsComboBox.getSelectedIndex());
                System.out.println("Add new profession");
            } else {
                // TODO: (Prionysis) Add new user to database and update existing users
                String userName = usernameTextField.getText();
                String userEmail = emailTextField.getText();
                String userPassword = String.valueOf(passwordField.getPassword());
                String userSubject = Objects.requireNonNull(userDetailsComboBox.getSelectedItem()).toString();
                Date userBirthday = new Date(userBirthDayPicker.getDate().getTime());
                int userGender = genderComboBox.getSelectedIndex();
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
                            User.getId(), isTeacher ? "teacher" : "student", userName, userGender, userBirthday, userEmail, isAdmin ? "Yes" : "No");
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }

                revertUIComponents();
            }
        });

        cancelButton.addActionListener(action -> {
            revertUIComponents();
        });

        editButton.addActionListener(action -> {
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            addButton.setText("Save");
            usersTable.setEnabled(false);
            usernameTextField.setText(usersTable.getValueAt(usersTable.getSelectedRow(), 0).toString());
            emailTextField.setText(usersTable.getValueAt(usersTable.getSelectedRow(), 1).toString());
            passwordField.setText("");
            passwordField.setEnabled(false);

            if (usersTable.getValueAt(usersTable.getSelectedRow(), 2).toString().equals("Teacher")) {
                userTypeComboBox.setSelectedIndex(0);
            } else {
                userTypeComboBox.setSelectedIndex(1);
            }

            userDetailsComboBox.setSelectedItem(usersTable.getValueAt(usersTable.getSelectedRow(), 3));

            if (usersTable.getValueAt(usersTable.getSelectedRow(), 4).toString().equals("Male")) {
                userTypeComboBox.setSelectedIndex(0);
            } else {
                userTypeComboBox.setSelectedIndex(1);
            }

            try {
                userBirthDayPicker.setDate(new SimpleDateFormat("dd/MM/yyyy").parse(usersTable.getValueAt(usersTable.getSelectedRow(), 5).toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            adminCheckBox.setSelected(usersTable.getValueAt(usersTable.getSelectedRow(), 6).toString().equals("Yes"));

            usersTable.clearSelection();
        });

        removeButton.addActionListener(action -> {
            // TODO: (Prionysis) Remove entry from database
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            usersTable.getSelectionModel().clearSelection();
        });

        backButton.addActionListener(action -> {
            // TODO: (IceDBorn) Close this panel and open the main panel
        });

        // Listen for changes in the user name text
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

        // Listen for changes in the email text
        emailTextField.getDocument().addDocumentListener(new DocumentListener() {
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

        usersTable.getSelectionModel().addListSelectionListener(selection -> {
            if (usersTable.getSelectedRow() != -1
                    && !usersTable.getValueAt(usersTable.getSelectedRow(), 0).toString().equals("")
                    && !usersTable.getValueAt(usersTable.getSelectedRow(), 1).toString().equals("")) {
                editButton.setEnabled(true);
                removeButton.setEnabled(true);
            } else {
                editButton.setEnabled(false);
                removeButton.setEnabled(false);
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

                if (!professionList.contains(subjectName))
                    professionList.add(subjectName);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        } finally {
            userDetailsComboBox.removeAllItems();
            userDetailsComboBox.addItem("Add new");

            for (String subject : professionList)
                userDetailsComboBox.addItem(subject);

            if (userDetailsComboBox.getItemCount() > 1) {
                userDetailsComboBox.setSelectedIndex(1);
            }
        }
    }

    private void createUIComponents() {
        // Add columns
        String[] usersTableColumns = {"Name", "Email", "Type", "Profession/School Year", "Gender", "Birthday", "Admin"};
        usersTableModel = new DefaultTableModel(usersTableColumns, 0);
        usersTable = new JTable(usersTableModel);
        // Stop users from interacting with the table
        usersTable.getTableHeader().setReorderingAllowed(false);
        updateUsers();
    }

    private void updateUsers() {
        // TODO: (Prionysis) Update table with users from database
        Object[] row = new Object[7];
        row[0] = "IceDBorn";
        row[1] = "ok@ok.com";
        row[2] = "Teacher";
        row[3] = "ne";
        row[4] = "Male";
        row[5] = "01/12/2021";
        row[6] = "Yes";
        usersTableModel.addRow(row);
        row[0] = "Prionysis";
        row[1] = "ne@ne.com";
        row[2] = "Student";
        row[3] = "3η Γυμνασίου";
        row[4] = "Female";
        row[5] = "17/12/2021";
        row[6] = "No";
        usersTableModel.addRow(row);
        usersTable.setModel(usersTableModel);
    }

    private void enableButtons() {
        // Enable or disable add button based on class name text
        if (!usernameTextField.getText().equals("") && !emailTextField.getText().equals("")
                && !(passwordField.getPassword().length == 0 && !passwordField.isEnabled())
                && !userBirthDayPicker.getDate().toString().equals("")) {
            addButton.setEnabled(true);
            cancelButton.setEnabled(true);
        } else {
            addButton.setEnabled(false);
            cancelButton.setEnabled(false);
        }
    }

    private void revertUIComponents() {
        usernameTextField.setText("");
        emailTextField.setText("");
        passwordField.setText("");
        passwordField.setEnabled(true);
        addButton.setText("Add");
        usersTable.setEnabled(true);
        userTypeComboBox.setSelectedIndex(0);

        if (userDetailsComboBox.getItemCount() > 1) {
            userDetailsComboBox.setSelectedIndex(1);
        }

        genderComboBox.setSelectedIndex(0);

        Date date = new Date(System.currentTimeMillis());
        userBirthDayPicker.setDate(date);

        adminCheckBox.setSelected(false);
        adminCheckBox.setEnabled(true);
    }
}
