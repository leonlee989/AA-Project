/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author danisg
 */
public class DbBean {

    // change the dbURL if necessary.
    private static String dbDriver = "com.mysql.jdbc.Driver";
    private static DataSource datasource;
    static String dbURL = "jdbc:mysql://localhost:3306/exchange";
    static String dbUser = "root";
    static String dbPassword = "root";
    //Read JDBC parameters from web.xml
    
    public static boolean connect() throws ClassNotFoundException, SQLException, NamingException {
        if (datasource == null) {
            // connects to the database using root. change your database id/password here if necessary    
            InitialContext cxt = new InitialContext();
            Context env = (Context) cxt.lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/ExchangeDB");
            dbURL = (String) env.lookup("dbURL");
            dbUser = (String) env.lookup("dbUser");
            dbPassword = (String) env.lookup("dbPassword");

        
            Class.forName(dbDriver);
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
        Connection connection = datasource.getConnection();
        //execute sql.
        Statement dbStatement = connection.createStatement();
        ResultSet rs = dbStatement.executeQuery(sql);
        
        if (dbStatement != null) try { dbStatement.close(); } catch (SQLException ignore) {}
        if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
        
        return rs;
    }
    
    public static ResultSet executeSql(PreparedStatement stmt){
        try {
            ResultSet rs = stmt.executeQuery();
            if (stmt != null) try { stmt.close(); } catch (SQLException ignore) {}
            
            return rs;
        } catch (SQLException ex) {
            Logger.getLogger(DbBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
        Connection dbConnection = datasource.getConnection();
        Statement dbStatement = dbConnection.createStatement();
        return dbStatement.executeUpdate(sql);
    }
    
    public static Connection getDbConnection(){
        try {
            return datasource.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DbBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}