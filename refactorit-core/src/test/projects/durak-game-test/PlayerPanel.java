import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class PlayerPanel extends ADrawPanel implements MouseListener {

    public PlayerPanel(int x_size, int y_size) {
        this.x_size = x_size;
        this.y_size = y_size;
        addMouseListener(this);
    }
    
    public Vector getSelected()
    {
        Vector selected = new Vector();
        for( int i = 0; i < cardGroup.size(); i++ )
        {
            Card current = (Card)(cardGroup.get(i));
            if( current.isSelected() )
            {
                selected.add( current );
            }
        }
        return selected;
    }
    private void sortCards()
    {
        for( int i = 0; i < cardGroup.size(); i++ )
        {
            for( int k = 0; k < cardGroup.size()-1; k++ )
            {
                Card current = (Card)cardGroup.get(k);
                Card next = (Card)cardGroup.get(k+1);
                if( Croupier.standardDeck.cards.indexOf( next ) > Croupier.standardDeck.cards.indexOf( current ) )
                {
                    cardGroup.setElementAt( 
                        cardGroup.set( 
                            cardGroup.indexOf( next ),
                            current ),
                        cardGroup.indexOf( current ) );
                }
            }
        }
    }
    
    private int shiftIncrement;
        
	public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            sortCards();
            
            g.setFont(new Font("Arial", Font.BOLD, 16)); // value
            g.drawString(text, x_size/2 - (text.length()/2)*font_size, font_size );
            

            for( int i = 0; i < cardGroup.size(); i++ ) {
                shiftIncrement = ((x_size - Card.width - ADrawPanel.corner_space)/ cardGroup.size());
                Card current = (Card)(cardGroup.get(i));
                current.setGraphics(g);
                current.placeTo(ADrawPanel.corner_space + i*shiftIncrement, ADrawPanel.corner_space);
                if( current.isBeaten() )
                {
                    current.setBeaten(false);
                }
                current.flipSide(true);
                current.draw();
            }
	}
        
        
    	public void mousePressed(MouseEvent evt) {
            
            int x = evt.getX();  
            int y = evt.getY(); 
	     
            if ( !evt.isMetaDown() ) {
                for( int i = 0; i < cardGroup.size(); i++ ) {
                    Card current = (Card)(cardGroup.get(i));
                    if( (current.getX() < x) && (x < current.getX() + shiftIncrement) && (x < current.getX() + current.width) )
                    {
                        if( current.isSelected() )
                        {
                            current.setSelected( false );
                        }
                        else
                        {
                            current.setSelected( true );
                        }
                    }
                }
		//System.out.println("true mouse pressed at "+x+" "+y);
                repaint();
            }
            /*else {
		repaint();
            }*/	
	}
	
	public void mouseReleased(MouseEvent evt) {}
	public void mouseClicked(MouseEvent evt) {}
	public void mouseEntered(MouseEvent evt) {} 
	public void mouseExited(MouseEvent evt) {}
}

