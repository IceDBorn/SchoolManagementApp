package views;

import controllers.databaseController;
import models.Database;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    public classroomsPanel() {
        add(classroomsPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setValue(1);
        model.setMinimum(1);
        model.setMaximum(99);
        classCapacitySpinner.setModel(model);

        addButton.addActionListener(action -> {
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
        });
    }

    private void createUIComponents() {
        // Add columns
        String[] classroomsTableColumns = {"Name", "Capacity"};
        DefaultTableModel classroomsTableModel = new DefaultTableModel(classroomsTableColumns, 0);
        classroomsTable = new JTable(classroomsTableModel);
        // Stop users from interacting with the table
        classroomsTable.getTableHeader().setReorderingAllowed(false);
        classroomsTable.setEnabled(false);
    }
}
