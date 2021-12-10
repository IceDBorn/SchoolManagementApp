package controllers;

import models.Database;

import java.sql.*;

public class userController {
    public static void Login(String userEmail, String userPassword) {
        try {
            Connection dbConnection = DriverManager.getConnection(Database.getDbURL(), Database.getDbUser(), Database.getDbPass());
            Statement dbStatement = dbConnection.createStatement();
            ResultSet dbResult = dbStatement.executeQuery(String.format("SELECT id, name FROM \"Users\" WHERE email = '%s' AND password = '%s'", userEmail, userPassword));

            // If a user exists with the same email and password, let the user successfully log in
            if (dbResult.next()) {
                // TODO: Fill user fields via setters
                int userId = dbResult.getInt(1);
                String userName = dbResult.getString(2);

                System.out.printf("userId %d successfully logged in as %s%n", userId, userName);
            } else System.out.println("You've specified an invalid email or password.");

            dbStatement.close();
            dbConnection.close();
        } catch (
        SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }
    }
}
