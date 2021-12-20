package views;

import controllers.databaseController;
import controllers.fileController;
import controllers.panelController;
import models.Database;
import models.User;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public class scheduleMakerPanel extends JFrame {
    private final String[] scheduleTableColumns = {"Lesson", "Teacher", "Day", "Time"};
    private JPanel coursesPanel;
    private JButton addButton;
    private JButton removeButton;
    private JComboBox<String> lessonsComboBox;
    private JComboBox<String> teachersComboBox;
    private JComboBox<String> classroomComboBox;
    private JComboBox<String> dayComboBox;
    private JTable scheduleTable;
    private JScrollPane scheduleScrollPane;
    private JButton backButton;
    private JButton editButton;
    private JComboBox<String> startTime;
    private JComboBox<String> endTime;
    private DefaultTableModel scheduleTableModel;

    public scheduleMakerPanel(Point location) throws IOException {

        add(coursesPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocation(location);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        scheduleScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setValue(1);
        model.setMinimum(1);
        model.setMaximum(99);

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
            StringWriter errors = new StringWriter();
            err.printStackTrace(new PrintWriter(errors));
            String message = errors.toString();
            fileController.saveFile("SQL Exception: " + message);

            panelController.createErrorPanel("Something went wrong.", this);
        }

        for (String time : Arrays.asList("8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00"))
            startTime.addItem(time);

        for (String day : Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"))
            dayComboBox.addItem(day);

        backButton.addActionListener(action -> {
            panelController.createMainPanel(this.getLocation());
            this.setVisible(false);
        });

        // Add course based on the selected data
        addButton.addActionListener(action -> {
            boolean isAddButton = addButton.getText().equals("Add");
            try {
                String lessonName = Objects.requireNonNull(lessonsComboBox.getSelectedItem()).toString();
                String teacherName = Objects.requireNonNull(teachersComboBox.getSelectedItem()).toString();
                String classroomName = Objects.requireNonNull(classroomComboBox.getSelectedItem()).toString();
                String courseDay = Objects.requireNonNull(dayComboBox.getSelectedItem()).toString();
                String courseTime = startTime.getSelectedItem() + " - " + endTime.getSelectedItem();

                // Get the lessonId using the selected lesson
                int lessonId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Lessons\" WHERE name = '%s'", lessonName));

                // Get the teacherId using the selected teacher
                int teacherId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Users\" WHERE name = '%s'", teacherName));

                // Get the classroomId using the selected classroom
                int classroomId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", classroomName));

                // Check if a course exists with the same classroom, day and time
                CachedRowSet courses = databaseController.selectQuery(String.format("SELECT id FROM \"Courses\" WHERE \"classroomId\" = '%d' AND day = '%s' AND time = '%s'", classroomId, courseDay, courseTime));
                courses.next();
                boolean courseExists = courses.isBeforeFirst();

                if (courseExists)
                    panelController.createErrorPanel("A course using this classroom at the given day and time already exists.", this);
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

                    // Get the id of the newly inserted course
                    int id = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());

                    preparedStatement.close();
                    connection.close();

                    fileController.saveFile("User (%d) %s%s schedule entry (%d).".formatted(
                            User.getId(), User.getName(), isAddButton ? " created " : " updated ", id));
                }
            } catch (SQLException | IOException err) {
                StringWriter errors = new StringWriter();
                err.printStackTrace(new PrintWriter(errors));
                String message = errors.toString();
                try {
                    fileController.saveFile("SQL Exception: " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                panelController.createErrorPanel("Something went wrong.", this);
            } finally {
                try {
                    updateCourses();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // TODO: When deleting the bottom row, the above row has a white column
        // Remove the selected course from the database
        removeButton.addActionListener(action -> {
            // Get the selected row index
            int selectedRow = scheduleTable.getSelectedRow();

            // Get the selected course data
            String lessonName = String.valueOf(scheduleTable.getValueAt(selectedRow, 0));
            String teacherName = String.valueOf(scheduleTable.getValueAt(selectedRow, 1));
            String classroomName = Objects.requireNonNull(classroomComboBox.getSelectedItem()).toString();
            String courseDay = String.valueOf(scheduleTable.getValueAt(selectedRow, 2));
            String courseTime = String.valueOf(scheduleTable.getValueAt(selectedRow, 3));

            try {
                // Get the lesson id using the name of the lesson from the selected row
                int lessonId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Lessons\" WHERE name = '%s'", lessonName));

                // Get the teacher id using the name of the lesson from the selected row
                int teacherId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Users\" WHERE name = '%s'", teacherName));

                // Get the classroom id using the name of the selected classroom
                int classroomId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", classroomName));

                // Get the courseId using the data from the selected row
                int courseId = databaseController.selectFirstIntColumn(String.format(
                        "SELECT id FROM \"Courses\" WHERE \"lessonId\" = '%d' AND \"teacherId\" = '%d' AND \"classroomId\" = '%d' AND day = '%s' AND time = '%s'",
                        lessonId, teacherId, classroomId, courseDay, courseTime));

                // Delete the selected course from the database
                Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM \"Courses\" WHERE id = ?");
                preparedStatement.setInt(1, courseId);
                preparedStatement.executeUpdate();
                preparedStatement.close();
                connection.close();

                fileController.saveFile("User (%d) %s deleted schedule entry (%d)".formatted(
                        User.getId(), User.getName(), courseId));
            } catch (SQLException | IOException err) {
                StringWriter errors = new StringWriter();
                err.printStackTrace(new PrintWriter(errors));
                String message = errors.toString();
                try {
                    fileController.saveFile("SQL Exception: " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                panelController.createErrorPanel("Something went wrong.", this);
            } finally {
                try {
                    updateCourses();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            scheduleTable.clearSelection();
        });

        lessonsComboBox.addActionListener(action -> {
            try {
                updateCourses();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        teachersComboBox.addActionListener(action -> {
            try {
                updateCourses();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        classroomComboBox.addActionListener(action -> {
            try {
                updateCourses();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        startTime.addActionListener(action -> enableButtons());

        startTime.addActionListener(action -> setEndTime());

        scheduleTable.getSelectionModel().addListSelectionListener(action -> {
            if (scheduleTable.getSelectedRow() != -1 && !scheduleTable.getValueAt(scheduleTable.getSelectedRow(), 0).toString().equals("")) {
                editButton.setEnabled(true);
                removeButton.setEnabled(true);
            } else {
                editButton.setEnabled(false);
                removeButton.setEnabled(false);
            }
        });

        setEndTime();
        enableButtons();
    }

    private void createUIComponents() throws IOException {
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
            StringWriter errors = new StringWriter();
            err.printStackTrace(new PrintWriter(errors));
            String message = errors.toString();
            fileController.saveFile("SQL Exception: " + message);

            panelController.createErrorPanel("Something went wrong.", this);
        }
    }

    private void updateCourses() throws IOException {
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
                row[0] = courses.getString(2);
                row[1] = courses.getString(3);
                row[2] = courses.getString(4);
                row[3] = courses.getString(5);
                scheduleTableModel.addRow(row);
            }
        } catch (SQLException err) {
            StringWriter errors = new StringWriter();
            err.printStackTrace(new PrintWriter(errors));
            String message = errors.toString();
            fileController.saveFile("SQL Exception: " + message);

            panelController.createErrorPanel("Something went wrong.", this);
        } finally {
            panelController.fillEmptyRows(scheduleTableModel);
            scheduleTable.setModel(scheduleTableModel);
        }
    }

    private void enableButtons() {
        addButton.setEnabled(lessonsComboBox.getSelectedItem() != null && teachersComboBox.getSelectedItem() != null
                             && classroomComboBox.getSelectedItem() != null && dayComboBox.getSelectedItem() != null
                             && startTime.getSelectedItem() != null && endTime.getSelectedItem() != null);
    }

    // Fill end time combobox with items coming after start time combobox selected item
    private void setEndTime() {
        endTime.removeAllItems();
        startTime.getSelectedIndex();
        for (int i = startTime.getSelectedIndex() + 1; i < startTime.getItemCount(); i++)
            endTime.addItem(startTime.getItemAt(i));

        endTime.setEnabled(endTime.getItemCount() > 0);
    }
}