package com.avairebot.orion.database.connections;

import com.avairebot.orion.contracts.database.StatementInterface;
import com.avairebot.orion.contracts.database.connections.HostnameDatabase;
import com.avairebot.orion.database.DatabaseManager;

import java.sql.*;

public class MySQL extends HostnameDatabase {

    /**
     * Creates a MySQL database connection instance with the parsed information, the port used will default to <code>3306</code>.
     *
     * @param dbm The database manager class instance.
     */
    public MySQL(DatabaseManager dbm) {
        super(
                dbm.getOrion().config.getDatabase().getHostname(),
                3306,
                dbm.getOrion().config.getDatabase().getDatabase(),
                dbm.getOrion().config.getDatabase().getUsername(),
                dbm.getOrion().config.getDatabase().getPassword()
        );

        setDatabaseManager(dbm);
    }

    @Override
    protected boolean initialize() {
        try {
            Class.forName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");

            return true;
        } catch (ClassNotFoundException ex) {
            dbm.getOrion().logger.fatal("MySQL DataSource class missing.");
            dbm.getOrion().logger.fatal(ex);
        }

        return false;
    }

    @Override
    public boolean open() throws SQLException {
        try {
            String url = String.format("jdbc:mysql://%s:%d/%s", getHostname(), getPort(), getDatabase());

            if (initialize()) {
                connection = DriverManager.getConnection(url, getUsername(), getPassword());

                return true;
            }
        } catch (SQLException ex) {
            String reason = "Could not establish a MySQL connection, SQLException: " + ex.getMessage();

            dbm.getOrion().logger.fatal(reason);
            dbm.getOrion().logger.fatal(ex);
            throw new SQLException(reason);
        }

        return false;
    }

    @Override
    protected void queryValidation(StatementInterface statement) throws SQLException {
        SQLException exception;

        switch ((MySQLStatement) statement) {
            case USE:
                exception = new SQLException("Please create a new connection to use a different database.");

                dbm.getOrion().logger.fatal("Please create a new connection to use a different database.");
                dbm.getOrion().logger.fatal(exception);
                throw exception;

            case PREPARE:
            case EXECUTE:
            case DEALLOCATE:
                exception = new SQLException("Please use the prepare() method to prepare a query.");

                dbm.getOrion().logger.fatal("Please use the prepare() method to prepare a query.");
                dbm.getOrion().logger.fatal(exception);

                throw exception;
        }
    }

    @Override
    public StatementInterface getStatement(String query) throws SQLException {
        String[] statement = query.trim().split(" ", 2);

        try {
            return MySQLStatement.valueOf(statement[0].toUpperCase());
        } catch (IllegalArgumentException ex) {
            dbm.getOrion().logger.fatal(String.format("Unknown statement: \"%s\"", statement[0]));
            dbm.getOrion().logger.fatal(ex);
        }

        return null;
    }

    @Override
    public boolean hasTable(String table) {
        try {
            DatabaseMetaData md = getConnection().getMetaData();

            try (ResultSet tables = md.getTables(null, null, table, new String[]{"TABLE"})) {
                if (tables.next()) {
                    tables.close();

                    return true;
                }
            }
        } catch (SQLException ex) {
            dbm.getOrion().logger.fatal(String.format("Failed to check if table exists \"%s\": %s", table, ex.getMessage()));
            dbm.getOrion().logger.fatal(ex);
        }

        return false;
    }

    @Override
    public boolean truncate(String table) {
        try {
            if (!hasTable(table)) {
                return false;
            }

            try (Statement statement = getConnection().createStatement()) {
                statement.executeUpdate(String.format("DELETE FROM `%s`;", table));
            }

            return true;
        } catch (SQLException ex) {
            dbm.getOrion().logger.fatal(String.format("Failed to truncate \"%s\": %s", table, ex.getMessage()));
            dbm.getOrion().logger.fatal(ex);
        }

        return false;
    }
}
