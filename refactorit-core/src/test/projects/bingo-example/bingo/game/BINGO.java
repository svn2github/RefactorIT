package bingo.game;

import java.net.*;
import java.rmi.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import bingo.shared.*;

public class BINGO {
    protected static String windowName = "BINGO Server";
    protected static String controlPaneTitle = "Game Parameters";
    protected static String statusPaneTitle = "Game Status";
    protected static boolean DEBUG = false;

    public static void main(String[] args) {
        String hostname = null;
	RingMaster ringMaster = null;

	if (DEBUG) {
	    System.out.println("In BINGO main method.");
	}
	System.setSecurityManager(new RMISecurityManager());

	if (DEBUG) {
	    System.out.println("Just set security manager.");
	    System.out.println("About to try connecting.");
	}
	try {
	    ringMaster = new RingMaster();

            RegistrarImpl registrar = new RegistrarImpl(ringMaster);
	    hostname = InetAddress.getLocalHost().getHostName();
	    Naming.rebind("//" + hostname + "/Registrar", registrar);
	} catch (java.rmi.ConnectException e) {
	    ErrorMessages.fatalError("You must run rmiregistry before "
				     + "starting the BINGO server.",
				     e);
	} catch (java.net.UnknownHostException e) {
	    ErrorMessages.fatalError("Can't get current host.", e);
	} catch (java.rmi.RemoteException e) {
	    ErrorMessages.fatalError("Can't create the registrar.", e);
	} catch (java.net.MalformedURLException e) {
	    ErrorMessages.fatalError("Can't bind the registrar.", e);
	} catch (java.io.IOException e) {
	    ErrorMessages.fatalError("Can't open multicast socket.", e);
	} catch (Exception e) {
	    ErrorMessages.fatalError("Unknown exception on server startup", e);
	}

	if (DEBUG) {
	    System.out.println("Successfully connected.");
	}
        JFrame frame = new JFrame(windowName);
	if (DEBUG) {
	    System.out.println("Created JFrame.");
	}
	Container container = frame.getContentPane();
	if (DEBUG) {
	    System.out.println("Got content pane.");
	}
	//b.setHgap(5); //use rigid area instead.
	//b.setVgap(5); //use rigid area instead.
	ControlPane controlPane = new ControlPane(hostname, ringMaster);
	if (DEBUG) {
	    System.out.println("Created control pane.");
	}

	OverallStatusPane statusPane = new OverallStatusPane();
	if (DEBUG) {
	    System.out.println("Created status pane.");
	}
	statusPane.setBorder(BorderFactory.createTitledBorder(
				statusPaneTitle));

        frame.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
		    System.exit(0);
		} 
	    });
	if (DEBUG) {
	    System.out.println("About to call pack.");
	}

	container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	controlPane.setAlignmentX(0.5f);
	container.add(controlPane);
	statusPane.setAlignmentX(0.5f);

	//Does the following work?  We want the table to be able
	//to take up as much space as it can.
	statusPane.setMaximumSize(new Dimension(Short.MAX_VALUE,
						Short.MAX_VALUE));
	container.add(statusPane);
        frame.pack();
	if (DEBUG) {
	    System.out.println("About to call show.");
	}
        frame.setVisible(true);
	if (DEBUG) {
	    System.out.println("At end of BINGO.main.");
	}
    }
}
