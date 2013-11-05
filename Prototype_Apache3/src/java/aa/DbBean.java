/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
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
    static String dbURL = "jdbc:mysql://localhost:3306/exchangemel";
    static String dbUser = "root";
    static String dbPassword = "root";
    //Read JDBC parameters from web.xml

    public static boolean connect() throws ClassNotFoundException, SQLException, NamingException {
        if (datasource == null) {
            // connects to the database using root. change your database id/password here if necessary    
            InitialContext cxt = new InitialContext();
            Context env = (Context) cxt.lookup("java:comp/env");
            datasource = (DataSource) cxt.lookup("java:comp/env/jdbc/ExchangeDB");
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
     * Execute the update statement and returns the # of affected rows.
     */

    public static int executeUpdate(String sql) throws SQLException, ClassNotFoundException, NamingException {
        //check the connection.
        if (!connect()) {
            return 0;
        }
        Connection cn = null;
        Statement stmt = null;
        int rs = 0;
        //Savepoint sp = null;
        try {
            cn = datasource.getConnection();
            //cn.setAutoCommit(false);
            //sp = cn.setSavepoint();
            stmt = cn.createStatement();
            rs = stmt.executeUpdate(sql);
            //cn.commit();
            //cn.setAutoCommit(true);
        } catch (SQLException e) {
            Logger.getLogger(DbBean.class.getName()).log(Level.SEVERE, null, e + "Error at the DbBean execute update");
            //if (cn!=null){
            //    cn.rollback();
            //    cn.releaseSavepoint(sp);
            //    cn.setAutoCommit(true);
            //}
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    Logger.getLogger(DbBean.class.getName()).log(Level.SEVERE, null, e);
                }
                stmt = null;
            }
            if (cn != null) {
                try {
                    cn.close();
                } catch (SQLException e) {
                    Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
                }
                cn = null;
            }
            return rs;
        }
    }

    public static Connection getDbConnection() {
        try {
            return datasource.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DbBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}