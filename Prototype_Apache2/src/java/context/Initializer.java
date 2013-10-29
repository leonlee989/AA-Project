package context;


import aa.DbBean;
import aa.StoredProcedure;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
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
        System.out.println("Servlet Context is initialized....");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Servlet Context is destroyed....");
    }
}
