package controllers;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import models.Database;

public class databaseController {
    /**
     * Return a CachedRowSet of the specified sql query.
     */
    public static CachedRowSet selectQuery(String sql) throws SQLException {
        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);

        // Transfer the ResultSet data to a CachedRowSet
        CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
        cachedRowSet.populate(resultSet);

        // Close connection
        resultSet.close();
        statement.close();
        connection.close();

        return cachedRowSet;
    }

    /**
     * Return the first row column of the specified sql query
     */
    public static int selectFirstIntColumn(String sql) throws SQLException {
        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);

        resultSet.next();
        int column = resultSet.getInt(1);

        resultSet.close();
        statement.close();
        connection.close();

        return column;
    }

    /**
     * Return the name of the specified profession id
     */
    public static String findProfessionName(int id) throws SQLException {
        CachedRowSet professions = selectQuery(String.format("SELECT name FROM \"Professions\" WHERE id = '%d'", id));
        professions.next();
        return professions.getString("name");
    }

    /**
     * Return the id of the specified profession name
     */
    public static int findProfessionId(String name) throws SQLException {
        CachedRowSet professions = selectQuery(String.format("SELECT id FROM \"Professions\" WHERE name = '%s'", name));
        professions.next();
        return professions.getInt("id");
    }

    /**
     * Return the name of the specified year id
     */
    public static String findYearName(int id) throws SQLException {
        CachedRowSet years = selectQuery(String.format("SELECT name FROM \"Years\" WHERE id = '%d'", id));
        years.next();
        return years.getString("name");
    }

    /**
     * Return the id of the specified year name
     */
    public static int findYearId(String name) throws SQLException {
        CachedRowSet years = selectQuery(String.format("SELECT id FROM \"Years\" WHERE name = '%s'", name));
        years.next();
        return years.getInt("id");
    }

    /**
     * Return the id of the first inserted row
     */
    public static int getInsertedRowId(ResultSet resultSet) throws SQLException {
        resultSet.next();
        int insertedId = resultSet.getInt(1);
        resultSet.close();

        return insertedId;
    }
}
