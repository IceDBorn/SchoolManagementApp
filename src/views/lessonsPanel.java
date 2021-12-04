package views;

import javax.swing.*;
import java.sql.*;

public class lessonsPanel extends JFrame {
    private JPanel lessonsPanel;
    private JTextField classNameTextField;
    private JButton addButton;
    private JComboBox<String> professionComboBox;
    private JComboBox<String> schoolYearComboBox;

    private static int userId;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";

    private Connection dbConnection;
    private Statement dbStatement;
    private PreparedStatement dbPreparedStatement;
    private ResultSet dbResult;

    public lessonsPanel(int userId) {
        this.userId = userId;

        add(lessonsPanel);
        setSize(400, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Get all distinct subjects from teachers
        try {
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery("SELECT DISTINCT(subject) FROM \"Teachers\"");

            while (dbResult.next())
                professionComboBox.addItem(dbResult.getString(1));

            dbStatement.close();
            dbConnection.close();
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }

        schoolYearComboBox.addItem("1η Γυμνασίου");
        schoolYearComboBox.addItem("2α Γυμνασίου");
        schoolYearComboBox.addItem("3η Γυμνασίου");
        schoolYearComboBox.addItem("1η Λυκείου");
        schoolYearComboBox.addItem("2α Λυκείου");
        schoolYearComboBox.addItem("3η Λυκείου");

        addButton.addActionListener(action -> {
            String lessonName = classNameTextField.getText();

            // Check if the text field is blank to avoid unnecessary sql errors
            if (!lessonName.equals("")) {
                String lessonSubject = professionComboBox.getItemAt(professionComboBox.getSelectedIndex());
                int lessonYear = schoolYearComboBox.getSelectedIndex() + 1;

                try {
                    dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
                    dbPreparedStatement = dbConnection.prepareStatement("INSERT INTO \"Lessons\"(name, subject, year) VALUES (?, ?, ?)");

                    dbPreparedStatement.setString(1, lessonName);
                    dbPreparedStatement.setString(2, lessonSubject);
                    dbPreparedStatement.setInt(3, lessonYear);
                    dbPreparedStatement.executeUpdate();
                    dbPreparedStatement.close();
                    dbConnection.close();

                    System.out.printf("userId %d created lesson: %s with subject: %s%n", userId, lessonName, lessonSubject);
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            } else System.out.println("You can not insert a blank name");
        });
    }
}
