/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package context;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application lifecycle listener.
 *
 * @author Melvrick
 */
public class StaticThreadsListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
    }

    public void contextDestroyed(ServletContextEvent sce) {
        try {
            System.out.println("Static threads listener activated");
            AbandonedConnectionCleanupThread.shutdown();
        } catch (InterruptedException e) {
        }
    }
}
