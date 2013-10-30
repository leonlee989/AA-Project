/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.ExchangeBean;
import aa.MatchedTransaction;
import aa.StoredProcedure;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Melvrick
 */
public class MatchedTransactionLogger implements Runnable{
    private ArrayList<MatchedTransaction> transactions;
    private String fileName;
    
    public MatchedTransactionLogger (ArrayList<MatchedTransaction> transactions, String fileName) {
        this.transactions = transactions;
        this.fileName = fileName;
    }
    
    public void run() {
        String transactionMessage = "";
      try {
        File matchedFile = new File(fileName);
        
        File parent = matchedFile.getParentFile();
      if (!parent.exists() && !parent.mkdirs()){
          //Take care of no path
          throw new IllegalStateException("ExchangeBean: Couldn't create directory: " + parent);
      }
      BufferedWriter out = new BufferedWriter(new FileWriter(matchedFile,true));
      for (MatchedTransaction m : transactions) {
        String message = m.toString();
        insertMatchedLog(message);
        transactionMessage += message + "\n";
      }
      out.write(transactionMessage);
      out.newLine();
      out.close();
    }catch (IOException e) {
      // Think about what should happen here...
      logRelativeFilePath(fileName.split("\\")[fileName.length()-1],transactionMessage);
    } catch (Exception e) {
      // Think about what should happen here... what is this???
      e.printStackTrace();
    }
  }
    
   private void insertMatchedLog(String logStatement){
      try{
          CallableStatement cs = StoredProcedure.connection.prepareCall("{call INSERT_MATCHED_LOG(?)}");
          cs.setString(1, logStatement);
          cs.executeQuery();
      }catch(SQLException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
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
}
