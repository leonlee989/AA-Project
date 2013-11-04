/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.Bid;
import aa.DbBean;
import aa.ExchangeBean;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Melvrick
 */
public class InsertBidThread implements Runnable {
    private Bid bid;
    private Connection cn;
    private Savepoint sp;
    
    public InsertBidThread (Bid bid){
        this.bid = bid;
    }
    
    public InsertBidThread (Bid bid, Connection cn, Savepoint sp){
        this.bid = bid;
        this.cn = cn;
        this.sp = sp;
    }

    public void run() {
        CallableStatement cs = null;
        int id = -1;
        try{
          if (cn == null){
              cn = DbBean.getDbConnection();
          }
          cn.setAutoCommit(false);
          cs = cn.prepareCall("{call INSERT_BID(?,?,?,?)}");
          cs.setString(1, bid.getStock());
          cs.setInt(2, bid.getPrice());
          cs.setString(3, bid.getUserId());
          cs.setTimestamp(4, new Timestamp(bid.getDate().getTime()));
          cs.executeQuery();
          cn.commit();
          id = getLastID(cn,cs);
          bid.setID(id);
      }catch(SQLException e){
          if (sp==null){
              try {cn.rollback();} catch (SQLException ex) {Logger.getLogger(InsertBidThread.class.getName()).log(Level.SEVERE, null, ex);}
          }else{
              try {cn.rollback(sp);} catch (SQLException ex) {Logger.getLogger(InsertBidThread.class.getName()).log(Level.SEVERE, null, ex);}
          }
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
      }finally{
            if (sp == null){
                close(null,cs,cn);
            }else{
                try {
                    cn.setAutoCommit(true);
                    close(null,cs,null);
                } catch (SQLException ex) {
                    Logger.getLogger(InsertBidThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
      }
    }
    
    private int getLastID(Connection cn, CallableStatement cs){
        ResultSet rs = null;
        int id = -1;
        try{
            cs = cn.prepareCall("{call LAST_ID_FROM_CLIENT}");
            rs = cs.executeQuery();
            if (rs.next()){
                id = rs.getInt("last_insert_id()");
            }
        }catch(SQLException e){
            Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
        }finally{
            close(rs,null,null);
        }
        return id;
    }

    private void close(ResultSet rs, CallableStatement cs, Connection cn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
            }
            rs = null;
        }
        if (cs != null) {
            try {
                cs.close();
            } catch (SQLException e) {
                Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
            }
            cs = null;
        }
        if (cn != null) {
            try {
                cn.close();
            } catch (SQLException e) {
                Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
            }
            cn = null;
        }
    }
}
