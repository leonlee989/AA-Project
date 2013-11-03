/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.DbBean;
import aa.ExchangeBean;
import aa.MatchedTransaction;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.CallableStatement;
import java.sql.Connection;
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
      //IO
      //BufferedWriter out = new BufferedWriter(new FileWriter(matchedFile,true));
      
      //NIO
      FileChannel outChannel = new FileOutputStream(matchedFile,true).getChannel();
      byte[] byteArray = ("stock: smu, amt: 10, bidID: 0, bidder userId: ijiji, askID: 0, seller userId: ijiji, date: date: Tue Oct 29 03:50:49 GMT+08:00 2013" + System.getProperty("line.separator").toString()).getBytes();
      ByteBuffer buffer = ByteBuffer.allocate(byteArray.length);
      
      
      
      for (MatchedTransaction m : transactions) {
        String message = m.toString();
        insertMatchedLog(message);
        //transactionMessage += message + "\n";
        try{
            buffer.put((message).getBytes());
            buffer.put(System.getProperty("line.separator").toString().getBytes());
            buffer.flip();
            outChannel.write(buffer);
            buffer.clear();
        }catch(IOException e){
            System.out.println("IO EXCEPTION: Cannot write to file");
	    e.printStackTrace();
        }
      }
      //NIO Close
      outChannel.close();
      //IO Close
      //out.write(transactionMessage);
      //out.newLine();
      //out.close();
    }catch (IOException e) {
      // Think about what should happen here...
      logRelativeFilePath(fileName.split("\\")[fileName.length()-1],transactionMessage);
    } catch (Exception e) {
      // Think about what should happen here... what is this???
      e.printStackTrace();
    }
  }
    
   private void insertMatchedLog(String logStatement){
      CallableStatement cs = null;
      Connection cn = null;       
       try{
          cn = DbBean.getDbConnection();
          cs = cn.prepareCall("{call INSERT_MATCHED_LOG(?)}");
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
