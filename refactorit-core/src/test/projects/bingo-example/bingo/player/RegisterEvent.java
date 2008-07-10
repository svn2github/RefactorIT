package bingo.player;

import java.awt.*;

/** Event posted when the user wants to register. */
//should subclass some BingoPlayerEvent class that defines constants?
class RegisterEvent extends AWTEvent {
    public RegisterEvent(Component source) {
	super(source, AWTEvent.RESERVED_ID_MAX + 2);
    }
}
