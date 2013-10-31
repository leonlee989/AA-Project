/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.Ask;
import aa.DbBean;
import aa.ExchangeBean;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Melvrick
 */
public class InsertAskThread implements Runnable {
    private Ask ask;
    
    public InsertAskThread (Ask ask){
        this.ask = ask;
    }

    public void run() {
        Connection cn = null;
        CallableStatement cs = null;
        try{
            cn = DbBean.getDbConnection();
            cs = cn.prepareCall("{call INSERT_ASK(?,?,?,?)}");
            cs.setString(1, ask.getStock());
            cs.setInt(2, ask.getPrice());
            cs.setString(3, ask.getUserId());
            cs.setTimestamp(4, new Timestamp(ask.getDate().getTime()));
            cs.executeQuery();
        }catch(SQLException e){
            Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
        }finally{
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
