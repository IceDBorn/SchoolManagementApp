package views;

import models.Database;
import models.User;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import java.sql.*;
import java.util.Arrays;

public class lessonsPanel extends JFrame {
    private JPanel lessonsPanel;
    private JTextField classNameTextField;
    private JButton addButton;
    private JComboBox<String> professionComboBox;
    private JComboBox<String> schoolYearComboBox;

    public lessonsPanel() {

        add(lessonsPanel);
        setSize(400, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Get all distinct subjects from teachers
        try {
            CachedRowSet subjects = Database.selectQuery("SELECT DISTINCT(subject) FROM \"Teachers\"");
            while (subjects.next())
                professionComboBox.addItem(subjects.getString(1));
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }

        for (String schoolYear : Arrays.asList("1η Γυμνασίου", "2α Γυμνασίου", "3η Γυμνασίου", "1η Λυκείου", "2α Λυκείου", "3η Λυκείου")) {
            schoolYearComboBox.addItem(schoolYear);
        }

        addButton.addActionListener(action -> {
            String lesson = classNameTextField.getText();

            // Check if the text field is blank to avoid unnecessary sql errors
            if (lesson.equals("")) {
                System.out.println("You can not insert a blank name");
            } else {
                String subject = professionComboBox.getItemAt(professionComboBox.getSelectedIndex());
                int year = schoolYearComboBox.getSelectedIndex() + 1;

                try {
                    Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO \"Lessons\"(name, subject, year) VALUES (?, ?, ?)");

                    preparedStatement.setString(1, lesson);
                    preparedStatement.setString(2, subject);
                    preparedStatement.setInt(3, year);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    connection.close();

                    System.out.printf("userId %d created lesson: %s (subject: %s, year: %d)%n", User.getId(), lesson, subject, year);
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            }
        });
    }
}
