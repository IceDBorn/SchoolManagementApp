package controllers;

import models.Database;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;

public class databaseController {
    /**
     * Returns a CachedRowSet of the specified sql query.
     */
    public static CachedRowSet selectQuery(String sql) throws SQLException {
        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);

        // Transfer the ResultSet data to a CachedRowSet
        CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
        cachedRowSet.populate(resultSet);

        resultSet.close();
        statement.close();
        connection.close();

        return cachedRowSet;
    }

    /**
     * Returns the very first row id of the specified sql query
     */
    public static int selectFirstId(String sql) throws SQLException {
        Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);

        resultSet.next();
        int id = resultSet.getInt("id");

        resultSet.close();
        statement.close();
        connection.close();

        return id;
    }

    /**
     * Returns the name of the specified profession id
     */
    public static String findProfessionName(int id) throws SQLException {
        CachedRowSet professions = selectQuery(String.format("SELECT name FROM \"Professions\" WHERE id = '%d'", id));
        professions.next();
        return professions.getString("name");
    }

    /**
     * Returns the id of the specified profession name
     */
    public static int findProfessionId(String name) throws SQLException {
        CachedRowSet professions = selectQuery(String.format("SELECT id FROM \"Professions\" WHERE name = '%s'", name));
        professions.next();
        return professions.getInt("id");
    }

    /**
     * Returns the name of the specified year id
     */
    public static String findYearName(int id) throws SQLException {
        CachedRowSet years = selectQuery(String.format("SELECT name FROM \"Years\" WHERE id = '%d'", id));
        years.next();
        return years.getString("name");
    }

    /**
     * Returns the id of the specified year name
     */
    public static int findYearId(String name) throws SQLException {
        CachedRowSet years = selectQuery(String.format("SELECT id FROM \"Years\" WHERE name = '%s'", name));
        years.next();
        return years.getInt("id");
    }

    /**
     * Returns the id of the first inserted row
     */
    public static int getInsertedRowId(ResultSet resultSet) throws SQLException {
        resultSet.next();
        int insertedId = resultSet.getInt("id");
        resultSet.close();

        return insertedId;
    }
}
