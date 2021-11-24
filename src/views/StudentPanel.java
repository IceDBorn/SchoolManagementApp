package views;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import java.sql.Connection;
import java.sql.DriverManager;

public class StudentPanel extends JFrame {

    private JTable scheduleTable;
    private JPanel studentPanel;

    private static String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static String dbUser = "postgres";
    private static String dbPass = "kekw123";
    private static Connection dbConnection;

    public StudentPanel() {
        add(studentPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void createUIComponents() {
        // Initialise & Close Connection
        try {
            Class.forName("org.postgresql.Driver");
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            dbConnection.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        // Add columns
        String[] studentTableColumns = {"Lesson", "Class", "Date"};
        DefaultTableModel studentTableModel = new DefaultTableModel(studentTableColumns, 0);
        scheduleTable = new JTable(studentTableModel);

        // Add rows
        Object[] row = new Object[3];
        row[0] = "Mathematics";
        row[1] = "G1";
        row[2] = "Monday - 13:00";

        studentTableModel.addRow(row);
    }
}
