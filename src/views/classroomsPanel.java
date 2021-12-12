package views;

import controllers.databaseController;
import models.Database;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class classroomsPanel extends JFrame {
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

    public classroomsPanel() {
        add(classroomsPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        classroomsTable.setDefaultEditor(Object.class, null);

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
            // TODO: (Prionysis) Edit listener to update selected row changes to the database and update classroom table
            String classroomName = classNameTextField.getText();
            int classroomLimit = (int) classCapacitySpinner.getValue();

            // Check if the text field is blank to avoid unnecessary sql errors
            if (!classroomName.equals("")) {
                try {
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
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            } else System.out.println("You can not insert a blank name");

            // Revert UI components to initial state
            classNameTextField.setText("");
            classCapacitySpinner.setValue(1);
            addButton.setText("Add");
            classroomsTable.setEnabled(true);
            cancelButton.setEnabled(false);
        });

        cancelButton.addActionListener(action -> {
            // Revert UI components to initial state
            classNameTextField.setText("");
            classCapacitySpinner.setValue(1);
            addButton.setText("Add");
            classroomsTable.setEnabled(true);
            cancelButton.setEnabled(false);
        });

        editButton.addActionListener(action -> {
            // Get selected row's class name and capacity
            classNameTextField.setText(String.valueOf(classroomsTable.getValueAt(classroomsTable.getSelectedRow(), 0)));
            classCapacitySpinner.setValue(Integer.parseInt(String.valueOf(classroomsTable.getValueAt(classroomsTable.getSelectedRow(), 1))));
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
            if (classroomsTable.getSelectedRow() != -1) {
                editButton.setEnabled(true);
                removeButton.setEnabled(true);
            }
        });
    }

    private void createUIComponents() {
        // TODO: (Prionysis) Update table values from database
        // Add columns
        String[] classroomsTableColumns = {"Name", "Capacity"};
        DefaultTableModel classroomsTableModel = new DefaultTableModel(classroomsTableColumns, 0);
        classroomsTable = new JTable(classroomsTableModel);
        // Stop users from interacting with the table
        classroomsTable.getTableHeader().setReorderingAllowed(false);

        Object[] row = new Object[2];
        row[0] = "A1";
        row[1] = 10;

        classroomsTableModel.addRow(row);

        classroomsTable.setModel(classroomsTableModel);
    }
}
