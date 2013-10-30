/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.Bid;
import aa.ExchangeBean;
import aa.StoredProcedure;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Melvrick
 */
public class DeleteBidThread implements Runnable{
    private Bid bid;
    
    public DeleteBidThread (Bid bid){
        this.bid = bid;
    }

    public void run() {
        try {
          CallableStatement cs = StoredProcedure.connection.prepareCall("{call DELETE_BID(?,?,?,?)}");
          cs.setString(1, bid.getStock());
          cs.setInt(2, bid.getPrice());
          cs.setString(3, bid.getUserId());
          cs.setTimestamp(4, new Timestamp(bid.getDate().getTime()));
          cs.executeQuery();
      }catch(SQLException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
}
