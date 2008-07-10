package bingo.player;

import java.awt.*;

/** Event posted when the user requests status. */
//should subclass some BingoPlayerEvent class that defines constants?
class StatusRequestEvent extends AWTEvent {
    public StatusRequestEvent(Component source) {
	super(source, AWTEvent.RESERVED_ID_MAX + 3);
    }
}
