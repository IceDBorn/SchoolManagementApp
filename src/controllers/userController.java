package controllers;

import models.User;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class userController {
    public static void Login(String email, String password, Component panel) {
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

                System.out.printf("userId %d successfully logged in as a %s%s%n",
                        User.getId(), User.isTeacher() ? "teacher" : "student", User.isAdmin() ? " with admin rights" : "");
            } else panelController.createErrorPanel("You've specified an invalid email or password.", panel);
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
            panelController.createErrorPanel("Something went wrong.", panel);
        }
    }

    public static void Logout() {
        System.out.printf("userId %d successfully logged out as a %s%s%n",
                User.getId(), User.isTeacher() ? "teacher" : "student", User.isAdmin() ? " with admin rights" : "");

        User.setId(-1);
        User.setSpecificField(-1);
        User.setName("");
        User.setEmail("");
        User.setTeacher(false);
        User.setAdmin(false);
    }
}
