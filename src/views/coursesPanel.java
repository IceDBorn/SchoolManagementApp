package views;

import com.github.lgooddatepicker.components.TimePicker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class coursesPanel extends JFrame {
    private JPanel classroomsPanel;
    private JTextField classNameTextField;
    private JSpinner classCapacitySpinner;
    private JButton addButton;
    private JComboBox coursesComboBox;
    private JComboBox teachersComboBox;
    private JComboBox dayComboBox;
    private TimePicker timePicker;
    private JTable scheduleTable;
    private JButton removeButton;
    private JScrollPane scheduleScrollPane;

    private static int userId;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";

    private Connection dbConnection;
    private PreparedStatement dbPreparedStatement;

    public coursesPanel(int userId) {
        this.userId = userId;

        scheduleScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        coursesComboBox.addItem("Mathematics");
        coursesComboBox.addItem("Physics");
        coursesComboBox.addItem("Greek");

        teachersComboBox.addItem("Γιάννης Ευσταθίου");
        teachersComboBox.addItem("Λέανδρος Κατσιμάκης");

        dayComboBox.addItem("Δευτέρα");
        dayComboBox.addItem("Τρίτη");
        dayComboBox.addItem("Τετάρτη");
        dayComboBox.addItem("Πέμπτη");
        dayComboBox.addItem("Παρασκευή");
        dayComboBox.addItem("Σάββατο");
        dayComboBox.addItem("Κυριακή");

        add(classroomsPanel);
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setValue(1);
        model.setMinimum(1);
        model.setMaximum(99);

        addButton.addActionListener(action -> {
            String classroomName = classNameTextField.getText();
            int classroomLimit = (int) classCapacitySpinner.getValue();

            if (!classroomName.equals("")) {
                try {
                    dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);

                    dbPreparedStatement = dbConnection.prepareStatement("INSERT INTO \"Classrooms\"(name, \"limit\") VALUES (?, ?)");
                    dbPreparedStatement.setString(1, classroomName);
                    dbPreparedStatement.setInt(2, classroomLimit);
                    dbPreparedStatement.executeUpdate();

                    System.out.printf("userId %d created classroom: %s with limit: %d%n", userId, classroomName, classroomLimit);
                    dbConnection.close();
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            } else System.out.println("You can not insert a blank name");
        });
    }

    private void createUIComponents() {
        String[] scheduleTableColumns = {"Lesson", "Teacher", "Day", "Time"};
        DefaultTableModel scheduleTableModel = new DefaultTableModel(scheduleTableColumns, 0);
        scheduleTable = new JTable(scheduleTableModel);
        Object[] scheduleRows = new Object[4];
        scheduleRows[0] = "Mathematics";
        scheduleRows[1] = "Γιάννης Ευσταθίου";
        scheduleRows[2] = "Τρίτη";
        scheduleRows[3] = "14:00-16:00";
        scheduleTableModel.addRow(scheduleRows);
    }
}
