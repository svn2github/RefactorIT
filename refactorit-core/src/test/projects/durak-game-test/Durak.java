import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Durak
{
    static final int x_size = 800;
    static final int y_size = 600;
    static final int maxPlayers = 6;
    static final int cardsPerPlayer = 6;
    
    public static void main(String[] args) {
        RunApplet.run(new DurakApplet(), x_size, y_size);
    }
}