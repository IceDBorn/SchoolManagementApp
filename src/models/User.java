package models;

public class User {
    private static int userId = -1;
    private static int userSpecificField = -1;
    private static String username = "";
    private static String userEmail = "";
    private static boolean isTeacher;
    private static boolean isAdmin;

    public static int getId() {
        return userId;
    }

    public static void setId(int userId) {
        User.userId = userId;
    }

    public static String getName() {
        return username;
    }

    public static void setName(String username) {
        User.username = username;
    }

    public static String getEmail() {
        return userEmail;
    }

    public static void setEmail(String userEmail) {
        User.userEmail = userEmail;
    }

    public static int getSpecificField() {
        return userSpecificField;
    }

    public static void setSpecificField(int userSpecificField) {
        User.userSpecificField = userSpecificField;
    }

    public static boolean isTeacher() {
        return isTeacher;
    }

    public static void setTeacher(boolean isTeacher) {
        User.isTeacher = isTeacher;
    }

    public static boolean isAdmin() {
        return isAdmin;
    }

    public static void setAdmin(boolean isAdmin) {
        User.isAdmin = isAdmin;
    }
}