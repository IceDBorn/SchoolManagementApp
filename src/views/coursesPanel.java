package views;

import com.github.lgooddatepicker.components.TimePicker;
import controllers.panelController;
import models.*;
import controllers.databaseController;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public class coursesPanel extends JFrame {
    private final String[] scheduleTableColumns = {"Lesson", "Teacher", "Day", "Time"};
    private JPanel coursesPanel;
    private JButton addButton;
    private JButton removeButton;
    private JComboBox<String> lessonsComboBox;
    private JComboBox<String> teachersComboBox;
    private JComboBox<String> classroomComboBox;
    private JComboBox<String> dayComboBox;
    private TimePicker timePickerStart;
    private TimePicker timePickerEnd;
    private JTable scheduleTable;
    private JScrollPane scheduleScrollPane;
    private DefaultTableModel scheduleTableModel;

    public coursesPanel() {
        scheduleScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Fill the all available combo boxes
        try {
            // Select all lesson names and display them in coursesComboBox
            CachedRowSet lessons = databaseController.selectQuery("SELECT name FROM \"Lessons\"");
            while (lessons.next())
                lessonsComboBox.addItem(lessons.getString("name"));

            // Select all teacher names and display them in teachersComboBox
            CachedRowSet teachers = databaseController.selectQuery("SELECT name FROM \"Users\" INNER JOIN \"Teachers\" ON \"Users\".id = \"Teachers\".id");
            while (teachers.next())
                teachersComboBox.addItem(teachers.getString("name"));

            // Select all classroom names and display them in classroomComboBox
            CachedRowSet classrooms = databaseController.selectQuery("SELECT name FROM \"Classrooms\"");
            while (classrooms.next())
                classroomComboBox.addItem(classrooms.getString("name"));
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }

        add(coursesPanel);
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

        // Add course based on the selected data
        addButton.addActionListener(action -> {
            if (timePickerStart.getText().equals("") || timePickerEnd.getText().equals(""))
                System.out.println("You can not have a blank start or end time.");
            else if (timePickerStart.getText().equals(timePickerEnd.getText()))
                System.out.println("You can not have the same start and end time.");
            else {
                try {
                    String lessonName = Objects.requireNonNull(lessonsComboBox.getSelectedItem()).toString();
                    String teacherName = Objects.requireNonNull(teachersComboBox.getSelectedItem()).toString();
                    String classroomName = Objects.requireNonNull(classroomComboBox.getSelectedItem()).toString();
                    String courseDay = Objects.requireNonNull(dayComboBox.getSelectedItem()).toString();
                    String courseTime = timePickerStart.getText() + "-" + timePickerEnd.getText();

                    // Get the lessonId using the selected lesson from the panel
                    CachedRowSet lessons = databaseController.selectQuery(String.format("SELECT id FROM \"Lessons\" WHERE name = '%s'", lessonName));
                    lessons.next();
                    int lessonId = lessons.getInt("id");

                    // Get the teacherId using the selected teacher from the panel
                    CachedRowSet teachers = databaseController.selectQuery(String.format("SELECT id FROM \"Users\" WHERE name = '%s'", teacherName));
                    teachers.next();
                    int teacherId = teachers.getInt("id");

                    // Get the classroomId using the selected classroom from the panel
                    CachedRowSet classrooms = databaseController.selectQuery(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", classroomName));
                    classrooms.next();
                    int classroomId = classrooms.getInt("id");

                    // Check if a course exists with the same classroom, day and time
                    CachedRowSet courses = databaseController.selectQuery(String.format("SELECT id FROM \"Courses\" WHERE \"classroomId\" = '%d' AND day = '%s' AND time = '%s'", classroomId, courseDay, courseTime));
                    courses.next();
                    boolean courseExists = courses.isBeforeFirst();

                    if (courseExists)
                        System.out.println("A course already exists with the same classroom, day and time");
                    else {
                        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO \"Courses\"(\"lessonId\", \"teacherId\", \"classroomId\", day, time) VALUES (?, ?, ?, ?, ?)",
                                PreparedStatement.RETURN_GENERATED_KEYS);
                        preparedStatement.setInt(1, lessonId);
                        preparedStatement.setInt(2, teacherId);
                        preparedStatement.setInt(3, classroomId);
                        preparedStatement.setString(4, courseDay);
                        preparedStatement.setString(5, courseTime);
                        preparedStatement.executeUpdate();

                        // Get the courseId of the newly inserted course
                        int courseId = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());

                        preparedStatement.close();
                        connection.close();

                        System.out.printf("userId %d created course: %d (lessonId: %d, teacherId %d, classroomId: %d, day %s, time: %s%n",
                                User.getId(), courseId, lessonId, teacherId, classroomId, courseDay, courseTime);
                    }
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                } finally {
                    updateCourses();
                }
            }
        });

        // TODO: When deleting the bottom row, the above row has a white column
        // Remove the selected course from the database
        removeButton.addActionListener(action -> {
            // Get the selected row index
            int selectedRow = scheduleTable.getSelectedRow();

            // Check if a row is selected
            if (!scheduleTable.isRowSelected(selectedRow))
                System.out.println("You don't have a selected row.");
            else {
                // Get the selected row's data
                String lessonName = String.valueOf(scheduleTable.getValueAt(selectedRow, 0));
                String teacherName = String.valueOf(scheduleTable.getValueAt(selectedRow, 1));
                String classroomName = Objects.requireNonNull(classroomComboBox.getSelectedItem()).toString();
                String courseDay = String.valueOf(scheduleTable.getValueAt(selectedRow, 2));
                String courseTime = String.valueOf(scheduleTable.getValueAt(selectedRow, 3));

                try {
                    // Get the lessonId using the name of the lesson from the selected row
                    CachedRowSet lessons = databaseController.selectQuery(String.format("SELECT id FROM \"Lessons\" WHERE name = '%s'", lessonName));
                    lessons.next();
                    int lessonId = lessons.getInt("id");

                    // Get the teacherId using the name of the lesson from the selected row
                    CachedRowSet teachers = databaseController.selectQuery(String.format("SELECT id FROM \"Users\" WHERE name = '%s'", teacherName));
                    teachers.next();
                    int teacherId = teachers.getInt("id");

                    // Get the classroomId using the name of the selected classroom
                    CachedRowSet classrooms = databaseController.selectQuery(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", classroomName));
                    classrooms.next();
                    int classroomId = classrooms.getInt("id");

                    // Get the courseId using the data from the selected row
                    CachedRowSet courses = databaseController.selectQuery(String.format("""
                            SELECT id FROM "Courses"
                            WHERE "lessonId" = '%d' AND "teacherId" = '%d' AND "classroomId" = '%d' AND day = '%s' AND time = '%s'""", lessonId, teacherId, classroomId, courseDay, courseTime
                    ));
                    courses.next();
                    int courseId = courses.getInt("id");

                    // Delete the selected course from the database
                    Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                    PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM \"Courses\" WHERE id = ?");
                    preparedStatement.setInt(1, courseId);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    connection.close();

                    System.out.printf("userId %d deleted course: %d (lessonId: %d, teacherId %d, classroomId: %d, day %s, time: %s%n",
                            User.getId(), courseId, lessonId, teacherId, classroomId, courseDay, courseTime);
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                } finally {
                    updateCourses();
                }
            }
        });

        lessonsComboBox.addActionListener(action -> updateCourses());
        teachersComboBox.addActionListener(action -> updateCourses());
        classroomComboBox.addActionListener(action -> updateCourses());
    }

    private void createUIComponents() {
        scheduleTableModel = new DefaultTableModel(scheduleTableColumns, 0);
        scheduleTable = new JTable(scheduleTableModel);

        // Fill the schedule table with all courses from the database
        try {
            CachedRowSet courses = databaseController.selectQuery("""
                    SELECT DISTINCT("Courses".id), "Lessons".name, "Users".name, "Courses".day, "Courses".time
                    FROM "Courses"
                    INNER JOIN "Classrooms" ON "Courses"."classroomId" = "Classrooms".id
                    INNER JOIN "Users" ON "Courses"."teacherId" = "Users".id
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id""");

            // Add rows
            Object[] row = new Object[4];

            while (courses.next()) {
                row[0] = courses.getString(2);
                row[1] = courses.getString(3);
                row[2] = courses.getString(4);
                row[3] = courses.getString(5);
                scheduleTableModel.addRow(row);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }
    }

    private void updateCourses() {
        // Remove all rows
        IntStream.iterate(scheduleTableModel.getRowCount() - 1, i -> i > -1, i -> i - 1).forEach(i -> scheduleTableModel.removeRow(i));

        String teacherName = Objects.requireNonNull(teachersComboBox.getSelectedItem()).toString();
        String lessonName = Objects.requireNonNull(lessonsComboBox.getSelectedItem()).toString();
        String classroomName = Objects.requireNonNull(classroomComboBox.getSelectedItem()).toString();

        try {
            CachedRowSet courses = databaseController.selectQuery(String.format("""
                    SELECT DISTINCT("Courses".id), "Lessons".name, "Users".name, "Courses".day, "Courses".time
                    FROM "Courses"
                    INNER JOIN "Classrooms" ON "Courses"."classroomId" = "Classrooms".id
                    INNER JOIN "Users" ON "Courses"."teacherId" = "Users".id
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id
                    WHERE "Users".name = '%s' AND "Lessons"."name" = '%s' AND "Classrooms".name = '%s'""", teacherName, lessonName, classroomName));

            // Add rows
            Object[] row = new Object[4];

            while (courses.next()) {
                row[0] = courses.getString("\"Lessons\".name");
                row[1] = courses.getString("\"Users\".name");
                row[2] = courses.getString("\"Courses\".day");
                row[3] = courses.getString("\"Courses\".time");
                scheduleTableModel.addRow(row);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        } finally {
            panelController.fillEmptyRows(scheduleTableModel);
            scheduleTable.setModel(scheduleTableModel);
        }
    }
}