/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.DbBean;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Melvrick
 */
public class UpdateCreditThread implements Runnable {
    private int credit;
    private String username;
    
    public UpdateCreditThread (int credit, String username){
        this.credit = credit;
        this.username = username;
    }

    public void run() {
        Connection cn = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            cn = DbBean.getDbConnection();
            cn.setAutoCommit(false);
            
            cs = cn.prepareCall("{call UPDATE_CREDIT_LIMIT(?,?)}");
            cs.setInt(1, credit);
            cs.setString(2,username);
            cs.executeUpdate();

            cn.commit();
            cn.setAutoCommit(true);
        } catch (SQLException ex) {
            Logger.getLogger(UpdateCreditThread.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if (rs != null){
                try { rs.close(); } catch (SQLException e) { ; }
                rs = null;
            }
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
