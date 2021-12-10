package models;

public class User {
    private static int userId;
    private static String username;
    private static String userEmail;
    private static String userSpecificField;
    private static boolean isTeacher;
    private static boolean isAdmin;

    public static int getUserId() {
        return userId;
    }

    public static void setUserId(int userId) {
        User.userId = userId;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        User.username = username;
    }

    public static String getUserEmail() {
        return userEmail;
    }

    public static void setUserEmail(String userEmail) {
        User.userEmail = userEmail;
    }

    public static String getUserSpecificField() {
        return userSpecificField;
    }

    public static void setUserSpecificField(String userSpecificField) {
        User.userSpecificField = userSpecificField;
    }

    public static boolean isTeacher() {
        return isTeacher;
    }

    public static void setIsTeacher(boolean isTeacher) {
        User.isTeacher = isTeacher;
    }

    public static boolean isAdmin() {
        return isAdmin;
    }

    public static void setIsAdmin(boolean isAdmin) {
        User.isAdmin = isAdmin;
    }
}