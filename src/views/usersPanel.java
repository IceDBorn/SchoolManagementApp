package views;

import controllers.databaseController;
import controllers.panelController;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

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
    private int selectedUserId;
    private String selectedUserName;
    private boolean selectedUserIsTeacher;

    private TitledBorder title;

    public usersPanel() {
        this.professionList = new ArrayList<>();
        add(usersPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        usersTable.setDefaultEditor(Object.class, null);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Center combobox text
        ((JLabel) userDetailsComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel) userTypeComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel) genderComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        // Set current date and custom format to birthday picker
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        userBirthDayPicker.setDate(date);
        userBirthDayPicker.setFormats(format);

        updateSubjects();

        // Set profession panel titled border
        title = BorderFactory.createTitledBorder(new EmptyBorder(0, 0, 0, 0), "Profession");
        title.setTitleJustification(TitledBorder.CENTER);
        professionPanel.setBorder(title);

        userTypeComboBox.addItem("Teacher");
        userTypeComboBox.addItem("Student");

        genderComboBox.addItem("Male");
        genderComboBox.addItem("Female");

        userTypeComboBox.addItemListener(item -> {
            userDetailsComboBox.removeAllItems();

            if (Objects.requireNonNull(userTypeComboBox.getSelectedItem()).toString().equals("Teacher")) {
                title = BorderFactory.createTitledBorder(new EmptyBorder(0, 0, 0, 0), "Profession");
                title.setTitleJustification(TitledBorder.CENTER);
                professionPanel.setBorder(title);
                adminCheckBox.setEnabled(true);
                updateSubjects();
            } else {
                title = BorderFactory.createTitledBorder(new EmptyBorder(0, 0, 0, 0), "School Year");
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
                try {
                    String userName = usernameTextField.getText();
                    String userEmail = emailTextField.getText();
                    String userPassword = String.valueOf(passwordField.getPassword());
                    String userSubject = Objects.requireNonNull(userDetailsComboBox.getSelectedItem()).toString();
                    Date userBirthday = new Date(userBirthDayPicker.getDate().getTime());
                    int userGender = genderComboBox.getSelectedIndex();
                    int userYear = userDetailsComboBox.getSelectedIndex() + 1;
                    boolean isTeacher = Objects.requireNonNull(userTypeComboBox.getSelectedItem()).toString().equals("Teacher");
                    boolean isAdmin = adminCheckBox.isSelected();
                    boolean isAddButton = addButton.getText().equals("Add");
                    boolean userExists = databaseController.selectQuery(String.format("SELECT id FROM \"Users\" WHERE email = '%s'", userEmail)).isBeforeFirst();

                    // Check if a user already exists with the same email if the name has been changed
                    if (userExists && !selectedUserName.equals(userName))
                        System.out.println("A user already exists with that email.");
                    else {
                        String query;
                        if (isAddButton)
                            query = "INSERT INTO \"Users\"(name, gender, birthday, \"isAdmin\", \"isTeacher\", email, password) VALUES (?, ?, ?, ?, ?, ?)";
                        else
                            query = "UPDATE \"Users\" SET name = ?, gender = ?, birthday = ?, \"isAdmin\" = ?, \"isTeacher\" = ?, email = ? WHERE id = ?";

                        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                        PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

                        preparedStatement.setString(1, userName);
                        preparedStatement.setInt(2, userGender);
                        preparedStatement.setDate(3, userBirthday);
                        preparedStatement.setBoolean(4, isTeacher && isAdmin);
                        preparedStatement.setBoolean(5, isTeacher);
                        preparedStatement.setString(6, userEmail);

                        if (isAddButton)
                            preparedStatement.setString(7, userPassword);
                        else
                            preparedStatement.setInt(7, selectedUserId);

                        preparedStatement.executeUpdate();

                        // Get the userId of the newly inserted user
                        int userId = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());
                        preparedStatement.close();

                        if (isAddButton) {
                            // Check whether the existing user was a teacher or a student and delete them from the corresponding table.
                                preparedStatement = connection.prepareStatement(selectedUserIsTeacher ? "DELETE FROM \"Teachers\" WHERE id = ?" : "DELETE FROM \"Students\" WHERE id = ?");
                            preparedStatement.setInt(1, selectedUserId);
                            preparedStatement.executeUpdate();
                            preparedStatement.close();
                        }

                        // Check whether the new user type is a student or a teacher and import them into the corresponding table
                        preparedStatement = connection.prepareStatement(isTeacher ? "INSERT INTO \"Teachers\"(id, subject) VALUES (?, ?)" : "INSERT INTO \"Students\"(id, year) VALUES (?, ?)");
                        preparedStatement.setInt(1, userId);

                        if (isTeacher)
                            preparedStatement.setString(2, userSubject);
                        else
                            preparedStatement.setInt(2, userYear);

                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        connection.close();

                        System.out.printf("userId %d %s user: %d (type: %s, name: %s, gender: %s, birthday: %s, email: %s, admin: %s)%n",
                                User.getId(),
                                isAddButton ? "created" : "updated",
                                userId,
                                isTeacher ? "teacher" : "student",
                                userName,
                                userGender,
                                new SimpleDateFormat("dd/MM/yyyy").format(userBirthday),
                                userEmail,
                                isAdmin ? "Yes" : "No");
                    }
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                } finally {
                    updateUsers();
                    updateSubjects();
                    revertUIComponents();
                }
            }
        });

        cancelButton.addActionListener(action -> revertUIComponents());

        editButton.addActionListener(action -> {
            // Check if a row is selected
            if (!usersTable.isRowSelected(usersTable.getSelectedRow()))
                System.out.println("You don't have a selected row.");
            else {
                // Get the selected userId and store it to a global variable
                try {
                    CachedRowSet users = databaseController.selectQuery(String.format("SELECT id, name FROM \"Users\" WHERE email = '%s'", usersTable.getValueAt(usersTable.getSelectedRow(), 1).toString()));
                    users.next();
                    selectedUserId = users.getInt("id");
                    selectedUserName = users.getString("name");
                    selectedUserIsTeacher = databaseController.selectQuery(String.format("SELECT id FROM \"Teachers\" WHERE id = '%d'", selectedUserId)).isBeforeFirst();
                } catch (SQLException err) {
                    System.out.println("SQL Exception: ");
                    err.printStackTrace();
                }

                editButton.setEnabled(false);
                removeButton.setEnabled(false);
                addButton.setText("Save");
                usersTable.setEnabled(false);
                passwordField.setText("");
                passwordField.setEnabled(false);
                usernameTextField.setText(usersTable.getValueAt(usersTable.getSelectedRow(), 0).toString());
                emailTextField.setText(usersTable.getValueAt(usersTable.getSelectedRow(), 1).toString());

                if (usersTable.getValueAt(usersTable.getSelectedRow(), 2).toString().equals("Teacher"))
                    userTypeComboBox.setSelectedIndex(0);
                else
                    userTypeComboBox.setSelectedIndex(1);

                userDetailsComboBox.setSelectedItem(usersTable.getValueAt(usersTable.getSelectedRow(), 3));

                if (usersTable.getValueAt(usersTable.getSelectedRow(), 4).toString().equals("Male"))
                    userTypeComboBox.setSelectedIndex(0);
                else
                    userTypeComboBox.setSelectedIndex(1);

                try {
                    userBirthDayPicker.setDate(new SimpleDateFormat("dd/MM/yyyy").parse(usersTable.getValueAt(usersTable.getSelectedRow(), 5).toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                adminCheckBox.setSelected(usersTable.getValueAt(usersTable.getSelectedRow(), 6).toString().equals("Yes"));

                usersTable.clearSelection();
            }
        });

        removeButton.addActionListener(action -> {
            // Get the selected row index
            int selectedRow = usersTable.getSelectedRow();

            try {
                // Get the userId of the selected user
                CachedRowSet users = databaseController.selectQuery(String.format("SELECT id FROM \"Users\" WHERE email = '%s'", usersTable.getValueAt(selectedRow, 1).toString()));
                users.next();
                int userId = users.getInt("id");

                // Check whether the user is a teacher or not
                boolean isTeacher = usersTable.getValueAt(usersTable.getSelectedRow(), 2).toString().equals("Teacher");

                // Check whether the selected user is a teacher or a student and delete them from the corresponding table
                Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                PreparedStatement preparedStatement = connection.prepareStatement(isTeacher ? "DELETE FROM \"Teachers\" WHERE id = ?" : "DELETE FROM \"Students\" WHERE id = ?");
                preparedStatement.setInt(1, userId);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                // Delete the user from the database
                preparedStatement = connection.prepareStatement("DELETE FROM \"Users\" WHERE id = ?");
                preparedStatement.setInt(1, userId);
                preparedStatement.executeUpdate();
                preparedStatement.close();
                connection.close();

                System.out.printf("userId %d deleted user: %d (type: %s)%n",
                        User.getId(), userId, isTeacher ? "teacher" : "student");
            } catch (SQLException err) {
                System.out.println("SQL Exception: ");
                err.printStackTrace();
            } finally {
                updateSubjects();
                updateUsers();
                revertUIComponents();
            }

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
        IntStream.iterate(usersTableModel.getRowCount() - 1, i -> i > -1, i -> i - 1).forEach(i -> usersTableModel.removeRow(i));
        Object[] userRow = new Object[8];

        try {
            CachedRowSet users = databaseController.selectQuery("""
                    SELECT name, email, gender, birthday, "isAdmin", "isTeacher", year, subject FROM "Users"
                    LEFT JOIN "Students" on "Users".id = "Students".id
                    LEFT JOIN "Teachers" on "Users".id = "Teachers".id
                    ORDER BY name""");

            // Add rows
            while (users.next()) {
                boolean isTeacher = users.getBoolean("isTeacher");

                userRow[0] = users.getString("name");
                userRow[1] = users.getString("email");
                userRow[2] = isTeacher ? "Teacher" : "Student";
                userRow[3] = isTeacher ? users.getString("subject") : panelController.getYear(users.getInt("year"));
                userRow[4] = users.getInt("gender") == 0 ? "Male" : "Female";
                userRow[5] = users.getDate("birthday");
                userRow[6] = users.getBoolean("isAdmin") ? "Yes" : "No";
                usersTableModel.addRow(userRow);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        } finally {
            panelController.fillEmptyRows(userRow, usersTableModel);
            usersTable.setModel(usersTableModel);
        }
    }

    private void enableButtons() {
        // Enable or disable add button based on class name text
        if (!usernameTextField.getText().equals("") && !emailTextField.getText().equals("")
            && !(passwordField.getPassword().length == 0 && passwordField.isEnabled())
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

        if (userDetailsComboBox.getItemCount() > 1)
            userDetailsComboBox.setSelectedIndex(1);

        genderComboBox.setSelectedIndex(0);

        Date date = new Date(System.currentTimeMillis());
        userBirthDayPicker.setDate(date);

        adminCheckBox.setSelected(false);
        adminCheckBox.setEnabled(true);

        selectedUserId = -1;
        selectedUserName = "";
        selectedUserIsTeacher = false;
    }
}
