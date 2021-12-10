package models;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;

public class Database {

    private static final String url = "jdbc:postgresql://localhost:5432/postgres";
    private static final String user = "postgres";
    private static final String pass = "kekw123";

    public static String getURL() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getPass() {
        return pass;
    }

    public static CachedRowSet selectQuery(String sql) throws SQLException {
        Connection connection = DriverManager.getConnection(getURL(), getUser(), getPass());
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        // Transfer the ResultSet data to a CachedRowSet
        CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
        cachedRowSet.populate(resultSet);

        resultSet.close();
        statement.close();
        connection.close();

        return cachedRowSet;
    }
}
