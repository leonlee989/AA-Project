/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aa;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class StoredProcedure {
    public static Connection connection = DbBean.getDbConnection();
    
    public static void deleteAllStoredProcedures()throws SQLException{
        String[] queryDropProcedures = {"DROP PROCEDURE IF EXISTS GET_ALL_ASKS;","DROP PROCEDURE IF EXISTS GET_ALL_BIDS;","DROP PROCEDURE IF EXISTS GET_ALL_MATCHED_TRANSACTIONS;"};
        Statement stmtDrop = null;
        try{
            for (String query : queryDropProcedures){
                stmtDrop = connection.createStatement();
                stmtDrop.execute(query);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public static void createProcedureGetAllAsks()throws SQLException{
        String createProcedure = null;
        
        createProcedure =
            "DELIMITER $$ " +
            "CREATE PROCEDURE GET_ALL_ASKS() " +
                "BEGIN " +
                "SELECT ask.stockName, ask.price, ask.userID, ask.askDate from ask; " +
                " END $$ " +
            "DELIMITER ;";
        
        Statement stmt = null;

        try{
            stmt = connection.createStatement();
            stmt.executeUpdate(createProcedure);
        }catch(SQLException e) {
            e.printStackTrace();
        }finally{
            if(stmt!=null){stmt.close();}
        }
    }
    
    public static void createProcedureGetAllBids()throws SQLException{
        String createProcedure = null;
        
        createProcedure =
            "DELIMITER $$ " +
            "CREATE PROCEDURE GET_ALL_BIDS() " +
                "BEGIN " +
                "SELECT bid.stockName, bid.price, bid.userID, bid.bidDate from bid; " +
                " END $$ " +
            "DELIMITER ;";
        
        Statement stmt = null;

        try{
            stmt = connection.createStatement();
            stmt.executeUpdate(createProcedure);
        }catch(SQLException e) {
            e.printStackTrace();
        }finally{
            if(stmt!=null){stmt.close();}
        }
    }
    
    public static void createProcedureGetAllMatchedTransactions()throws SQLException{
        String createProcedure = null;
        
        createProcedure =
            "DELIMITER $$ " +
            "CREATE PROCEDURE GET_ALL_MATCHED_TRANSACTIONS() " +
                "begin " +
                "select matchedTransactionDB.bidPrice, matchedTransactionDB.bidUserID, matchedTransactionDB.bidDate, matchedTransactionDB.askPrice,matchedTransactionDB.askUserID, matchedTransactionDB.askDate, matchedTransactionDB.matchDate,matchedTransactionDB.price, matchedTransactionDB.stockName from matchedTransactionDB; " +
                " END $$ " +
            "DELIMITER ;";
        Statement stmt = null;
        
        try{
            stmt = connection.createStatement();
            stmt.executeUpdate(createProcedure);
        }catch(SQLException e) {
            e.printStackTrace();
        }finally{
            if(stmt!=null){stmt.close();}
        }
    }
}
