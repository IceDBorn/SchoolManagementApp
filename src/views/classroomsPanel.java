package views;

import controllers.databaseController;
import models.Database;
import models.User;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.IntStream;

public class classroomsPanel extends JFrame {
    DefaultTableModel classroomsTableModel;
    private JPanel classroomsPanel;
    private JTextField classNameTextField;
    private JSpinner classCapacitySpinner;
    private JButton addButton;
    private JTable classroomsTable;
    private JScrollPane scrollPane;
    private JButton editButton;
    private JButton removeButton;
    private JButton backButton;
    private JButton cancelButton;
    private int selectedClassroomId;

    public classroomsPanel() {
        add(classroomsPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        classroomsTable.setDefaultEditor(Object.class, null);
        classroomsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setValue(1);
        model.setMinimum(1);
        model.setMaximum(99);
        classCapacitySpinner.setModel(model);

        // Listeners
        backButton.addActionListener(action -> {
            // TODO: (IceDBorn) Close this panel and open the main panel
        });

        addButton.addActionListener(action -> {
            String classroomName = classNameTextField.getText();
            int classroomLimit = (int) classCapacitySpinner.getValue();

            // Check if the text field is blank to avoid unnecessary sql errors
            if (classroomName.equals(""))
                System.out.println("You can not have a blank classroom name.");
            else if (addButton.getText().equals("Save")) {
                try {
                    Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                    PreparedStatement preparedStatement = connection.prepareStatement("UPDATE \"Classrooms\" SET name = ?, \"limit\" = ? WHERE id = ?");
                    preparedStatement.setString(1, classroomName);
                    preparedStatement.setInt(2, classroomLimit);
                    preparedStatement.setInt(3, selectedClassroomId);
                    preparedStatement.executeUpdate();

                    preparedStatement.close();
                    connection.close();

                    System.out.printf("userId %d updated classroom: %d (name: %s, limit :%d)%n",
                            User.getId(), selectedClassroomId, classroomName, classroomLimit);
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                } finally {
                    updateClassrooms();
                    revertUIComponents();
                }
            } else if (addButton.getText().equals("Add")) {
                try {
                    boolean classroomExists = databaseController.selectQuery(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", classroomName)).isBeforeFirst();

                    // Check if a classroom already exists with that name
                    if (classroomExists)
                        System.out.println("A classroom already exists with that name.");
                    else {
                        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO \"Classrooms\"(name, \"limit\") VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
                        preparedStatement.setString(1, classroomName);
                        preparedStatement.setInt(2, classroomLimit);
                        preparedStatement.executeUpdate();

                        // Get the classroomId of the newly inserted classroom
                        int classroomId = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());

                        preparedStatement.close();
                        connection.close();

                        System.out.printf("userId %d created classroom: %d (name: %s, limit :%d)%n",
                                User.getId(), classroomId, classroomName, classroomLimit);
                    }
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                } finally {
                    updateClassrooms();
                    revertUIComponents();
                }
            }
        });

        cancelButton.addActionListener(action -> revertUIComponents());

        editButton.addActionListener(action -> {
            // Get selected row's classroom name and capacity
            int selectedRow = classroomsTable.getSelectedRow();
            String classroomName = classroomsTable.getValueAt(selectedRow, 0).toString();
            int classroomLimit = Integer.parseInt(classroomsTable.getValueAt(selectedRow, 1).toString());

            // Check whether the selected row is empty or not
            if (classroomsTable.getValueAt(selectedRow, 0).toString().equals(""))
                System.out.println("You can not edit an empty row.");
            else {
                try {
                    CachedRowSet classrooms = databaseController.selectQuery(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", classroomName));
                    classrooms.next();
                    selectedClassroomId = classrooms.getInt("id");

                } catch (SQLException err) {
                    System.out.println("SQL Exception: ");
                    err.printStackTrace();
                }

                // Change name and capacity to match the ones of the selected row
                classNameTextField.setText(classroomName);
                classCapacitySpinner.setValue(classroomLimit);

                // Change add button text to save
                addButton.setText("Save");
                cancelButton.setEnabled(true);

                // Disable UI components and clear table selection until the save button is pressed
                classroomsTable.setEnabled(false);
                editButton.setEnabled(false);
                removeButton.setEnabled(false);
                classroomsTable.getSelectionModel().clearSelection();
            }
        });

        removeButton.addActionListener(action -> {
            // TODO: (Prionysis) Remove selected row from database
            // Disable UI components and clear table selection
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            classroomsTable.getSelectionModel().clearSelection();
        });

        // Listen for changes in the class name text
        classNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                action();
            }

            public void removeUpdate(DocumentEvent e) {
                action();
            }

            public void insertUpdate(DocumentEvent e) {
                action();
            }

            public void action() {
                // Enable or disable add button based on class name text
                addButton.setEnabled(!classNameTextField.getText().equals(""));
                cancelButton.setEnabled(!classNameTextField.getText().equals(""));
            }
        });

        classroomsTable.getSelectionModel().addListSelectionListener(selection -> {
            if (classroomsTable.getSelectedRow() != -1
                    && !classroomsTable.getValueAt(classroomsTable.getSelectedRow(), 0).toString().equals("")
                    && !classroomsTable.getValueAt(classroomsTable.getSelectedRow(), 1).toString().equals("")) {
                editButton.setEnabled(true);
                removeButton.setEnabled(true);
            } else {
                editButton.setEnabled(false);
                removeButton.setEnabled(false);
            }
        });
    }

    public void updateClassrooms() {
        IntStream.iterate(classroomsTableModel.getRowCount() - 1, i -> i > -1, i -> i - 1).forEach(i -> classroomsTableModel.removeRow(i));

        Object[] row = new Object[2];
        try {
            CachedRowSet classrooms = databaseController.selectQuery("SELECT name, \"limit\" FROM \"Classrooms\"");

            // Add rows
            while (classrooms.next()) {
                row[0] = classrooms.getString("name");
                row[1] = classrooms.getInt("limit");
                classroomsTableModel.addRow(row);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        } finally {
            // Fill missing rows to fix white space
            int rowCount = classroomsTableModel.getRowCount();

            if (rowCount < 16) IntStream.range(0, 16 - rowCount).forEach(i -> {
                row[0] = "";
                row[1] = "";
                classroomsTableModel.addRow(row);
            });
            classroomsTable.setModel(classroomsTableModel);
        }
    }

    /**
     * Revert UI components to initial state
     */
    private void revertUIComponents() {
        classNameTextField.setText("");
        classCapacitySpinner.setValue(1);
        addButton.setText("Add");
        classroomsTable.setEnabled(true);
        cancelButton.setEnabled(false);
        selectedClassroomId = -1;
    }

    private void createUIComponents() {
        // Add columns
        String[] classroomsTableColumns = {"Name", "Capacity"};
        classroomsTableModel = new DefaultTableModel(classroomsTableColumns, 0);
        classroomsTable = new JTable(classroomsTableModel);
        // Stop users from interacting with the table
        classroomsTable.getTableHeader().setReorderingAllowed(false);
        updateClassrooms();
    }
}
