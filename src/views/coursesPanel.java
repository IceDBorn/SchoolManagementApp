package views;

import com.github.lgooddatepicker.components.TimePicker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Arrays;
import java.util.Objects;

public class coursesPanel extends JFrame {
    private JPanel classroomsPanel;
    private JButton addButton;
    private JButton removeButton;
    private JComboBox<String> lessonsComboBox;
    private JComboBox<String> teachersComboBox;
    private JComboBox<String> dayComboBox;
    private JComboBox<String> classroomComboBox;
    private TimePicker timePickerStart;
    private TimePicker timePickerEnd;
    private JTable scheduleTable;
    private JScrollPane scheduleScrollPane;

    private final String[] scheduleTableColumns = {"Lesson", "Teacher", "Day", "Time"};
    private DefaultTableModel scheduleTableModel;

    private static int userId;

    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String dbUser = "postgres";
    private static final String dbPass = "kekw123";

    private Connection dbConnection;
    private Statement dbStatement;
    private PreparedStatement dbPreparedStatement;
    private ResultSet dbResult;

    public coursesPanel(int userId) {
        this.userId = userId;

        scheduleScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Fill the all available combo boxes
        try {
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);

            // Select all lesson names and display them in coursesComboBox
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery("SELECT name FROM \"Lessons\"");
            while (dbResult.next())
                lessonsComboBox.addItem(dbResult.getString(1));

            dbStatement.close();

            // Select all teacher names and display them in teachersComboBox
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery("SELECT \"Users\".name FROM \"Users\" INNER JOIN \"Teachers\" ON \"Users\".id = \"Teachers\".id");
            while (dbResult.next())
                teachersComboBox.addItem(dbResult.getString(1));

            dbStatement.close();

            // Select all classroom names and display them in classroomComboBox
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery("SELECT name FROM \"Classrooms\"");
            while (dbResult.next())
                classroomComboBox.addItem(dbResult.getString(1));

            dbStatement.close();
            dbConnection.close();
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }

        add(classroomsPanel);
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setValue(1);
        model.setMinimum(1);
        model.setMaximum(99);

