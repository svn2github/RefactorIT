package bingo.game;

import bingo.shared.*;

class BallAnnouncer extends Thread {

    private RingMaster ringMaster;

    BallAnnouncer(RingMaster ringMaster) {
        super("Bingo Ball Announcer");

	this.ringMaster = ringMaster;
    }

    public synchronized void run() {
        BagOfBalls bagOfBalls = (BagOfBalls)(new RandomBag());

        while (ringMaster.gameInProgress()) {
	    if (!ringMaster.isCheckingForWinner()) {
	        try {
		    ringMaster.announceBall(bagOfBalls.getNext());
	        } catch (NoMoreBallsException e) {
		    ringMaster.setGameOver();
	        }
	    }

	    try {
		wait(ringMaster.getGameParameters().getDelay());
	    } catch (InterruptedException e) { }
        }
    }
}
