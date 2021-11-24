package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import mdlaf.themes.AbstractMaterialTheme;
import mdlaf.utils.MaterialColors;

public class StudentPanel extends JFrame {

  private JTable scheduleTable;
  private JPanel studentPanel;
  private JLabel usernameLabel;
  private JButton homeButton;

  public StudentPanel() {
    add(studentPanel);
    setSize(1280, 720);
    setResizable(false);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    usernameLabel.setText("IceDBorn");
  }

  private void createUIComponents() {
    // Add columns
    String[] studentTableColumns = {"Lesson", "Class", "Date"};
    DefaultTableModel studentTableModel = new DefaultTableModel(studentTableColumns, 0);
    scheduleTable = new JTable(studentTableModel);
    // Make table cells non-editable
    scheduleTable.setDefaultEditor(Object.class, null);

    // Add rows
    Object[] row = new Object[3];
    row[0] = "Mathematics";
    row[1] = "G1";
    row[2] = "Monday - 13:00";

    studentTableModel.addRow(row);
  }
}
