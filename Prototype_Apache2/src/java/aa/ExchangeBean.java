package aa;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import thread.BackOfficeThread;
import thread.DeleteAskThread;
import thread.DeleteBidThread;
import thread.HighestBidThread;
import thread.InsertAskThread;
import thread.InsertBidThread;
import thread.InsertMatchedTransaction;
import thread.InsertUserCredit;
import thread.LowestAskThread;
import thread.MatchedTransactionLogger;
import thread.RejectedTransactionLogger;
import thread.UpdateCreditThread;
 
public class ExchangeBean {
  private static final int NTHREADS = Runtime.getRuntime().availableProcessors();;
  //5 core threads kept alive, 100 max threads,
  private static final ExecutorService executor = new ThreadPoolExecutor(0,NTHREADS,60L,TimeUnit.SECONDS,new SynchronousQueue<Runnable>());
  
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
      String returnString = ""; 
      try {
          CallableStatement cs = StoredProcedure.connection.prepareCall("{call GET_FILTERED_BIDS(?)}");
          cs.setString(1,stock);
          ResultSet rs = cs.executeQuery();
            
          while (rs.next()){
              String stockName = rs.getString("stockName");
              int price = rs.getInt("price");
              String userID = rs.getString("userID");
              Date bidDate = rs.getTimestamp("bidDate");
              Bid b = new Bid(stockName,price,userID,bidDate);
              returnString += b.toString() + "<br />";
          }
      } catch (SQLException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
      return returnString;
  }

  // returns a String of unfulfilled asks for a particular stock
  // returns an empty string if no such ask
  // asks are separated by <br> for display on HTML page
  
