import java.net.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Client 
{
    public static final int PORT = Server.PORT ;
        
    Socket socket;
    private static PrintWriter out;
    BufferedReader in;
    
    public Client( InetAddress addr ) throws IOException {
        
    
        //MUTE System.out.println("addr = " + addr);
        
        //try {
            
            socket = new Socket(addr, PORT);
            
            //MUTE System.out.println("socket = " + socket);
            
            in =
                new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream()));
            
            out =
                new PrintWriter(
                     new BufferedWriter(
                            new OutputStreamWriter(
                                socket.getOutputStream())),true);
            
        //} 
        /*finally {
            System.out.println("closing...");
            try { socket.close(); } catch (IOException e) {}
        }*/
    }
    
    static String listCmd = "list";
    static char commandChar = '/';
    
    public String getText() { // returns plain text, commands don't release loop
        
        try {
            String text;
            while ( true ) {
                String read = in.readLine();
                //MUTE System.out.println("Client got:"+read);
                text = parseServerText( read );
                if ( text == null || text.compareTo("") != 0 ) break;
            }
            return text;
        }
        catch(IOException e) { return null; }
    }
    
    private String parseServerText ( String str )
    {
        if( str == null )
            return null;
        if( str.compareTo("") == 0 )
        {
            return "";
        }
            if ( str.charAt(0) == commandChar )
            {
                str = str.substring(1); //remove '/'
                parseCommands( str ) ;
                return "";
            }
            else //just chatting text
            {
                return str;
            }
    }
    private int parseCommands ( String str )
    { 
        checkForList( str );
        checkForStart( str );
        checkForNames( str );
        checkForMyCards( str );
        checkForDeckCards( str );
        checkForOppCards( str );
        checkForTurn( str );
        checkForTableCards( str );
        checkForGameOver( str );
        return 1;
    }

    
    String gameOverCmd = "looser";
    private void checkForGameOver( String str )
    {
        String looserName = ServeOne.compareToCmd( gameOverCmd, str );
        if( looserName != null )
        {
            GameScreen.tablePanel.drawLooserName( looserName.trim() );
        }
    }
    
    String myCardsCmd = "mycards";
    String turnCmd = "turn";
    private void checkForMyCards( String str )
    {
        //check if server sends me my cards
        
        String cards = ServeOne.compareToCmd( myCardsCmd, str );
        if( cards != null )
        {
            if( cards.compareTo("") != 0 ) // just clear
            {
                String cardsList[] = (cards.trim()).split( " " );        
                int gotCards = Array.getLength( cardsList ); //
    
                int returnCardsList[] = new int[gotCards];
                
                for( int i = 0; i < gotCards; i++ )
                {
                    returnCardsList[i] = Integer.parseInt( cardsList[i] );
                    //System.out.println(returnCardsList[i]);
                }
                Croupier.setMyCards( returnCardsList, gotCards );
            }
            else
            {
                Croupier.setMyCards( null, 0 );
            }
        }
    }
    
    private int[] parseIntsFromString( String str )
    {
        String intList[] = (str.trim()).split( " " );        
        int gotints = Array.getLength( intList ); //

        int returnList[] = new int[gotints];
            
        for( int i = 0; i < gotints; i++ )
        {
            returnList[i] = Integer.parseInt( intList[i] );
        }
        return returnList;
    }
    
    String namesCmd = "names";
    private void checkForNames( String str )
    {
        //check for incoming players list
        String names = ServeOne.compareToCmd( namesCmd, str );
        if( names != null )
        {
            String namesList[] = (names.trim()).split(" ");
            try {
            GameScreen.playerPanel.setText( namesList[0] );
            GameScreen.playerPanel.repaint();
            
            GameScreen.op1Panel.setText( namesList[1] );
                GameScreen.op1Panel.repaint();
            GameScreen.op2Panel.setText( namesList[2] );
                GameScreen.op2Panel.repaint();
            GameScreen.op3Panel.setText( namesList[3] );
                GameScreen.op3Panel.repaint();
            GameScreen.op4Panel.setText( namesList[4] );
                GameScreen.op4Panel.repaint();
            GameScreen.op5Panel.setText( namesList[5] );
                GameScreen.op5Panel.repaint();
            }
            catch( ArrayIndexOutOfBoundsException e ) {}
        }
    }
    
    private void checkForList( String str )
    {
        //check for incoming players list
        String list = ServeOne.compareToCmd( listCmd, str );
        if( list != null )
        {
            String userList[] = list.split(" ");        
            PreGameScreen.userList.setListData( userList );
        }
    }
    String tableCardsCmd = "table";
    private void checkForTableCards( String str )
    {
        //System.out.println(str+"!!!");
        String table = ServeOne.compareToCmd( tableCardsCmd, str );
        if( table != null )
        {
            if( table.compareTo("") != 0 ) // if empty string, just clear
            { 
                Vector pairs = new Vector();
                String tableList[] = table.split(" ");  // 18-23, 23, 44 ...
                for( int i = 0; i < Array.getLength( tableList ); i++ ) //kol-vo par
                {
                    String pair[] = tableList[i].split("-");
                    //System.out.println("got array "+ Array.getLength(pair));
                    if( Array.getLength(pair) == 2 )
                    {
                        int[] newpair = new int[2];
                        newpair[0] = Integer.parseInt( pair[0] );
                        newpair[1] = Integer.parseInt( pair[1] );
                        pairs.add( newpair );
                    }
                    else
                    {
                        int[] newpair = new int[1];
                        newpair[0] = Integer.parseInt( pair[0] );
                        pairs.add( newpair );
                    }
                }
                Croupier.setTableCards( pairs );
            }
            else
            {
                Croupier.clearTable();
            }
        }
    }
    
    private void checkForStart( String str )
    {
        //check game start
        if( ServeOne.compareToCmd( ServeOne.startCmd, str ) != null )
        {
            new GameScreen( this ); //!!!!!!!!
        }
    }
    private void checkForTurn( String str )
    {
        //check server tells who turn
        String turn = ServeOne.compareToCmd( turnCmd, str );
        if( turn != null )
        {
            turn.trim();       
            GameScreen.tablePanel.setTurn( Integer.parseInt( turn ) );
        }
    }
    
    private void checkForOppCards( String str )
    {
        //check if server sends us opponents cards
        String oppCards = ServeOne.compareToCmd( ServeOne.oppCardsCmd, str );
        if( oppCards != null ) //never null?
        {
            String oppList[] = (oppCards.trim()).split( " " );        
            int gotOps = Array.getLength( oppList ); //

            int returnOpsList[] = new int[gotOps];
            
            for( int i = 0; i < gotOps; i++ )
            {
                returnOpsList[i] = Integer.parseInt( oppList[i] );
                //System.out.println(returnOpsList[i]);
            }
            Croupier.setOpponentCards( returnOpsList );
        }
    }
    
    private void checkForDeckCards( String str )
    {
        //check if server sent us deck cards
        String trumpAndNumInDeck = ServeOne.compareToCmd( ServeOne.deckCardsCmd, str );
        if( trumpAndNumInDeck != null )
        {
            
            String split[] = (trumpAndNumInDeck.trim()).split( " " );        
            Croupier.setDeckCards( Integer.parseInt( split[0] ), Integer.parseInt( split[1] ) );
            //System.out.println(trumpAndNumInDeck);
            //!?!?!?
        }
    }
    
    public static void putText( String text ){
        out.println( text );
    }
    
    
    
    /*Croupier croupier;
    public void assignCroupier( Croupier croupier )
    {
        this.croupier = croupier;
    }*/
    
    //game interface
    public void requestForMyCards()
    {
        out.println( "/mycards" );
    }
    public void requestForDeckCards()
    {
        out.println( "/"+ServeOne.deckCardsCmd );
    }
    public void requestForStart()
    {
        out.println( "/start" );
    }
    public void requestForOppCards()
    {
        out.println( ServeOne.commandChar+ServeOne.oppCardsCmd );
    }
    /*public void requestForNames()
    {
        out.println( "/names" );
    }*/
}
