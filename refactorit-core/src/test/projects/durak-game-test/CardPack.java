import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.reflect.Array;

class Library { 
    
    //white
    
    static String sizes[] = {"small","medium"};
    static String colors[] = {"white","yellow","green"};
    static String faces[] = {"spade","heart","diamond","club"};
    public static Image[][][] collection = 
        new Image[ Array.getLength(faces) ][ Array.getLength(sizes)][ Array.getLength(colors) ];
    
    public static void load() { 
        for( int f = 0; f < Array.getLength(faces); f++) 
        {
            for( int s = 0; s < Array.getLength(sizes); s++) 
            {
                for( int k = 0; k < Array.getLength(colors); k++) 
                {
                    collection[f][s][k] = loadImage("images/"+sizes[s]+"/"+colors[k]+"/"+faces[f]+".gif");
                }
            }
        }
        //MUTE System.out.println("Image Library loaded.");
    }
    
    private static Image loadImage( String name ) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();       
        Image image;         
        image = toolkit.getImage(name);
        image.getWidth(null); // F.O.R.C.E. image to be loaded BEFORE it is needed
        return image;
    }
}

class UnknownCard extends Card {
    public UnknownCard() {          
            value_string = "0";
            value_name = "Unknown";
            value = 0;            
    }
    
    void layout() {}
}

abstract class Spade extends Card {
    
    public Spade() {     
        suit = 0;        
        small_img = Library.collection[0][0][0]; //face, size, color
        medium_img = Library.collection[0][1][0];
        suit_name = "Spade";
        suit_color = Color.black; 
    }
}

abstract class Heart extends Card {
    
    public Heart() {  
        suit = 1;        
        small_img = Library.collection[1][0][0];
        medium_img = Library.collection[1][1][0];
        suit_name = "Heart";
        suit_color = Color.red; 
    }
}

abstract class Diamond extends Card {
    
    public Diamond() {    
        suit = 2;         
        small_img = Library.collection[2][0][0];
        medium_img = Library.collection[2][1][0];
        suit_name = "Diamond";
        suit_color = Color.red; 
    }
}

abstract class Club extends Card {
    
    public Club() {  
        suit = 3;         
        small_img = Library.collection[3][0][0];
        medium_img = Library.collection[3][1][0];
        suit_name = "Club";
        suit_color = Color.black; 
    }
}

class SixClub extends Club {
    public SixClub() {          
            value_string = "6";
            value_name = "Six";
            value = 6;            
    }
    
    void layout() {
        layoutSix();
    }
}
class SevenClub extends Club {
    public SevenClub() {          
            value_string = "7";
            value_name = "Seven";
            value = 7;            
    }
    
    void layout() {
        layoutSeven();
    }
}
class EightClub extends Club {
    public EightClub() {          
            value_string = "8";
            value_name = "Eight";
            value = 8;            
    }
    
    void layout() {
        layoutEight();
    }
}
class NineClub extends Club {
    public NineClub() {          
            value_string = "9";
            value_name = "Nine";
            value = 9;
    }
    
    void layout() {
        layoutNine();
    }
}
class TenClub extends Club {
    public TenClub() {          
            value_string = "10";
            value_name = "Ten";
            value = 10;
    }
    
    void layout() {
        layoutTen();
    }
}
class JackClub extends Club {
    public JackClub() {          
            value_string = "J";
            value_name = "Jack";
            value = 11;
    }
    
    void layout() {
        layoutHigh();
    }
}
class QueenClub extends Club {
    public QueenClub() {          
            value_string = "Q";
            value_name = "Queen";
            value = 12;
    }
    
    void layout() {
        layoutHigh();
    }
}
class KingClub extends Club {
    public KingClub() {          
            value_string = "K";
            value_name = "King";
            value = 13;
    }
    
    void layout() {
        layoutHigh();
    }
}
class AceClub extends Club {
    public AceClub() {          
            value_string = "A";
            value_name = "Ace";
            value = 14;
    }
    
    void layout() {
        layoutHigh();
    }
}
//hearts
class SixHeart extends Heart {
    public SixHeart() {          
            value_string = "6";
            value_name = "Six";
            value = 6;            
    }
    
    void layout() {
        layoutSix();
    }
}
class SevenHeart extends Heart {
    public SevenHeart() {          
            value_string = "7";
            value_name = "Seven";
            value = 7;            
    }
    
    void layout() {
        layoutSeven();
    }
}
class EightHeart extends Heart {
    public EightHeart() {          
            value_string = "8";
            value_name = "Eight";
            value = 8;            
    }
    
    void layout() {
        layoutEight();
    }
}
class NineHeart extends Heart {
    public NineHeart() {          
            value_string = "9";
            value_name = "Nine";
            value = 9;
    }
    
    void layout() {
        layoutNine();
    }
}
class TenHeart extends Heart {
    public TenHeart() {          
            value_string = "10";
            value_name = "Ten";
            value = 10;
    }
    
    void layout() {
        layoutTen();
    }
}
class JackHeart extends Heart {
    public JackHeart() {          
            value_string = "J";
            value_name = "Jack";
            value = 11;
    }
    
    void layout() {
        layoutHigh();
    }
}
class QueenHeart extends Heart {
    public QueenHeart() {          
            value_string = "Q";
            value_name = "Queen";
            value = 12;
    }
    
    void layout() {
        layoutHigh();
    }
}
class KingHeart extends Heart {
    public KingHeart() {          
            value_string = "K";
            value_name = "King";
            value = 13;
    }
    
