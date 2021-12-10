package views;

import javax.swing.*;
import java.sql.*;
import models.*;

public class classroomsPanel extends JFrame {
    private JPanel classroomsPanel;
    private JTextField classNameTextField;
    private JSpinner classCapacitySpinner;
    private JButton addButton;

    private Connection dbConnection;
    private PreparedStatement dbPreparedStatement;
    private ResultSet dbResult;

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
                    dbConnection = DriverManager.getConnection(Database.getDbURL(), Database.getDbUser(), Database.getDbPass());
                    dbPreparedStatement = dbConnection.prepareStatement("INSERT INTO \"Classrooms\"(name, \"limit\") VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    dbPreparedStatement.setString(1, classroomName);
                    dbPreparedStatement.setInt(2, classroomLimit);
                    dbPreparedStatement.executeUpdate();

                    // Get the classroomId of the newly inserted classroom
                    dbResult = dbPreparedStatement.getGeneratedKeys();
                    dbResult.next();
                    int classroomId = dbResult.getInt(1);

                    dbPreparedStatement.close();
                    dbConnection.close();

                    System.out.printf("userId %d created classroom: %d (name: %s, limit :%d)%n",
                            User.getUserId(), classroomId, classroomName, classroomLimit);
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            } else System.out.println("You can not insert a blank name");
        });
    }
}
