import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.Array;

public class Server extends Thread 
{
    public static final int PORT = 4668;
    ServerSocket s;
    public static Vector connectedList = new Vector();
    
    public Server() {
        logThis( "\n\nServer started.\n"); // log
        try
        {
            s = new ServerSocket(PORT);
        }
        catch(IOException e)
        {}
        
        //MUTE System.out.println("Started: " + s);
    }
    static boolean run = true;
    public void run()
    {
        try
        {
            while(run)
            {
                // block until connection
                Socket socket = s.accept();
                try
                {
                  new ServeOne(socket);
                }
                catch(IOException e)
                {
                  try {socket.close();} catch(IOException ea){}
                }
            }
        }
        catch(IOException e) {}
        finally
        {
            try {s.close();} catch(IOException ea){}
        }
    }
    
    public static void stopServer()
    {
        run = false;
        for( int i = 0; i < connectedList.size(); i++ )
        {
            ( (ServeOne)connectedList.get(i) ).die();
        }
        logThis( "Server shutdown.\n\n"); // log
    }
    public static void printToAll(String text) {
        //System.out.println(text);
        for ( int i = 0; i < connectedList.size(); i++ ) 
        {
            ((ServeOne)connectedList.get(i)).out.println( text );
        }
    }
    public static void printToPlayer(String text, int player) {
        //System.out.println(text+" | "+player);
        ((ServeOne)readyPlayers.get(player)).out.println( text );
    }
    
    public static void logThis( String text )
    {
        FileOutputStream appendLog = null;
        
        try
        {
            appendLog = new FileOutputStream( "server.log", true );
            for( int i = 0; i < text.length(); i++ )
            {
                appendLog.write( text.charAt(i) );
            }
            appendLog.close();
        }
        catch (IOException ioe) 
        {
            System.out.println( "\nIO error: " + ioe );
        }
    }
    public static boolean checkForDulicate( String nick )
    {
        for ( int i = 0; i < connectedList.size(); i++ ) 
        {
            if( nick.compareTo( ((ServeOne)connectedList.get(i)).nickName ) == 0 )
            {
                return true;
            }
        }
        return false;
    }
    public static void sendNickNames()
    {
        for( int i = 0; i < readyPlayers.size(); i++ )
        {
            printToPlayer("/names "+getRelativeNames(i), i);
        }
    }
    public static String getRelativeNames( int player_id )
    {
        //        player_id+1 player_id+2 << players id1 id2 << player_id
        String result = "";
        for( int i = player_id; i < readyPlayers.size(); i++ )
        {
            result += " " + ( (ServeOne)readyPlayers.get(i) ).nickName;
        }
        for( int i = 0; i < player_id; i++ )
        {
            result += " " + ( (ServeOne)readyPlayers.get(i) ).nickName;
        }
        return result;
    }
    
    public static String getNickNames()
    {
        String nicks = "";
        for ( int i = 0; i < connectedList.size(); i++ ) 
        {
            ServeOne current = (ServeOne)connectedList.get(i);
            nicks += current.nickName + nameStatus( readyPlayers.indexOf( current ) + 1 )+ " ";
        }
        return nicks.trim();
    }
    public static String getNickNameByID( int id )
    {
        if( id < readyPlayers.size() )
            return ( (ServeOne)readyPlayers.get(id) ).nickName;
        else
            return "";
    }
    private static String nameStatus(int gameID )
    {
        if( gameID == 0 )
        {
            return "(Observer)";
        }
        else
        {
            return "(Ready,ID:"+gameID+")";
        }
    }
    
    public static boolean kickUser(String nick)
    {
        for ( int i = 0; i < connectedList.size(); i++ ) 
        {
            ServeOne current = (ServeOne)connectedList.get(i);
            if( nick.compareTo( current.nickName ) == 0 )
            {
                current.die();
                return true;
            }
        }
        return false;
    }

    private static Vector readyPlayers = new Vector();
    
    public static int getMyID( ServeOne current )
    {
        return readyPlayers.indexOf( current );
    }
        
    public static int getNumberOfPlayers()
    {
        return readyPlayers.size();
    }

    
    public static int ready(ServeOne current)
    {
        if( readyPlayers.indexOf ( current ) == -1 ) //not in list
        {
            if( readyPlayers.size() < Durak.maxPlayers )
            {
                readyPlayers.add( current );
                
                return readyPlayers.indexOf( current ) + 1;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            return -2;
        }
    }
    public static int notReady( ServeOne current )
    {
        if( readyPlayers.indexOf ( current ) != -1 )
        {
            readyPlayers.remove( current );
            //System.out.println("N"+ readyPlayers.indexOf( current ) );
            return 0;
        }
        else
        {
            return -1;
        }
    }
}

class ServeOne extends Thread
{
    private Socket socket;
    public BufferedReader in;
    public PrintWriter out;
    private boolean owner;

