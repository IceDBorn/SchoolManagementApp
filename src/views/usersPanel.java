package views;

import controllers.databaseController;
import controllers.fileController;
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
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

    public usersPanel(Point location) throws IOException {
        add(usersPanel);
        setTitle("Users");
        setSize(1280, 720);
        setResizable(false);
        setLocation(location);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon("res/school.png").getImage());

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

        // Update professionList with profession names
        this.professionList = new ArrayList<>();
        panelController.updateList("SELECT name FROM \"Professions\"", professionList, this);

        // Update yearList with year names
        this.yearList = new ArrayList<>();
        panelController.updateList("SELECT name FROM \"Years\"", yearList, this);

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
                boolean isTeacher = Objects.requireNonNull(userTypeComboBox.getSelectedItem()).toString().equals("Teacher");

                panelController.createAddNewEntryPanel(isTeacher);

                try {
                    if (isTeacher) {
                        // Update professionList when a new profession is added to the database
                        panelController.updateList("SELECT name FROM \"Professions\"", professionList, this);
                        updateDetails(true);
                    } else {
                        // Update yearList when a new year is added to the database
                        panelController.updateList("SELECT name FROM \"Years\"", yearList, this);
                        updateDetails(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    String email = emailTextField.getText();
                    boolean userExists = databaseController.selectQuery(String.format("SELECT id FROM \"Users\" WHERE email = '%s'", email)).isBeforeFirst();

                    // Check if a user already exists with the same email if the name has been changed
                    if (userExists && !selectedUserEmail.equals(email))
                        panelController.createErrorPanel("A user with that email already exists", this, 300);
                    else {
                        String username = usernameTextField.getText();
                        String details = Objects.requireNonNull(userDetailsComboBox.getSelectedItem()).toString();
                        Date birthday = new Date(userBirthDayPicker.getDate().getTime());
                        int gender = genderComboBox.getSelectedIndex();
                        boolean isTeacher = Objects.requireNonNull(userTypeComboBox.getSelectedItem()).toString().equals("Teacher");
                        boolean isAdmin = adminCheckBox.isSelected();
                        boolean isAddButton = addButton.getText().equals("Add");

                        int count = 0;

                        if (!isAddButton)
                            count = databaseController.selectFirstIntColumn(String.format(isTeacher ?
                                    "SELECT COUNT(id) FROM \"Courses\" WHERE \"teacherId\" = '%d'" :
                                    "SELECT COUNT(id) FROM \"StudentLessons\" WHERE \"studentId\" = '%d'", selectedUserId));

                        // Check if the user is being used by a course or a student lesson and ask for deletion confirmation
                        if (count > 0) {
                            if (panelController.createConfirmationPanel(this) == JOptionPane.YES_OPTION) {
                                Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                                PreparedStatement preparedStatement;

                                // If the selected user is a teacher, delete all student lessons associated with the selected teacher's courses
                                if (!isTeacher) {
                                    preparedStatement = connection.prepareStatement("DELETE FROM \"StudentLessons\" WHERE \"courseId\" IN (SELECT id FROM \"Courses\" WHERE \"teacherId\" = ?)");
                                    preparedStatement.setInt(1, selectedUserId);
                                    preparedStatement.addBatch();
                                }

                                // Delete all courses or student lessons associated with the user
                                preparedStatement = connection.prepareStatement(isTeacher ? "DELETE FROM \"Courses\" WHERE \"teacherId\" = ?" : "DELETE FROM \"StudentLessons\" WHERE \"studentId\" = ?");
                                preparedStatement.setInt(1, selectedUserId);
                                preparedStatement.addBatch();

                                preparedStatement.executeBatch();
                                preparedStatement.close();
                                connection.close();
                            }
                        } else {
                            Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                            PreparedStatement preparedStatement;

                            if (isTeacher != selectedUserIsTeacher) {
                                preparedStatement = connection.prepareStatement(selectedUserIsTeacher ? "DELETE FROM \"Teachers\" WHERE id = ?" : "DELETE FROM \"Students\" WHERE id = ?");
                                preparedStatement.setInt(1, selectedUserId);
                            }

                            preparedStatement = connection.prepareStatement(isAddButton ?
                                    "INSERT INTO \"Users\"(name, gender, birthday, \"isAdmin\", \"isTeacher\", email, password) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                                    "UPDATE \"Users\" SET name = ?, gender = ?, birthday = ?, \"isAdmin\" = ?, \"isTeacher\" = ?, email = ? WHERE id = ?", PreparedStatement.RETURN_GENERATED_KEYS);

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

                            int id = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());

                            preparedStatement.close();

                            // Check if it's a save button
                            if (!isAddButton) {
                                // Check if the user type is different from the previous one
                                if (isTeacher != selectedUserIsTeacher) {
                                    preparedStatement = connection.prepareStatement(selectedUserIsTeacher ? "DELETE FROM \"Teachers\" WHERE id = ?" : "DELETE FROM \"Students\" WHERE id = ?");
                                    preparedStatement.setInt(1, selectedUserId);
                                    preparedStatement.addBatch();

                                    // Import the user into their corresponding table type
                                    preparedStatement = connection.prepareStatement(isTeacher ? "INSERT INTO \"Teachers\"(id, \"professionId\") VALUES (?, ?)" : "INSERT INTO \"Students\"(id, \"yearId\") VALUES (?, ?)");
                                    preparedStatement.setInt(1, id);
                                    preparedStatement.setInt(2, isTeacher ? databaseController.findProfessionId(details) : databaseController.findYearId(details));
                                    preparedStatement.addBatch();

                                    preparedStatement.executeBatch();
                                } else {
                                    preparedStatement = connection.prepareStatement(selectedUserIsTeacher ? "UPDATE \"Teachers\" SET \"professionId\" = ? WHERE id = ?" : "UPDATE \"Students\" SET \"yearId\" = ? WHERE id = ?");
                                    preparedStatement.setInt(1, selectedUserIsTeacher ? databaseController.findProfessionId(details) : databaseController.findYearId(details));
                                    preparedStatement.setInt(2, selectedUserId);
                                    preparedStatement.executeUpdate();
                                }
                            } else {
                                // Import the user into their corresponding table type
                                preparedStatement = connection.prepareStatement(isTeacher ? "INSERT INTO \"Teachers\"(id, \"professionId\") VALUES (?, ?)" : "INSERT INTO \"Students\"(id, \"yearId\") VALUES (?, ?)");
                                preparedStatement.setInt(1, id);
                                preparedStatement.setInt(2, isTeacher ? databaseController.findProfessionId(details) : databaseController.findYearId(details));
                                preparedStatement.executeUpdate();
                            }

                            preparedStatement.close();
                            connection.close();

                            fileController.saveFile("User (%d) %s%s user (%d) %s.".formatted(
                                    User.getId(), User.getName(), isAddButton ? " created " : " updated ", id, username));
                        }
                    }
                } catch (SQLException | IOException err) {
                    StringWriter errors = new StringWriter();
                    err.printStackTrace(new PrintWriter(errors));
                    String message = errors.toString();
                    try {
                        fileController.saveFile("SQL Exception: " + message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    panelController.createErrorPanel("Something went wrong.", this, 220);
                } finally {
                    updateDetails(true);
                    try {
                        updateUsers();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                StringWriter errors = new StringWriter();
                err.printStackTrace(new PrintWriter(errors));
                String message = errors.toString();
                try {
                    fileController.saveFile("SQL Exception: " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                panelController.createErrorPanel("Something went wrong.", this, 220);
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
            try {
                // Get the selected row index
                int selectedRow = usersTable.getSelectedRow();

                // Get the selected user id
                int id = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Users\" WHERE email = '%s'", usersTable.getValueAt(selectedRow, 1).toString()));

                // Check whether the user is a teacher or not
                boolean isTeacher = usersTable.getValueAt(selectedRow, 2).toString().equals("Teacher");

                // Check how many courses or student lessons exist using that userId
                int count = databaseController.selectFirstIntColumn(String.format(isTeacher ?
                        "SELECT COUNT(id) FROM \"Courses\" WHERE \"teacherId\" = '%d'" :
                        "SELECT COUNT(id) FROM \"StudentLessons\" WHERE \"studentId\" = '%d'", id));

                if (count > 0) {
                    if (panelController.createConfirmationPanel(this) == JOptionPane.YES_OPTION) {
                        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                        PreparedStatement preparedStatement;

                        // If the selected user is a teacher, delete all student lessons associated with the selected teacher's courses
                        if (!isTeacher) {
                            preparedStatement = connection.prepareStatement("DELETE FROM \"StudentLessons\" WHERE \"courseId\" IN (SELECT id FROM \"Courses\" WHERE \"teacherId\" = ?)");
                            preparedStatement.setInt(1, id);
                            preparedStatement.addBatch();
                        }

                        // Check whether the selected user is a teacher or a student and delete them from the corresponding table
                        preparedStatement = connection.prepareStatement(isTeacher ? "DELETE FROM \"Courses\" WHERE \"teacherId\" = ?" : "DELETE FROM \"StudentLessons\" WHERE \"studentId\" = ?");
                        preparedStatement.setInt(1, id);
                        preparedStatement.addBatch();

                        preparedStatement.executeBatch();
                        preparedStatement.close();
                        connection.close();

                        fileController.saveFile("User (%d) %s deleted user (%d).".formatted(
                                User.getId(), User.getName(), id));
                    }
                } else {
                    Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                    PreparedStatement preparedStatement;

                    // Delete the user from their corresponding table type
                    preparedStatement = connection.prepareStatement(isTeacher ? "DELETE FROM \"Teachers\" WHERE id = ?" : "DELETE FROM \"Students\" WHERE id = ?");
                    preparedStatement.setInt(1, id);
                    preparedStatement.addBatch();

                    // Delete the user from the database
                    preparedStatement = connection.prepareStatement("DELETE FROM \"Users\" WHERE id = ?");
                    preparedStatement.setInt(1, id);
                    preparedStatement.addBatch();

                    preparedStatement.executeBatch();
                    preparedStatement.close();
                    connection.close();

                    fileController.saveFile("User (%d) %s deleted user (%d).".formatted(
                            User.getId(), User.getName(), id));
                }
            } catch (SQLException | IOException err) {
                StringWriter errors = new StringWriter();
                err.printStackTrace(new PrintWriter(errors));
                String message = errors.toString();
                try {
                    fileController.saveFile("SQL Exception: " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                panelController.createErrorPanel("Something went wrong.", this, 220);
            } finally {
                try {
                    updateUsers();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                revertUIComponents();

                editButton.setEnabled(false);
                removeButton.setEnabled(false);
                usersTable.getSelectionModel().clearSelection();
            }
        });

        backButton.addActionListener(action -> {
            panelController.createMainPanel(this.getLocation());
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

    private void updateUsers() throws IOException {
        IntStream.iterate(usersTableModel.getRowCount() - 1, i -> i > -1, i -> i - 1).forEach(i -> usersTableModel.removeRow(i));

        try {
            CachedRowSet users = databaseController.selectQuery("""
                    SELECT "Users".name as name,
                           email,
                           gender,
                           birthday,
                           "isAdmin",
                           "isTeacher",
                           "Professions".name as profession,
                           "Years".name as year
                    FROM "Users"
                             LEFT JOIN "Students" ON "Users".id = "Students".id
                             LEFT JOIN "Teachers" ON "Users".id = "Teachers".id
                             LEFT JOIN "Professions" ON "professionId" = "Professions".id
                             LEFT JOIN "Years" ON "yearId" = "Years".id
                    ORDER BY "Users".id""");

            // Add rows
            Object[] row = new Object[7];

            while (users.next()) {
                boolean isTeacher = users.getBoolean("isTeacher");

                row[0] = users.getString("name");
                row[1] = users.getString("email");
                row[2] = isTeacher ? "Teacher" : "Student";
                row[3] = isTeacher ? users.getString("profession") : users.getString("year");
                row[4] = users.getInt("gender") == 0 ? "Male" : "Female";
                row[5] = users.getDate("birthday");
                row[6] = users.getBoolean("isAdmin") ? "Yes" : "No";

                usersTableModel.addRow(row);
            }
        } catch (SQLException err) {
            StringWriter errors = new StringWriter();
            err.printStackTrace(new PrintWriter(errors));
            String message = errors.toString();
            fileController.saveFile("SQL Exception: " + message);

            panelController.createErrorPanel("Something went wrong.", this, 220);
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

    private void createUIComponents() throws IOException {
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
