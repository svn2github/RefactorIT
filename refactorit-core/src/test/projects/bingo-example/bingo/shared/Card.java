package bingo.shared;

import java.util.Random;
import java.util.Vector;
import java.util.Stack;
import java.io.Serializable;
import java.security.*;

// a Bingo card, created by the GameKeeper and signed
public class Card implements Serializable {
    public static final int SIZE = 5;
    public static char[] columnTitles = { 'B', 'I', 'N', 'G', 'O' };
    public BingoBall[][] boardValues = new BingoBall[SIZE][SIZE];

    private byte[] signature;

    public Card() {
	this(new Random(System.currentTimeMillis()));
    }

    public Card(Random generator) {
	int min=0, max=0;

	for (int i = 0; i < SIZE; i ++) {
            int numBalls = BingoBall.RANGE;
            Vector balls = new Vector(numBalls);
	    Stack randomBalls = new Stack();

	    switch (i) {
	    case 0:
		min = BingoBall.MIN;
		max = BingoBall.MAX_B;
		break;
	    case 1:
		min = max + 1;
		max = BingoBall.MAX_I;
		break;
	    case 2:
		min = max + 1;
		max = BingoBall.MAX_N;
		break;
	    case 3:
		min = max + 1;
		max = BingoBall.MAX_G;
		break;
	    case 4:
		min = max + 1;
		max = BingoBall.MAX_O;
		break;
	    }
                // generate all 15 balls in this range
            for (int j = min; j <= max; j++) {
                balls.addElement(new BingoBall(j));
	    }

                // randomize the 15 balls
            for (int k = BingoBall.RANGE - 1; k >= 0; k--) {
                int num = (int)(generator.nextDouble() * (k+1));
                randomBalls.push(balls.elementAt(num));
                balls.removeElementAt(num);
            }

		 // choose 5 of them to put on the card
	    for (int j = 0; j < SIZE; j ++) {
		boardValues[j][i] = (BingoBall)randomBalls.pop();
	    }
	}

	boardValues[2][2] = new BingoBall(BingoBall.FREE_SPACE);

    }

    public void setSignature(byte[] sig) {
	signature = sig;
    }

    public byte[] getSignature() {
	return signature;
    }
}
