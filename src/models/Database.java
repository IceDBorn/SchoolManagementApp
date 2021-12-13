package models;

public class Database {

    private static final String url = "jdbc:postgresql://pog.knp.one:45432/postgres";
    private static final String user = "postgres";
    private static final String pass = "poggers";

    public static String getURL() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getPass() {
        return pass;
    }
}
