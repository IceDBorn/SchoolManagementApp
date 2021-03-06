package views;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import controllers.databaseController;
import controllers.fileController;
import controllers.panelController;
import models.Database;
import models.User;

public class gradesPanel extends JFrame {
    private JPanel gradesPanel;
    private JScrollPane infoScrollPane;
    private JScrollPane gradeScrollPane;
    private JTable infoTable;
    private JTable gradesTable;
    private JButton backButton;
    private JButton saveButton;

    public gradesPanel(Point location) {
        add(gradesPanel);
        setTitle("Grades");
        setSize(1280, 720);
        setResizable(false);
        setLocation(location);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("res/school.png").getImage());

        infoScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        gradeScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        infoTable.setDefaultEditor(Object.class, null);
        infoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gradesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Stop users from interacting with the table
        infoTable.getTableHeader().setReorderingAllowed(false);
        gradesTable.getTableHeader().setReorderingAllowed(false);

        // Sync tables scrolling
        infoScrollPane.getVerticalScrollBar().setModel(gradeScrollPane.getVerticalScrollBar().getModel());

        // Display the save button based on user type
        saveButton.setVisible(User.isTeacher());

        backButton.addActionListener(action -> {
            panelController.createMainPanel(this.getLocation());
            this.setVisible(false);
        });

        // Save grades by pressing the save button
        saveButton.addActionListener(action -> {
            try {
                // Loop through all the table rows in order to update them one by one.
                for (int i = 0; i < infoTable.getRowCount(); i++) {
                    int id = Integer.parseInt(infoTable.getValueAt(i, 0).toString());
                    float grade = Float.parseFloat(gradesTable.getValueAt(i, 0).toString());

                    if (grade <= 20 && grade >= 0) {
                        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE \"StudentLessons\" SET grade = ? WHERE id = ?");
                        preparedStatement.setFloat(1, grade);
                        preparedStatement.setInt(2, id);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        connection.close();

                        fileController.saveFile("User (%d) %s modified (%d) grade to %.1f.".formatted(
                                User.getId(), User.getName(), id, grade));
                    }

                    // Checks if the next row has a null id to end the loop
                    if (infoTable.getValueAt(i + 1, 0) == "")
                        break;
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
            }
        });

        gradesTable.getSelectionModel().addListSelectionListener(action -> {
            // Get selected row index
            int selectedRow = gradesTable.getSelectedRow();

            infoTable.setRowSelectionInterval(selectedRow, selectedRow);

            if (gradesTable.getValueAt(selectedRow, 0).toString().equals(""))
                gradesTable.setDefaultEditor(Object.class, null);
            else {
                gradesTable.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()));
                gradesTable.getDefaultEditor(Object.class).addCellEditorListener(new CellEditorListener() {
                    @Override
                    public void editingStopped(ChangeEvent e) {
                        String grade = gradesTable.getValueAt(selectedRow, 0).toString();

                        if (grade.equals(""))
                            gradesTable.setValueAt(0, selectedRow, 0);
                        else
                            gradesTable.setValueAt(grade.replaceAll("[^\\d.]", ""), selectedRow, 0);
                    }

                    @Override
                    public void editingCanceled(ChangeEvent e) {
                        // ignored
                    }
                });
            }
        });

        infoTable.getSelectionModel().addListSelectionListener(action -> gradesTable.setRowSelectionInterval(infoTable.getSelectedRow(), infoTable.getSelectedRow()));
    }

    private void createUIComponents() throws IOException {
        // Add columns
        String[] infoTableColumns;
        String[] gradeTableColumns = {"Grade"};
        String query;

        // Check whether the user is a teacher and show the corresponding panel
        if (User.isTeacher()) {
            infoTableColumns = new String[]{"ID", "Student", "Subject"};
            query = String.format("""
                    SELECT DISTINCT("StudentLessons".id), "Users".name AS username, "Lessons".name AS lesson, "StudentLessons".grade
                    FROM "StudentLessons"
                    INNER JOIN "Courses" ON "StudentLessons"."courseId" = "Courses".id
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id
                    INNER JOIN "Users" ON "StudentLessons"."studentId" = "Users".id
                    WHERE "teacherId" = %d""", User.getId());
        } else {
            infoTableColumns = new String[]{"Subject"};
            query = String.format("""
                    SELECT DISTINCT("StudentLessons".id), "Lessons".name AS lesson, "StudentLessons".grade
                    FROM "StudentLessons"
                    INNER JOIN "Courses" ON "StudentLessons"."courseId" = "Courses".id
                    INNER JOIN "Lessons" ON "Courses"."lessonId" = "Lessons".id
                    INNER JOIN "Users" ON "StudentLessons"."studentId" = "Users".id
                    WHERE "studentId" = %d""", User.getId());
        }

        DefaultTableModel infoTableModel = new DefaultTableModel(infoTableColumns, 0);
        DefaultTableModel gradeTableModel = new DefaultTableModel(gradeTableColumns, 0);
        infoTable = new JTable(infoTableModel);
        gradesTable = new JTable(gradeTableModel);

        try {
            CachedRowSet lessons = databaseController.selectQuery(query);

            // Add rows
            Object[] infoRows = new Object[3];
            Object[] gradeRow = new Object[1];
            while (lessons.next()) {
                if (!User.isTeacher())
                    infoRows[0] = lessons.getString("lesson");
                else {
                    infoRows[0] = lessons.getString("id");
                    infoRows[1] = lessons.getString("username");
                    infoRows[2] = lessons.getString("lesson");
                }

                gradeRow[0] = lessons.getFloat("grade");

                infoTableModel.addRow(infoRows);
                gradeTableModel.addRow(gradeRow);
            }
        } catch (SQLException err) {
            StringWriter errors = new StringWriter();
            err.printStackTrace(new PrintWriter(errors));
            String message = errors.toString();
            fileController.saveFile("SQL Exception: " + message);

            panelController.createErrorPanel("Something went wrong.", this, 220);
        } finally {
            panelController.fillEmptyRows(infoTableModel);
            panelController.fillEmptyRows(gradeTableModel);

            infoTable.setModel(infoTableModel);
            gradesTable.setModel(gradeTableModel);
        }
    }
}