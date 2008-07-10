package bingo.player;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.rmi.*;

import bingo.shared.*;

/*
 * TODO: Make number buttons prettier (use images).
 * Make headings prettier?  General layout tweaking.
 */
public class CardWindow extends JFrame 
			implements ActionListener {
    private Card card;
    Player player;
    static int cellsPerSide = Card.SIZE;

    public CardWindow(Card card, Player player) {
	super("Bingo Card");

	this.card = card;
	this.player = player;
        BingoBall[][] boardValues = card.boardValues;
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	PaperPane paper = new PaperPane(null);
	getContentPane().add("Center", paper);

	JPanel numberPanel = new JPanel(false); 
	numberPanel.setLayout(new GridLayout(cellsPerSide+1,
					     cellsPerSide));

	Heading heading;
	char c;
	for (int col = 0; col < cellsPerSide; col++) {
	    heading = new Heading(card.columnTitles[col]);
	    numberPanel.add(heading);
	}

	NumberButton button;
	int number;
	for (int col = 0; col < cellsPerSide; col++) {
	    for (int row = 0; row < cellsPerSide; row++) {
		number = boardValues[col][row].getNumber();
		if (number == BingoBall.FREE_SPACE) {
		    //XXX should use an icon for free space.
	            button = new NumberButton("Free");
		} else {
	            button = new NumberButton(Integer.toString(number));
		}
		numberPanel.add(button);
	    }
	}

	//XXX: Could make the winner button use an icon.
	JButton winner = new JButton("Bingo! I won! Bingo!");
	winner.addActionListener(this);

	//Now that we've created the components, do the layout.
	paper.setLayout(new BoxLayout(paper, BoxLayout.Y_AXIS));

	paper.add(numberPanel);

	//extra vertical space
	paper.add(Box.createVerticalStrut(10)); 

	winner.setAlignmentX(0.5f);
	paper.add(winner);

	//extra vertical space
	paper.add(Box.createVerticalStrut(10)); 
    }

    public void actionPerformed(ActionEvent e) {
	if (player != null) {
	    player.IWon(this);
	    if (Player.DEBUG) {
		System.out.println("CardWindow called player.IWon");
	    }
	} else {
	    System.err.println("player is null, so can't tell it "
			       + "to check for a win.");
	    showStatusDialog("player is null");
	}
    }

    public Card getCard() {
	return card;
    }

    //MUST be called from the event dispatching thread.
    public void showStatusDialog(String text) {
	JOptionPane.showMessageDialog(null, text);
    }

    public static void main(String[] args) {
	CardWindow cw = new CardWindow(new Card(), null);
	cw.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
		    System.exit(0);
	        }
	    });
	cw.pack();
	cw.setVisible(true);
    }
}

class Heading extends JLabel {
    static Font font = new Font("serif", Font.ITALIC, 36);

    Heading(char c) {
	super(String.valueOf(c), JLabel.CENTER);
	setFont(font);
    }
}
