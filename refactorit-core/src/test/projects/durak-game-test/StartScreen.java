import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.BorderFactory; 
import javax.swing.border.Border;

import java.util.regex.*;
import java.io.*;
import java.net.*;

class StartScreen 
{
    public Container mainContainer;
    
    public StartScreen( Container mainContainer )
    {
        this.mainContainer = mainContainer;
        setUpScreen();
    }
    
    private JTextField nickName = new JTextField(10);
    private JTextField ipAddress = new JTextField("localhost",10);
    private JButton hostGame = new JButton("Host Game");
    private JButton joinGame = new JButton("Join Game");
    private LogoPanel logoPanel = new LogoPanel();
    private DemoPanel demoPanel = new DemoPanel();
    private JLabel credits = new JLabel("Durak v1.05 by iTake!");
    private JLabel infoBar = new JLabel("Enter your nickname and host game or enter ip to join.");
    
    public void setUpScreen()
    {
        mainContainer.removeAll();
        mainContainer.setLayout( new GridLayout(1,2));        
        
        Border space = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        
        hostGame.addActionListener(new HostGame( this ));
        joinGame.addActionListener(new JoinGame( this ));
        
        Container controlContainer = new Container();
        controlContainer.setLayout( new GridLayout(2,4) );
        
        controlContainer.add(new JLabel("Nickname : "));
        controlContainer.add(nickName);
        controlContainer.add(hostGame);
        controlContainer.add(new JLabel("IP : "));
        controlContainer.add(ipAddress);
        controlContainer.add(joinGame);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout( new BorderLayout() );
        bottomPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Menu"), space ) );
        bottomPanel.add (controlContainer, BorderLayout.PAGE_START);
        bottomPanel.add (credits, BorderLayout.PAGE_END);
        bottomPanel.add (infoBar, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout ( new GridLayout(2,1) );
        leftPanel.add (logoPanel);
        leftPanel.add (bottomPanel);
        
        mainContainer.add(leftPanel);
        mainContainer.add(demoPanel);
        
        mainContainer.doLayout();
        leftPanel.doLayout();
        bottomPanel.doLayout();
        controlContainer.doLayout();
    }
    public String getNickName()
    {
        return nickName.getText();
    }
    public boolean checkCorrectNickName() 
    {
        if( getNickName().compareTo("") == 0) 
        { return false; } // no blank names
        return getNickName().matches("[a-zA-Z_0-9]*");
    }
    
    class HostGame implements ActionListener 
    {
        StartScreen screen;
        public HostGame ( StartScreen screen ) 
        { 
            this.screen = screen;
        }
        
        public void actionPerformed(ActionEvent e) {
            if( screen.checkCorrectNickName()  == true ) {
                (new Server()).start();
                
                //connect to server
                InetAddress addr = null;
                try 
                {
                    addr = InetAddress.getByName( null );
                }
                catch(IOException ex) 
                { 
                    screen.infoBar.setText( "Unable to resolve hostname!" );
                }
                    
                try {
                    Client client = new Client( addr ); //conn to serv
                    client.putText( "/nick " + screen.getNickName() );
                
                    new PreGameScreen( screen.mainContainer, client );
                }
                catch (IOException ey) 
                { 
                    screen.infoBar.setText( "Unable to connect!" );
                }
                }
            else {
                infoBar.setText( "Erroneous nickname! Must be in range [a-zA-Z_0-9]." );
            }
        }
    }
    
    class JoinGame implements ActionListener 
    {
        StartScreen screen;
        public JoinGame ( StartScreen screen ) 
        { 
            this.screen = screen;
        }
        
        public void actionPerformed(ActionEvent e) {
            if( screen.checkCorrectNickName()  == true ) {
                InetAddress addr = null;
                try 
                {
                    addr = InetAddress.getByName( screen.ipAddress.getText() );
                }
                catch(IOException ex) 
                { 
                    screen.infoBar.setText( "Unable to resolve hostname!" );
                }
                
                try {
                    Client client = new Client( addr ); //conn to serv
                    client.putText( "/nick " + screen.getNickName() );
                    
                    new PreGameScreen( screen.mainContainer, client );
                }
                catch (IOException ey) 
                { 
                    screen.infoBar.setText( "Unable to connect!" );
                }
            }
            else
            {
                infoBar.setText( "Erroneous nickname! Must be in range [a-zA-Z_0-9]." );
            }
        }
    }
}