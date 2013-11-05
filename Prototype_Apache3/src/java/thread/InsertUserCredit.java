/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.DbBean;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Melvrick
 */
public class InsertUserCredit implements Runnable {
    private String userID;
    private int creditLimit;
    
    public InsertUserCredit (String userID, int creditLimit){
        this.userID = userID;
        this.creditLimit = creditLimit;
    }
    
    public void run() {
        CallableStatement cs = null;
        Connection cn = null;
        try {
            cn = DbBean.getDbConnection();
            cs = cn.prepareCall("{call INSERT_USER_CREDIT(?,?)}");
            cs.setString(1, userID);
            cs.setInt(2, creditLimit);
            cs.executeQuery();
        }  catch (MySQLIntegrityConstraintViolationException ex) {
            System.out.println("InsertUserCredit > existing attempt to insert > screw it" + ex);
        } catch (SQLException ex) {
            Logger.getLogger(InsertUserCredit.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (cs!=null){
                try { cs.close(); } catch (SQLException e) { ; }
                cs = null;
            }
            
            if (cn!=null){
                try { cn.close(); } catch (SQLException e) { ; }
                cn = null;
            }
        }
    }
    
}