        for (String day : Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"))
            dayComboBox.addItem(day);

        addButton.addActionListener(action -> {
            if (timePickerStart.getText().equals("") || timePickerEnd.getText().equals(""))
                System.out.println("You can not have a blank start and/or end date.");
            else {
                try {
                    dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);

                    // Get the lessonId using the selected lesson from the panel
                    String lessonName = Objects.requireNonNull(lessonsComboBox.getSelectedItem()).toString();
                    dbStatement = dbConnection.createStatement();
                    dbResult = dbStatement.executeQuery(String.format("SELECT id FROM \"Lessons\" WHERE name = '%s'", lessonName));
                    dbResult.next();
                    int lessonId = dbResult.getInt(1);
                    dbStatement.close();

                    // Get the teacherId using the selected teacher from the panel
                    String teacherName = Objects.requireNonNull(teachersComboBox.getSelectedItem()).toString();
                    dbStatement = dbConnection.createStatement();
                    dbResult = dbStatement.executeQuery(String.format("SELECT id FROM \"Users\" WHERE name = '%s'", teacherName));
                    dbResult.next();
                    int teacherId = dbResult.getInt(1);
                    dbStatement.close();

                    // Get the classroomId using the selected classroom from the panel
                    String classroomName = Objects.requireNonNull(classroomComboBox.getSelectedItem()).toString();
                    dbStatement = dbConnection.createStatement();
                    dbResult = dbStatement.executeQuery(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", classroomName));
                    dbResult.next();
                    int classroomId = dbResult.getInt(1);
                    dbStatement.close();

                    String courseDay = Objects.requireNonNull(dayComboBox.getSelectedItem()).toString();
                    String courseTime = timePickerStart.getText() + "-" + timePickerEnd.getText();

                    dbPreparedStatement = dbConnection.prepareStatement("INSERT INTO \"Courses\"(\"lessonId\", \"teacherId\", \"classroomId\", day, time) VALUES (?, ?, ?, ?, ?)",
                            PreparedStatement.RETURN_GENERATED_KEYS);
                    dbPreparedStatement.setInt(1, lessonId);
                    dbPreparedStatement.setInt(2, teacherId);
                    dbPreparedStatement.setInt(3, classroomId);
                    dbPreparedStatement.setString(4, courseDay);
                    dbPreparedStatement.setString(5, courseTime);
                    dbPreparedStatement.executeUpdate();

                    dbResult = dbPreparedStatement.getGeneratedKeys();
                    dbResult.next();
                    int courseId = dbResult.getInt(1);
                    dbPreparedStatement.close();

                    System.out.printf("userId %d created course: %d (lessonId: %d, teacherId %d, classroomId: %d, day %s, time: %s%n",
                            userId, courseId, lessonId, teacherId, classroomId, courseDay, courseTime);

                    dbConnection.close();
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            }
            updateTableRows();
        });

        // Removes the selected course from the database
        removeButton.addActionListener(action -> {
            int selectedRow = scheduleTable.getSelectedRow();
            if (!scheduleTable.isRowSelected(selectedRow))
                System.out.println("You haven't selected any rows.");
            else {
                String lessonName = String.valueOf(scheduleTable.getValueAt(selectedRow, 0));
                String teacherName = String.valueOf(scheduleTable.getValueAt(selectedRow, 1));
                String classroomName = Objects.requireNonNull(classroomComboBox.getSelectedItem()).toString();
                String courseDay = String.valueOf(scheduleTable.getValueAt(selectedRow, 2));
                String courseTime = String.valueOf(scheduleTable.getValueAt(selectedRow, 3));

                int courseId;
                int lessonId;
                int teacherId;
                int classroomId;

                try {
                    dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);

                    // Get the lessonId using the name of the lesson from the selected row
                    dbStatement = dbConnection.createStatement();
                    dbResult = dbStatement.executeQuery(String.format("SELECT id FROM \"Lessons\" WHERE name = '%s'", lessonName));
                    dbResult.next();
                    lessonId = dbResult.getInt(1);
                    dbStatement.close();

                    // Get the teacherId using the name of the lesson from the selected row
                    dbStatement = dbConnection.createStatement();
                    dbResult = dbStatement.executeQuery(String.format("SELECT id FROM \"Users\" WHERE name = '%s'", teacherName));
                    dbResult.next();
                    teacherId = dbResult.getInt(1);
                    dbStatement.close();

                    // Get the classroomId using the name of the selected classroom
                    dbStatement = dbConnection.createStatement();
                    dbResult = dbStatement.executeQuery(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", classroomName));
                    dbResult.next();
                    classroomId = dbResult.getInt(1);
                    dbStatement.close();

                    // Get the courseId using the data from the selected row
                    dbStatement = dbConnection.createStatement();
                    dbResult = dbStatement.executeQuery(String.format("""
                            SELECT id FROM "Courses"
                            WHERE "lessonId" = '%d' AND "teacherId" = '%d' AND "classroomId" = '%d' AND day = '%s' AND time = '%s'""", lessonId, teacherId, classroomId, courseDay, courseTime
                    ));
                    dbResult.next();
                    courseId = dbResult.getInt(1);
                    dbStatement.close();

                    dbPreparedStatement = dbConnection.prepareStatement("DELETE FROM \"Courses\" WHERE id = ?");
                    dbPreparedStatement.setInt(1, courseId);
                    dbPreparedStatement.executeUpdate();
                    dbPreparedStatement.close();

                    dbConnection.close();

                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
                scheduleTableModel.removeRow(selectedRow);
            }
        });

        lessonsComboBox.addActionListener(action -> updateTableRows());
        teachersComboBox.addActionListener(action -> updateTableRows());
        classroomComboBox.addActionListener(action -> updateTableRows());
    }

    private void createUIComponents() {
        scheduleTableModel = new DefaultTableModel(scheduleTableColumns, 0);
        scheduleTable = new JTable(scheduleTableModel);

        try {
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery("""
                    SELECT DISTINCT("Courses".id), "Lessons".name, "Users".name, "Courses".day, "Courses".time
                    FROM "Courses"
                    INNER JOIN "Classrooms" ON "Courses"."classroomId" = "Classrooms".id
                    INNER JOIN "Users" ON "Courses"."teacherId" = "Users".id
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id""");

            // Add rows
            Object[] scheduleRows = new Object[4];
            while (dbResult.next()) {
                scheduleRows[0] = dbResult.getString(2);
                scheduleRows[1] = dbResult.getString(3);
                scheduleRows[2] = dbResult.getString(4);
                scheduleRows[3] = dbResult.getString(5);
                scheduleTableModel.addRow(scheduleRows);
            }

            dbStatement.close();
            dbConnection.close();
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }
    }

    private void updateTableRows() {
        // Remove all rows
        for (int i = scheduleTableModel.getRowCount() - 1; i > -1; i--)
            scheduleTableModel.removeRow(i);

        String teacherName = Objects.requireNonNull(teachersComboBox.getSelectedItem()).toString();
        String lessonName = Objects.requireNonNull(lessonsComboBox.getSelectedItem()).toString();
        String classroomName = Objects.requireNonNull(classroomComboBox.getSelectedItem()).toString();

        try {
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPass);
            dbStatement = dbConnection.createStatement();
            dbResult = dbStatement.executeQuery(String.format("""
                    SELECT DISTINCT("Courses".id), "Lessons".name, "Users".name, "Courses".day, "Courses".time
                    FROM "Courses"
                    INNER JOIN "Classrooms" ON "Courses"."classroomId" = "Classrooms".id
                    INNER JOIN "Users" ON "Courses"."teacherId" = "Users".id
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id
                    WHERE "Users".name = '%s' AND "Lessons"."name" = '%s' AND "Classrooms".name = '%s'""", teacherName, lessonName, classroomName));

            // Add rows
            Object[] scheduleRows = new Object[4];
            while (dbResult.next()) {
                scheduleRows[0] = dbResult.getString(2);
                scheduleRows[1] = dbResult.getString(3);
                scheduleRows[2] = dbResult.getString(4);
                scheduleRows[3] = dbResult.getString(5);
                scheduleTableModel.addRow(scheduleRows);
            }

            dbStatement.close();
            dbConnection.close();
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }
        this.repaint();
    }
}