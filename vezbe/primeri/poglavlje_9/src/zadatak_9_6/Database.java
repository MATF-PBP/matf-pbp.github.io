package zadatak_9_6;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

/**
 * Database is the abstract base class for all database contexts which allow an
 * application to perform basic operations on a database, using the JDBC API.
 * 
 * In order to use this class, for each database, there needs to exists an
 * inherited class from this class. The inherited class needs to set the
 * following protected members in its constructor method: <code>dbName</code>,
 * <code>url</code>, <code>username</code>, <code>password</code>.
 */
public abstract class Database implements AutoCloseable {
    /**
     * The object which is used for keeping the resources used in a connection
     * to a database.
     */
    protected Connection con;

    /**
     * The name of the database used for printing purposes.
     */
    protected String dbName = null;
    /**
     * The Type 4 JDBC URL used for connecting to a database.
     */
    protected String url = null;
    /**
     * The user which is being used for connecting to a database and executing
     * the statements.
     */
    protected String username = null;
    /**
     * The password for the specified user.
     */
    protected String password = null;

    /**
     * The helper function for getting the name of a database.
     * 
     * @return the name of a database
     */
    protected String getName() {
        return dbName;
    }

    /**
     * The helper function for getting the URL of a database.
     * 
     * @return the URL of a database
     */
    protected String getUrl() {
        return url;
    }

    /**
     * The helper function for getting the username of a database.
     * 
     * @return the username of a database
     */
    protected String getUsername() {
        return username;
    }

    /**
     * The helper function for getting the password of a database user.
     * 
     * @return the password of a database user
     */
    protected String getPassword() {
        return password;
    }

    /**
     * Connects to a database using the information stored in the following
     * protected members: <code>url</code>, <code>username</code>,
     * <code>password</code>. These members must be set in the constructor
     * method of inherited classes.
     * 
     * @throws SQLException
     *             If an SQL error occured while connecting to a database
     */
    public void connect() throws SQLException {
        if (null == con) {
            System.out.println("Povezivanje na " + getName() + "...");
            con = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
            con.setAutoCommit(false);
            System.out.println("Uspesno je ostvarena konekcija!");
        }
    }

    /**
     * Disconnects from a previously connected database. Does nothing if the
     * connection does not exists.
     * 
     * @param successful
     *            whether to commit or rollback potential changes made to the
     *            database
     * @throws SQLException
     *             If an SQL error occured while disconnecting from a database
     */
    public void disconnect(boolean successful) throws SQLException {
        if (null != con) {
            if (successful) {
                commit();
            } else {
                rollback();
            }

            con.close();
            con = null;

            System.out.println("Diskonektovano sa " + getName() + "!");
        }
    }

    /**
     * Commits the potential changes made to the database. Ignores any SQL
     * errors that might occur.
     */
    public void commit() {
        try {
            if (null != con) {
                con.commit();
            }

            System.out.println("Sve izmene su pohranjene u bazi podataka " + getName());
        } catch (SQLException e) {
        }
    }

    /**
     * Rolls back the potential changes made to the database. Ignores any SQL
     * errors that might occur.
     */
    public void rollback() {
        try {
            if (null != con) {
                con.rollback();
            }
            System.out.println("Sve izmene su ponistene u bazi podataka " + getName());
        } catch (SQLException e) {
        }
    }

    /**
     * Reads the SQL statement from the file located at the given pathname
     * <code>filename</code>.
     * 
     * @param filename
     * @return
     * @throws IOException
     */
    protected static String readSQLFromFile(String filename) throws IOException {
        StringBuilder sql = new StringBuilder();
        Files.lines(Paths.get(filename)).forEach(linija -> sql.append(linija).append("\n"));
        return sql.toString();
    }

    /**
     * Closes the connection.
     */
    @Override
    public void close() throws Exception {
        if (null != con) {
            con.close();
        }
    }

}
