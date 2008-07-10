package bingo.game;

import java.util.Vector;
import bingo.shared.PlayerRecord;

class Roster {
    private Vector roster;
    private int nextPlayerID = 0;

    Roster() {
	roster = new Vector();
    }

    int nextPlayerID() {
	return ++nextPlayerID;
    }

    PlayerRecord searchForPlayerWithID(int ID) {
	int size = roster.size();

	for (int i = 0; i < size; i ++) {
	    PlayerRecord pl = (PlayerRecord)(roster.elementAt(i));
	    if (pl.ID == ID)
		return pl;
	}

	return null;
    }

    void addElement(PlayerRecord p, RingMaster ringMaster) {
	roster.addElement(p);

        if (roster.size() == 1)
            ringMaster.startCountDown();

	ringMaster.sendPlayerStatusMessage(p);
    }

    void removeAllElements() {
	roster.removeAllElements();
	nextPlayerID = 0;
    }

    int size() {
	return roster.size();
    }
}
