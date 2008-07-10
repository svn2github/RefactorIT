import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class OpponentPanel extends ADrawPanel 
{
    private static int counter = 0;
    public final int id;
    
    public OpponentPanel(int x_size, int y_size) 
    {
        counter++;
        id = counter;
        
        this.x_size = x_size;
        this.y_size = y_size;
    }
	public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            g.setFont(new Font("Arial", Font.BOLD, 16)); // value
            g.drawString(text, x_size/2 - (text.length()/2)*font_size, font_size );
            
            
            for( int i = 0; i < cardGroup.size(); i++ ) {
                Card current = (Card)(cardGroup.get(i));
                current.setGraphics(g);
                current.placeTo(ADrawPanel.corner_space + i*((x_size - Card.width - ADrawPanel.corner_space)/ cardGroup.size()), ADrawPanel.corner_space);
                current.flipSide(false);
                current.draw();
            }
	}
}

