package aa;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
 
public class ExchangeBean {

  // location of log files - change if necessary
  private final String MATCH_LOG_FILE = "c:\\temp\\matched.log";
  private final String REJECTED_BUY_ORDERS_LOG_FILE = "c:\\temp\\rejected.log";

  // used to calculate remaining credit available for buyers
  private final int DAILY_CREDIT_LIMIT_FOR_BUYERS = 1000000;

  // keeps track of the remaining credit limits of each buyer. This should be
  // checked every time a buy order is submitted. Buy orders that breach the
  // credit limit should be rejected and logged
  // The key for this Hashtable is the user ID of the buyer, and the corresponding value is the REMAINING credit limit
  // the remaining credit limit should not go below 0 under any circumstance!
  // --- Credit is now stored in database. ----
  //private Hashtable <String, Integer> creditRemaining = new Hashtable<String, Integer>();

  private Connection connection = DbBean.getDbConnection();
  
  // this method is called once at the end of each trading day. It can be called manually, or by a timed daemon
  // this is a good chance to "clean up" everything to get ready for the next trading day
  public void endTradingDay() throws Exception{
    // reset attributes
    updateLatestPrice("smu",-1);
    updateLatestPrice("nus",-1);
    updateLatestPrice("ntu",-1);

    // dump all unfulfilled buy and sell orders from their respective tables
    clearTable("ask");
    clearTable("bid");
    // Reset the credit in database #SD#.
    clearTable("credit");
  }

  // returns a String of unfulfilled bids for a particular stock
  // returns an empty string if no such bid
  // bids are separated by <br> for display on HTML page
  public String getUnfulfilledBidsForDisplay(String stock) {
    ArrayList<Bid> allBids = getAllBids();
    String returnString = "";
    for (int i = 0; i < allBids.size(); i++) {
      Bid bid = allBids.get(i);
      if (bid.getStock().equals(stock)) {
        returnString += bid.toString() + "<br />";
      }
    }
    return returnString;
  }

  // returns a String of unfulfilled asks for a particular stock
  // returns an empty string if no such ask
  // asks are separated by <br> for display on HTML page
  
    public boolean sendToBackOffice(String txnDescription){
      aa.Service service = new aa.Service();
      boolean status = false;
      
      try {
        // create new instances of remote Service objects
        aa.ServiceSoap port = service.getServiceSoap();

        // invoke the remote method by calling port.processTransaction().
        // processTransaction() will return false if the teamID &/or password is wrong
        // it will return true if the web service is correctly called
        status = port.processTransaction("G3T7", "lime", txnDescription);
        return status;
      }
      catch (Exception ex) {
          // may come here if a time out or any other exception occurs
          // what should you do here??
      }
      return false; // failure due to exception
  }
    
