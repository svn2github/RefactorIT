package bingo.player;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

class NumberButton extends JToggleButton {
    static protected Font font;
    static protected ImageIcon selectedIcon, invisibleIcon;

    NumberButton(String label) {
	super(label);
	setHorizontalTextPosition(AbstractButton.CENTER);
	setFocusPainted(false);
	setBorderPainted(false);

	if (font == null) {
	    font = new Font("serif", Font.BOLD, 24);
 	}
	setFont(font);

	if (selectedIcon == null) {
	    selectedIcon = new ImageIcon("chit.gif");
	}
	setSelectedIcon(selectedIcon);

	/*
	 * No selected/pressed/rollover icons get shown unless
	 * the toggle button's default icon is non-null.  The 
	 * workaround is to create a transparent, full-sized icon 
	 * for the default icon.
	 */
	if (invisibleIcon == null) {
	    invisibleIcon = new ImageIcon("invisible.gif");
	}
	setIcon(invisibleIcon);
    }
}
