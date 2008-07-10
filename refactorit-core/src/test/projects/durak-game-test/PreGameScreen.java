import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.BorderFactory; 
import javax.swing.border.Border;
import java.io.*;
import java.net.*;
import java.util.*;

class PreGameScreen 
{
    Container mainContainer;
    Client client;
    
    public PreGameScreen ( Container mainContainer, Client client ) 
    {
        this.mainContainer = mainContainer;
        this.client = client;
        setUpScreen();
    }
    
    static JTextArea chatArea = new JTextArea();
    static JScrollPane charAreaScrollPane;
    static JTextField typeField = new JTextField();
    public static JList userList = new JList();
    JButton startGame = new JButton("Start Game");
    JButton kickPlayer = new JButton("Kick Player");
    public static JButton readyMyself = new JButton("Ready");
    JButton quit = new JButton("Disconnect");
    
    public void setUpScreen ()
    {
        mainContainer.removeAll();
        
        Border space = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        
        chatArea.setLineWrap( true );
        chatArea.setEditable( false );
        chatArea.setBorder( space );
        
        charAreaScrollPane = new JScrollPane(chatArea);
        charAreaScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        charAreaScrollPane.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Chat Window"),
                                space ),
                charAreaScrollPane.getBorder()));

        typeField.addActionListener(new SendText( typeField, client ));
        
        typeField.setMaximumSize( new Dimension ( 30, 30 ) );
        JPanel typeFieldPanel = new JPanel();   
        typeFieldPanel.setLayout( new BorderLayout() );
        typeFieldPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Send message"), space ) );
        typeFieldPanel.add( typeField, BorderLayout.PAGE_START );
        
        
        JScrollPane userListScrollPane = new JScrollPane(userList);
        userListScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //areaScrollPane.setPreferredSize(new Dimension(250, 250));
        userListScrollPane.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Players List"),
                                space ),
                userListScrollPane.getBorder()));
        
        
        startGame.addActionListener(new StartGame( mainContainer, client ));
        quit.addActionListener(new Quit( mainContainer, client ));
        kickPlayer.addActionListener(new Kick( client ));
        readyMyself.addActionListener(new Ready( client ));
        
        /*startGame.setSize( new Dimension(100,100) );
        quit.setSize( new Dimension(100,100) );
        kickPlayer.setSize( new Dimension(100,100) );
        readyMyself.setSize( new Dimension(100,100) );*/
        
        /*startGame.doLayout();
        quit.doLayout();
        kickPlayer.doLayout();
        readyMyself.doLayout();*/
        
        JPanel controlButtonsPanel = new JPanel();      
        controlButtonsPanel.setLayout( new GridLayout(2,2) );
        controlButtonsPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Control Buttons"), space ) );
                
        controlButtonsPanel.add(startGame);
        controlButtonsPanel.add(kickPlayer);
        controlButtonsPanel.add(readyMyself);
        controlButtonsPanel.add(quit);
        
        JPanel mainPanel = new JPanel();
        
        GridBagLayout mainLayout = new GridBagLayout();
        mainPanel.setLayout( mainLayout );
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        mainLayout.setConstraints(charAreaScrollPane, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 0.1;
        c.weighty = 1.0;
        mainLayout.setConstraints(userListScrollPane, c);
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.weightx = 1.0;
        c.weighty = 0.3;
        mainLayout.setConstraints(typeFieldPanel, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 0.3;
        c.weightx = 0.1;
        mainLayout.setConstraints(controlButtonsPanel, c);
        
        mainPanel.setBorder(space);
        
        mainContainer.add( mainPanel );
        
        mainPanel.add(charAreaScrollPane);
        mainPanel.add(userListScrollPane);
        mainPanel.add(typeFieldPanel);
        mainPanel.add(controlButtonsPanel);
        
        mainContainer.add(mainPanel);
        
        mainContainer.doLayout();
        mainPanel.doLayout();
        controlButtonsPanel.doLayout();
        charAreaScrollPane.doLayout();        
        userListScrollPane.doLayout();
        controlButtonsPanel.doLayout();
        typeFieldPanel.doLayout();
        
        ( new ChatReader( chatArea, client ) ).start();
    }
    public String getNickName() {
        return "";
    }
    
    class StartGame implements ActionListener 
    {
        Container mainContainer;
        Client client;
        
        public StartGame ( Container mainContainer, Client client ) 
        { 
            this.client = client;
            this.mainContainer = mainContainer;
        }
        
        public void actionPerformed(ActionEvent e) {
            client.requestForStart();
        }
    }
    
    class SendText implements ActionListener 
    {
        Client client;
        JTextField typeField;
        //Container mainContainer;
        public SendText ( JTextField typeField, Client client ) 
        { 
            this.typeField = typeField;
            this.client = client;
            //this.mainContainer = mainContainer;
        }
        
        public void actionPerformed(ActionEvent e) {
            //System.out.println( typeField.getText() );
            if( (typeField.getText()).compareTo("") != 0 )
            {
                client.putText( typeField.getText() );
                typeField.setText("");
            }
            //new GameScreen( mainContainer );
        }
    }
    
    class Kick implements ActionListener 
    {
        Client client;
        
        public Kick ( Client client ) 
        { 
            this.client = client;
        }
        
        public void actionPerformed(ActionEvent e) {
            String kickCandidate = (String)userList.getSelectedValue();
            if( kickCandidate != null ) {
                client.putText( "/kick "+  parseUser(kickCandidate));
            }
        }
        private String parseUser( String str )
        {
            String parsed[] = str.split("\\p{Punct}");
            return parsed[0];
        }
    }
    class Ready implements ActionListener 
    {
        Client client;
        boolean state = false; //knopku silno kolnasit kogda menjaesh text ;'(
        
        public Ready ( Client client ) 
        { 
            this.client = client;
        }
        
        public void actionPerformed(ActionEvent e) {
            if( state == false ) //(readyMyself.getText()).compareTo("Ready")
            {
                state = true; 
                client.putText( "/ready" );
                //readyMyself.setText("Not Ready");
            }
            else
            {   
                state = false;
                client.putText( "/notready" );
                //readyMyself.setText("Ready");
            }
        }
    }
    
    class Quit implements ActionListener 
    {
        Client client;
        
        public Quit (Container mainContainer, Client client ) 
        { 
            this.client = client;
        }
        
        public void actionPerformed(ActionEvent e) {
            client.putText( "/quit" );
            //new StartScreen( mainContainer );
        }
    }
}

class ChatReader extends Thread 
{
    JTextArea chatArea;
    Client client;
    
    public ChatReader( JTextArea chatArea, Client client ) 
    {
        this.chatArea = chatArea;
        this.client = client;
    }
    public void run() 
    {
        while( true ) 
        {
            String newText = client.getText();
            if( newText == null ) {
                chatArea.setCaretPosition( chatArea.getDocument().getLength() ); 
                chatArea.append( "Connection closed from host.\n" );
                chatArea.setText( chatArea.getText() );
                break;
            }
            else
            {
                chatArea.setCaretPosition( chatArea.getDocument().getLength() ); 
                chatArea.append( newText +"\n" );
                chatArea.setText( chatArea.getText() );
                //chatArea.setText( client.getText() );
            }
            try {
                sleep(200);
            }
            catch(InterruptedException e) {
            }
        }
    }
}