  private void insertBid(Bid bid){
      String insertValueSQL = "INSERT INTO bid (stock,price,userID,bidDate) VALUES ('%s','%s','%s','%s')";
      try{
          String sqlStmt = String.format(insertValueSQL, bid.getStock(),bid.getPrice(),bid.getUserId(),new Timestamp(bid.getDate().getTime()));
          DbBean.executeUpdate(sqlStmt);      
      }catch(SQLException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
      }catch(ClassNotFoundException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
      }catch(NamingException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);      
      }
  }

  private void insertAsk(Ask ask){
      String insertValueSQL = "INSERT INTO ask (stock,price,userID,askDate) VALUES ('%s','%s','%s','%s')";
      try{
          String sqlStmt = String.format(insertValueSQL, ask.getStock(),ask.getPrice(),ask.getUserId(),new Timestamp(ask.getDate().getTime()));
          DbBean.executeUpdate(sqlStmt); 
      }catch(SQLException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
      }catch(ClassNotFoundException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
      }catch(NamingException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);      
      }
  }
  
  private void insertLog(String logTableName, String logStatement){
      String insertValueSQL = "INSERT INTO " + logTableName + " (logStatement) VALUES ('%s')";
      try{
          String sqlStmt = String.format(insertValueSQL,logStatement);
          DbBean.executeUpdate(sqlStmt); 
      }catch(SQLException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
      }catch(ClassNotFoundException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
      }catch(NamingException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);      
      }
  }
    
  public String getUnfulfilledAsks(String stock) {
    ArrayList<Ask> allAsks = getAllAsks();
    String returnString = "";
    for (int i = 0; i < allAsks.size(); i++) {
      Ask ask = allAsks.get(i);
      if (ask.getStock().equals(stock)) {
        returnString += ask.toString() + "<br />";
      }
    }
    return returnString;
  }

  // returns the highest bid for a particular stock
  // returns -1 if there is no bid at all
  public int getHighestBidPrice(String stock) {
    Bid highestBid = getHighestBid(stock);
    if (highestBid == null) {
      return -1;
    } else {
      return highestBid.getPrice();
    }
  }

  // retrieve unfulfiled current (highest) bid for a particular stock
  // returns null if there is no unfulfiled bid for this stock
  private Bid getHighestBid(String stock) {
      try {
          ResultSet rs = DbBean.executeSql(String.format("select * from bid WHERE stock = '%s' order by price DESC, bidDate ASC LIMIT 1", stock));
          if (rs == null){
            return null;
          }
          return new Bid(rs.getString("stock"),rs.getInt("price"),rs.getString("userID"),rs.getTimestamp("bidDate"));
      }catch(SQLException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }catch(ClassNotFoundException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
      }catch(NamingException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);      
      }
    return null;
  }

  // returns the lowest ask for a particular stock
  // returns -1 if there is no ask at all
  public int getLowestAskPrice(String stock) {
    Ask lowestAsk = getLowestAsk(stock);
    if (lowestAsk == null) {
      return -1;
    } else {
      return lowestAsk.getPrice();
    }
  }
  
  // retrieve unfulfiled current (lowest) ask for a particular stock
  // returns null if there is no unfulfiled asks or errors
  private Ask getLowestAsk(String stock) {
      try {
          ResultSet rs = DbBean.executeSql(String.format("select * from ask WHERE stock = '%s' order by price ASC, askDate ASC LIMIT 1",stock));
          if (rs == null){
            return null;
          }
          return new Ask(rs.getString("stock"),rs.getInt("price"),rs.getString("userID"),rs.getTimestamp("askDate"));
      }catch(SQLException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }catch(ClassNotFoundException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
      }catch(NamingException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);      
      }
    return null;
  }

  // get credit remaining for a particular buyer
  private int getCreditRemaining(String buyerUserId) throws ClassNotFoundException, SQLException, NamingException{
//    if (!(creditRemaining.containsKey(buyerUserId))){
//      // this buyer is not in the hash table yet. hence create a new entry for him
//      creditRemaining.put(buyerUserId, DAILY_CREDIT_LIMIT_FOR_BUYERS);
//    }
//    return creditRemaining.get(buyerUserId);
    
   //read the credit limit from database #SD#
   ResultSet rs = DbBean.executeSql(String.format("select credit_limit from credit where userid='%s'", buyerUserId));
    
    if (rs.next())
    {
        return rs.getInt("credit_limit");        
    }    
    else
    {
        DbBean.executeUpdate(
                String.format("insert into credit (userid,credit_limit) values('%s',%s)",
                buyerUserId, DAILY_CREDIT_LIMIT_FOR_BUYERS));
        
        return DAILY_CREDIT_LIMIT_FOR_BUYERS;
    }
    
  }

  // check if a buyer is eligible to place an order based on his credit limit
  // if he is eligible, this method adjusts his credit limit and returns true
  // if he is not eligible, this method logs the bid and returns false
  private boolean validateCreditLimit(Bid b) throws ClassNotFoundException, SQLException,NamingException{
    // calculate the total price of this bid
    int totalPriceOfBid = b.getPrice() * 1000; // each bid is for 1000 shares
    int remainingCredit = getCreditRemaining(b.getUserId());
    int newRemainingCredit = remainingCredit - totalPriceOfBid;

    if (newRemainingCredit < 0){
      // no go - log failed bid and return false
      logRejectedBuyOrder(b);
      return false;
    }
    else {
      // it's ok - adjust credit limit and return true
      //creditRemaining.put(b.getUserId(), newRemainingCredit);
      
     //Update the credit limit in the database. #SD#
      DbBean.executeUpdate(String.format("update credit set credit_limit=%s where userid='%s'", newRemainingCredit, b.getUserId()));
      
      return true;
    }
  }

  // call this to append all rejected buy orders to log file
  private void logRejectedBuyOrder(Bid b) {
      String bidMessage = b.toString() + "\n";
    try {
      File rejectedLogFile = new File(REJECTED_BUY_ORDERS_LOG_FILE);
      File parent = rejectedLogFile.getParentFile();
      if (!parent.exists() && !parent.mkdirs()){
          //Take care of no path
          throw new IllegalStateException("ExchangeBean: Couldn't create directory: " + parent);
      }
      PrintWriter outFile = new PrintWriter(rejectedLogFile);
      outFile.append(bidMessage);
      outFile.close();
    } catch (IOException e) {
      // If Java has no admin rights n cannot write to hard-disk temp files
      System.out.println("IO EXCEPTIOn: Cannot write to file");
      logRelativeFilePath(REJECTED_BUY_ORDERS_LOG_FILE.split("\\")[REJECTED_BUY_ORDERS_LOG_FILE.length()-1],b.toString());
      e.printStackTrace();
    } catch (Exception e) {
      // Think about what should happen here...
      System.out.println("EXCEPTION: Cannot write to file");
      e.printStackTrace();
    }
    insertLog("rejectedLog",bidMessage);
  }

  // call this to append all matched transactions in matchedTransactions to log file and clear matchedTransactions
  private void logMatchedTransactions() {
    
      try {
        File matchedFile = new File(MATCH_LOG_FILE);
        
        File parent = matchedFile.getParentFile();
      if (!parent.exists() && !parent.mkdirs()){
          //Take care of no path
          throw new IllegalStateException("ExchangeBean: Couldn't create directory: " + parent);
      }
        PrintWriter outFile = new PrintWriter(matchedFile);
      ArrayList<MatchedTransaction> transactions = new ArrayList<MatchedTransaction>();
      for (MatchedTransaction m : transactions) {
        String transactionMessage = m.toString();
        outFile.append(transactionMessage + "\n");
        insertLog("matchedLog",transactionMessage);
      }
      clearTable("matches"); // clean this out
      outFile.close();
    } catch (IOException e) {
      // Think about what should happen here...
      System.out.println("IO EXCEPTIOn: Cannot write to file");
      logRelativeFilePath(MATCH_LOG_FILE.split("\\")[MATCH_LOG_FILE.length()-1],"error to be handled");
      e.printStackTrace();
    } catch (Exception e) {
      // Think about what should happen here...
      System.out.println("EXCEPTION: Cannot write to file");
      e.printStackTrace();
    }
  }

  // returns a string of HTML table rows code containing the list of user IDs and their remaining credits
  // this method is used by viewOrders.jsp for debugging purposes
  public String getAllCreditRemainingForDisplay() throws Exception{
    String returnString = "";

//    Enumeration items = creditRemaining.keys();
//
//    while (items.hasMoreElements()){
//      String key = (String)items.nextElement();
//      int value = creditRemaining.get(key);
//      returnString += "<tr><td>" + key + "</td><td>" + value + "</td></tr>";
//    }
    
    //get the credit limit for all users from database. #SD#
    ResultSet rs = DbBean.executeSql("select * from credit");
    while(rs.next())
    {
        returnString += "<tr><td>" + rs.getString("userid") + "</td><td>" + rs.getInt("credit_limit") + "</td></tr>";        
    }
    return returnString;
  }

  // call this method immediatley when a new bid (buying order) comes in
  // this method returns false if this buy order has been rejected because of a credit limit breach
  // it returns true if the bid has been successfully added
  public boolean placeNewBidAndAttemptMatch(Bid newBid) throws Exception{
    ArrayList<Bid> allBids = getAllBids();
    ArrayList<Ask> allAsks = getAllAsks();
    // step 0: check if this bid is valid based on the buyer's credit limit
    boolean okToContinue = validateCreditLimit(newBid);
    if (!okToContinue){
      return false; 
    }

    // step 1: insert new bid into unfulfilledBids
    //INSERT IN RUN TIME ARRAYLIST
    allBids.add(newBid);

    // step 2: check if there is any unfulfilled asks (sell orders) for the new bid's stock. if not, just return
    // count keeps track of the number of unfulfilled asks for this stock
    //LOOP THROUGH ALL ASKS TO SEE IF CURRENT BID EXISTS
    int count = 0;
    for (int i = 0; i < allAsks.size(); i++) {
      if (allAsks.get(i).getStock().equals(newBid.getStock())) {
        count++;
      }
    }
    if (count == 0) {
      return true; // no unfulfilled asks of the same stock
    }

    // step 3: identify the current/highest bid in unfulfilledBids of the same stock
    //LOOK IN DB FOR CURRENT HIGHEST BID
    Bid highestBid = getHighestBid(newBid.getStock());

    // step 4: identify the current/lowest ask in unfulfilledAsks of the same stock
    //LOOK IN DB FOR CURRENT LOWEST ASK
    Ask lowestAsk = getLowestAsk(newBid.getStock());

    // step 5: check if there is a match.
    // A match happens if the highest bid is bigger or equal to the lowest ask
    if (highestBid.getPrice() >= lowestAsk.getPrice()) {
      // a match is found!
      deleteBid(highestBid);
      deleteAsk(lowestAsk);
      // this is a BUYING trade - the transaction happens at the higest bid's timestamp, and the transaction price happens at the lowest ask
      MatchedTransaction match = new MatchedTransaction(highestBid, lowestAsk, highestBid.getDate(), lowestAsk.getPrice());
      executeInsertMatchedTransaction(match);

      // to be included here: inform Back Office Server of match
      // to be done in v1.0

      updateLatestPrice(match);
      logMatchedTransactions();
    }

    return true; // this bid is acknowledged
  }

  // call this method immediatley when a new ask (selling order) comes in
  public void placeNewAskAndAttemptMatch(Ask newAsk) {
    ArrayList<Ask> allAsks = getAllAsks();
    ArrayList<Bid> allBids = getAllBids();
    // step 1: insert new ask into unfulfilledAsks
    allAsks.add(newAsk);

    // step 2: check if there is any unfulfilled bids (buy orders) for the new ask's stock. if not, just return
    // count keeps track of the number of unfulfilled bids for this stock
    int count = 0;
    for (int i = 0; i < allBids.size(); i++) {
      if (allBids.get(i).getStock().equals(newAsk.getStock())) {
        count++;
      }
    }
    if (count == 0) {
      return; // no unfulfilled asks of the same stock
    }

    // step 3: identify the current/highest bid in unfulfilledBids of the same stock
    Bid highestBid = getHighestBid(newAsk.getStock());

    // step 4: identify the current/lowest ask in unfulfilledAsks of the same stock
    Ask lowestAsk = getLowestAsk(newAsk.getStock());


    // step 5: check if there is a match.
    // A match happens if the lowest ask is <= highest bid
    if (lowestAsk.getPrice() <= highestBid.getPrice()) {
      // a match is found!
      allBids.remove(highestBid);
      allAsks.remove(lowestAsk);
      // this is a SELLING trade - the transaction happens at the lowest ask's timestamp, and the transaction price happens at the highest bid
      MatchedTransaction match = new MatchedTransaction(highestBid, lowestAsk, lowestAsk.getDate(), highestBid.getPrice());
      executeInsertMatchedTransaction(match);

      // to be included here: inform Back Office Server of match
      // to be done in v1.0

      updateLatestPrice(match);
      logMatchedTransactions();
    }
  }

  public ArrayList<Bid> getAllBids(){
      try {
          ArrayList<Bid> bids = new ArrayList<Bid>();
          ResultSet rs = DbBean.executeSql("select * from bid");
          while (rs.next()){
              String stock = rs.getString("stock");
              int price = rs.getInt("price");
              String userID = rs.getString("userID");
              Date bidDate = rs.getTimestamp("bidDate");
              bids.add(new Bid(stock,price,userID,bidDate));
          }
          return bids;
      } catch (ClassNotFoundException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (SQLException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (NamingException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
  }
  
  public ArrayList<MatchedTransaction> getAllMatchedTransactions () {
      try {
          ArrayList<MatchedTransaction> transactions = new ArrayList<MatchedTransaction>();
          ResultSet rs = DbBean.executeSql("select * from matches");
          while (rs.next()){
              int bidPrice = rs.getInt("bidPrice");
              String bidUserId = rs.getString("bidUserID");
              Date bidDate = rs.getTimestamp("bidDate");
              int askPrice = rs.getInt("askPrice");
              String askUserId = rs.getString("askUserID");
              Date askDate = rs.getTimestamp("askDate");
              Date matchDate = rs.getTimestamp("matchDate");
              int price = rs.getInt("price");
              String stock = rs.getString("stock");
              Ask ask = new Ask(stock,askPrice,askUserId,askDate);
              Bid bid = new Bid(stock,bidPrice,bidUserId,bidDate);
              transactions.add(new MatchedTransaction(bid,ask,matchDate,price));
          }
          return transactions;
      } catch (ClassNotFoundException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (SQLException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (NamingException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
  } 
  
  public ArrayList<Ask> getAllAsks(){
      try {
          ArrayList<Ask> asks = new ArrayList<Ask>();
          ResultSet rs = DbBean.executeSql("select * from ask");
          while (rs.next()){
              String stock = rs.getString("stock");
              int price = rs.getInt("price");
              String userID = rs.getString("userID");
              Date askDate = rs.getTimestamp("askDate");
              asks.add(new Ask(stock,price,userID,askDate));
          }
          return asks;
      } catch (ClassNotFoundException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (SQLException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (NamingException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
  }
  
  private void updateLatestPrice(String stock,int price){
      try {
          ResultSet rs = DbBean.executeSql(String.format("UPDATE stock SET price = '%s' WHERE stock = '%s'",price,stock));
          if (rs == null){
            executeInsertPrice(stock,price);//if stock is not found, insert new value for the stock
          }
      } catch (ClassNotFoundException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (SQLException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (NamingException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
  
  // updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
  // based on the MatchedTransaction object passed in
  private void updateLatestPrice(MatchedTransaction m) {
    String stock = m.getStock();
    int price = m.getPrice();
    updateLatestPrice(stock,price);
  }
    
  // updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
  // based on the MatchedTransaction object passed in
  public int getLatestPrice(String stock) {
      try {
          ResultSet rs = DbBean.executeSql(String.format("select price from stock WHERE stock = '%s'",stock));
          if (rs == null){
            executeInsertPrice(stock,-1);
          }
          return rs.getInt("price");
      } catch (ClassNotFoundException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (SQLException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (NamingException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
    return -1;//no such stock
  }
  
  private void clearTable(String tableName){
      try {
         DbBean.executeUpdate("delete from " + tableName);
      } catch (SQLException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
          try {
              DbBean.executeUpdate("SET SQL_SAFE_UPDATES=0; delete from " + tableName);
          }catch(SQLException ex1) {
              Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex1);
          }catch(NamingException ex2){
              Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex2);
          }catch(ClassNotFoundException ex3){
              Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex3);
          }
      }catch(ClassNotFoundException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }catch(NamingException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
  
  private void deleteAsk(Ask ask){
      try {
          DbBean.executeUpdate(String.format("delete from ask where stock = '%s' and price = '%s' and userID = '%s' and askDate = '%s'",
                  ask.getStock(),ask.getPrice(),ask.getUserId(),new Timestamp(ask.getDate().getTime())));
      }catch(SQLException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }catch(ClassNotFoundException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }catch(NamingException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
  
  private void deleteBid(Bid bid){
      try {
          DbBean.executeUpdate(String.format("delete from bid where stock = '%s' and price = '%s' and userID = '%s' and bidDate = '%s'",
                  bid.getStock(),bid.getPrice(),bid.getUserId(),new Timestamp(bid.getDate().getTime())));
      }catch(SQLException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }catch(ClassNotFoundException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }catch(NamingException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
  }

    private void executeInsertPrice(String stock, int price) {
        try{
            DbBean.executeUpdate(String.format("INSERT INTO stock (stock,price) VALUES ('%s','%s')",stock,price));
        }catch(SQLException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }catch(ClassNotFoundException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }catch(NamingException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    
    private void executeInsertMatchedTransaction(MatchedTransaction m){
        String insertValueSQL = "INSERT INTO matches (bidPrice,bidUserID,bidDate,askPrice,askUserID,askDate,matchDate,price,stock) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s')";
        Ask ask = m.getAsk();
        Bid bid = m.getBid();
        try{
            DbBean.executeUpdate(String.format(insertValueSQL, bid.getPrice(),bid.getUserId(),
                    new Timestamp(bid.getDate().getTime()),ask.getPrice(),ask.getUserId(),
                    new Timestamp(ask.getDate().getTime()),new Timestamp(m.getDate().getTime()),
                    m.getPrice(),m.getStock()));
        }catch(SQLException ex){
            Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
        }catch(ClassNotFoundException ex){
            Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
        }catch(NamingException ex){
            Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
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
          PrintWriter outFile = new PrintWriter(file);
          outFile.append(stringMessage);
          outFile.close();
      } catch (URISyntaxException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException e){e.printStackTrace();}
    }
}
