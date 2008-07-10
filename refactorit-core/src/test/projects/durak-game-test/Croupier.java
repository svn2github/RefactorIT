import java.util.*;
import java.lang.reflect.Array;

public class Croupier 
{
    static CardPack standardDeck = new CardPack();
    int shuffledCards[] = new int[ CardPack.number ];
    final int shuffleTimes = 5000 + Math.round( (float)( Math.random() * 10000 ) ) ;
    final int cardsPerPlayer = 6;
    int currentCard = 0;
    
    static Vector playersCards[];
    private static Vector deckCards = new Vector();
    private static Card trump;
    static Vector tableCards = new Vector();
    static int players;
    static int turn;
    static boolean freeturn = true; // first turn - any card
    
    private static int nextAbsolutePlayer( int current )
    {
        if( current == players-1)
            return 0;
        else
            return current+1;
    }
    
    private static int nextPlayer( int current )
    { //skip players with no cards
        int thisPlayer = nextAbsolutePlayer( current );
        if( playersCards[ thisPlayer ].size() != 0 )
            return thisPlayer;
        else
            return nextPlayer( thisPlayer );
    }
    
    private static int pervAbsolutePlayer( int current )
    {
        if( current == 0)
            return (players-1);
        else
            return current-1;
    }
    private static int pervPlayer( int current )
    { //skip players with no cards
        int thisPlayer = pervAbsolutePlayer( current );
        if( playersCards[ thisPlayer ].size() != 0 )
            return thisPlayer;
        else
            return pervPlayer( thisPlayer );
    }
    
    public Croupier( int players ) 
    {
        this.players = players;
        
        playersCards = new Vector[ players ]; //create car aray for players
        
        for( int i = 0; i < players; i++)
        {
            playersCards[i] = new Vector();
        }
        
        for( int i = 0; i < CardPack.number; i++ )  //shuffle cards
        {
            shuffledCards[i] = i;
        }
        shuffleCards();
        
        //spawn deck
        
        for( int i = 0; i < CardPack.number; i++ )
        {
            deckCards.add( standardDeck.cards.get( shuffledCards[i] ) );
        }
        trump = (Card)deckCards.lastElement(); // set trump to be the last card
        
        // give cards to players from deck
        // determine player with lowest card of trump suit
        Card playerTrump = null ;
        int playerWithLowestTrump = 0 ;
        for( int i = 0; i < players; i++ )
        {
            for( int k = 0 ; k < Durak.cardsPerPlayer; k++ )
            {
                Card current = (Card)deckCards.get( 0 );
                playersCards[i].add( current ); //add to player
                
                if( trump.isSameSuit( current ) )
                {
                    if( playerTrump == null || current.isLower( playerTrump ) )
                    {
                        playerTrump = current;
                        playerWithLowestTrump = i;
                    }
                }
                deckCards.remove( 0 );//remove from deck
            }
        }
        //MUTE System.out.println("Crop created, lower trump got "+
         //       playerWithLowestTrump);
        turn = playerWithLowestTrump;
        nextPlayer = nextPlayer( turn );
        sendTurn();
        Server.sendNickNames();
    }
    static int nextPlayer;
    void shuffleCards()
    {
        int temp;
        for( int i = 0; i < shuffleTimes; i++ ) 
        {
            int pos1 = Math.round( (float)Math.random() * ( CardPack.number - 1 ) );
            int pos2 = Math.round( (float)Math.random() * ( CardPack.number - 1 ) );
            temp = shuffledCards[pos1]; 
            shuffledCards[pos1] = shuffledCards[pos2];
            shuffledCards[pos2] = temp;
        }
    }
    
    void printShuffledCards()
    {
        for( int i = 0; i < CardPack.number; i++ ) 
        {
            System.out.print( shuffledCards[i] + " ");
            System.out.println( ((Card)standardDeck.cards.get( shuffledCards[i] )).getName() );
        }
    }
    
    //client interface?
    void giveCardsToOpponent( OpponentPanel panel )
    {
        int players = 5;
        if( panel.id < players ) 
        {
            for( int i = 0; i < cardsPerPlayer; i++ ) 
            {
                panel.addCard( new UnknownCard() );
                currentCard++;
            }
        }
    }
    
    //client interface
    public static void setMyCards( int [] cards, int num )
    {
        GameScreen.playerPanel.flushCards();
        for( int i = 0; i < num; i++ )
        {
            GameScreen.playerPanel.addCard( (Card)standardDeck.cards.get( cards[i] ) );
        }
        GameScreen.playerPanel.repaint();
    }
    
    public static void setDeckCards( int trump, int num )
    {
        GameScreen.deckPanel.flushCards();
        GameScreen.deckPanel.setTrump( (Card)standardDeck.cards.get(trump) ); // first drawn trump
        for( int i = 0; i < num; i++ ) 
        {
            GameScreen.deckPanel.addCard( new UnknownCard() );
        }
        GameScreen.deckPanel.repaint();
    }
    
