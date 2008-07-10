package bingo.player;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.rmi.*;
import java.net.*;

import bingo.shared.*;

public class Player extends JPanel
		    implements ActionListener,
			       ItemListener,
			       BallListener {

    protected PlayerParameters params;

    protected static boolean DEBUG = false;

    protected ControlPane controlPane;
    protected GameStatusLabel gameStatusLabel;
    protected LightBoardPane lightBoardPane;
    protected JCheckBox beepButton;

    protected static int SMALLPAD = 5;
    protected static int BIGPAD = 20;

    static String register = "Join next game";
    static char registerKey = 'j';
    static String clear = "Reset";
    static char clearKey = 'r';
    static String beep = "Beep to announce new balls";
    static char beepKey = 'b';
    static String gameStatusTitle = "Game Status";
    static String windowTitle = "BINGO Player";

    private static Toolkit toolkit;

    Registrar registrar;
    int numCardWindows = 0;
    CardWindow[] cardWindows = new CardWindow[3];

    protected Ticket ticket;
    protected PlayerQueue playerQueue;

    public Player() {
	super(false);
	params = new PlayerParameters();

	playerQueue = new PlayerQueue(this);

	controlPane = new ControlPane(this);

        // status from the game
	JPanel statusPane = new JPanel(false);
	statusPane.setBorder(
		BorderFactory.createTitledBorder(
		    gameStatusTitle));
	statusPane.setLayout(new BoxLayout(statusPane, BoxLayout.Y_AXIS));

        gameStatusLabel = new GameStatusLabel();
	gameStatusLabel.setAlignmentX(0.0f);
	statusPane.add(gameStatusLabel);
        lightBoardPane = new LightBoardPane(0);
	lightBoardPane.setAlignmentX(0.0f);
	statusPane.add(lightBoardPane);

	//Choose where the app beeps whenever a ball arrives.
	beepButton = new JCheckBox(beep);
	beepButton.setSelected(params.getShouldBeep());
	beepButton.setMnemonic(beepKey);
	beepButton.addItemListener(this);
	beepButton.setAlignmentX(0.0f); 
	statusPane.add(beepButton);

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	controlPane.setAlignmentX(0.0f); //left align
        add(controlPane);
	statusPane.setAlignmentX(0.0f); //left align
        add(statusPane);

	//XXX hack to turn off old-style event handling:
	//XXX add any kind of listener to this component
	//XXX 1.1 only, I think
	addContainerListener(new ContainerAdapter(){});

	// Get current status.
	playerQueue.postEvent(new StatusRequestEvent(this));

	// Initialize the toolkit.
	toolkit = Toolkit.getDefaultToolkit();
    }

    /* Called from AWT event dispatch thread. */
    public void itemStateChanged(ItemEvent e) {
	if (e.getStateChange() == ItemEvent.SELECTED) {
	    params.setShouldBeep(true);
	} else {
	    params.setShouldBeep(false);
	}
    }

    /* Called from AWT event dispatch thread. */
    public void actionPerformed(ActionEvent e) {
        String factoryName = null;

	String command = e.getActionCommand();
        if (command == register) {
	    params.setNames(controlPane.nameField.getText(),
	                    controlPane.hostField.getText());
	    //We didn't used to do the following, since seed isn't
	    //a saved property, but the alternative (doing
	    //invokeAndWait when you really need the value)
	    //might be prone to deadlock.
	    params.setSeed(Long.parseLong(controlPane.seedField.getText()));
	    playerQueue.postEvent(new RegisterEvent(this));
	} else if (command == clear) {
	    clearGame();
        } else {
	    int numCards;
	    try {
		numCards = Integer.parseInt(command);
	        params.setNumCards(numCards);
	    } catch (Throwable exc) {
		//Ignore the action since we don't understand it.
	    }
	}
    }

    /* Can be invoked from any thread. */
    public void showServerStatus(String message) {
	if (gameStatusLabel != null) {
	    final String msg = message; //so we can access it in inner class
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
	    		gameStatusLabel.setText(msg);
		    }
		});
	} else {
	    System.err.println("Player.gameStatusLabel is null, "
			       + "so couldn't say: "
			       + message);
	} 

	if (DEBUG) {
	    System.out.println("showServerStatus: " + message);
	}
    }

    /**
     * BallListener method. 
     */
    public void noMoreBalls() {
	controlPane.gameOver();  //safe from any thread
	showServerStatus("Game Over."); //safe from any thread
    }

    /**
     * BallListener method.
     */
    public void ballCalled(BingoBall b) {
	final BingoBall ball = b; //for use in inner class
	if (ball.getNumber() != BingoBall.GAME_OVER) {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
                    lightBoardPane.displayNewBall(ball);
	            if (params.getShouldBeep()) {
			toolkit.beep();
		    }
	        }
	    });
	}
    }

    /* Called from action event handler (AWT event dispatch thread). */
    private void clearGame() {
	ticket = null;
	for (int i = 0; i < numCardWindows; i++) {
	    cardWindows[i].dispose();
	}
	controlPane.reset();
	lightBoardPane.clear();
    }

    /* Called from player queue thread. */
    void handleStatusRequestEvent(StatusRequestEvent event) {
	lookUpRegistrar(params.getHostname()); 
	if (registrar != null) {
	    String statusText = "";
	    try {
                statusText = registrar.whatsHappening();
	    } catch (java.rmi.ConnectException exc) {
		if (DEBUG) {
		    System.err.println("Not connected to Bingo server.");
		    System.err.println("registrar = " + registrar);
		}
		//XXX Update status?  We aren't connected to the Bingo
		//XXX server, but rmiregistry is running.
	    } catch (Exception exc) {
		System.err.println("Unexpected exception on status request.");
                exc.printStackTrace();
		return;
	    }
	    showServerStatus(statusText);  //safe from any thread
	}
    }

    /* Can be safely called from any thread. */
    private void lookUpRegistrar(String host) {
	if (registrar != null) 
	    return;

	try {
	    registrar = (Registrar)Naming.lookup("//"
	                + host + "/Registrar");
	} catch (java.rmi.NotBoundException exc) {
	    if (DEBUG) {
	        System.err.println("Couldn't find BINGO Server running on host "
			           + host + ".");
	        System.err.println("RMI seems to be running fine."); 
	    }
	    //XXX Advise them to start up BINGO server.
	} catch (java.rmi.UnmarshalException exc) {
	    System.err.println("Unmarshal exception on host "
			       + host + ".");
	    System.err.println("Try recompiling everything and starting over?");
	    System.err.println("registrar = " + registrar);
	    //XXX Advise the user?  Fatal error?
	} catch (java.rmi.ConnectException exc) {
	    if (DEBUG) {
	        System.err.println("RMI isn't running on "
				   + host + ".");
	 	System.err.println("Or maybe it is, but it "
				   + "started after this program.");
	    }
	    //XXX Update status?  We aren't connected to the Bingo
	    //XXX server, but rmiregistry is running.  I think.
	} catch (java.rmi.UnknownHostException exc) {
	    System.err.println("Unknown host: "
			       + host);
	    //XXX Update status?  Ask user to enter another host and retry?
	} catch (java.rmi.ConnectIOException exc) {
	    System.err.println("Couldn't get to host "
			       + host + ".");
	    //XXX Update status?  We might have had a network glitch.
	} catch (Exception exc) {
	    System.err.println("Unexpected exception when "
			       + "trying to find registrar.");
	    System.err.println("Exception type: "
			       + exc.toString());
            exc.printStackTrace();
	}
    }

    /* Called from player queue thread. */
    void handleRegisterEvent(RegisterEvent event) {
	if (registrar == null) {
	    lookUpRegistrar(params.getHostname()); 
	}
	if (registrar == null) {
	    return;  //XXX should do this with an exception instead.
	}

	if (ticket == null) {
	    long seed = params.getSeed(); 
	    lookUpRegistrar(params.getHostname()); 

	    //Get the ticket.
	    try {
	        ticket = registrar.mayIPlay(params.getName(),
			   	            params.getNumCards(),
					    seed);
	    } catch (java.rmi.ConnectException exc) {
	        if (DEBUG) {
	            System.err.println("Not connected to Bingo server.");
	            System.err.println("registrar = " + registrar);
	        }
	        //XXX Update status?  We aren't connected to the Bingo
	        //XXX server, but rmiregistry is running.
		return;
	    } catch (Exception exc) {
	        System.err.println("Unexpected exception on register attempt.");
	        exc.printStackTrace();
	        //XXX Update status?  
		return;
	    }

	    //React to registration results.
	    try {
	        if (ticket.ID != Ticket.DENIED) {
		    numCardWindows = ticket.cards.length;
		    final Player player = this;
		    SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			        controlPane.didRegister();
				repaint();
				//XXX Should delay the following
				//XXX (so there's feedback when you
				//XXX register)?
		                for (int i = 0; i < numCardWindows; i++) {
			            cardWindows[i] = new
				            CardWindow(ticket.cards[i],
					               player);
			            cardWindows[i].pack();
			            cardWindows[i].setVisible(true);
		                }
			    }
		        });
        	    try {
	    	        new BallListenerThread(this).start();
        	    } catch (java.io.IOException e) {
	                if (DEBUG) {
	                    System.err.println("IOException on "
			                       + "BallListenerThread "
					       + "creation/startup.");
        	        }
		    }
	        } else { 
		    showServerStatus(ticket.message);
		    ticket = null;
	        }
	    } catch (NullPointerException exc) {
	        System.err.println("NullPointerException; probably "
			           + "ticket was null");
	    } catch (Exception exc) {
	        System.err.println("Unexpected exception on register attempt.");
	        exc.printStackTrace();
	    }
	}
    }

    /* Called from player queue thread. */
    void handleIWonEvent(IWonEvent event) {
	if (registrar == null) {
            showServerStatus("This player isn't connected to a server: "
                             + "can't tell a server you won.");
	    return;
        }

	try {
	    Answer a = registrar.BINGO(ticket.ID, event.getCard());
	    if (a.didIWin) {
                showDialog(event.getCardWindow(),
		 	   "You won!");
            } else {
		showServerStatus(a.message);
                showDialog(event.getCardWindow(),
                           "You didn't win.");
            }
        } catch (RemoteException e) {
	    //...show status?
	    System.err.println("RMI Exception when "
			       + "informing server of win.");
	}
    }

    protected void showDialog(CardWindow cw, String status) {
	final String statusText = status;
	final CardWindow cardWindow = cw;
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		cardWindow.showStatusDialog(statusText);
	    }
	});
    }

    /** 
     * Requests that handleIWonEvent be called from
     * player queue thread.
     */
    void IWon(CardWindow cw) {
	playerQueue.postEvent(new IWonEvent(this, cw));
    }

    /*
     * Called from either player queue thread or AWT event dispatch
     * thread, as appropriate.
     * XXX Can be cleaner in 1.2.
     */
    protected void processEvent(AWTEvent event) {
        if (event instanceof StatusRequestEvent) {
            if (DEBUG) {
                System.out.println("Player processEvent received StatusRequestEvent");
            }      
            handleStatusRequestEvent((StatusRequestEvent)event);
        } else if (event instanceof RegisterEvent) {
            if (DEBUG) {
                System.out.println("Player processEvent received RegisterEvent");
            }      
            handleRegisterEvent((RegisterEvent)event);
        } else if (event instanceof IWonEvent) { 
            if (DEBUG) {
                System.out.println("Player processEvent received IWonEvent");
            }      
            handleIWonEvent((IWonEvent)event);
        } else {
	    super.processEvent(event);
	}
    }

    public static void main(String[] args) {
	JFrame frame = new JFrame(windowTitle);

	frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);	//XXX
		}
	    });

	Player player = new Player();
	frame.getContentPane().add("Center", player);
	frame.pack();
	frame.setVisible(true);
    }

    public static void fatalError(String message, Exception e) {
        e.printStackTrace();
        System.err.println(message);
        System.err.println("Exiting.....");
        System.exit(-1);
    }
}
