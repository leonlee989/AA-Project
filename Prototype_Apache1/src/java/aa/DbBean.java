/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author danisg
 */
public class DbBean {

    // change the dbURL if necessary.
    private static String dbDriver = "com.mysql.jdbc.Driver";
    private static Connection dbConnection;
    static String dbURL = null;
    static String dbUser = null;
    static String dbPassword = null;
    //Read JDBC parameters from web.xml
    
    private static boolean connect() throws ClassNotFoundException, SQLException, NamingException {

        if (dbConnection == null || dbConnection.isClosed()) {
            
            // connects to the database using root. change your database id/password here if necessary    
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            dbURL = (String) env.lookup("dbURL");
            dbUser = (String) env.lookup("dbUser");
            dbPassword = (String) env.lookup("dbPassword");

        
            Class.forName(dbDriver);
            // login credentials to your MySQL server
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
            return true;
        } else {
            return true;
        }

    }
    /*
     * Execute the given sql and returns the resultset.
     */

    public static ResultSet executeSql(String sql) throws ClassNotFoundException, SQLException, NamingException {
        //check the connection
        if (!connect()) {
            return null;
        }

        //execute sql.
        Statement dbStatement = dbConnection.createStatement();
        return dbStatement.executeQuery(sql);
    }
    /*
     * Execute the update statement and returns the # of affected rows.
     */

    public static int executeUpdate(String sql) throws SQLException, ClassNotFoundException, NamingException {
        //check the connection.
        if (!connect()) {
            return 0;
        }

        //execute the update.
        Statement dbStatement = dbConnection.createStatement();
        return dbStatement.executeUpdate(sql);
    }
    /*
     * Close the connection.
     */

    public static void close() throws SQLException {
        if (dbConnection != null && !dbConnection.isClosed()) {
            dbConnection.close();
        }
    }
}