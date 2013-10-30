/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.Ask;
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
public class LowestAskThread implements Callable<Ask> {
    private String stockName;
    
    public LowestAskThread (String name) {
        this.stockName = name;
    }
    
    public Ask call() throws Exception{
        try {
            CallableStatement cs = StoredProcedure.connection.prepareCall("{call GET_LOWEST_ASK(?)}");
            cs.setString(1, stockName);
            ResultSet rs = cs.executeQuery();
            if (rs == null){
              return null;
            }
            while (rs.next()){
              String stockName = rs.getString("stockName");
              int price = rs.getInt("price");
              String id = rs.getString("userID");
              Date askDate = rs.getTimestamp("askDate");
              return new Ask(stockName,price,id,askDate);
            }
        }catch(SQLException ex){
            Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
        }
      return null;
    }
    
}
