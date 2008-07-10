package bingo.shared;

import java.awt.*;
import javax.swing.*;

// used by the game keeper to show its status
public class OverallStatusPane extends JPanel
			       implements BallListener {
    GameStatusLabel gameStatusLabel;
    LightBoardPane lightBoardPane;
    PlayerInfoPane playerInfoPane;

    public OverallStatusPane() {
	super(false);

        // Do the layout.
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	Dimension pad = new Dimension(0, 5);

        // first row
	gameStatusLabel =  new GameStatusLabel();
	gameStatusLabel.setAlignmentX(0.0f); //left align
	add(gameStatusLabel);
	add(Box.createVerticalStrut(5));

	// second row
	lightBoardPane = new LightBoardPane(0);
	lightBoardPane.setAlignmentX(0.0f); //left align
	add(lightBoardPane);
	add(Box.createVerticalStrut(5));

	// third row
	playerInfoPane = new PlayerInfoPane();
	//playerInfoPane.setPreferredSize(new Dimension(300, 100));
	playerInfoPane.setMaximumSize(new Dimension(Short.MAX_VALUE,
						    Short.MAX_VALUE));
	playerInfoPane.setAlignmentX(0.0f);
	add(playerInfoPane);

        try {
            new BallListenerThread(this).start();
        } catch (java.io.IOException e) {
            System.err.println("IOException when creating/starting BallListenerThread.");
        }
    }

    /*
     * BallListener methods
     */
    public void noMoreBalls() {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
	            playerInfoPane.clear();
	            lightBoardPane.clear();
		}
	    });
    }

    public void ballCalled(BingoBall b) {
	final BingoBall ball = b; //cache so inner class can use it.
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    lightBoardPane.displayNewBall(ball);
		}
	    });
    }
}
