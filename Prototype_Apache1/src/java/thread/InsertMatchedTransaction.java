/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.Ask;
import aa.Bid;
import aa.DbBean;
import aa.ExchangeBean;
import aa.MatchedTransaction;
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
public class InsertMatchedTransaction implements Runnable {
    private MatchedTransaction m;
    
    public InsertMatchedTransaction (MatchedTransaction m) {
        this.m = m;
    }

    public void run() {
        Ask ask = m.getAsk();
        Bid bid = m.getBid();
        
        CallableStatement cs = null;
        Connection cn = null;
        
        try{
            cn = DbBean.getDbConnection();
            cs = cn.prepareCall("{call INSERT_MATCHED_TRANSACTION(?,?,?,?,?,?,?,?,?,?,?)}");
            cs.setInt(1, bid.getPrice());
            cs.setString(2, bid.getUserId());
            cs.setTimestamp(3, new Timestamp(bid.getDate().getTime()));
            cs.setInt(4, ask.getPrice());
            cs.setString(5, ask.getUserId());
            cs.setTimestamp(6, new Timestamp(ask.getDate().getTime()));
            cs.setTimestamp(7,new Timestamp(m.getDate().getTime()));
            cs.setInt(8, m.getPrice());
            cs.setString(9, m.getStock());
            cs.setInt(10, ask.getID());
            cs.setInt(11, bid.getID());
            cs.executeQuery();
        }catch(SQLException ex){
            Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
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
