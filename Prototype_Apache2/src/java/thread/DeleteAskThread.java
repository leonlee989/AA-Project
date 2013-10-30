/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.Ask;
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
public class DeleteAskThread implements Runnable {
    private Ask ask;
    
    public DeleteAskThread (Ask ask){
        this.ask = ask;
    }

    public void run() {
        try {
          CallableStatement cs = StoredProcedure.connection.prepareCall("{call DELETE_ASK(?,?,?,?)}");
          cs.setString(1, ask.getStock());
          cs.setInt(2, ask.getPrice());
          cs.setString(3, ask.getUserId());
          cs.setTimestamp(4, new Timestamp(ask.getDate().getTime()));
          cs.executeQuery();
      }catch(SQLException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
}
