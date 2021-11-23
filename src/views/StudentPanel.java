package views;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

public class StudentPanel extends JFrame {

  private JTable scheduleTable;
  private JPanel studentPanel;

  public StudentPanel() {
    add(studentPanel);
    setSize(1280, 720);
    setResizable(false);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  private void createUIComponents() {
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
