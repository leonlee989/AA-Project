package context;

import aa.DbBean;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Web application lifecycle listener.
 *
 * @author Melvrick
 */
public class Initializer implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        try {
            DbBean.connect();
            System.out.println("Servlet Context is initialized....");
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException....");
            Logger.getLogger(Initializer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            System.out.println("SQLException....");
            Logger.getLogger(Initializer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            System.out.println("NamingException....");
            Logger.getLogger(Initializer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Servlet Context is destroyed....");
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            System.out.println("Drivers to close > " + driver.getClass().getSimpleName());
            try {
                DriverManager.deregisterDriver(driver);
                System.out.println("driver deregistered");
                Logger.getLogger(context.Initializer.class.getName()).log(Level.SEVERE, null, String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
                Logger.getLogger(context.Initializer.class.getName()).log(Level.SEVERE, null, String.format("Error deregistering driver %s", driver));
            }

        }

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        for (Thread t : threadArray) {
            if (t.getName().contains("Abandoned connection cleanup thread")) {
                synchronized (t) {
                    try {
                        System.out.println(t.getName() + "   Shutdown thread called and executed");
                        AbandonedConnectionCleanupThread.shutdown();
                        t = null;
                    } catch (InterruptedException e) {
                        Logger.getLogger(context.Initializer.class.getName()).log(Level.SEVERE, null, "SEVERE problem cleaning up: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
