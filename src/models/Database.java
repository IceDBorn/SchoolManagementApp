package models;

public class Database {

    public static String getDbURL() {
        return "jdbc:postgresql://localhost:5432/postgres";
    }

    public static String getDbUser() {
        return "postgres";
    }

    public static String getDbPass() {
        return "kekw123";
    }
}
