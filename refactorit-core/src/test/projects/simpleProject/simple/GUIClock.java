package simple;

import java.applet.Applet;
import java.awt.Graphics;
import java.util.*;

public class GUIClock extends Applet implements Sleeper {

    private AlarmClock clock;

    public void init() {
        clock = new AlarmClock();
    }
    public void start() {
        clock.letMeSleepFor(this, ONE_SECOND);
    }
    public void paint(Graphics g) {
        Date now = new Date();
        g.drawString(now.getHours() + ":" + now.getMinutes() + ":" + now.getSeconds(), 5, 10);
    }
    public void wakeUp() {
        repaint();
        clock.letMeSleepFor(this, ONE_SECOND);
    }
}

