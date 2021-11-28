package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class gradesPanel extends JFrame {
    private JPanel gradesPanel;
    private JLabel usernameLabel;
    private JScrollPane infoScrollPane;
    private JScrollPane gradeScrollPane;
    private JTable infoTable;
    private JTable gradeTable;
    private JButton homeButton;
    private JButton saveButton;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";

    private static int teacherId;

    private Connection dbConnection;
    private Statement dbStatement;
    private PreparedStatement dbPreparedStatement;
    private ResultSet dbResult;
    private String dbQuery;

    public gradesPanel(int teacherId, String teacherName) {
        this.teacherId = teacherId;

        add(gradesPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        usernameLabel.setText(teacherName);

        infoScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        gradeScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        saveButton.addActionListener(action -> {
            try {
                dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);

                for (int i = 0; i < infoTable.getRowCount(); i++) {
                    int studentId = Integer.parseInt(infoTable.getValueAt(i, 0).toString());
                    int studentGrade = Integer.parseInt(gradeTable.getValueAt(i, 0).toString());

                    dbPreparedStatement = dbConnection.prepareStatement("UPDATE \"StudentLessons\" SET grade = ? WHERE id = ?");
                    dbPreparedStatement.setInt(1, studentGrade);
                    dbPreparedStatement.setInt(2, studentId);
                    dbPreparedStatement.executeUpdate();

                    // Checks if the next row has a null id to end the loop
                    if (infoTable.getValueAt(i + 1, 0) == "") break;
                }
                dbConnection.close();
            } catch (SQLException err) {
                System.out.println("SQL Exception:");
                err.printStackTrace();
            }
        });
    }

    private void createUIComponents() {
        // Add columns
        String[] infoTableColumns = {"ID", "Student", "Subject"};
        String[] gradeTableColumns = {"Grade"};
        DefaultTableModel infoTableModel = new DefaultTableModel(infoTableColumns, 0);
        DefaultTableModel gradeTableModel = new DefaultTableModel(gradeTableColumns, 0);
        infoTable = new JTable(infoTableModel);
        gradeTable = new JTable(gradeTableModel);
        // Stop users from interacting with the table
        infoTable.getTableHeader().setReorderingAllowed(false);
        gradeTable.getTableHeader().setReorderingAllowed(false);
        infoTable.setEnabled(false);

        try {
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery(String.format("""
                    SELECT DISTINCT("StudentLessons".id), "Users".name, "Lessons".name, "StudentLessons".grade
                    FROM "StudentLessons"
                    INNER JOIN "Courses" ON "StudentLessons"."lessonId" = "Courses"."lessonId"
                    INNER JOIN "Lessons" ON "StudentLessons"."lessonId" = "Lessons".id
                    INNER JOIN "Users" ON "StudentLessons"."studentId" = "Users".id
                    WHERE "Courses"."teacherId" = %d""", teacherId));

            // Add rows
            Object[] infoRows = new Object[3];
            Object[] gradeRow = new Object[1];

            while (dbResult.next()) {
                infoRows[0] = dbResult.getString(1);
                infoRows[1] = dbResult.getString(2);
                infoRows[2] = dbResult.getString(3);
                gradeRow[0] = dbResult.getInt(4);

                infoTableModel.addRow(infoRows);
                gradeTableModel.addRow(gradeRow);
            }

            // Fill rows missing fixing white space
            int rowCount = infoTableModel.getRowCount();

            if (rowCount < 16) {
                for (int i = 0; i < 16 - rowCount; i++) {
                    infoRows[0] = "";
                    infoRows[1] = "";
                    infoRows[2] = "";
                    gradeRow[0] = "";

                    infoTableModel.addRow(infoRows);
                    gradeTableModel.addRow(gradeRow);
                }
            }

            dbStatement.close();
            dbConnection.close();

        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();

            Object[] infoRows = new Object[3];
            Object[] gradeRow = new Object[1];
            for (int i = 0; i < 16; i++) {
                infoRows[0] = "";
                infoRows[1] = "";
                infoRows[2] = "";
                gradeRow[0] = "";

                infoTableModel.addRow(infoRows);
                gradeTableModel.addRow(gradeRow);
            }
        }
    }
}