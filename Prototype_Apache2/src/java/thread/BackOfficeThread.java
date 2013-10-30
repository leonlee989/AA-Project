/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.StoredProcedure;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Melvrick
 */
public class BackOfficeThread implements Callable<Boolean>{
    private String des;
    private ExecutorService executor;
    
    public BackOfficeThread(String txnDescription, ExecutorService executor){
        des = txnDescription;
        this.executor = executor;
    }
    
    public Boolean call() throws Exception{
        aa.Service service = new aa.Service();
      boolean status = false;
      
      try {
        // create new instances of remote Service objects
        aa.ServiceSoap port = service.getServiceSoap();

        // invoke the remote method by calling port.processTransaction().
        // processTransaction() will return false if the teamID &/or password is wrong
        // it will return true if the web service is correctly called
        status = port.processTransaction("G3T7", "lime", des);
        
        if (status && checkBackOfficeLogs()){
            //successful processing of transaction + backoffice has stuff
            BackOfficeResendThread bot = new BackOfficeResendThread(downloadBackOfficeLogs());
            Future<Boolean> result = executor.submit(bot);
            return result.get();
        }
        
        return status;
      }
      catch (Exception ex) {
          // may come here if a time out or any other exception occurs
          // what should you do here??
          logRejectedBackOfficeTransactions(des);
      }
      return false; // failure due to exception
    }
    
    private void logRejectedBackOfficeTransactions(String txnDescription){
        try {
            CallableStatement cs = StoredProcedure.connection.prepareCall("{call INSERT_BACKOFFICE_LOG(?)}");
            cs.setString(1,txnDescription);
            cs.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(BackOfficeThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean checkBackOfficeLogs(){
        try {
            CallableStatement cs = StoredProcedure.connection.prepareCall("{call CHECK_IF_BACKOFFICEMESSAGE_EXISTS}");
            ResultSet rs = cs.executeQuery();
            if (rs!=null){
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(BackOfficeThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private ArrayList<String> downloadBackOfficeLogs(){
        ArrayList<String> logs = new ArrayList<String>();
        try {
            CallableStatement cs = StoredProcedure.connection.prepareCall("{call DUMP_FROM_BACKOFFICE}");
            ResultSet rs = cs.executeQuery();
            while(rs.next()){
                logs.add(rs.getString("logStatement"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(BackOfficeThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return logs;
    }
}
