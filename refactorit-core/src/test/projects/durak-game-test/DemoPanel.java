import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.BorderFactory; 
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;

public class DemoPanel extends ADrawPanel {
    
    CardPack demoPack = new CardPack();
    
    public DemoPanel() {
        setLayout(new BorderLayout(0,0));    
        
        Border loweredbevel;
        loweredbevel = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);        
        //Compound borders
        Border compound;
        Border space = BorderFactory.createEmptyBorder(0, 0, 0, 0); //BorderFactory.createLineBorder(Color.red);
        //This creates a nice frame.
        compound = BorderFactory.createCompoundBorder(loweredbevel, space);
        setBorder(compound);
        
        //setBounds(100,100,100,100);
        //setSize( new Dimension(100,100) );
        setBackground(Color.gray);
        
        for( int i = 0; i < flows; i++ ) {
            flowPosition[i] = 20+Math.round( (float)Math.random() * Durak.x_size/3);
            flowLevel[i] = -Card.height - i*180;
            paintingCard[i] = Math.round( (float)Math.random() * ( CardPack.number - 1 ) );;
        }
        
        (new FastPainterThread(this)).start();
    }

    static int flows = 4;
    static int paintingCard[] = new int[flows];
    static int flowPosition[] = new int[flows];
    static int flowLevel[] = new int[flows];
    
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for( int i = 0; i < flows; i++ ) {
        
            ((Card)demoPack.cards.get( paintingCard[i] )).setGraphics(g);
            ((Card)demoPack.cards.get( paintingCard[i] )).placeTo( 
                flowPosition[i], 
                flowLevel[i]);
        
            ((Card)demoPack.cards.get( paintingCard[i] )).flipSide(true);
            ((Card)demoPack.cards.get( paintingCard[i] )).draw();
            
            flowLevel[i] += 1;
        
            
            if( flowLevel[i] > Durak.y_size ) {
                flowLevel[i] = - Card.height;
                flowPosition[i] = 20+Math.round( (float)Math.random() * Durak.x_size/3);
                paintingCard[i] = Math.round( (float)Math.random() * ( CardPack.number - 1 ) );
            }
        }
    }
}

