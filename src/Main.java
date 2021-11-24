import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;
import views.StudentPanel;

public class Main {

  public static void main(String[] args)
      throws UnsupportedLookAndFeelException {
    UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialOceanicTheme()));

    SwingUtilities.invokeLater(() -> {
      StudentPanel student = new StudentPanel();
      student.setVisible(true);
    });
  }
}
