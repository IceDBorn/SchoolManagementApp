package views;

import controllers.databaseController;
import controllers.fileController;
import controllers.panelController;
import models.Database;
import models.User;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.IntStream;

public class lessonsPanel extends JFrame {
    DefaultTableModel lessonsTableModel;
    private JPanel lessonsPanel;
    private JTextField lessonNameTextField;
    private JButton addButton;
    private JComboBox<String> professionComboBox;
    private JComboBox<String> schoolYearComboBox;
    private JButton backButton;
    private JTable lessonsTable;
    private JScrollPane scrollPane;
    private JButton editButton;
    private JButton removeButton;
    private JButton cancelButton;

    private int selectedLessonId;

    public lessonsPanel(Point location) throws IOException {
        add(lessonsPanel);
        setTitle("Lessons");
        setSize(1280, 720);
        setResizable(false);
        setLocation(location);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Customize table to have no border, disable cell editing and switch single row selection
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        lessonsTable.setDefaultEditor(Object.class, null);
        lessonsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Center combobox text
        ((JLabel) professionComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel) schoolYearComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Update professionList from the professions table
        ArrayList<String> professionList = new ArrayList<>();
        panelController.updateList("SELECT name FROM \"Professions\"", professionList, this);
        professionComboBox.removeAllItems();

        for (String profession : professionList)
            professionComboBox.addItem(profession);

        // Update yearList from the years table
        ArrayList<String> yearList = new ArrayList<>();
        panelController.updateList("SELECT name FROM \"Years\"", yearList, this);
        schoolYearComboBox.removeAllItems();

        for (String year : yearList)
            schoolYearComboBox.addItem(year);

        backButton.addActionListener(action -> {
            panelController.createMainPanel(this.getLocation());
            this.setVisible(false);
        });

        addButton.addActionListener(action -> {
            try {
                boolean isAddButton = addButton.getText().equals("Add");

                Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                PreparedStatement preparedStatement = connection.prepareStatement(
                        isAddButton ? "INSERT INTO \"Lessons\"(name, \"professionId\", \"yearId\") VALUES (?, ?, ?)" : "UPDATE \"Lessons\" SET name = ?, \"professionId\" = ?, \"yearId\" = ? WHERE id = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);

                String name = lessonNameTextField.getText();
                int professionId = databaseController.findProfessionId(Objects.requireNonNull(professionComboBox.getSelectedItem()).toString());
                int yearId = databaseController.findYearId(Objects.requireNonNull(schoolYearComboBox.getSelectedItem()).toString());

                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, professionId);
                preparedStatement.setInt(3, yearId);

                if (!isAddButton)
                    preparedStatement.setInt(4, selectedLessonId);

                preparedStatement.executeUpdate();

                // Get the id of the newly inserted lesson
                int id = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());

                preparedStatement.close();
                connection.close();

                fileController.saveFile("User (%d) %s%s lesson (%d) %s.".formatted(
                        User.getId(), User.getName(), isAddButton ? " created " : " updated ", id, name));
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
                    updateLessons();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                revertUIComponents();
            }
        });

        cancelButton.addActionListener(action -> revertUIComponents());

        editButton.addActionListener(action -> {
            // Get the selected row index
            int selectedRow = lessonsTable.getSelectedRow();

            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            addButton.setText("Save");
            lessonsTable.setEnabled(false);
            lessonNameTextField.setText(lessonsTable.getValueAt(selectedRow, 0).toString());
            professionComboBox.setSelectedItem(lessonsTable.getValueAt(selectedRow, 1));
            schoolYearComboBox.setSelectedItem(lessonsTable.getValueAt(selectedRow, 2));
            lessonsTable.clearSelection();

            // Store the selected user id to a global variable
            try {
                selectedLessonId = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Lessons\" WHERE name = '%s'", lessonNameTextField.getText()));
            } catch (SQLException err) {
                StringWriter errors = new StringWriter();
                err.printStackTrace(new PrintWriter(errors));
                String message = errors.toString();
                try {
                    fileController.saveFile("SQL Exception: " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                panelController.createErrorPanel("Something went wrong.", this);
            }
        });