    public ServeOne(Socket s) throws IOException
    {
        socket = s;
        //MUTE System.out.println("Connection accepted thread started: " + socket);
        in = new BufferedReader(
                 new InputStreamReader(
                     socket.getInputStream()));
        out = new PrintWriter(
                  new BufferedWriter(
                      new OutputStreamWriter(
                          socket.getOutputStream())),true);
          
          
        start();
    }
    
    private void disconnect()
    {
        try {socket.close();} catch(IOException ea){}
    }
    public String nickName;
    //public int gameID = 0;
    
    static char commandChar = '/';
    static String nickCmd = "nick";
    static String listCmd = "list";
    static String quitCmd = "quit";
    static String readyCmd = "ready";
    static String startCmd = "start";
    static String oppCardsCmd = "oppcards";
    static String notReadyCmd = "notready";
    private boolean quit = false;
    
    private void parseIncomingText( String str )
    {
        if( str.compareTo("") != 0 )
        {
            if ( str.charAt(0) == commandChar )
            {
                Server.logThis( "CMD from "+nickName+" : "+str+"\n" ); // log command
                str = str.substring(1); //remove '/'
                parseCommands( str ) ;
    
            }
            else //just chatting text
            {
                Server.printToAll( "<"+nickName+"> "+str);
                Server.logThis( "<"+nickName+"> "+str+"\n"); // log chat
            }
        }
    }
    
    private int parseCommands ( String str )
    { 
    
        checkForQuit( str );
        
        //allow other commands only when nickname NOT null
        if( nickName == null ) { //if null allow only /nick command
            checkForNickFirstTime( str );
        }
        else //if nick is set, check for all other commands
        {
            checkForNewNick( str );
            checkForList( str );
            checkForReady( str );
            checkForNotReady( str );
            checkForKick( str );
            checkForStart( str );
            checkForMyCards( str );
            checkForDeckCards( str );
            checkForOppCards( str );
            checkForMoveCards( str );
            checkForBeatCards( str );
            checkForDone( str );
        }
        return 1;
    }
    /*private void checkForNames()
    {
        Server.sendNickNames();
    }*/
    