  public boolean sendToBackOffice(String txnDescription){
      try {
          BackOfficeThread bot = new BackOfficeThread(txnDescription,executor);
          Future<Boolean> status = executor.submit(bot);
          boolean finalStatus = status.get();
          return finalStatus;
      } catch (InterruptedException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ExecutionException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
      return false;
  }
  
  private boolean checkIfAskExists(String stockName)throws SQLException{
      CallableStatement cs = StoredProcedure.connection.prepareCall("{call CHECK_IF_ASK_EXISTS(?)}");
      cs.setString(1, stockName);
      ResultSet rs = cs.executeQuery();
      return rs.next();
  }
  
  private boolean checkIfBidExists(String stockName)throws SQLException{
      CallableStatement cs = StoredProcedure.connection.prepareCall("{call CHECK_IF_BID_EXISTS(?)}");
      cs.setString(1, stockName);
      ResultSet rs = cs.executeQuery();
      return rs.next();
  }
    
  private void insertBid(Bid bid){
      InsertBidThread ibt = new InsertBidThread(bid);
      executor.execute(ibt);
  }

  private void insertAsk(Ask ask){
      InsertAskThread iat = new InsertAskThread(ask);
      executor.execute(iat);
  }
  
  private void insertRejectedLog(String logStatement){
      try{
          CallableStatement cs = StoredProcedure.connection.prepareCall("{call INSERT_REJECTED_LOG(?)}");
          cs.setString(1, logStatement);
          cs.executeQuery();
      }catch(SQLException e){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, e);
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
    
  public String getUnfulfilledAsks(String stock) {
    String returnString = ""; 
      try {
          CallableStatement cs = StoredProcedure.connection.prepareCall("{call GET_FILTERED_ASKS(?)}");
          cs.setString(1,stock);
          ResultSet rs = cs.executeQuery();
            
          while (rs.next()){
              String stockName = rs.getString("stockName");
              int price = rs.getInt("price");
              String userID = rs.getString("userID");
              Date askDate = rs.getTimestamp("askDate");
              Ask a = new Ask(stockName,price,userID,askDate);
              returnString += a.toString() + "<br />";
          }
      } catch (SQLException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
      return returnString;
  }

  // returns the highest bid for a particular stock
  // returns -1 if there is no bid at all
  public int getHighestBidPrice(String stock) {
      try {
          Bid highestBid = getHighestBid(stock).get();
          if (highestBid == null) {
            return -1;
          } else {
            return highestBid.getPrice();
          }
      } catch (InterruptedException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ExecutionException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
      return -1;
  }

  // retrieve unfulfiled current (highest) bid for a particular stock
  // returns null if there is no unfulfiled bid for this stock
  private Future<Bid> getHighestBid(String stock) {
      HighestBidThread hbt = new HighestBidThread(stock);
      return executor.submit(hbt);
  }

  // returns the lowest ask for a particular stock
  // returns -1 if there is no ask at all
  public int getLowestAskPrice(String stock) {
      try {
          Ask lowestAsk = getLowestAsk(stock).get();
          if (lowestAsk == null) {
            return -1;
          } else {
            return lowestAsk.getPrice();
          }
      } catch (InterruptedException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ExecutionException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
      return -1;
  }
  
  // retrieve unfulfiled current (lowest) ask for a particular stock
  // returns null if there is no unfulfiled asks or errors
  private Future<Ask> getLowestAsk(String stock) {
      LowestAskThread lat = new LowestAskThread(stock);
      return executor.submit(lat);
  }

  // get credit remaining for a particular buyer
  private int getCreditRemaining(String buyerUserId) throws ClassNotFoundException, SQLException, NamingException{
//    if (!(creditRemaining.containsKey(buyerUserId))){
//      // this buyer is not in the hash table yet. hence create a new entry for him
//      creditRemaining.put(buyerUserId, DAILY_CREDIT_LIMIT_FOR_BUYERS);
//    }
//    return creditRemaining.get(buyerUserId);
    
   //read the credit limit from database #SD#
   CallableStatement cs = StoredProcedure.connection.prepareCall("{call GET_USER_CREDIT_LIMIT(?)}");
   cs.setString(1, buyerUserId);
   ResultSet rs = cs.executeQuery();
    if (rs.next())
    {
        return rs.getInt("credit_limit");        
    }    
    else
    {
        InsertUserCredit iuct = new InsertUserCredit(buyerUserId,DAILY_CREDIT_LIMIT_FOR_BUYERS);
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
      UpdateCreditThread uct = new UpdateCreditThread(newRemainingCredit,b.getUserId());
      executor.execute(uct);
      return true;
    }
  }

  // call this to append all rejected buy orders to log file
  private void logRejectedBuyOrder(Bid b) {
      RejectedTransactionLogger rtl = new RejectedTransactionLogger(b,REJECTED_BUY_ORDERS_LOG_FILE);
      executor.execute(rtl);
  }

  // call this to append all matched transactions in matchedTransactions to log file and clear matchedTransactions
  private void logMatchedTransactions() {
      try {
        ArrayList<MatchedTransaction> transactions = getAllMatchedTransactions();
        MatchedTransactionLogger mtl = new MatchedTransactionLogger(transactions,MATCH_LOG_FILE);
        executor.execute(mtl);
        clearTable("matchedTransactionDB"); // clean this out
    } catch (Exception e) {
      // Think about what should happen here...
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
    // step 0: check if this bid is valid based on the buyer's credit limit
    String newBidStockName = newBid.getStock();
    boolean okToContinue = validateCreditLimit(newBid);
    if (!okToContinue){
      return false; 
    }
    // step 1: insert new bid into unfulfilledBids
    //Update DB
    insertBid(newBid);

    // step 2: check if there is any unfulfilled asks (sell orders) for the new bid's stock
    if (!checkIfAskExists(newBidStockName)) {
      return true; // no unfulfilled asks of the same stock
    }

    // step 3: identify the current/highest bid in unfulfilledBids of the same stock
    //LOOK IN DB FOR CURRENT HIGHEST BID
    Bid highestBid = null;
      try {
          highestBid = getHighestBid(newBidStockName).get();
      } catch (InterruptedException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ExecutionException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
    // step 4: identify the current/lowest ask in unfulfilledAsks of the same stock
    //LOOK IN DB FOR CURRENT LOWEST ASK
    Ask lowestAsk = null;
      try {
          lowestAsk = getLowestAsk(newBidStockName).get();
      } catch (InterruptedException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ExecutionException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
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
  public void placeNewAskAndAttemptMatch(Ask newAsk) throws SQLException{
    String askStockName = newAsk.getStock();
    // step 1: insert new ask into unfulfilledAsks
    //insert in DB ask
    insertAsk(newAsk);
    // step 2: check if there is any unfulfilled bids (buy orders) for the new ask's stock. if not, just return
    // count keeps track of the number of unfulfilled bids for this stock
    if (!checkIfBidExists(askStockName)){
        return;
    }

    // step 3: identify the current/highest bid in unfulfilledBids of the same stock
    Bid highestBid = null;
      try {
          highestBid = getHighestBid(askStockName).get();
      } catch (InterruptedException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ExecutionException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }

    // step 4: identify the current/lowest ask in unfulfilledAsks of the same stock
    Ask lowestAsk = null;
      try {
          lowestAsk = getLowestAsk(askStockName).get();
      } catch (InterruptedException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ExecutionException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }


    // step 5: check if there is a match.
    // A match happens if the lowest ask is <= highest bid
    if (lowestAsk.getPrice() <= highestBid.getPrice()) {
      // a match is found! Start removing from runtime & db
      deleteBid(highestBid);
      deleteAsk(lowestAsk);
      // this is a SELLING trade - the transaction happens at the lowest ask's timestamp, and the transaction price happens at the highest bid
      MatchedTransaction match = new MatchedTransaction(highestBid, lowestAsk, lowestAsk.getDate(), highestBid.getPrice());
      executeInsertMatchedTransaction(match);

      // to be included here: inform Back Office Server of match
      // to be done in v1.0

      updateLatestPrice(match);
      logMatchedTransactions();
    }
  }
  
  public ArrayList<MatchedTransaction> getAllMatchedTransactions () {
      try {
          ArrayList<MatchedTransaction> transactions = new ArrayList<MatchedTransaction>();
          ResultSet rs = DbBean.executeSql("select * from matchedTransactionDB");
          while (rs.next()){
              int bidPrice = rs.getInt("bidPrice");
              String bidUserId = rs.getString("bidUserID");
              Date bidDate = rs.getTimestamp("bidDate");
              int askPrice = rs.getInt("askPrice");
              String askUserId = rs.getString("askUserID");
              Date askDate = rs.getTimestamp("askDate");
              Date matchDate = rs.getTimestamp("matchDate");
              int price = rs.getInt("price");
              String stock = rs.getString("stockName");
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
  
  private void updateLatestPrice(String stock,int price){
      CallableStatement cs = null;
      try {
          cs = StoredProcedure.connection.prepareCall("{call UPDATE_STOCK_PRICE(?,?)}");
          cs.setInt(1, price);
          cs.setString(2, stock);
          cs.executeQuery();
      } catch (SQLException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
          executeInsertPrice(stock,price);//if stock is not found, insert new value for the stock
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
          ResultSet rs = DbBean.executeSql(String.format("select * from stock WHERE stockName = '%s'",stock));
          if (rs == null){
            executeInsertPrice(stock,-1);
          }
          if (rs.next()){
            return rs.getInt("price");
          }
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
          try {
              DbBean.executeUpdate("delete from " +tableName);
          } catch (NamingException ex) {
              Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
          } catch (ClassNotFoundException ex){
              Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);              
          }
      } catch (SQLException ex) {
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
          try {
              DbBean.executeUpdate("SET SQL_SAFE_UPDATES=0;");
              DbBean.executeUpdate("delete from " +tableName);
          }catch(SQLException ex1) {
              Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex1);
          }catch(NamingException ex2){
              Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex2);
          }catch(ClassNotFoundException ex3){
              Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex3);
          }
      }
  }
  
  private void deleteAsk(Ask ask){
      DeleteAskThread dat = new DeleteAskThread(ask);
      executor.execute(dat);
  }
  
  private void deleteBid(Bid bid){
      DeleteBidThread dbt = new DeleteBidThread(bid);
      executor.execute(dbt);
  }

    private void executeInsertPrice(String stock, int price) {
      try{
          CallableStatement cs = StoredProcedure.connection.prepareCall("{call INSERT_PRICE(?,?)}");
          cs.setInt(1, price);
          cs.setString(2, stock);
      }catch(SQLException ex){
          Logger.getLogger(ExchangeBean.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    
    private void executeInsertMatchedTransaction(MatchedTransaction m){
        InsertMatchedTransaction imt = new InsertMatchedTransaction(m);
        executor.execute(imt);
    }
}