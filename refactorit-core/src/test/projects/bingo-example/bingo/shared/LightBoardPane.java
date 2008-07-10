package bingo.shared;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This class displays the most recently called ball
 * in a panel on the left, and all of the
 * balls called in a lighted display on the right.
 * This class isn't thread-safe, so be sure to call
 * its methods from the AWT thread.
 */
public class LightBoardPane extends JPanel {
    JLabel[][] allBalls = new JLabel[Card.SIZE][BingoBall.RANGE];
    JLabel[] rowTitles = new JLabel[Card.SIZE];
    JPanel allBallsPane, newBallPane;

    JLabel newBallLabel = null;
    Color litColor = new Color(0.98f, 0.97f, 0.85f);

    public LightBoardPane(int ignored) {
	super(false); //XXX Maybe should double buffer?

	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	Font rowTitleFont = new Font("serif", Font.BOLD, 14);
	Font ballFont = new Font("serif", Font.PLAIN, 12);
	Font bigFont = new Font("serif", Font.BOLD, 36);

	GridLayout g;

	allBallsPane = new JPanel(false);
	g = new GridLayout(Card.SIZE, BingoBall.RANGE + 1);
	g.setHgap(5);
	g.setVgap(5);
	allBallsPane.setBorder(BorderFactory.createLoweredBevelBorder());
	allBallsPane.setLayout(g);
	allBallsPane.setBackground(Color.darkGray);

	for (int i = 0; i < Card.SIZE; i++) {
	    for (int j = 0; j < BingoBall.RANGE; j++) {
		if (j == 0) {
	            rowTitles[i] = new JLabel(new Character(Card.columnTitles[i]).toString(), JLabel.CENTER);
	            rowTitles[i].setFont(rowTitleFont);
	    	    rowTitles[i].setForeground(litColor);
		    allBallsPane.add(rowTitles[i]);
		}
	        allBalls[i][j] = new JLabel(new Integer((j+1)+(i*BingoBall.RANGE)).toString(), JLabel.CENTER);
	        allBalls[i][j].setFont(ballFont);
	        allBallsPane.add(allBalls[i][j]);
	    }
	}

	newBallPane = new JPanel(false);
	newBallPane.setLayout(new GridLayout(1, 1));
	newBallLabel = new JLabel("Waiting...", JLabel.CENTER);
	newBallLabel.setFont(bigFont);
	newBallPane.setBorder(BorderFactory.createLoweredBevelBorder());
	newBallPane.add(newBallLabel);
	newBallPane.setMinimumSize(newBallPane.getPreferredSize());
	newBallPane.setPreferredSize(newBallPane.getPreferredSize());

	add(newBallPane);
	add(allBallsPane);
    }

    /* This pane shouldn't get any taller than its preferred size. */
    public Dimension getMaximumSize() {
	Dimension d = getPreferredSize();
	d.width = Short.MAX_VALUE;
	return d;
    }

    /* Must be called from AWT thread. */
    public void displayNewBall(BingoBall b) {
	// light up the number in red
	int i = (b.number-1)/BingoBall.RANGE;
	int j = (b.number-1)%BingoBall.RANGE;
	allBalls[i][j].setForeground(litColor); 

	// change the new ball display
	if (b.number == BingoBall.GAME_OVER) {
	    newBallLabel.setText("Game Over");
	} else {
	    newBallLabel.setText(b.toString());
	}
	allBallsPane.repaint(); // XXXX should not be necessary
    }

    /* Must be called from AWT thread. */
    public void clear() {
	for (int i = 0; i < Card.SIZE; i++) {
	    for (int j = 0; j < BingoBall.RANGE; j++) {
	        allBalls[i][j].setForeground(Color.black);
	    }
	}
	newBallLabel.setText("Waiting...");
	allBallsPane.repaint(); // XXXX should not be necessary
    }
}
