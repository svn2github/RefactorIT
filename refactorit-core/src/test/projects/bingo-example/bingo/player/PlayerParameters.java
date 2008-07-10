package bingo.player;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;

import bingo.shared.*;

class PlayerParameters extends Parameters {

    private boolean shouldBeep = true;
    private String name = "";
    private String hostname = "";
    private int numCards = 1;
    private long seed; //NOT a saved property

    private String shouldBeepName =  "player.shouldbeep";
    private String nameName = "player.name";
    private String hostnameName = "server.name";
    private String numCardsName = "num.cards";

    PlayerParameters() {
	super("bingoPlayer.props", "BINGO Player Properties");

	try {
            hostname = InetAddress.getLocalHost().getHostName();
	} catch (java.net.UnknownHostException e) {
	    //do nothing
	}

	getParameters();
    }

    protected void setDefaults(Properties defaults) {
	defaults.put(shouldBeepName, new Boolean(shouldBeep).toString());
	defaults.put(nameName, name);
	defaults.put(hostnameName, hostname);
	defaults.put(numCardsName, new Integer(numCards).toString());
    }

    protected void updateSettingsFromProperties() {
	try {
	    String tmp;
	    tmp = properties.getProperty(shouldBeepName);
	    shouldBeep = new Boolean(tmp).booleanValue();

	    name = properties.getProperty(nameName);

	    hostname = properties.getProperty(hostnameName);

	    tmp = properties.getProperty(numCardsName);
	    numCards = Integer.parseInt(tmp);
	} catch (NumberFormatException e) {
            // we don't care if the property was of the wrong format,
            // they've all got default values. So catch the exception
            // and keep going.
	}
    }

    protected void updatePropertiesFromSettings() {
	properties.put(shouldBeepName,
		       new Boolean(shouldBeep).toString());
	properties.put(nameName,
		       name);
	properties.put(hostnameName,
		       hostname);
	properties.put(numCardsName,
		       new Integer(numCards).toString());
    }

    public String toString() {
	return "["
	       + shouldBeep + ","
	       + name + ","
	       + hostname + ","
	       + numCards + "]";
    }

    void setShouldBeep(boolean shouldBeep) {
	this.shouldBeep = shouldBeep;
	saveParameters();
    }
    boolean getShouldBeep() {
	return shouldBeep;
    }

    void setName(String name) {
	this.name = name;
	saveParameters();
    }
    String getName() {
	return name;
    }

    void setHostname(String hostname) {
	this.hostname = hostname;
	saveParameters();
    }
    String getHostname() {
	return hostname;
    }

    void setNames(String name, String hostname) {
	this.name = name;
	this.hostname = hostname;
	saveParameters();
    }

    void setNumCards(int numCards) {
	this.numCards = numCards;
	saveParameters();
    }
    int getNumCards() {
	return numCards;
    }

    void setSeed(long seed) {
	this.seed = seed;
    }
    long getSeed() {
	return seed;
    }

}
