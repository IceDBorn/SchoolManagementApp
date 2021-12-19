// TODO: Add a label or an alert box to each panel for errors, instead of showing them in the console.

import controllers.panelController;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;
import views.loginPanel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialOceanicTheme()));

        SwingUtilities.invokeLater(() -> {
            loginPanel login = new loginPanel();
            login.setVisible(true);
    });
}
}