    void layout() {
        layoutHigh();
    }
}
class AceHeart extends Heart {
    public AceHeart() {          
            value_string = "A";
            value_name = "Ace";
            value = 14;
    }
    
    void layout() {
        layoutHigh();
    }
}
//Diamonds
class SixDiamond extends Diamond {
    public SixDiamond() {          
            value_string = "6";
            value_name = "Six";
            value = 6;            
    }
    
    void layout() {
        layoutSix();
    }
}
class SevenDiamond extends Diamond {
    public SevenDiamond() {          
            value_string = "7";
            value_name = "Seven";
            value = 7;            
    }
    
    void layout() {
        layoutSeven();
    }
}
class EightDiamond extends Diamond {
    public EightDiamond() {          
            value_string = "8";
            value_name = "Eight";
            value = 8;            
    }
    
    void layout() {
        layoutEight();
    }
}
class NineDiamond extends Diamond {
    public NineDiamond() {          
            value_string = "9";
            value_name = "Nine";
            value = 9;
    }
    
    void layout() {
        layoutNine();
    }
}
class TenDiamond extends Diamond {
    public TenDiamond() {          
            value_string = "10";
            value_name = "Ten";
            value = 10;
    }
    
    void layout() {
        layoutTen();
    }
}
class JackDiamond extends Diamond {
    public JackDiamond() {          
            value_string = "J";
            value_name = "Jack";
            value = 11;
    }
    
    void layout() {
        layoutHigh();
    }
}
class QueenDiamond extends Diamond {
    public QueenDiamond() {          
            value_string = "Q";
            value_name = "Queen";
            value = 12;
    }
    
    void layout() {
        layoutHigh();
    }
}
class KingDiamond extends Diamond {
    public KingDiamond() {          
            value_string = "K";
            value_name = "King";
            value = 13;
    }
    
    void layout() {
        layoutHigh();
    }
}
class AceDiamond extends Diamond {
    public AceDiamond() {          
            value_string = "A";
            value_name = "Ace";
            value = 14;
    }
    
    void layout() {
        layoutHigh();
    }
}
// spade
class SixSpade extends Spade {
    public SixSpade() {          
            value_string = "6";
            value_name = "Six";
            value = 6;            
    }
    
    void layout() {
        layoutSix();
    }
}
class SevenSpade extends Spade {
    public SevenSpade() {          
            value_string = "7";
            value_name = "Seven";
            value = 7;            
    }
    
    void layout() {
        layoutSeven();
    }
}
class EightSpade extends Spade {
    public EightSpade() {          
            value_string = "8";
            value_name = "Eight";
            value = 8;            
    }
    
    void layout() {
        layoutEight();
    }
}
class NineSpade extends Spade {
    public NineSpade() {          
            value_string = "9";
            value_name = "Nine";
            value = 9;
    }
    
    void layout() {
        layoutNine();
    }
}
class TenSpade extends Spade {
    public TenSpade() {          
            value_string = "10";
            value_name = "Ten";
            value = 10;
    }
    
    void layout() {
        layoutTen();
    }
}
class JackSpade extends Spade {
    public JackSpade() {          
            value_string = "J";
            value_name = "Jack";
            value = 11;
    }
    
    void layout() {
        layoutHigh();
    }
}
class QueenSpade extends Spade {
    public QueenSpade() {          
            value_string = "Q";
            value_name = "Queen";
            value = 12;
    }
    
    void layout() {
        layoutHigh();
    }
}
class KingSpade extends Spade {
    public KingSpade() {          
            value_string = "K";
            value_name = "King";
            value = 13;
    }
    
    void layout() {
        layoutHigh();
    }
}
class AceSpade extends Spade {
    public AceSpade() {          
            value_string = "A";
            value_name = "Ace";
            value = 14;
    }
    
    void layout() {
        layoutHigh();
    }
}

public class CardPack {
    
    public final static int number = 36;
        
    public Vector cards = new Vector();
        
    public CardPack() {
        
        //spades
        cards.add( new SixSpade() );
        cards.add( new SevenSpade() );
        cards.add( new EightSpade() );
        cards.add( new NineSpade() );
        cards.add( new TenSpade() );
        cards.add( new JackSpade() );
        cards.add( new QueenSpade() );
        cards.add( new KingSpade() );
        cards.add( new AceSpade() );
        //hearts
        cards.add( new SixHeart() );
        cards.add( new SevenHeart() );
        cards.add( new EightHeart() );
        cards.add( new NineHeart() );
        cards.add( new TenHeart() );
        cards.add( new JackHeart() );
        cards.add( new QueenHeart() );
        cards.add( new KingHeart() );
        cards.add( new AceHeart() );
        //diamonds
        cards.add( new SixDiamond() );
        cards.add( new SevenDiamond() );
        cards.add( new EightDiamond() );
        cards.add( new NineDiamond() );
        cards.add( new TenDiamond() );
        cards.add( new JackDiamond() );
        cards.add( new QueenDiamond() );
        cards.add( new KingDiamond() );
        cards.add( new AceDiamond() );
        //clubs
        cards.add( new SixClub() );
        cards.add( new SevenClub() );
        cards.add( new EightClub() );
        cards.add( new NineClub() );
        cards.add( new TenClub() );
        cards.add( new JackClub() );
        cards.add( new QueenClub() );
        cards.add( new KingClub() );
        cards.add( new AceClub() );
    }
}