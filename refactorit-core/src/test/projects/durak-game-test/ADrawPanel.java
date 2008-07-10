import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.BorderFactory; 
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;

public class ADrawPanel extends JPanel {
    protected int x_size, y_size;
    static final int corner_space = 20;
    
    public ADrawPanel() {
        setLayout(new BorderLayout(0,0));    
        
        Border loweredbevel;
        loweredbevel = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);        
        //Compound borders
        Border compound;
        Border space = BorderFactory.createEmptyBorder(0, 0, 0, 0); //BorderFactory.createLineBorder(Color.red);
        //This creates a nice frame.
        compound = BorderFactory.createCompoundBorder(loweredbevel, space);
        setBorder(compound);
        
        setBackground(Color.white);     
    }
	
	protected Vector cardGroup = new Vector();
        
        public void addCard( Card card ) {
            cardGroup.add(card);
        }
        public void flushCards() 
        {
            cardGroup.clear();
        }
        public Card getCard(int index) {
            return (Card)cardGroup.get( index );
        }

	public void paintComponent(Graphics g) {
            
            super.paintComponent(g);
            
	}
	
        protected String text = "";
        protected int font_size = 15;
        
        public void setText(String text) {
            this.text = text;
        }
}