    public static void setOpponentCards( int[] opCards )
    {
        OpponentPanel[] panels = new OpponentPanel[5];
        panels[0] = GameScreen.op1Panel;
        panels[1] = GameScreen.op2Panel;
        panels[2] = GameScreen.op3Panel;
        panels[3] = GameScreen.op4Panel;
        panels[4] = GameScreen.op5Panel;
        
        for( int k = 0; k < Array.getLength( opCards ); k++ )
        {
            panels[k].flushCards();
            for( int i = 0; i < opCards[k]; i++ ) 
            {
                panels[k].addCard( new UnknownCard() );
            }
            panels[k].repaint();
        }
    }
    
    public static void moveCardsToTable( int[] cards, int player )
    {
        // esli turn == player --> hodit, mozhet podkladqvatj
        // esli turn+1 == player --> otbivaetsa, ne mozhet podkladqvatj
        // esli turn != player --> podkidqvaet
        
        //System.out.println(player + "|"+turn);
        if( turn == player && freeturn ) //first card sent in free turn until next turn
        {
            //System.out.println("hodjashii");
            // mozhno hoditj gruppoi kart odinakovogo value
            
            if( checkForGroupMatch( cards ) ) {
            //move to table
                if( putCardsInTableVector ( cards, player ) )
                    freeturn = false; //no more free turns ;-))
            }
            else
            { Server.printToPlayer("Select group or one card of the same value.", player); }
        }
        else { //otbivajushisa
            if( turn == pervPlayer( player ) )
            {
                Server.printToPlayer("It's not your turn to place cards.", player);
            }
            else { //drugoj
                if( checkForGroupMatch( cards ) ) {
                    if( findOnTableValueMatch( (Card)Croupier.standardDeck.cards.get( cards[0] ) ) )
                    {
                        putCardsInTableVector( cards, player );
                        //System.out.println("drugoj");
                    }
                    else { Server.printToPlayer("Must place cards, with value presented on the table.", player); }
                }
                else
                { Server.printToPlayer("Select group or one card of the same value.", player); }
            }
        }
    }
    
    private static int numberOfUnbeatenCardsOnTable()
    {
        int unbeaten = 0;
        for( int i = 0; i < tableCards.size(); i++ )
        {
            Card[] pair = (Card[])tableCards.get(i);
            if( pair[1] == null )
            {
                unbeaten++;
            }
        }
        return unbeaten;
    }
    private static boolean putCardsInTableVector( int cards[], int player )
    {   
        if( (numberOfUnbeatenCardsOnTable() + Array.getLength(cards)) <= playersCards[ nextPlayer ].size() )
        {
                for( int i = 0; i < Array.getLength(cards); i++ )
                {
                    Card current = (Card)Croupier.standardDeck.cards.get( cards[i] );
                    tableAddToPair( current );
                    //remove this cards from player
                    playersCards[player].remove( current );
                }
                sendTableCards();
                sendMyCards( player );
                sendOpponentCards( );
                return true;
        }
        else
        {
            Server.printToPlayer("Cannot add more cards to table.", player);
            return false;
        }
            
    }
    private static boolean checkForGroupMatch( int cards[] )
    {
            int groupValue = ((Card)Croupier.standardDeck.cards.get( cards[0] )).getValue();
            
            for( int i = 0; i < Array.getLength(cards); i++ )
            {
                int currentValue = ((Card)Croupier.standardDeck.cards.get( cards[i] )).getValue();
                if( currentValue != groupValue)
                {
                    return false;
                }
            }
            return true;
    }
    //server comands
    private static void sendTurn()
    {
            for( int i = 0; i <= turn; i++ )
            {
                Server.printToPlayer("/turn "+(turn-i),i);
            }
            for( int i = 1; i < players-turn; i++ )
            {
                Server.printToPlayer("/turn "+(players-i),turn+i);
            }
    }
    public static void sendOpponentCards( )
    {
        for( int i = 0; i < players; i++ )
        {
            Server.printToPlayer("/oppcards "+getOpponentCards( i ),i);
        }
    }
    public static String getOpponentCards( int player_id )
    {
        //        player_id+1 player_id+2 << players id1 id2 << player_id
        String result = "";
        for( int i = player_id + 1; i < players; i++ )
        {
            result += " " + playersCards[ i ].size();
        }
        for( int i = 0; i < player_id; i++ )
        {
            result += " " + playersCards[ i ].size();
        }
        return result;
    }
    
