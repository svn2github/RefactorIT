import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.BorderFactory; 
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;

public class LogoPanel extends JPanel {
    
    public LogoPanel() {
        setLayout(new BorderLayout(0,0));    
        
        Border loweredbevel;
        loweredbevel = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);        
        //Compound borders
        Border compound;
        Border space = BorderFactory.createEmptyBorder(0, 0, 0, 0); //BorderFactory.createLineBorder(Color.red);
        //This creates a nice frame.
        compound = BorderFactory.createCompoundBorder(loweredbevel, space);
        setBorder(compound);
        
        setBackground(Color.WHITE);
                
            for( int i = 0; i < flows; i++ ) {                    
                flowLevel[i] = maxFlowSize/2 +Math.round( (float)Math.random() * Durak.y_size/3);
                //flowLevel[i] = maxFlowSize/2 + i*(Durak.y_size/9);
                flowSize[i] = minFlowSize + Math.round( (float)Math.random() * ( maxFlowSize ) );
                //flowPosition[i] = - (flowSize[i] * text.length()) - i*20 - Math.round( (float)Math.random() * Durak.x_size/3);
                flowPosition[i] = - (flowSize[i] * text.length()) - i*Durak.x_size/8;
            }
        (new FastPainterThread(this)).start();
    }
    String text = "Durak ®";
    
    static int flows = 4;
    //static int paintingFlow[] = new int[flows];
    static float flowPosition[] = new float[flows];
    static int flowLevel[] = new int[flows];
    static int flowSize[] = new int[flows];
    static int minFlowSize = 15;
    static int maxFlowSize = 100 - minFlowSize;
    
	public void paintComponent(Graphics g) 
        {
            super.paintComponent(g);            
             
            
            for( int i = 0; i < flows; i++ ) {
                g.setFont(new Font("Arial", Font.ITALIC , flowSize[i])); // value
                g.drawString(text, Math.round(flowPosition[i]), flowLevel[i]);
                
                flowPosition[i] += 3*(float)flowSize[i]/maxFlowSize;
        
            
                if( flowPosition[i] > Durak.x_size/2 ) {
                    flowLevel[i] = maxFlowSize/2 + Math.round( (float)Math.random() * Durak.y_size/3);
                    flowSize[i] = minFlowSize + Math.round( (float)Math.random() * ( maxFlowSize ) );
                    flowPosition[i] = - Math.round((float)(flowSize[i] * text.length()));
                }
            }
            
	}
}

