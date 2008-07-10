import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class DeckPanel extends ADrawPanel  implements MouseListener {

    public DeckPanel(int x_size, int y_size) {
        this.x_size = x_size;
        this.y_size = y_size;
        addMouseListener(this);
    }
    /*private Card trump;
    public void setTrump(Card trump)
    {
        this.trump = trump;
    }*/
    private Card trump;
    public void setTrump( Card trump )
    {
        if( this.trump == null )
        {
            this.trump = trump;
        }
        this.trump.setBeaten(false);
    }
    
	public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if( cardGroup.size() != 0 )
            {
                for( int i = 0; i < cardGroup.size(); i++ ) {
                    Card current = (Card)(cardGroup.get(i));
                    current.setGraphics(g);
                    current.placeTo(ADrawPanel.corner_space + i/2, ADrawPanel.corner_space - i/2);
                    current.flipSide(false);
                    current.draw();
                }
                //kozqrj
            }
            if( trump != null )
                {
                    trump.setGraphics(g);
                    trump.placeTo(2*ADrawPanel.corner_space + Card.width, ADrawPanel.corner_space);
                    trump.flipSide(true);
                    trump.draw();
                }

	}

        
    	public void mousePressed(MouseEvent evt) 
        {    
            int x = evt.getX();  
            int y = evt.getY(); 
	     
            if ( !evt.isMetaDown() ) 
            {
                Client.putText("/done");
            }

	}
	
	public void mouseReleased(MouseEvent evt) {}
	public void mouseClicked(MouseEvent evt) {}
	public void mouseEntered(MouseEvent evt) {} 
	public void mouseExited(MouseEvent evt) {}
}
