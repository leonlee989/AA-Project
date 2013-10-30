/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.StoredProcedure;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Melvrick
 */
public class InsertUserCredit implements Runnable {
    private String userID;
    private int creditLimit;
    
    public InsertUserCredit (String userID, int creditLimit){
        this.userID = userID;
        this.creditLimit = creditLimit;
    }
    
    public void run() {
        try {
            CallableStatement cs2 = StoredProcedure.connection.prepareCall("{call INSERT_USER_CREDIT(?,?)}");
            cs2.setString(1, userID);
            cs2.setInt(2, creditLimit);
            cs2.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(InsertUserCredit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