    static String moveCardsCmd = "move";
    private void checkForMoveCards( String str )
    {
        //move to tabl
        String cards = compareToCmd( moveCardsCmd, str );
        if( cards != null )
        {
            Croupier.moveCardsToTable(parseIntsFromString( cards ), Server.getMyID( this ) );
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
    
    
    static String deckCardsCmd = "deckcards";
    private void checkForDeckCards( String str )
    {
        //send card on req
        if( compareToCmd( deckCardsCmd, str ) != null )
        {
            printDeckCards();
        }
    }
    private void printDeckCards()
    {
        out.println( "/" + deckCardsCmd + " " + Croupier.getDeckCards() );
        //Server.printToAll( "/mycards 5 6 3 2 1" );
    }
    
    static String doneCmd = "done";
    private void checkForDone( String str )
    {
        //send card on req
        if( compareToCmd( doneCmd, str ) != null )
        {
            int player = Server.getMyID( this );
            if( player != -1 )
            {
                Croupier.takeCardsFromTable( player );
            }
            else
            {
                out.println("You are an observer!");
            }
        }
    }
    
    private void checkForQuit( String str )
    {
        if( compareToCmd( quitCmd, str ) != null )
        {
            quit = true;
            Server.printToAll(nickName + " has part.");
            Server.connectedList.remove( this );
            out.println("Connection closed.");
            printList(1);
            disconnect();
        }
    }
    private void checkForNickFirstTime( String str )
    {
        String newNick = compareToCmd( nickCmd, str );
        if ( newNick != null )
        {
            if ( checkCorrectNickName( newNick ) == true )
            {
                //check for duplicate name
                if( Server.checkForDulicate( newNick ) == true )
                {
                    out.println( "This nickname already in use! Choose another!"  );
                }
                else 
                {
                    Server.connectedList.add( this );
                    if( Server.connectedList.get(0) == this ) //if i'm an owner (first one)
                    {
                        owner = true;
                    }
                    Server.printToAll( newNick + " has joined. ");
                    //System.out.println("is now known as: " + str);
                    nickName = newNick;
                    printList(1); //refresh list?
                }
            }
            else //return error
            {
                out.println( "Erroneous nickname: '"+newNick+"'. Must be in range [a-zA-Z_0-9]. type /nick to change"  );
            }
        }
        else
        {
            out.println( "First type /nick to choose you nick"  );
        }
    }
    private void checkForNewNick( String str )
    {
        String newNick = compareToCmd( nickCmd, str );
        if ( newNick != null )
        {
            if ( checkCorrectNickName( newNick ) == true )
            {
                //check for duplicate name
                if( Server.checkForDulicate( newNick ) == true )
                {
                    out.println( "This nickname already in use! Choose another!"  );
                }
                else 
                {
                    Server.printToAll( nickName + " is now known as " + newNick );
                    nickName = newNick;
                    printList(1);
                    Server.sendNickNames();
                }
            }
            else //return error
            {
                out.println( "Erroneous nickname: '"+newNick+"'. Must be in range [a-zA-Z_0-9]. type /nick to change"  );
            }
        }
    }
    private void printList(int mode)
    {
        String outList = "/list " + Server.getNickNames();
        if( mode == 0 ) {
            out.println( outList );
        }
        else
        {
            Server.printToAll( outList );
        }
    }
    private void checkForList( String str )
    {
        //list users
        if( compareToCmd( listCmd, str ) != null )
        {
            printList(0);
        }
    }
    private void checkForReady( String str )
    {    
        //ready
        if( compareToCmd( readyCmd, str ) != null )
        {
            int status = Server.ready( this );
            
            if( status == -1 ) // too many players
            {
                out.println("Cannot ready, already maximum of 6 players are ready!");
            }
            else
            {
                if( status == -2 )
                {
                    out.println("You are ready!");
                }
                else
                {
                    Server.printToAll( nickName+ " is Ready!");
                    printList(1); //refresh list?
                }
            }
        }
    }
    private void checkForNotReady( String str )
    {
        //NotReady
        if( compareToCmd( notReadyCmd, str ) != null )
        {
            if( Server.notReady( this ) != -1 )
            {
                Server.printToAll( nickName+ " is not Ready!");
                printList(1); //refresh list?
            }
            else
            {
                out.println("You are not ready!");
            }
        }
    }
    private void checkForKick( String str )
    {
        //kick some ass ;-)
        String kickName = compareToCmd( kickCmd, str );
        if( kickName != null )
        {
            if ( owner == true ) {
                if( Server.kickUser(kickName) )
                {
                    printList(1); //refresh list?
                    Server.printToAll(kickName + " was kicked.");
                }
                else
                {
                    out.println( "No such user." );
                }
            }
            else
            {
                out.println("Cannot kick, not hosting this game.");
            }
        }
    }
    private void checkForStart( String str ) //Start game
    {
        if( compareToCmd( startCmd, str ) != null )
        {
            if ( owner == true ) 
            {
                if( Server.getNumberOfPlayers() > 1 )
                {
                    Croupier croupier = new Croupier( Server.getNumberOfPlayers() ); //start croupier
                    Server.printToAll( "/start" ); //tell everybodey to start
                }
                else
                {
                    out.println("Must be at least 2 ready players to start game.");
                }
            }
            else 
            {
                out.println("Host must start this game.");
            }
        }
    }
    static String myCards = "mycards";
    private void checkForMyCards( String str )
    {
        //send card on req
        if( compareToCmd( myCards, str ) != null )
        {
            printMyCards();
        }
    }
    private void printMyCards()
    {
        //System.out.println( Croupier.getMyCards( Server.getMyID( this ) ) );
        out.println( "/mycards " + Croupier.getMyCards( Server.getMyID( this ) ) );
        //Server.printToAll( "/mycards 5 6 3 2 1" );
    }
    
    
    private void checkForOppCards( String str )
    {
        //client requests for opp cards
        if( compareToCmd( oppCardsCmd, str ) != null )
        {
            printOppCards();
        }
    }
    
    String beatCmd = "beat";
    private void checkForBeatCards( String str )
    {
        //client requests for opp cards
        String beatCards = compareToCmd( beatCmd, str );
        if( beatCards  != null )
        {
            int[] cards = parseIntsFromString( beatCards );
            Croupier.setBeatCard( cards[1], cards[0], Server.getMyID( this ) );
        }
    }
    
    private void printOppCards()
    {
        out.println( commandChar+oppCardsCmd+" " + Croupier.getOpponentCards( ( Server.getMyID( this ) ) ) );
    }
    
    static String kickCmd = "kick";
    
    public void die()
    {
        quit = true;
        out.println("Connection closed.");
        Server.notReady( this );
        Server.connectedList.remove( this );
        disconnect();
    }
    
    public static String compareToCmd( String cmd, String str )
    {
        try 
        {
            if ( cmd.compareTo( str.substring(0, cmd.length() ) ) == 0 )
            {
                str = str.substring( cmd.length() ); //cut command
                str = str.trim(); //cut space
                return str;
            }
            else {
                return null;
            }
        }
        catch ( StringIndexOutOfBoundsException e ) // recv str is shorten than cmd
        {
            return null;
        } 
    }
    
    private boolean checkCorrectNickName( String nickName ) 
    {
        if( nickName.compareTo("") == 0) 
        { return false; } // no blank names
        return nickName.matches("[a-zA-Z_0-9]*");
    }
    
    public void run()
    {
        try
        {
            while( true )
            {
                String str = in.readLine();
                
                if( quit == true ) break;
                //MUTE System.out.println(Server.getMyID(this)+": " + str);
                
                parseIncomingText( str );
            }
        }
        catch(IOException e){}
        finally
        {
            try
            {
                socket.close();
            }
            catch(IOException e){}
        }
    }
}
