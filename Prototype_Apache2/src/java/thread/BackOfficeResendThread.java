/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.DbBean;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Melvrick
 */
public class BackOfficeResendThread implements Callable<Boolean>{
    private ArrayList<String> resendLogs;
    
    public BackOfficeResendThread (ArrayList<String> logs){
        resendLogs = logs;
    }
    
    public Boolean call() throws Exception {
        // create new instances of remote Service objects
        aa.Service service = new aa.Service();
        aa.ServiceSoap port = service.getServiceSoap();

        // invoke the remote method by calling port.processTransaction().
        // processTransaction() will return false if the teamID &/or password is wrong
        // it will return true if the web service is correctly called        
        for (String message : resendLogs){
            try{
                if(!port.processTransaction("G3T7", "lime", message)){//if processed unsuccessfully
                    return false;
                }else{
                    removeFromBackOfficeLog(message);
                }
            }catch(Exception e){//error in logging it
                return false;
            }
            
        }
        return true;
    }
    
    private void removeFromBackOfficeLog(String logMessage){
        Connection cn = null;
        CallableStatement cs = null;
        try {
            cn = DbBean.getDbConnection();
            cs = cn.prepareCall("{call DELETE_BACKOFFICELOG(?)}");
            cs.setString(1,logMessage);
            cs.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(BackOfficeThread.class.getName()).log(Level.SEVERE, null, ex);
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
