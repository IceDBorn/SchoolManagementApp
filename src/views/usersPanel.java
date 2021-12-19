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
import java.util.Objects;
import java.util.stream.IntStream;

public class usersPanel extends JFrame {
    private final ArrayList<String> professionList;
    private final ArrayList<String> yearList;
    private DefaultTableModel usersTableModel;
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
    private String selectedUserEmail;
    private boolean selectedUserIsTeacher;

    private TitledBorder title;

    public usersPanel() {
        this.professionList = new ArrayList<>();
        this.yearList = new ArrayList<>();

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
        ((JLabel) userDetailsComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel) userTypeComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel) genderComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Set current date and custom format to birthday picker
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        userBirthDayPicker.setDate(date);
        userBirthDayPicker.setFormats(format);

        // Get all distinct professions from teachers and update professionlist
        panelController.updateList("SELECT name FROM \"Professions\"", professionList);

        // Get all year names and update yearList
        panelController.updateList("SELECT name FROM \"Years\"", yearList);

        updateDetails(true);

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
                updateDetails(true);
            } else {
                title = BorderFactory.createTitledBorder(new EmptyBorder(0, 0, 0, 0), "School Year");
                title.setTitleJustification(TitledBorder.CENTER);
                professionPanel.setBorder(title);
                adminCheckBox.setEnabled(false);
                adminCheckBox.setSelected(false);
                updateDetails(false);
            }
        });

        addButton.addActionListener(action -> {
            if (userDetailsComboBox.getSelectedIndex() == 0) {
                // TODO: (IceDBorn) Add profession addition panel
                // TODO: (Prionysis) Add new profession to the database
                System.out.println(userDetailsComboBox.getSelectedIndex());
                System.out.println("Add new profession");

                // TODO: (Prionysis) Update professionlist or yearList when a new profession or year is added
                boolean isTeacher = Objects.requireNonNull(userTypeComboBox.getSelectedItem()).toString().equals("Teacher");

                if (isTeacher)
                    // Update professionList when a new profession is added to the database
                    panelController.updateList("SELECT name FROM \"Professions\"", professionList);
                else
                    // Update yearList when a new year is added to the database
                    panelController.updateList("SELECT name FROM \"Years\"", yearList);
            } else {
                try {
                    String email = emailTextField.getText();
                    boolean userExists = databaseController.selectQuery(String.format("SELECT id FROM \"Users\" WHERE email = '%s'", email)).isBeforeFirst();

                    // Check if a user already exists with the same email if the name has been changed
                    if (userExists && !selectedUserEmail.equals(email))
                        System.out.println("A user already exists with that email.");
                    else {
                        boolean isAddButton = addButton.getText().equals("Add");

                        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                        PreparedStatement preparedStatement = connection.prepareStatement(isAddButton ?
                                "INSERT INTO \"Users\"(name, gender, birthday, \"isAdmin\", \"isTeacher\", email, password) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                                "UPDATE \"Users\" SET name = ?, gender = ?, birthday = ?, \"isAdmin\" = ?, \"isTeacher\" = ?, email = ? WHERE id = ?", PreparedStatement.RETURN_GENERATED_KEYS);

                        String username = usernameTextField.getText();
                        String details = Objects.requireNonNull(userDetailsComboBox.getSelectedItem()).toString();
                        Date birthday = new Date(userBirthDayPicker.getDate().getTime());
                        int gender = genderComboBox.getSelectedIndex();
                        boolean isTeacher = Objects.requireNonNull(userTypeComboBox.getSelectedItem()).toString().equals("Teacher");
                        boolean isAdmin = adminCheckBox.isSelected();

                        preparedStatement.setString(1, username);
                        preparedStatement.setInt(2, gender);
                        preparedStatement.setDate(3, birthday);
                        preparedStatement.setBoolean(4, isTeacher && isAdmin);
                        preparedStatement.setBoolean(5, isTeacher);
                        preparedStatement.setString(6, email);

                        if (isAddButton)
                            preparedStatement.setString(7, String.valueOf(passwordField.getPassword()));
                        else
                            preparedStatement.setInt(7, selectedUserId);

                        preparedStatement.executeUpdate();

                        // Get the id of the inserted or updated user
                        int id = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());
                        preparedStatement.close();

                        // If it's a save button, check whether user was a student or a teacher and delete them from the corresponding table
                        if (!isAddButton) {
                            preparedStatement = connection.prepareStatement(selectedUserIsTeacher ? "DELETE FROM \"Teachers\" WHERE id = ?" : "DELETE FROM \"Students\" WHERE id = ?");
                            preparedStatement.setInt(1, selectedUserId);
                            preparedStatement.executeUpdate();
                            preparedStatement.close();
                        }

                        // Check whether the new user type is a student or a teacher and import them into the corresponding table
                        preparedStatement = connection.prepareStatement(isTeacher ? "INSERT INTO \"Teachers\"(id, \"professionId\") VALUES (?, ?)" : "INSERT INTO \"Students\"(id, \"yearId\") VALUES (?, ?)");
                        preparedStatement.setInt(1, id);
                        preparedStatement.setInt(2, isTeacher ? databaseController.findProfessionId(details) : databaseController.findYearId(details));

                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        connection.close();

                        System.out.printf("userId %d %s user: %d (type: %s, name: %s, gender: %s, birthday: %s, email: %s, admin: %s)%n",
                                User.getId(),
                                isAddButton ? "created" : "updated",
                                id,
                                isTeacher ? "teacher" : "student",
                                username,
                                gender,
                                new SimpleDateFormat("dd/MM/yyyy").format(birthday),
                                email,
                                isAdmin ? "Yes" : "No");
                    }
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                } finally {
                    updateDetails(true);
                    updateUsers();
                    revertUIComponents();
                }
            }
        });

        cancelButton.addActionListener(action -> revertUIComponents());

        editButton.addActionListener(action -> {
            // Get the selected row index
            int selectedRow = usersTable.getSelectedRow();

            // Store the selected user id, email and type to a global variable
            try {
                CachedRowSet users = databaseController.selectQuery(String.format("SELECT id, email, \"isTeacher\" FROM \"Users\" WHERE email = '%s'", usersTable.getValueAt(selectedRow, 1).toString()));
                users.next();

                selectedUserId = users.getInt("id");
                selectedUserEmail = users.getString("email");
                selectedUserIsTeacher = users.getBoolean("isTeacher");
            } catch (SQLException err) {
                System.out.println("SQL Exception:");
                err.printStackTrace();
            }

            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            addButton.setText("Save");
            usersTable.setEnabled(false);
            passwordField.setText("");
            passwordField.setEnabled(false);
            usernameTextField.setText(usersTable.getValueAt(selectedRow, 0).toString());
            emailTextField.setText(usersTable.getValueAt(selectedRow, 1).toString());

            if (usersTable.getValueAt(selectedRow, 2).toString().equals("Teacher"))
                userTypeComboBox.setSelectedIndex(0);
            else
                userTypeComboBox.setSelectedIndex(1);

            userDetailsComboBox.setSelectedItem(usersTable.getValueAt(selectedRow, 3));

            if (usersTable.getValueAt(selectedRow, 4).toString().equals("Male"))
                userTypeComboBox.setSelectedIndex(0);
            else
                userTypeComboBox.setSelectedIndex(1);

            try {
                userBirthDayPicker.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(usersTable.getValueAt(selectedRow, 5).toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            adminCheckBox.setSelected(usersTable.getValueAt(selectedRow, 6).toString().equals("Yes"));

            usersTable.clearSelection();
        });

        removeButton.addActionListener(action -> {
            // Get the selected row index
            int selectedRow = usersTable.getSelectedRow();

            try {
                // Get the id of the selected user
                CachedRowSet users = databaseController.selectQuery(String.format("SELECT id FROM \"Users\" WHERE email = '%s'", usersTable.getValueAt(selectedRow, 1).toString()));
                users.next();
                int id = users.getInt("id");

                // Check whether the user is a teacher or not
                boolean isTeacher = usersTable.getValueAt(selectedRow, 2).toString().equals("Teacher");

                // Check whether the selected user is a teacher or a student and delete them from the corresponding table
                Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                PreparedStatement preparedStatement = connection.prepareStatement(isTeacher ? "DELETE FROM \"Teachers\" WHERE id = ?" : "DELETE FROM \"Students\" WHERE id = ?");
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                // Delete the user from the database
                preparedStatement = connection.prepareStatement("DELETE FROM \"Users\" WHERE id = ?");
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();
                preparedStatement.close();
                connection.close();

                System.out.printf("id %d deleted user: %d (type: %s)%n",
                        User.getId(), id, isTeacher ? "teacher" : "student");
            } catch (SQLException err) {
                System.out.println("SQL Exception: ");
                err.printStackTrace();
            } finally {
                updateUsers();
                revertUIComponents();
            }

            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            usersTable.getSelectionModel().clearSelection();
        });

        backButton.addActionListener(action -> {
            panelController.createMainPanel();
            this.setVisible(false);
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
     * Update userDetailsComboBox if true with professions, if false with school years
     */
    private void updateDetails(boolean showProfessions) {
        userDetailsComboBox.removeAllItems();
        userDetailsComboBox.addItem("Add new");

        if (showProfessions) {
            for (String profession : professionList)
                userDetailsComboBox.addItem(profession);

        } else {
            for (String year : yearList)
                userDetailsComboBox.addItem(year);
        }

        if (userDetailsComboBox.getItemCount() > 1)
            userDetailsComboBox.setSelectedIndex(1);
    }

    private void updateUsers() {
        IntStream.iterate(usersTableModel.getRowCount() - 1, i -> i > -1, i -> i - 1).forEach(i -> usersTableModel.removeRow(i));

        try {
            CachedRowSet users = databaseController.selectQuery("""
                    SELECT name, email, gender, birthday, "isAdmin", "isTeacher", "yearId", "professionId" FROM "Users"
                    LEFT JOIN "Students" on "Users".id = "Students".id
                    LEFT JOIN "Teachers" on "Users".id = "Teachers".id
                    ORDER BY name""");

            // Add rows
            Object[] row = new Object[8];

            while (users.next()) {
                boolean isTeacher = users.getBoolean("isTeacher");

                row[0] = users.getString("name");
                row[1] = users.getString("email");
                row[2] = isTeacher ? "Teacher" : "Student";
                row[3] = isTeacher ? databaseController.findProfessionName(users.getInt("professionId")) : databaseController.findYearName(users.getInt("yearId"));
                row[4] = users.getInt("gender") == 0 ? "Male" : "Female";
                row[5] = users.getDate("birthday");
                row[6] = users.getBoolean("isAdmin") ? "Yes" : "No";

                usersTableModel.addRow(row);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        } finally {
            panelController.fillEmptyRows(usersTableModel);
            usersTable.setModel(usersTableModel);
        }
    }

    /**
     * Enable or disable the add button based on class name text
     */
    private void enableButtons() {
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

    private void createUIComponents() {
        // Add columns
        String[] usersTableColumns = {"Name", "Email", "Type", "Profession/School Year", "Gender", "Birthday", "Admin"};
        usersTableModel = new DefaultTableModel(usersTableColumns, 0);
        usersTable = new JTable(usersTableModel);
        // Stop users from interacting with the table
        usersTable.getTableHeader().setReorderingAllowed(false);
        updateUsers();
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
        selectedUserEmail = "";
        selectedUserIsTeacher = false;
    }
}
