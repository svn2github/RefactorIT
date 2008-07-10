package bingo.game;

import bingo.shared.*;

class GamesThread extends Thread {

    private RingMaster ringMaster;
    private boolean moreGames = true;

    GamesThread(RingMaster ringMaster) {
	super("Bingo GamesThread");
	this.ringMaster = ringMaster;
    }

    public void run() {
	long now, startGameAt;
	int timeRemaining;

	while (moreGames) {
	    ringMaster.waitForFirstPlayer();

	    now = System.currentTimeMillis();
	    startGameAt = now + ringMaster.getGameParameters().getCountDown();

	    while (ringMaster.isCountingDown()) {
	        timeRemaining = (int)(Math.ceil((double)(startGameAt - now)/Constants.ONE_SECOND));
	        ringMaster.sendTimeRemainingMessage(timeRemaining);

	        try {
	            Thread.currentThread().sleep(Constants.FIVE_SECONDS);
	        } catch (InterruptedException e) {
	        }

	        now = System.currentTimeMillis();
	        if (now > startGameAt) {
 		    ringMaster.startGame();
		}
	    }
            new BallAnnouncer(ringMaster).start();

	    ringMaster.waitForGameToEnd();
	}
    }

    void noMoreGames() {
	moreGames = false;
    }
}
