package controllers;

import models.User;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;

public class userController {
    public static void Login(String email, String password) {
        try {
            CachedRowSet user = databaseController.selectQuery(String.format("""
                    SELECT "Users".id, name, email, "isTeacher", "isAdmin", year, subject FROM "Users"
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
                    User.setSpecificField(user.getString("subject"));
                else
                    User.setSpecificField(databaseController.findYearName(user.getInt("year")));

                System.out.printf("userId %d successfully logged in as a %s%s%n",
                        User.getId(), User.isTeacher() ? "teacher" : "student", User.isAdmin() ? " with admin rights" : "");
            } else System.out.println("You've specified an invalid email or password.");
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }
    }

    public static void Logout() {
        System.out.printf("userId %d successfully logged out as a %s%s%n",
                User.getId(), User.isTeacher() ? "teacher" : "student", User.isAdmin() ? " with admin rights" : "");

        User.setId(-1);
        User.setName("");
        User.setEmail("");
        User.setSpecificField("");
        User.setTeacher(false);
        User.setAdmin(false);
    }
}
