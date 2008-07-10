package bingo.player;

import java.awt.*;
import javax.swing.*;
import bingo.shared.Utilities;

public class ControlPane extends JPanel {
    protected long seed = System.currentTimeMillis();

    protected static boolean DEBUG = false;

    protected JLabel nameLabel;
    protected JTextField nameField;
    protected String name = "";
    protected JLabel seedLabel;
    protected JTextField seedField;
    protected JLabel hostLabel;
    protected String hostString = "BINGO server's hostname:";
    protected JTextField hostField;
    protected JLabel cardsLabel;
    protected String cardsString = "Number of cards:";
    protected static int MAX_NUM_CARDS = 3; //XXX
    protected JRadioButton cardRB[];
    protected JButton registerButton;
    protected JButton clearButton;
    protected String parametersTitle = "Game Parameters";

    private Player player;

    protected int numCardWindows = 0;
    protected CardWindow[] cardWindows = new CardWindow[3]; //XXX

    public ControlPane(Player player) {
	//super(false);
	this.player = player;

	// The player can type their name.
        nameLabel = new JLabel("Your name:", JLabel.RIGHT);
        nameField = new JTextField(20);
	nameField.setText(player.params.getName());

	// The player can enter a seed value for their card.
        seedLabel = new JLabel("A random number seed:", JLabel.RIGHT);
        seedField = new JTextField(new Long(seed).toString(), 20);

	// The player can choose the number of cards to play (1-MAX_NUM_CARDS).
        cardsLabel = new JLabel(cardsString, JLabel.RIGHT);
	String rbString;
	char rbChar;
	int num;
        ButtonGroup group = new ButtonGroup();
        cardRB = new JRadioButton[MAX_NUM_CARDS]; //XXX
	for (int i = 0; i < MAX_NUM_CARDS; i++) {
	    num = i + 1;
	    rbString = new Integer(num).toString();
	    rbChar = rbString.charAt(0);
            cardRB[i] = new JRadioButton(rbString);
            cardRB[i].setActionCommand(rbString);
            cardRB[i].setMnemonic(rbChar); 
            cardRB[i].addActionListener(player); 
	    group.add(cardRB[i]);
	}
	int numCards = player.params.getNumCards();
	if (numCards < MAX_NUM_CARDS) {
	    cardRB[numCards - 1].setSelected(true);
	} else {
	    cardRB[MAX_NUM_CARDS - 1].setSelected(true);
	}

	//XXXNeed to delete Player.one, etc.

	// Choose the host that the GameKeeper is running on.
        hostLabel = new JLabel(hostString, JLabel.RIGHT);
        hostField = new JTextField(20);
	hostField.setText(player.params.getHostname());

	/*
	 * The player clicks this button when
	 * ready to register.
	 */
        registerButton = new JButton(Player.register);
	registerButton.setMnemonic(Player.registerKey);
	registerButton.setActionCommand(Player.register);
	registerButton.addActionListener(player);

	/* 
	 * The player clicks this button after a game
	 * is finished to clear the game.
	 */
        clearButton = new JButton(Player.clear);
	clearButton.setMnemonic(Player.clearKey);
	clearButton.setActionCommand(Player.clear);
	clearButton.addActionListener(player);
	clearButton.setEnabled(false);

	// Do the layout.
	JPanel parameters = new JPanel(false);
	GridBagLayout gridbag = new GridBagLayout();
	parameters.setLayout(gridbag);
	parameters.setBorder(BorderFactory.createTitledBorder(
				parametersTitle));

        // three rows
        Utilities.addParameterRow(parameters, nameLabel, nameField);
        Utilities.addParameterRow(parameters, seedLabel, seedField);
        Utilities.addParameterRow(parameters, hostLabel, hostField);

        // radio button row
        Box cardNumBox = Utilities.makeEvenlySpacedBox(cardRB);
        Utilities.addParameterRow(parameters, cardsLabel, cardNumBox);

        // register and clear buttons
	JComponent compList[] = new JComponent[2];
	compList[0] = registerButton;
	compList[1] = clearButton;
	Box buttonBox = Utilities.makeEvenlySpacedBox(compList);

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	add(parameters);
	add(Box.createRigidArea(new Dimension(Player.BIGPAD, 
					      Player.BIGPAD)));
	add(buttonBox);
	add(Box.createRigidArea(new Dimension(Player.BIGPAD, 
					      Player.BIGPAD)));
    }

    /** Maximum size == preferred height, unlimited width. */
    public Dimension getMaximumSize() {
	Dimension d = getPreferredSize();
	    d.width = Short.MAX_VALUE;
	return d;
    }


    /** Need not be called from AWT thread. */
    public void reset() {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    enableAll(true);
		    clearButton.setEnabled(false);
		}
	    });
    }

    /** Need not be called from AWT thread. */
    public void gameOver() {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    clearButton.setEnabled(true);
		}
	    });
    }

    /** Call this only from the AWT thread. */
    public void didRegister() {
	enableAll(false);
    }

    /** Call this only from AWT thread. */
    /*
     * This couldn't be private in 1.1 because it's called from
     * an inner class, which causes the 1.1.4 and earlier compilers
     * to hang.
     */
    void enableAll(boolean enable) {
        nameLabel.setEnabled(enable);
        seedLabel.setEnabled(enable);
        hostLabel.setEnabled(enable);
        cardsLabel.setEnabled(enable);

	//XXX JTextFields never become disabled (in Swing 0.5.1).
        nameField.setEnabled(enable);
        seedField.setEnabled(enable);
        hostField.setEnabled(enable);

	for (int i = 0; i < MAX_NUM_CARDS; i++) {
	    cardRB[i].setEnabled(enable);
	}
	registerButton.setEnabled(enable);

	repaint(); //XXX this shouldn't be necessary, but it is
    }
}
