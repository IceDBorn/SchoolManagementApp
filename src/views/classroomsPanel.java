package views;

import javax.swing.*;
import java.sql.*;
import models.*;

public class classroomsPanel extends JFrame {
    private JPanel classroomsPanel;
    private JTextField classNameTextField;
    private JSpinner classCapacitySpinner;
    private JButton addButton;

    public classroomsPanel() {
        add(classroomsPanel);
        setSize(400, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

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
                    ResultSet resultSet = preparedStatement.getGeneratedKeys();
                    resultSet.next();
                    int classroomId = resultSet.getInt(1);

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
}
