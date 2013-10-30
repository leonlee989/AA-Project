/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import java.util.concurrent.Callable;

/**
 *
 * @author Melvrick
 */
public class BackOfficeThread implements Callable<Boolean>{
    private String des;
    
    public BackOfficeThread(String txnDescription){
        des = txnDescription;
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
        return status;
      }
      catch (Exception ex) {
          // may come here if a time out or any other exception occurs
          // what should you do here??
      }
      return false; // failure due to exception
    }
    
}
