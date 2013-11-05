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
        CallableStatement cs = null;
        Connection cn = null;
        ResultSet rs = null;
        try {
            cn = DbBean.getDbConnection();
            cs = cn.prepareCall("{call GET_LOWEST_ASK(?)}");
            cs.setString(1, stockName);
            rs = cs.executeQuery();
            if (rs == null){
              return null;
            }
            while (rs.next()){
              int askID = rs.getInt("id");
              String stockName = rs.getString("stockName");
              int price = rs.getInt("price");
              String id = rs.getString("userID");
              Date askDate = rs.getTimestamp("askDate");
              return new Ask(askID,stockName,price,id,askDate);
            }
        }catch(SQLException ex){
            Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if (rs!=null){
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
      return null;
    }
    
}
