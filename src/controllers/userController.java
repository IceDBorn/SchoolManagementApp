package controllers;

import models.Database;
import models.User;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;

public class userController {
    public static void Login(String email, String password) {
        CachedRowSet results;
        try {
            results = Database.selectQuery(String.format("SELECT id, name, email, \"isAdmin\" FROM \"Users\" WHERE email = '%s' AND password = '%s'", email, password));

            // If a user exists with the same email and password, let the user successfully log in
            if (results.next()) {
                User.setId(results.getInt("id"));
                User.setName(results.getString("name"));
                User.setEmail(results.getString("email"));
                User.setAdmin(results.getBoolean("\"isAdmin\""));

                results = Database.selectQuery(String.format("SELECT subject FROM \"Teachers\" WHERE id = '%d'", User.getId()));
                User.setTeacher(results.isBeforeFirst());

                if (!User.isTeacher()) {
                    results = Database.selectQuery(String.format("SELECT year FROM \"Students\" WHERE id = '%d'", User.getId()));
                    User.setSpecificField(results.getString("year"));
                } else
                    User.setSpecificField(results.getString("subject"));

                System.out.printf("userId %d successfully logged in as %s%s%n",
                        User.getId(), User.getName(), User.isAdmin() ? " with admin rights" : "");
            } else System.out.println("You've specified an invalid email or password.");
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }
    }

    public static void Logout() {
        User.setId(-1);
        User.setName("");
        User.setEmail("");
        User.setSpecificField("");
        User.setTeacher(false);
        User.setAdmin(false);
    }
}
