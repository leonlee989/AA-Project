/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import aa.DbBean;
import java.sql.CallableStatement;
import java.sql.Connection;
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
public class BackOfficeThread implements Runnable {

    private ExecutorService executor;

    public BackOfficeThread(ExecutorService executor) {
        this.executor = executor;
    }

    private boolean checkBackOfficeLogs() {
        CallableStatement cs = null;
        Connection cn = null;
        ResultSet rs = null;
        try {
            cn = DbBean.getDbConnection();
            cs = cn.prepareCall("{call CHECK_IF_BACKOFFICEMESSAGE_EXISTS}");
            rs = cs.executeQuery();
            if (rs != null) {
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(BackOfficeThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {;
                }
                rs = null;
            }

            if (cs != null) {
                try {
                    cs.close();
                } catch (SQLException e) {;
                }
                cs = null;
            }

            if (cn != null) {
                try {
                    cn.close();
                } catch (SQLException e) {;
                }
                cn = null;
            }
        }
        return false;
    }

    private ArrayList<String> downloadBackOfficeLogs() {
        ArrayList<String> logs = new ArrayList<String>();
        Connection cn = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            cn = DbBean.getDbConnection();
            cs = cn.prepareCall("{call DUMP_FROM_BACKOFFICE}");
            rs = cs.executeQuery();
            while (rs.next()) {
                logs.add(rs.getString("logStatement"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(BackOfficeThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {;
                }
                rs = null;
            }

            if (cs != null) {
                try {
                    cs.close();
                } catch (SQLException e) {;
                }
                cs = null;
            }

            if (cn != null) {
                try {
                    cn.close();
                } catch (SQLException e) {;
                }
                cn = null;
            }
        }
        return logs;
    }

    public void run() {
        aa.Service service = new aa.Service();
        boolean status = false;
        
        while (true){
            try {
                try {
                    aa.ServiceSoap port = service.getServiceSoap();

                    if (status && checkBackOfficeLogs()) {
                        BackOfficeResendThread bot = new BackOfficeResendThread(downloadBackOfficeLogs());
                    }
                } catch (Exception ex) {
                    // may come here if a time out or any other exception occurs
                    // what should you do here??
                }
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(BackOfficeThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}
