import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class TablePanel extends ADrawPanel  implements MouseListener {

    public TablePanel(int x_size, int y_size) {
        this.x_size = x_size;
        this.y_size = y_size;
        addMouseListener(this);
        
        /*addToPair( (Card)Croupier.standardDeck.cards.get(2) );
        addToPair( (Card)Croupier.standardDeck.cards.get(3) );
        addToPair( (Card)Croupier.standardDeck.cards.get(4) );
        addToPair( (Card)Croupier.standardDeck.cards.get(5) );
        addToPair( (Card)Croupier.standardDeck.cards.get(6) );*/
    }
    
    private int shiftIncrement;
    
    private int playerTurn = -1;
    public void setTurn( int playerID )
    {
        playerTurn = playerID;
        repaint();
    }
    private void drawTurnMark( Graphics g )
    {
        g.setColor(Color.red);
        
        if( playerTurn == 0 )
            g.fillOval(GameScreen.table_x_size/2, GameScreen.center_y_size - 20, 15, 15);
        if( playerTurn == 1 )
            g.fillOval(5, GameScreen.center_y_size/2, 15, 15);
        if( playerTurn == 2 )
            g.fillOval(5, 5, 15, 15);
        if( playerTurn == 3 )
            g.fillOval(GameScreen.table_x_size/2, 5, 15, 15);
        if( playerTurn == 4 )
            g.fillOval(GameScreen.table_x_size-20, 5, 15, 15);
        if( playerTurn == 5 )
            g.fillOval(GameScreen.table_x_size-20, GameScreen.center_y_size/2, 15, 15);
    }
    boolean gameOver = false;
    String looser = "";
    public void drawLooserName( String name )
    {
        gameOver = true;
        looser = name;
    }
    private void checkGameOver( Graphics g )
    {
        if( gameOver == true )
        {
            g.setColor( Color.red );
            font_size = 30;
            g.setFont(new Font("Arial", Font.BOLD, font_size)); // value
            String output = looser+" DURAK!";
            g.drawString(output, GameScreen.table_x_size/2 - Math.round( (float)(((float)output.length())/2*font_size*0.7) ), GameScreen.center_y_size/2 );
        }
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        //turn
        drawTurnMark( g );
        checkGameOver( g );
        
        //System.out.println("table paint called");
        for( int i = 0; i < cardGroup.size(); i++ ) {
            shiftIncrement = ((x_size - Card.width/2 - ADrawPanel.corner_space)/ cardGroup.size());
            Card currentPair[] = (Card[])(cardGroup.get(i));
            for( int k = 0; k < 2; k++ )
            {
                if( currentPair[k] != null ) {
                    currentPair[k].setGraphics(g);
                    currentPair[k].placeTo( ADrawPanel.corner_space + i*shiftIncrement, 
                            ADrawPanel.corner_space + k*35);
                    
                    currentPair[k].flipSide( true );
                    currentPair[k].draw();
                }
            }
        }
    }
    	public void mousePressed(MouseEvent evt) {
            
            int x = evt.getX();  
            int y = evt.getY(); 
	     
            if ( !evt.isMetaDown() ) {
                boolean beat = false;
                Vector moveCards = GameScreen.playerPanel.getSelected();
                
                for( int i = 0; i < cardGroup.size(); i++ ) 
                {
                    Card[] currentPair = (Card[])(cardGroup.get(i));
                    Card current = currentPair[0];
                    if( (current.getX() < x) && (x < current.getX() + shiftIncrement) && (x < current.getX() + current.width))
                    {
                        beat = true;
                        if( moveCards.size() == 1 ) {
                            Client.putText("/beat " + Croupier.standardDeck.cards.indexOf( current ) + " "+Croupier.standardDeck.cards.indexOf( moveCards.get(0) ));
                            //System.out.println("true mouse pressed at "+Croupier.standardDeck.cards.indexOf( current ));
                        }
                        else
                        {
                            PreGameScreen.chatArea.append( "Select ONE card to beat another ONE!\n" );
                        }
                    }
                }
                if( beat == false )
                {
                    String result = "";
                    for( int i = 0; i < moveCards.size(); i++ )
                    {
                        result += " " + Croupier.standardDeck.cards.indexOf( moveCards.get(i) );
                    }
                    if( result.compareTo("") != 0 )
                    {
                        Client.putText("/move"+ result );
                    }
                    else
                    {
                        PreGameScreen.chatArea.append( "Select at leat one card to make move!\n" );
                    }
                }
            }
	}
	
	public void mouseReleased(MouseEvent evt) {}
	public void mouseClicked(MouseEvent evt) {}
	public void mouseEntered(MouseEvent evt) {} 
	public void mouseExited(MouseEvent evt) {}
}

