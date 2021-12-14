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
     * Returns the id of the first inserted row
     */
    public static int getInsertedRowId(ResultSet resultSet) throws SQLException {
        resultSet.next();
        int insertedId = resultSet.getInt("id");
        resultSet.close();

        return insertedId;
    }
}
