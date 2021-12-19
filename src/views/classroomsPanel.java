package views;

import controllers.databaseController;
import controllers.panelController;
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
    private String selectedClassroomName;

    public classroomsPanel() {
        add(classroomsPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // Customize table to have no border, disable cell editing and switch single row selection
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
            panelController.createMainPanel();
            this.setVisible(false);
        });

        addButton.addActionListener(action -> {
            try {
                String name = classNameTextField.getText();
                int limit = (int) classCapacitySpinner.getValue();
                boolean classroomExists = databaseController.selectQuery(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", name)).isBeforeFirst();

                if (classroomExists && !selectedClassroomName.equals(name))
                    System.out.println("A classroom already exists with that name.");
                else {
                    boolean isAddButton = addButton.getText().equals("Add");

                    Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                    PreparedStatement preparedStatement = connection.prepareStatement(
                            isAddButton ? "INSERT INTO \"Classrooms\"(name, \"limit\") VALUES (?, ?)" : "UPDATE \"Classrooms\" SET name = ?, \"limit\" = ? WHERE id = ?",
                            PreparedStatement.RETURN_GENERATED_KEYS);

                    preparedStatement.setString(1, name);
                    preparedStatement.setInt(2, limit);

                    if (!isAddButton)
                        preparedStatement.setInt(3, selectedClassroomId);

                    preparedStatement.executeUpdate();

                    // Get the userId of the inserted or updated classroom
                    int classroomId = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());

                    preparedStatement.close();
                    connection.close();

                    System.out.printf("userId %d %s classroom: %d (name: %s, limit :%d)%n",
                            User.getId(), isAddButton ? "created" : "updated", classroomId, name, limit);
                }
            } catch (SQLException err) {
                System.out.println("SQL Exception:");
                err.printStackTrace();
            } finally {
                updateClassrooms();
                revertUIComponents();
            }
        });

        cancelButton.addActionListener(action -> revertUIComponents());

        editButton.addActionListener(action -> {
            // Get the selected row's data
            int selectedRow = classroomsTable.getSelectedRow();
            String name = classroomsTable.getValueAt(selectedRow, 0).toString();
            int limit = Integer.parseInt(classroomsTable.getValueAt(selectedRow, 1).toString());

            // Get the selected classroomId and store it to a global variable
            try {
                CachedRowSet classrooms = databaseController.selectQuery(String.format("SELECT id, name FROM \"Classrooms\" WHERE name = '%s'", name));
                classrooms.next();
                selectedClassroomId = classrooms.getInt("id");
                selectedClassroomName = classrooms.getString("name");
            } catch (SQLException err) {
                System.out.println("SQL Exception: ");
                err.printStackTrace();
            }

            // Change name and capacity to match the ones of the selected row
            classNameTextField.setText(name);
            classCapacitySpinner.setValue(limit);

            // Change add button text to save
            addButton.setText("Save");
            cancelButton.setEnabled(true);

            // Disable UI components and clear table selection until the save button is pressed
            classroomsTable.setEnabled(false);
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            classroomsTable.getSelectionModel().clearSelection();

        });

        removeButton.addActionListener(action -> {
            try {
                // Get the selected row index
                int selectedRow = classroomsTable.getSelectedRow();

                // Get the selected row's data
                String name = classroomsTable.getValueAt(selectedRow, 0).toString();
                int limit = Integer.parseInt(classroomsTable.getValueAt(selectedRow, 1).toString());

                // Get the id of the selected classroom
                int id = databaseController.selectFirstId(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", name));

                // Check how many classrooms exist using that classroomId
                int count = databaseController.selectFirstId(String.format("SELECT COUNT(id) FROM \"Courses\" WHERE \"classroomId\" = '%d'", id));

                if (count > 0) {
                    System.out.printf("You are about to delete %d course(s) that use the classroomId %d", count, id);

                    boolean delete = false;

                    // TODO: (IceDBorn) Create a confirmation panel before deleting any courses.
                    if (delete) {
                        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM \"Courses\" WHERE \"classroomId\" = ?");
                        preparedStatement.setInt(1, id);
                        preparedStatement.executeUpdate();

                        preparedStatement.close();
                        connection.close();

                        System.out.printf("userId %d deleted %d course(s) using the classroomId %d%n",
                                User.getId(), count, id);
                    }
                } else {
                    Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                    PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM \"Classrooms\" WHERE id = ?");
                    preparedStatement.setInt(1, id);
                    preparedStatement.executeUpdate();

                    preparedStatement.close();
                    connection.close();

                    System.out.printf("userId %d deleted classroom: %d (name: %s, limit: %d)%n",
                            User.getId(), id, name, limit);
                }

            } catch (SQLException err) {
                System.out.println("SQL Exception:");
                err.printStackTrace();
            } finally {
                updateClassrooms();
                revertUIComponents();

                editButton.setEnabled(false);
                removeButton.setEnabled(false);
                classroomsTable.getSelectionModel().clearSelection();
            }
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
            // Get selected row index
            int selectedRow = classroomsTable.getSelectedRow();

            if (selectedRow != -1
                && !classroomsTable.getValueAt(selectedRow, 0).toString().equals("")
                && !classroomsTable.getValueAt(selectedRow, 1).toString().equals("")) {
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

        try {
            CachedRowSet classrooms = databaseController.selectQuery("SELECT name, \"limit\" FROM \"Classrooms\" ORDER BY name");

            // Add rows
            Object[] row = new Object[2];

            while (classrooms.next()) {
                row[0] = classrooms.getString("name");
                row[1] = classrooms.getInt("limit");

                classroomsTableModel.addRow(row);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        } finally {
            panelController.fillEmptyRows(classroomsTableModel);
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
        selectedClassroomName = "";
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
