/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.Bid;
import aa.DbBean;
import aa.ExchangeBean;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Melvrick
 */
public class RejectedTransactionLogger implements Runnable {
    private Bid bid;
    private String fileName;
    
    public RejectedTransactionLogger (Bid bid, String fileName) {
        this.bid = bid;
        this.fileName = fileName;
    }

    public void run() {
        String bidMessage = bid.toString();
        try {
          File rejectedLogFile = new File(fileName);
          File parent = rejectedLogFile.getParentFile();
          if (!parent.exists() && !parent.mkdirs()){
              //Take care of no path
              throw new IllegalStateException("ExchangeBean: Couldn't create directory: " + parent);
          }
          insertRejectedLog(bidMessage);
          BufferedWriter out = new BufferedWriter(new FileWriter(rejectedLogFile,true));
          out.write(bidMessage);
          out.newLine();
          out.flush();
          out.close();
        } catch (IOException e) {
          // If Java has no admin rights n cannot write to hard-disk temp files
          logRelativeFilePath(fileName.split("\\")[fileName.length()-1],bid.toString()+"\n");
          e.printStackTrace();
        } catch (Exception e) {
          // Think about what should happen here...
          e.printStackTrace();
        }
    }
    
    private void logRelativeFilePath(String fileName, String stringMessage) {
      try {
          ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
          URL url = classLoader.getResource(fileName);
          File file = new File(url.toURI());
          if (!file.exists()){
              file.createNewFile();
          }
          PrintWriter outFile = new PrintWriter(new FileWriter(file,true));
          outFile.append(stringMessage);
          outFile.close();
      } catch (URISyntaxException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException e){e.printStackTrace();}
    }
    
    private void insertRejectedLog(String logStatement){
        CallableStatement cs = null;
        Connection cn = null;
        try{
            cn = DbBean.getDbConnection();
            cs = cn.prepareCall("{call INSERT_REJECTED_LOG(?)}");
            cs.setString(1, logStatement);
            cs.executeQuery();
        }catch(SQLException e){
            Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
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