    public static void sendMyCards( int player_id )
    {
        Server.printToPlayer("/mycards " + getMyCards( player_id ),  player_id );
    }
    public static String getMyCards( int player_id )
    {
        //no cards for observer!
        if( player_id != -1 )
        {
            //System.out.println("GMK req "+ player_id);
            int hasCards = playersCards[ player_id ].size();
            String result = "";
            for( int i = 0; i < hasCards; i++ )
            {
                result += standardDeck.cards.indexOf( playersCards[ player_id ].get(i) ) + " ";
            }
            //System.out.println(result);
            return result;
        }
        else
        {
            return "";
        }
    }
    
    public static String getDeckCards()
    {
        //last card is kozqrj, ostalnqe unknown, ili prosto 6esterkami zabombim? ;-)
        String result = standardDeck.cards.indexOf( trump ) +" " + deckCards.size();
        return result;
    }
    public static void sendDeckCards()
    {
        Server.printToAll( "/deckcards "+ getDeckCards() );
    }
    public static void sendTableCards()
    {
        Server.printToAll("/table"+getTableCards());
    }
    public static String getTableCards()
    {
        // /tablecards 11-23 13 23 24-21
        String result = "";
        for( int i = 0; i < tableCards.size(); i++ ) 
        {
            Card currentPair[] = (Card[])(tableCards.get(i));
            result += " " + standardDeck.cards.indexOf( currentPair[0] );
            if( currentPair[1] != null ) 
            {
                result += "-" + standardDeck.cards.indexOf( currentPair[1] );
            }
        }
        return result;
    }
    
    public static void clearTable()
    {
        GameScreen.tablePanel.cardGroup.clear();
        GameScreen.tablePanel.repaint();
    }
    public static void setTableCards(Vector pairs)
    {
        // /tablecards 11-23 13 23 24-21
        //flush tablecards
        GameScreen.tablePanel.cardGroup.clear();
        for( int i = 0; i < pairs.size(); i++ )
        {
            int[] current = (int[])pairs.get(i);
            if( Array.getLength( current ) == 1 ) //client interface!!! add to table directly!
            {
                Card[] pair = new Card[2];
                pair[0] = (Card)standardDeck.cards.get( current[0] );
                pair[0].setSelected ( false );
                GameScreen.tablePanel.cardGroup.add( pair );
            }
            else
            {
                Card[] pair = new Card[2];
                pair[0] = (Card)standardDeck.cards.get( current[0] );
                pair[0].setSelected ( false );
                pair[0].setBeaten ( true );
                pair[1] = (Card)standardDeck.cards.get( current[1] );
                pair[1].setSelected ( false );
                pair[1].setBeaten ( true );
                GameScreen.tablePanel.cardGroup.add( pair );
            }
        }
        GameScreen.tablePanel.repaint();
        //assignTablePairs();
    }
    public static void assignTablePairs() // ?? depricated!!
    {
        GameScreen.tablePanel.flushCards();
        for( int i = 0; i < tableCards.size(); i++ )
        {
            GameScreen.tablePanel.cardGroup.add( tableCards.get(i) );
        }
        GameScreen.tablePanel.repaint();
    }
    
    public static void setBeatCard( int newcard, int oncard, int player )
    {
        if( player == nextPlayer ( turn ) ) //otbivaajushisa only ;)
        {
            Card newCard = (Card)standardDeck.cards.get( newcard );
            Card onCard = (Card)standardDeck.cards.get( oncard );
            
            if( !onCard.isBeaten() ) //karta uzhe pobita
            {
                if( newCard.isGreather( onCard ) || ( newCard.isSameSuit(trump) && !onCard.isSameSuit(trump) ) )
                {
                    tableAddToPair( newCard, onCard );
                    playersCards[player].remove( newCard );
                    
                    sendTableCards();
                    sendMyCards( player );
                    sendOpponentCards( );
                }
                else
                {
                    Server.printToPlayer("Select card with bigger value of the same suit(or use trump) to beat this card. (or click on card deck to take cards)", player);
                }
            }
            else
            {
                Server.printToPlayer("This card is already beaten!", player);
            }
        }
        else
        {
            Server.printToPlayer("Not your turn to beat cards!", player);
        }
    }
    
    public static void tableAddToPair( Card newcard )
    {
        Card pair[] = new Card[2];
        pair[0] = newcard;
        newcard.setSelected( false );
        tableCards.add( pair );
    }
    
    public static void tableAddToPair( Card newcard, Card oncard )
    {
        for( int i = 0; i < tableCards.size(); i++ )
        {
            Card pair[] = (Card[])tableCards.get(i);
            //System.out.println(pair[0].getName()+"!"+oncard.getName());
            //System.out.println(pair[0].getName()+"!"+oncard.getName());
            if( pair[0] == oncard )
            {
                newcard.setSelected( false );
                oncard.setSelected( false );
                newcard.setBeaten( true );
                oncard.setBeaten( true );
                pair[1] = newcard;
            }
        }
    }
    public static boolean findOnTableValueMatch( Card current )
    {
        for( int i = 0; i < tableCards.size(); i++ )
        {
            Card pair[] = (Card[])tableCards.get(i);
            for( int k = 0; k < 2; k++ )
            {
                if( pair[k] != null) {
                if( pair[k].isSameValue( current ) )
                    return true;
                }
            }
        }
        return false;
    }
    
