package views;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class lessonsPanel extends JFrame {
    private JPanel lessonsPanel;
    private JTextField classNameTextField;
    private JButton addButton;
    private JComboBox professionComboBox;
    private JComboBox schoolYearComboBox;

    private static int userId;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";

    private Connection dbConnection;
    private PreparedStatement dbPreparedStatement;

    public lessonsPanel(int userId) {
        this.userId = userId;

        add(lessonsPanel);
        setSize(400, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Fill combobox
        professionComboBox.addItem("Mathematics");
        professionComboBox.addItem("Programming");

        schoolYearComboBox.addItem("1η Γυμνασίου");
        schoolYearComboBox.addItem("2α Γυμνασίου");
        schoolYearComboBox.addItem("3η Γυμνασίου");
        schoolYearComboBox.addItem("1η Λυκείου");
        schoolYearComboBox.addItem("2α Λυκείου");
        schoolYearComboBox.addItem("3η Λυκείου");

        addButton.addActionListener(action -> {
            String classroomName = classNameTextField.getText();

            if (!classroomName.equals("")) {
                try {
                    dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);

                    dbPreparedStatement = dbConnection.prepareStatement("INSERT INTO \"Classrooms\"(name, \"limit\") VALUES (?, ?)");
                    dbPreparedStatement.setString(1, classroomName);
                    dbPreparedStatement.executeUpdate();
                    dbConnection.close();
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            } else System.out.println("You can not insert a blank name");
        });
    }
}
