//package ee.ttu.joop.t020542.utils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Bruce Eckel
 */
public class RunApplet {
    public static void run(JApplet applet, int width, int height) {
    JFrame frame = new JFrame(title(applet));
    setupClosing(frame);
    frame.getContentPane().add(applet);
    frame.setSize(width, height);
    applet.init();
    applet.start();
    frame.setVisible(true);
        
    /*JFrame frame2 = new JFrame();
    setupClosing(frame2);
    frame2.setSize(width, height);
    frame2.setVisible(true);*/
  }

  public static String title(Object o) {
    String t = o.getClass().toString();
    // Remove the word "class":
    if (t.indexOf("class") != -1)
      t = t.substring(6);
    return t;
  }

  public static void setupClosing(JFrame frame) {
    // The JDK 1.2 Solution as an
    // anonymous inner class:
//    frame.addWindowListener(new WindowAdapter() {
//      public void windowClosing(WindowEvent e) {
//        System.exit(0);
//      }
//    });
    // The improved solution in JDK 1.3:
    //System.out.println("CLOSING!");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
} ///:~
