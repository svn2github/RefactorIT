package bingo.shared;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.Vector;

public class PlayerInfoPane extends JPanel implements PlayerListener {
    protected JScrollPane scrollPane;
    protected JTable cardsAndPlayers;
    protected PlayerInfoModel model;

    public PlayerInfoPane() {
	super(false); //XXX

	model = new PlayerInfoModel();
        cardsAndPlayers = new JTable(model);
	model.addTableModelListener(cardsAndPlayers);
	
	scrollPane = new JScrollPane(cardsAndPlayers);

	//Add the scroll pane to this panel.
	setLayout(new GridLayout(1, 0));
        add(scrollPane);

	scrollPane.setPreferredSize(new Dimension(300, 50)); //arbitrary 

        try {
            new PlayerListenerThread(this).start();
        } catch (java.io.IOException e) {
            //PENDING what to do?
        }
    }

    // Must be called from the event-dispatch thread.
    public void updatePlayer(PlayerRecord playerRecord) {
	model.updatePlayer(playerRecord);
    }

    // Must be called from the event-dispatch thread.
    public void clear() {
	model.clear();
    }
}