        removeButton.addActionListener(action -> {
            try {
                // Get the selected row index
                int selectedRow = lessonsTable.getSelectedRow();

                // Get the selected lesson name
                String name = lessonsTable.getValueAt(selectedRow, 0).toString();

                // Get the selected lesson id
                int id = databaseController.selectFirstIntColumn(String.format("SELECT id FROM \"Lessons\" WHERE name = '%s'", name));

                // Check how many lessons exist using that lessonId
                int count = databaseController.selectFirstIntColumn(String.format("SELECT COUNT(id) FROM \"Courses\" WHERE \"lessonId\" = '%d'", id));

                if (count > 0) {
                    if (panelController.createConfirmationPanel(this) == JOptionPane.YES_OPTION) {
                        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM \"Courses\" WHERE \"lessonId\" = ?");
                        preparedStatement.setInt(1, id);
                        preparedStatement.executeUpdate();

                        preparedStatement.close();
                        connection.close();

                        fileController.saveFile("User (%d) %s deleted %d courses, by deleting lesson (%d) %s.".formatted(
                                User.getId(), User.getName(), count, id, name));
                    }
                } else {
                    Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                    PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM \"Lessons\" WHERE id = ?");
                    preparedStatement.setInt(1, id);
                    preparedStatement.executeUpdate();

                    preparedStatement.close();
                    connection.close();

                    fileController.saveFile("User (%d) %s deleted lesson (%d) %s.".formatted(
                            User.getId(), User.getName(), id, name));
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
                    updateLessons();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                revertUIComponents();

                editButton.setEnabled(false);
                removeButton.setEnabled(false);
                lessonsTable.getSelectionModel().clearSelection();
            }
        });

        // Listen for changes in the lesson name text
        lessonNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                enableButtons();
            }

            public void removeUpdate(DocumentEvent e) {
                enableButtons();
            }

            public void insertUpdate(DocumentEvent e) {
                enableButtons();
            }
        });

        lessonsTable.getSelectionModel().addListSelectionListener(selection -> {
            if (lessonsTable.getSelectedRow() != -1
                && !lessonsTable.getValueAt(lessonsTable.getSelectedRow(), 0).toString().equals("")
                && !lessonsTable.getValueAt(lessonsTable.getSelectedRow(), 1).toString().equals("")) {
                editButton.setEnabled(true);
                removeButton.setEnabled(true);
            } else {
                editButton.setEnabled(false);
                removeButton.setEnabled(false);
            }
        });
    }

    private void createUIComponents() throws IOException {
        // Add columns
        String[] lessonsTableColumns = {"Name", "Profession", "School Year"};
        lessonsTableModel = new DefaultTableModel(lessonsTableColumns, 0);
        lessonsTable = new JTable(lessonsTableModel);
        // Stop users from interacting with the table
        lessonsTable.getTableHeader().setReorderingAllowed(false);
        updateLessons();
    }

    private void revertUIComponents() {
        addButton.setEnabled(false);
        cancelButton.setEnabled(false);
        lessonNameTextField.setText("");
        lessonsTable.setEnabled(true);
        addButton.setText("Add");

        if (professionComboBox.getItemCount() > 1) {
            professionComboBox.setSelectedIndex(1);
        } else {
            professionComboBox.setSelectedIndex(0);
        }

        if (schoolYearComboBox.getItemCount() > 1) {
            schoolYearComboBox.setSelectedIndex(1);
        } else {
            schoolYearComboBox.setSelectedIndex(0);
        }

        selectedLessonId = -1;
    }

    /**
     * Enable or disable buttons based on class name text
     */
    private void enableButtons() {
        if (!lessonNameTextField.getText().equals("")) {
            addButton.setEnabled(true);
            cancelButton.setEnabled(true);
        } else {
            addButton.setEnabled(false);
            cancelButton.setEnabled(false);
        }
    }

    private void updateLessons() throws IOException {
        IntStream.iterate(lessonsTableModel.getRowCount() - 1, i -> i > -1, i -> i - 1).forEach(i -> lessonsTableModel.removeRow(i));

        try {
            Object[] row = new Object[3];
            CachedRowSet lessons = databaseController.selectQuery("SELECT name, \"professionId\", \"yearId\" FROM \"Lessons\" ORDER BY name");

            // Add rows
            while (lessons.next()) {
                row[0] = lessons.getString("name");
                row[1] = databaseController.findProfessionName(lessons.getInt("professionId"));
                row[2] = databaseController.findYearName(lessons.getInt("yearId"));

                lessonsTableModel.addRow(row);
            }
        } catch (SQLException err) {
            StringWriter errors = new StringWriter();
            err.printStackTrace(new PrintWriter(errors));
            String message = errors.toString();
            fileController.saveFile("SQL Exception: " + message);

            panelController.createErrorPanel("Something went wrong.", this);
        } finally {
            panelController.fillEmptyRows(lessonsTableModel);
            lessonsTable.setModel(lessonsTableModel);
        }
    }
}
