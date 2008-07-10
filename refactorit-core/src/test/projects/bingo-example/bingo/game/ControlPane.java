package bingo.game;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import bingo.shared.Constants;
import bingo.shared.Utilities;

public class ControlPane extends JPanel
			 implements ActionListener,
				    FocusListener {
    protected static final String go = 
	"Let the Games Begin";
    protected static final String stop = 
	"No More New Games";
    protected static final String delayString = 
	"Pause between balls (in seconds):";
    protected static final String countDownString = 
	"Countdown (in seconds):";
    protected static final String maxPlayersString = 
	"Maximum number of players:";
    protected static final String maxCardsString = 
	"Maximum number of cards per player:";
    protected static final String hostLabelString = 
	"This server's hostname:";

    // PENDING: should these number things be sliders to reduce risk of typos?
    protected JTextField delayField;
    protected JTextField countDownField;
    protected JTextField maxPlayersField;
    protected JTextField maxCardsField;

    protected JButton goButton;
    protected JButton stopButton;

    private RingMaster ringMaster;
    private GameParameters gameParameters;

    public ControlPane(String hostname, RingMaster ringMaster) {
	super(false);

	this.ringMaster = ringMaster;
	this.gameParameters = ringMaster.getGameParameters();

	    // create the properties fields
        JLabel delayLabel = new JLabel(delayString, JLabel.RIGHT);
        delayField = new JTextField(new
	    Long(gameParameters.getDelay()/Constants.ONE_SECOND).toString());
        delayField.setActionCommand(delayString);

        JLabel countDownLabel = new JLabel(countDownString, JLabel.RIGHT);
        countDownField = new JTextField(new 
	    Long(gameParameters.getCountDown()/Constants.ONE_SECOND).toString());
        countDownField.setActionCommand(countDownString);

        JLabel maxPlayersLabel = new JLabel(maxPlayersString, JLabel.RIGHT);
        maxPlayersField = new JTextField(new
 	    Integer(gameParameters.getMaxPlayers()).toString());
        maxPlayersField.setActionCommand(maxPlayersString);

        JLabel maxCardsLabel = new JLabel(maxCardsString, JLabel.RIGHT);
        maxCardsField = new JTextField(new
	    Integer(gameParameters.getMaxCards()).toString());
        maxCardsField.setActionCommand(maxCardsString);

        JLabel hostLabel = new JLabel(hostLabelString, JLabel.RIGHT);
        JLabel hostNameLabel = new JLabel(hostname);

	    // create the go and stop buttons
        goButton = new JButton(go);
	goButton.setMnemonic('g');
	goButton.setActionCommand(go);

        stopButton = new JButton(stop);
	stopButton.setMnemonic('s');
	stopButton.setActionCommand(stop);
	stopButton.setEnabled(false);

            // Register the listeners
        delayField.addActionListener(this);
        delayField.addFocusListener(this);
        countDownField.addActionListener(this);
        countDownField.addFocusListener(this);
        maxPlayersField.addActionListener(this);
        maxPlayersField.addFocusListener(this);
        maxCardsField.addActionListener(this);
        maxCardsField.addFocusListener(this);

	goButton.addActionListener(this);
	stopButton.addActionListener(this);

	    // Do the layout.
	JPanel parameterPane = new JPanel(false);
	parameterPane.setBorder(BorderFactory.createTitledBorder(
				  BINGO.controlPaneTitle));
	GridBagLayout gridbag = new GridBagLayout();
	parameterPane.setLayout(gridbag);

	    // many rows
	Utilities.addParameterRow(parameterPane,
				  delayLabel,
				  delayField);
	Utilities.addParameterRow(parameterPane,
				  countDownLabel,
				  countDownField);
	Utilities.addParameterRow(parameterPane,
				  maxPlayersLabel,
				  maxPlayersField);
	Utilities.addParameterRow(parameterPane,
				  maxCardsLabel,
				  maxCardsField);
	Utilities.addParameterRow(parameterPane,
				  hostLabel,
				  hostNameLabel);

	JComponent[] compList = new JComponent[2];
	compList[0] = goButton;
	compList[1] = stopButton;
	Box buttonBox = Utilities.makeEvenlySpacedBox(compList);

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	add(parameterPane);
	add(Box.createRigidArea(new Dimension(20, 20)));
	add(buttonBox);
	add(Box.createRigidArea(new Dimension(20, 20)));
    }

    private GamesThread gamesThread = null;

    public void focusLost(FocusEvent e) {
	//when a field loses the focus, generate an action event
	JTextField source;
	ActionEvent event;

	source = (JTextField)(e.getComponent());
	source.postActionEvent();
	//event = new ActionEvent(source,
				//ActionEvent.ACTION_PERFORMED,
				//source.getCommand());
	//actionPerformed(new 
	//ACK!  There's no JTextField getActionCommand or performAction!
	//is postActionEvent the same as the latter?
    }

    public void focusGained(FocusEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == go) {
	    if (gamesThread == null) {
	        gamesThread = new GamesThread(ringMaster);
	        gamesThread.start();
	        goButton.setEnabled(false);
	        stopButton.setEnabled(true);
	    }

        } else if (e.getActionCommand() == stop) {
	    if (gamesThread != null) {
	        gamesThread.noMoreGames();
	        gamesThread = null;
	        stopButton.setEnabled(false);
	        goButton.setEnabled(false);
	    }
        } else if (e.getActionCommand() == delayString) {
	    gameParameters.setDelay((Long.parseLong(delayField.getText()))*Constants.ONE_SECOND);

        } else if (e.getActionCommand() == countDownString) {
	    gameParameters.setCountDown((Long.parseLong(countDownField.getText()))*Constants.ONE_SECOND);

        } else if (e.getActionCommand() == maxPlayersString) {
	    gameParameters.setMaxPlayers(Integer.parseInt(maxPlayersField.getText()));

        } else if (e.getActionCommand() == maxCardsString) {
	    gameParameters.setMaxCards(Integer.parseInt(maxCardsField.getText()));

        }
    }

    public Dimension getMaximumSize() {
	Dimension d = getPreferredSize();
	d.width = Short.MAX_VALUE;
	return d;
    }
}
