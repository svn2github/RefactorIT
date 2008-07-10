package simple;
public class AlarmClock {

    static final int MAX_CAPACITY = 10;
    static final int UNUSED = -1;
    static final int NOROOM = -1;

    private Sleeper[] sleepers = new Sleeper[MAX_CAPACITY];
    long[] sleepFor = new long[MAX_CAPACITY];

    AlarmClock () {
	for (int i = 0; i < MAX_CAPACITY; i++)
	    sleepFor[i] = UNUSED;
    }

    public synchronized boolean letMeSleepFor(Sleeper theSleeper,
                                              long time) {
        int index = findNextSlot();
        if (index == NOROOM) {
            return false;
        } else {
            sleepers[index] = theSleeper;
            sleepFor[index] = time;
            new AlarmThread(this, index).start();
            return true;
        }
    }

    private synchronized int findNextSlot() {
        for (int i = 0; i < MAX_CAPACITY; i++) {
            if (sleepFor[i] == UNUSED)
                return i;
        }
        return NOROOM;
    }

    synchronized void wakeUpSleeper(int sleeperIndex) {
        sleepers[sleeperIndex].wakeUp();
        sleepers[sleeperIndex] = null;
        sleepFor[sleeperIndex] = UNUSED;
    }
}
