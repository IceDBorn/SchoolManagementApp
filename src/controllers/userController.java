package controllers;

import models.Database;
import models.User;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;

public class userController {
    public static void Login(String email, String password) {
        try {
            String query = String.format("SELECT id, name, email, \"isAdmin\" FROM \"Users\" WHERE email = '%s' AND password = '%s'", email, password);
            CachedRowSet results = Database.selectQuery(query);

            // If a user exists with the same email and password, let the user successfully log in
            if (results.next()) {
                User.setId(results.getInt(1));
                User.setName(results.getString(2));
                User.setEmail(results.getString(3));
                User.setAdmin(results.getBoolean(4));

                System.out.printf("userId %d successfully logged in as %s%s%n",
                        User.getId(), User.getName(), User.isAdmin() ? " with admin rights" : "");
            } else System.out.println("You've specified an invalid email or password.");
        } catch (SQLException err) {
            err.printStackTrace();

        }
    }
}
