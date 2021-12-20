package controllers;

import models.User;

import javax.sql.rowset.CachedRowSet;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

public class userController {
    public static void Login(String email, String password, Component panel) throws IOException {
        try {
            CachedRowSet user = databaseController.selectQuery(String.format("""
                    SELECT "Users".id, name, email, "isTeacher", "isAdmin", "yearId", "professionId" FROM "Users"
                    LEFT JOIN "Students" on "Users".id = "Students".id
                    LEFT JOIN "Teachers" on "Users".id = "Teachers".id
                    WHERE email = '%s' AND password = '%s'""", email, password));

            // If a user exists with the same email and password, let the user successfully log in
            if (user.next()) {
                User.setId(user.getInt("id"));
                User.setName(user.getString("name"));
                User.setEmail(user.getString("email"));
                User.setTeacher(user.getBoolean("isTeacher"));
                User.setAdmin(user.getBoolean("isAdmin"));

                if (User.isTeacher())
                    User.setSpecificField(user.getInt("professionId"));
                else
                    User.setSpecificField(user.getInt("yearId"));

                fileController.saveFile("User (%d) %s logged in as %s%s".formatted(
                        User.getId(), User.getName(), User.isTeacher() ? "teacher" : "student", User.isAdmin() ? " with admin rights." : "."));
            } else panelController.createErrorPanel("You've specified an invalid email or password.", panel, 350);
        } catch (SQLException | IOException err) {
            StringWriter errors = new StringWriter();
            err.printStackTrace(new PrintWriter(errors));
            String message = errors.toString();
            fileController.saveFile("SQL Exception: " + message);

            panelController.createErrorPanel("Something went wrong.", panel, 220);
        }
    }

    public static void Logout() throws IOException {
        fileController.saveFile("User (%d) %s logged out".formatted(
                User.getId(), User.getName()));

        User.setId(-1);
        User.setSpecificField(-1);
        User.setName("");
        User.setEmail("");
        User.setTeacher(false);
        User.setAdmin(false);
    }
}
