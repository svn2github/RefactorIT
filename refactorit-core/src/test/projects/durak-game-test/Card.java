import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


abstract class Card {
    
    static final int height = 140;
    static final int width = 90;
    static final int corner = 5;
    
    static final int small_img_size = 15; //rect, pixels
    static final int big_img_size = 30;
    static final int medium_img_size = 20;
    
    int value;
    int suit;    
    String suit_name;
    String value_name;
    String value_string;
    Image small_img, big_img, medium_img;
    Color suit_color;
    int x,y;
    Graphics g;
    boolean side;
    boolean beaten = false;
    
    abstract void layout();
    
    public void draw() {
        if( side == true ) {
            drawFront();    
        }
        else {
            drawBack();
        }
    }

    public boolean isSelected()
    {
        return selected;
    }
    public int getX()
    {
        return x;
    }
    
    public boolean isGreather( Card compare )
    {
        if( isSameSuit( compare ) && getValue() > compare.getValue() )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public boolean isLower( Card compare )
    {
        if( isSameSuit( compare ) && getValue() < compare.getValue() )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public boolean isSameValue( Card compare )
    {
        if( getValue() == compare.getValue() )
            return true;
        else
            return false;
    }
    public boolean isSameSuit( Card compare )
    {
        if( getSuit() == compare.getSuit() )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public int getSuit()
    {
        return suit;
    }
    
    public int getValue()
    {
        return value;
    }
        
    public void flipSide(boolean side) {
        this.side = side;
    }
    
    public void setGraphics( Graphics g ) {
        this.g = g;
    }
    
    public void placeTo(int x, int y) {
        this.x = x;
        this.y = y;
        //this.g = g;
    }
    
    public String getName() {
        return value_name + " " + suit_name;
    }
        
    private void drawBack() {
        g.setColor(Color.LIGHT_GRAY ); // gray area
        g.fillRoundRect(x, y, width, height, corner, corner);
        drawBorder();
    }
    private Color backgroundColor = Color.white;
    
    boolean selected = false;
    public void setSelected( boolean mode )
    {
        if( mode )
        {
            selected = true;
            small_img = Library.collection[suit][0][1];
            medium_img = Library.collection[suit][1][1];
            backgroundColor = Color.YELLOW;
        }
        else
        {
            small_img = Library.collection[suit][0][0];
            medium_img = Library.collection[suit][1][0];
            selected = false;
            backgroundColor = Color.white;
        }
    }
    public void setBeaten( boolean mode )
    {
            if( mode == true )
            {
                beaten = true;
                small_img = Library.collection[suit][0][2];
                medium_img = Library.collection[suit][1][2];
                backgroundColor = Color.green;
            }
            else
            {
                beaten = false;
                small_img = Library.collection[suit][0][0];
                medium_img = Library.collection[suit][1][0];
                backgroundColor = Color.white;
            }
    }
    public boolean isBeaten()
    {
        return beaten;
    }
    public void resetColor()
    {
        small_img = Library.collection[suit][0][0];
        medium_img = Library.collection[suit][1][0];
        backgroundColor = Color.white;
    }
    private void drawFront() {
        g.setColor( backgroundColor ); //white area!
        g.fillRoundRect(x, y, width, height, corner, corner);
        
        drawBorder();
        
        g.drawImage(small_img, x+3, y+20 , null); // small corner picture
        
        g.setColor(suit_color);
        if(value == 10) { //draw 10 little differently, shift a bit left
            g.setFont(new Font("Arial", Font.BOLD, 16)); // value
            g.drawString(value_string, x+1, y+17 );
        }
        else {
            g.setFont(new Font("Arial", Font.BOLD, 16)); // value
            g.drawString(value_string, x+6, y+17 );
        }
        
        layout();
    }
    private void drawBorder() {
        g.setColor(Color.black); //black border
        g.drawRoundRect(x, y, width, height, corner, corner);    
    }
    
    private void drawColumn(int rows, int x_shift) {
        for(int i = y + height/(rows+1) - medium_img_size/2 ; i < y + height- height/(rows+1); i += (height/(rows+1) ) ) {
            g.drawImage(medium_img,
                x + x_shift,
                i , null);
        }        
    }

    
    //card layouts by value is different
    protected void layoutSix() {
        drawColumn(3,width/3 - medium_img_size/2);
        drawColumn(3,width - width/3 - medium_img_size/2);        
    }
    
    protected void layoutSeven() {
        layoutSix();
        g.drawImage(medium_img,
                x + width/2 - medium_img_size/2,
                y + height/4 + height/8 - medium_img_size/2 , null);
    }
    
    protected void layoutEight() {
        drawColumn(4,width/3 - medium_img_size/2);
        drawColumn(4,width - width/3 - medium_img_size/2);        
    }
    
    protected void layoutNine() {
        layoutEight();
        g.drawImage(medium_img,
                x + width/2 - medium_img_size/2,
                y + height/5 + height/10 - medium_img_size/2 , null);        
    }
    
    protected void layoutTen() {
        layoutEight();
        g.drawImage(medium_img,
                x + width/2 - medium_img_size/2,
                y + height/5 + height/10 - medium_img_size/2 , null);        
        g.drawImage(medium_img,
                x + width/2 - medium_img_size/2,
                y + 3*height/5 + height/10 - medium_img_size/2 , null);        
    }
    
    protected void layoutHigh() {
        g.setColor(suit_color);
        g.setFont(new Font("Arial", Font.BOLD, 30)); // value
        g.drawString(value_string, 
                x + width/2 -10, 
                y + height/2 +10);
        
        g.drawImage(medium_img,
                x + width/2 - medium_img_size/2,
                y + height/5 + height/10 - medium_img_size/2 , null);        
        g.drawImage(medium_img,
                x + width/2 - medium_img_size/2,
                y + 3*height/5 + height/10 - medium_img_size/2 , null);  
        
    }
}