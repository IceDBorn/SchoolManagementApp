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
import java.util.stream.IntStream;

public class scheduleMakerPanel extends JFrame {
    private final String[] scheduleTableColumns = {"Lesson", "Teacher", "Classroom", "Day", "Starts", "Ends"};
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

    private int selectedCourseId;
    private int selectedCourseLessonId;

    public scheduleMakerPanel(Point location) throws IOException {
        selectedCourseId = -1;
        selectedCourseLessonId = -1;

        add(coursesPanel);
        setTitle("Schedule Maker");
        setSize(1280, 720);
        setResizable(false);
        setLocation(location);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon("res/school.png").getImage());

        scheduleTable.setDefaultEditor(Object.class, null);

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

            panelController.createErrorPanel("Something went wrong.", this, 220);
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
            try {

                String lesson = lessonsComboBox.getItemAt(lessonsComboBox.getSelectedIndex());
                String teacher = teachersComboBox.getItemAt(teachersComboBox.getSelectedIndex());
                String classroom = classroomComboBox.getItemAt(classroomComboBox.getSelectedIndex());
                String day = dayComboBox.getItemAt(dayComboBox.getSelectedIndex());
                String starts = startTime.getItemAt(startTime.getSelectedIndex());
                String ends = endTime.getItemAt(endTime.getSelectedIndex());

                // Get the lessonId using the selected lesson
                int lessonId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Lessons\" WHERE name = '%s'", lesson));

                // Get the teacherId using the selected teacher
                int teacherId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Users\" WHERE name = '%s'", teacher));

                // Get the classroomId using the selected classroom
                int classroomId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", classroom));

                // Check if a course exists with the same classroom, day and time
                boolean courseExists = databaseController.selectQuery(String.format("""
                        SELECT id FROM "Courses" WHERE "classroomId" = '%d' AND day = '%s' AND "startTime" = '%s' AND "endTime" = '%s'""", classroomId, day, starts, ends)).isBeforeFirst();

                if (courseExists)
                    panelController.createErrorPanel("A course using this classroom at the given day and time already exists.", this, 500);
                else {
                    boolean isAddButton = addButton.getText().equals("Add");

                    Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());

                    PreparedStatement preparedStatement = connection.prepareStatement(isAddButton ?
                            "INSERT INTO \"Courses\"(\"lessonId\", \"teacherId\", \"classroomId\", day, \"startTime\", \"endTime\") VALUES (?, ?, ?, ?, ?, ?)" :
                            "UPDATE \"Courses\" SET \"lessonId\" = ?, \"teacherId\" = ?, \"classroomId\" = ?, day = ?, \"startTime\" = ?, \"endTime\" = ? WHERE id = ?", PreparedStatement.RETURN_GENERATED_KEYS);
                    preparedStatement.setInt(1, lessonId);
                    preparedStatement.setInt(2, teacherId);
                    preparedStatement.setInt(3, classroomId);
                    preparedStatement.setString(4, day);
                    preparedStatement.setString(5, starts);
                    preparedStatement.setString(6, ends);

                    if (!isAddButton)
                        preparedStatement.setInt(7, selectedCourseId);

                    preparedStatement.executeUpdate();

                    // Get the id of the inserted or updated course
                    int id = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());

                    // Delete all student lessons associated with the selected course
                    if (!isAddButton && lessonId != selectedCourseLessonId) {
                        preparedStatement = connection.prepareStatement("DELETE FROM \"StudentLessons\" WHERE \"courseId\" IN (SELECT id FROM \"Courses\" WHERE id = ?)");
                        preparedStatement.setInt(1, id);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    }

                    if (isAddButton || lessonId != selectedCourseLessonId) {
                        // Gets the year of the inserted course
                        int year = databaseController.selectFirstIntColumn(String.format("""
                                SELECT "yearId" FROM "Courses" INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id WHERE "Courses".id = '%d'""", id));

                        // Gets the classroom limit of the inserted course
                        int count = databaseController.selectFirstIntColumn(String.format("""
                                SELECT "limit" FROM "Courses" INNER JOIN "Classrooms" ON "Courses"."classroomId" = "Classrooms".id WHERE "Courses".id = '%d'""", id));

                        // Subtract the amount of users in a classroom from its limit to get the amount of available slots
                        count -= databaseController.selectFirstIntColumn(String.format("""
                                SELECT COUNT("StudentLessons".id)
                                FROM "Courses"
                                    INNER JOIN "StudentLessons" ON "Courses".id = "StudentLessons"."courseId"
                                    INNER JOIN "Classrooms" ON "Courses"."classroomId" = "Classrooms".id
                                WHERE "courseId" = '%d'""", id));

                        // Add students to the course based on available slots
                        if (count > 0) {
                            // Get all students that are not in a course with the same lesson
                            CachedRowSet students = databaseController.selectQuery(String.format("""
                                    SELECT "Students".id as id, "Users".name as name
                                    FROM "Students"
                                        INNER JOIN "Users" on "Users".id = "Students".id
                                    WHERE "yearId" = '%d' AND "Students".id NOT IN (
                                        SELECT COALESCE("studentId", 0)
                                        FROM "StudentLessons"
                                            RIGHT JOIN "Courses" on "StudentLessons"."courseId" = "Courses".id
                                        WHERE "lessonId" = '%d')
                                    LIMIT '%d'""", year, lessonId, count));

                            if (students.isBeforeFirst()) {
                                // Add each student to the inserted course
                                while (students.next()) {
                                    int studentId = students.getInt("id");

                                    preparedStatement = connection.prepareStatement("INSERT INTO \"StudentLessons\"(\"courseId\", \"studentId\") VALUES (?, ?)");
                                    preparedStatement.setInt(1, id);
                                    preparedStatement.setInt(2, studentId);

                                    preparedStatement.executeUpdate();
                                    preparedStatement.close();

                                    fileController.saveFile("Student (%d) has been added to schedule entry (%d).".formatted(
                                            studentId, id));
                                }

                                fileController.saveFile("Teacher (%d) %s %s schedule entry (%d).".formatted(
                                        User.getId(), User.getName(), isAddButton ? "created" : "updated", id));
                            } else
                                panelController.createErrorPanel("There aren't any available students to add.\nPlease create more users or delete this course.", this, 350);
                        } else
                            panelController.createErrorPanel("There aren't any available classrooms to put the students in, create another course.", this, 500);

                        connection.close();
                    }
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

                panelController.createErrorPanel("Something went wrong.", this, 220);
            } finally {
                try {
                    updateCourses();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Remove the selected course from the database
        removeButton.addActionListener(action -> {
            try {
                // Get the selected row index
                int selectedRow = scheduleTable.getSelectedRow();

                // Get the selected course data
                String lesson = String.valueOf(scheduleTable.getValueAt(selectedRow, 0));
                String teacher = String.valueOf(scheduleTable.getValueAt(selectedRow, 1));
                String classroom = String.valueOf(scheduleTable.getValueAt(selectedRow, 2));
                String day = String.valueOf(scheduleTable.getValueAt(selectedRow, 3));
                String starts = String.valueOf(scheduleTable.getValueAt(selectedRow, 4));
                String ends = String.valueOf(scheduleTable.getValueAt(selectedRow, 5));

                // Get the lesson id using the name of the lesson from the selected row
                int lessonId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Lessons\" WHERE name = '%s'", lesson));

                // Get the teacher id using the name of the lesson from the selected row
                int teacherId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Users\" WHERE name = '%s'", teacher));

                // Get the classroom id using the name of the selected classroom
                int classroomId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", classroom));

                // Get the courseId using the data from the selected row
                int courseId = databaseController.selectFirstIntColumn(String.format(
                        "SELECT id FROM \"Courses\" WHERE \"lessonId\" = '%d' AND \"teacherId\" = '%d' AND \"classroomId\" = '%d' AND day = '%s' AND \"startTime\" = '%s' AND \"endTime\" = '%s'",
                        lessonId, teacherId, classroomId, day, starts, ends));

                // Check how many student lessons exist using that courseId
                int count = databaseController.selectFirstIntColumn(String.format("SELECT COUNT(id) FROM \"StudentLessons\" WHERE \"courseId\" = '%d'", courseId));

                // Check if the course is being used by a student lesson and ask for deletion confirmation
                if (count > 0) {
                    if (panelController.createConfirmationPanel(this) == JOptionPane.YES_OPTION) {
                        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());

                        // Delete all student lessons associated with the selected course
                        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM \"StudentLessons\" WHERE \"courseId\" IN (SELECT id FROM \"Courses\" WHERE id = ?)");
                        preparedStatement.setInt(1, courseId);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();

                        // Delete the selected course from the database
                        preparedStatement = connection.prepareStatement("DELETE FROM \"Courses\" WHERE id = ?");
                        preparedStatement.setInt(1, courseId);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        connection.close();

                        fileController.saveFile("User (%d) %s deleted %d student lessons, by deleting schedule entry (%d).".formatted(
                                User.getId(), User.getName(), count, courseId));
                    }
                } else {
                    // Delete the selected course from the database
                    Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                    PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM \"Courses\" WHERE id = ?");
                    preparedStatement.setInt(1, courseId);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    connection.close();

                    fileController.saveFile("User (%d) %s deleted schedule entry (%d)".formatted(
                            User.getId(), User.getName(), courseId));
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

                panelController.createErrorPanel("Something went wrong.", this, 220);
            } finally {
                scheduleTable.clearSelection();

                try {
                    updateCourses();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        editButton.addActionListener(action -> {
            addButton.setText("Save");
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            scheduleTable.setEnabled(false);

            // Get the selected row index
            int selectedRow = scheduleTable.getSelectedRow();

            // Get the selected course data
            String lesson = String.valueOf(scheduleTable.getValueAt(selectedRow, 0));
            String teacher = String.valueOf(scheduleTable.getValueAt(selectedRow, 1));
            String classroom = String.valueOf(scheduleTable.getValueAt(selectedRow, 2));
            String day = String.valueOf(scheduleTable.getValueAt(selectedRow, 3));
            String starts = String.valueOf(scheduleTable.getValueAt(selectedRow, 4));
            String ends = String.valueOf(scheduleTable.getValueAt(selectedRow, 5));

            lessonsComboBox.setSelectedItem(lesson);
            teachersComboBox.setSelectedItem(teacher);
            classroomComboBox.setSelectedItem(classroom);
            dayComboBox.setSelectedItem(day);
            startTime.setSelectedItem(starts);
            endTime.setSelectedItem(ends);

            try {
                // Get the lesson id using the name of the lesson from the selected row
                int lessonId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Lessons\" WHERE name = '%s'", lesson));

                // Get the teacher id using the name of the lesson from the selected row
                int teacherId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Users\" WHERE name = '%s'", teacher));

                // Get the classroom id using the name of the selected classroom
                int classroomId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Classrooms\" WHERE name = '%s'", classroom));

                // Get the courseId using the data from the selected row
                CachedRowSet course = databaseController.selectQuery(String.format("""
                                SELECT id, "lessonId" FROM "Courses" WHERE "lessonId" = '%d' AND "teacherId" = '%d' AND "classroomId" = '%d' AND day = '%s' AND "startTime" = '%s' AND "endTime" = '%s'""",
                        lessonId, teacherId, classroomId, day, starts, ends));

                course.next();

                selectedCourseId = course.getInt("id");
                selectedCourseLessonId = course.getInt("lessonId");

            } catch (SQLException err) {
                StringWriter errors = new StringWriter();
                err.printStackTrace(new PrintWriter(errors));
                String message = errors.toString();

                try {
                    fileController.saveFile("SQL Exception: " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                panelController.createErrorPanel("Something went wrong.", this, 220);
            }
        });

        // Update teachersComboBox with teachers that have the same profession as the selected lesson
        lessonsComboBox.addActionListener(action -> {
            try {
                // Get the name of the selected lesson
                String lesson = lessonsComboBox.getItemAt(lessonsComboBox.getSelectedIndex());

                // Get the profession id of the selected lesson
                int professionId = databaseController.selectFirstIntColumn(String.format("SELECT \"professionId\" FROM \"Lessons\" WHERE name = '%s'", lesson));

                // Select all teachers that have the same profession id
                CachedRowSet teachers = databaseController.selectQuery(String.format("""
                        SELECT name FROM "Users" INNER JOIN "Teachers" ON "Users".id = "Teachers".id WHERE "professionId" = '%d'""", professionId));

                teachersComboBox.removeAllItems();
                while (teachers.next())
                    teachersComboBox.addItem(teachers.getString("name"));

            } catch (SQLException err) {
                StringWriter errors = new StringWriter();
                err.printStackTrace(new PrintWriter(errors));
                String message = errors.toString();

                try {
                    fileController.saveFile("SQL Exception: " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                panelController.createErrorPanel("Something went wrong.", this, 220);
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
            // Get selected row index
            int selectedRow = scheduleTable.getSelectedRow();

            if (selectedRow != -1 && !scheduleTable.getValueAt(selectedRow, 0).toString().equals("")) {
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
                    SELECT DISTINCT("Courses".id) as id, "Lessons".name as lesson, "Users".name as "user", "Classrooms".name as classroom, day, "startTime", "endTime"
                    FROM "Courses"
                    INNER JOIN "Classrooms" ON "Courses"."classroomId" = "Classrooms".id
                    INNER JOIN "Users" ON "Courses"."teacherId" = "Users".id
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id""");

            // Add rows
            Object[] row = new Object[6];

            while (courses.next()) {
                row[0] = courses.getString("lesson");
                row[1] = courses.getString("user");
                row[2] = courses.getString("classroom");
                row[3] = courses.getString("day");
                row[4] = courses.getString("startTime");
                row[5] = courses.getString("endTime");
                scheduleTableModel.addRow(row);
            }
        } catch (SQLException err) {
            StringWriter errors = new StringWriter();
            err.printStackTrace(new PrintWriter(errors));
            String message = errors.toString();
            fileController.saveFile("SQL Exception: " + message);

            panelController.createErrorPanel("Something went wrong.", this, 220);
        } finally {
            panelController.fillEmptyRows(scheduleTableModel);
            scheduleTable.setModel(scheduleTableModel);
        }
    }

    private void updateCourses() throws IOException {
        // Remove all rows
        IntStream.iterate(scheduleTableModel.getRowCount() - 1, i -> i > -1, i -> i - 1).forEach(i -> scheduleTableModel.removeRow(i));

        String classroom = classroomComboBox.getItemAt(classroomComboBox.getSelectedIndex());

        try {
            CachedRowSet courses = databaseController.selectQuery(String.format("""
                    SELECT DISTINCT("Courses".id) as id, "Lessons".name as lesson, "Users".name as "user", "Classrooms".name as classroom, day, "startTime", "endTime"
                    FROM "Courses"
                    INNER JOIN "Classrooms" ON "Courses"."classroomId" = "Classrooms".id
                    INNER JOIN "Users" ON "Courses"."teacherId" = "Users".id
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id
                    WHERE "Classrooms".name = '%s'""", classroom));

            // Add rows
            Object[] row = new Object[6];

            while (courses.next()) {
                row[0] = courses.getString("lesson");
                row[1] = courses.getString("user");
                row[2] = courses.getString("classroom");
                row[3] = courses.getString("day");
                row[4] = courses.getString("startTime");
                row[5] = courses.getString("endTime");
                scheduleTableModel.addRow(row);
            }
        } catch (SQLException err) {
            StringWriter errors = new StringWriter();
            err.printStackTrace(new PrintWriter(errors));
            String message = errors.toString();
            fileController.saveFile("SQL Exception: " + message);

            panelController.createErrorPanel("Something went wrong.", this, 220);
        } finally {
            panelController.fillEmptyRows(scheduleTableModel);
            scheduleTable.setModel(scheduleTableModel);
        }
    }

    private void enableButtons() {
        addButton.setEnabled(
                lessonsComboBox.getSelectedItem() != null
                && teachersComboBox.getSelectedItem() != null
                && classroomComboBox.getSelectedItem() != null
                && dayComboBox.getSelectedItem() != null
                && startTime.getSelectedItem() != null
                && endTime.getSelectedItem() != null);
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