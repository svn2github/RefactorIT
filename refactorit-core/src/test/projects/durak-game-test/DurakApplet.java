import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class DurakApplet extends JApplet //implements WindowListener
{
    public static Container mainContainer;
        
    public void init() 
    { 
        mainContainer = getContentPane();
        Library.load();
        //addWindowListener(this);
        (new MainLoop( mainContainer ) ).start();
    }
        /*public void windowOpened(WindowEvent e) {}
        public void windowIconified(WindowEvent e) {}
        public void windowDeiconified(WindowEvent e)  {}
        public void windowDeactivated(WindowEvent e)  {}
        public void windowClosing(WindowEvent e)  
        { 
            System.out.println(e);
        }
        public void windowClosed(WindowEvent e)  {}
        public void windowActivated(WindowEvent e)  {}*/

        /*public void actionPerformed(ActionEvent e) {
            System.out.println(e);
        }*/
}

class MainLoop extends Thread 
{
    Container mainContainer;
    
    public MainLoop( Container mainContainer ) 
    {
        this.mainContainer = mainContainer;
    }
    
    public void run() 
    {
        new StartScreen( mainContainer );
    }
}

class PainterThread extends Thread 
{
    JPanel current;
    public PainterThread(JPanel current) 
    {
        this.current = current;
    }

    public void run() 
    {
        try 
        {
            while(true) 
            {
                sleep(300);
                current.repaint();
            }
        }
        catch(InterruptedException e) 
        {
        }
    }
}

class FastPainterThread extends Thread 
{
    JPanel current;
    public FastPainterThread(JPanel current) 
    {
        this.current = current;
    }

    public void run() 
    {
        try 
        {
            while(true) 
            {
                sleep(25); //40fps ;-)
                current.repaint();
            }
        }
        catch(InterruptedException e) 
        {
        }
    }
}