// TODO: Add a label or an alert box to each panel for errors, instead of showing them in the console.

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;
import views.*;

public class Main {
    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialOceanicTheme()));

        SwingUtilities.invokeLater(() -> {
            int userPanel = 6;

            switch (userPanel) {
                case 1 -> {
                    schedulePanel schedule = new schedulePanel();
                    schedule.setVisible(true);
                }
                case 2 -> {
                    gradesPanel grades = new gradesPanel();
                    grades.setVisible(true);
                }
                case 3 -> {
                    classroomsPanel classrooms = new classroomsPanel();
                    classrooms.setVisible(true);
                }
                case 4 -> {
                    lessonsPanel lessons = new lessonsPanel();
                    lessons.setVisible(true);
                }
                case 5 -> {
                    personPanel person = new personPanel();
                    person.setVisible(true);
                }
                case 6 -> {
                    coursesPanel courses = new coursesPanel();
                    courses.setVisible(true);
                }
                case 7 -> {
                    loginPanel login = new loginPanel();
                    login.setVisible(true);
                }
                default -> throw new IllegalStateException("Unexpected value: " + userPanel);
            }
        });
    }
}
