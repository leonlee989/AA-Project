
import java.io.IOException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Melvrick
 */
public class Nginx {
    public static void main (String[]args) throws InterruptedException, IOException{
        Process p = Runtime.getRuntime().exec("C:\\nginx-1.5.5\\nginx.exe");
        p.waitFor();
    }
}
