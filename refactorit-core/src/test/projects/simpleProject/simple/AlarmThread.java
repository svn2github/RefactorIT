package simple;

class AlarmThread extends Thread {
    int mySleeper;
    AlarmClock myClock;
    AlarmThread(AlarmClock clock, int sleeperIndex) {
	super();
	mySleeper = sleeperIndex;
	myClock = clock;
    }
    public void run() {
	try {
	    sleep(myClock.sleepFor[mySleeper]);
	} catch (InterruptedException e) {}
	myClock.wakeUpSleeper(mySleeper);
    }
}
