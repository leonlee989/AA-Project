/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.Bid;
import aa.ExchangeBean;
import aa.StoredProcedure;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Melvrick
 */
public class HighestBidThread implements Callable<Bid> {
    private String bidStockName;
    
    public HighestBidThread (String name) {
        this.bidStockName = name;
    }

    public Bid call() throws Exception {
        try {
          CallableStatement cs = StoredProcedure.connection.prepareCall("{call GET_HIGHEST_BID(?)}");
          cs.setString(1, bidStockName);
          ResultSet rs = cs.executeQuery();
          if (rs == null){
            return null;
          }
          while (rs.next()){
              String stockName = rs.getString("stockName");
              int price = rs.getInt("price");
              String id = rs.getString("userID");
              Date bidDate = rs.getTimestamp("bidDate");
              return new Bid(stockName,price,id,bidDate);
          }
      }catch(SQLException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
        return null;
    }
    
    
}