    public static void takeCardsFromTable( int playerClicked )
    {
        //ubratj karq so stola v otboi esli vse kartq beaten=true ili playeru esli ne vse
        // dodatj vsem igrokam do 6ti kart
        //System.out.println("TAKING OFF!");
        if( playerClicked == nextPlayer( turn ) )
        {
            if( !isTableEmpty() ) // don't allow to skip turn ;'))
            {
                if( checkForAllBeaten() )
                { // move to otboy
                    //pobelitj karty
                    setTableCardsNotBeaten();
                    tableCards.clear(); // ;-)
                    //perevesti hod
                    turn = nextPlayer( turn );
                    nextPlayer = nextPlayer( turn );
                    GameScreen.tablePanel.setTurn(turn);
                }
                else
                { // move to player
                    moveCardsFromTableToPlayer();
                    //perevesti hod, propuskaja playera
                    turn = nextPlayer( turn );
                    turn = nextPlayer( turn );
                    nextPlayer = nextPlayer( turn );
                    GameScreen.tablePanel.setTurn(turn);
                }
                //dodatj kartq
                giveCardsToPlayersTillFull();
                //update everything
                updateEverything();
                //enable furst-free turn
                freeturn = true;
                //check for lose situation
                checkForLoseSituation();
            }
            else
            {
                Server.printToPlayer("Place at least one card.", playerClicked );
            }
        }
        else
        {
            Server.printToPlayer("It's not your turn!", playerClicked );
        }
        
    }
    private static void checkForLoseSituation()
    {
        //if there are no cards in deck 
        //and no-one expect one player got cards,
        //then it's a lose situation
        //System.out.println("!deck:"+deckCards.size());
        int looser = onlyOnePlayerGotCards();
        if( deckCards.size() == 0 && looser != -1 )
        {
            //declare him a looser
            Server.printToAll("GAME OVER!");
            Server.printToAll("/looser "+ Server.getNickNameByID( looser ));
            updateEverything();
            Server.stopServer();
            //System.out.println("*LOOSER*");
        }
    }
    private static int onlyOnePlayerGotCards()
    {
        boolean one = false;
        boolean more = false;
        int player = -1;
        for( int i = 0; i < players; i++ )
        {
            if( playersCards[i].size() != 0 )
            {
                //System.out.print("!"+i+" got cards" );
                player = i;
                if( one == true )
                {
                    more = true;
                }
                else
                {
                    one = true;
                }
            }
        }
        //System.out.println(more);
        if( !more )
            return player;
        else
            return -1;
    }
    public static void updateEverything()
    {
        sendTableCards();
        sendOpponentCards( );
        sendTurn();
        sendDeckCards();
        for( int i = 0; i < players; i++ )
        {
            sendMyCards( i ); // send new pack ;-) of cards
        }
    }
    
    private static void giveCardsToPlayersTillFull()
    {
        for( int i = 0; i < players; i++ )
        {
            for( int k = playersCards[i].size(); k < Durak.cardsPerPlayer; k++ )
            {
                if( deckCards.size() != 0 )
                {
                    playersCards[i].add( deckCards.get( 0 ) );
                    deckCards.remove( 0 );
                }
            }
        }
        
    }
    private static void moveCardsFromTableToPlayer( )
    {
        int playerTakes = nextPlayer( turn );
        for( int i = 0; i < tableCards.size(); i++)
        {
            Card[] pair = (Card[])tableCards.get(i);
            pair[0].setBeaten( false );
            playersCards[ playerTakes ].add( pair[0] );
            if( pair[1] != null )
            {
                pair[1].setBeaten( false );
                playersCards[ playerTakes ].add( pair[1] );
            }
        }
        tableCards.clear();
    }
    private static void setTableCardsNotBeaten()
    {
        for( int i = 0; i < tableCards.size(); i++)
        {
            Card[] pair = (Card[])tableCards.get(i);
            pair[0].setBeaten( false );
            if( pair[1] != null )
            {
                pair[1].setBeaten( false );
            }
        }
    }
    private static boolean checkForAllBeaten()
    {
        for( int i = 0; i < tableCards.size(); i++)
        {
            Card[] pair = (Card[])tableCards.get(i);
            if( !pair[0].isBeaten() )
            {
                return false;
            }
        }
        return true;
    }
    private static boolean isTableEmpty()
    {
        if( tableCards.size() == 0 )
            return true;
        else
            return false;
    }
    
}