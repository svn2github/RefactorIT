package bingo.game;

import java.util.Properties;
import bingo.shared.*;

class GameParameters extends Parameters implements Constants {

    private long delay = TWENTY_SECONDS;
    private long countDown = FIVE_MINUTES;
    private int maxPlayers = 100;
    private int maxCards = 3;

    private String delayName = "ball.delay";
    private String countDownName = "count.down";
    private String maxPlayersName = "max.players";
    private String maxCardsName = "max.cards";

    GameParameters() {
        super("bingoServer.props", "BINGO Server Properties");
	getParameters();
    }

    protected void setDefaults(Properties defaults) {
	defaults.put(delayName, new Long(delay/ONE_SECOND).toString());
	defaults.put(countDownName, new Long(countDown/ONE_SECOND).toString());
	defaults.put(maxPlayersName, new Integer(maxPlayers).toString());
	defaults.put(maxCardsName, new Integer(maxCards).toString());
    }

    protected void updateSettingsFromProperties() {
	try {
	    delay = Long.parseLong(properties.getProperty(delayName)) * ONE_SECOND;
	    countDown = Long.parseLong(properties.getProperty(countDownName)) * ONE_SECOND;
	    maxPlayers = Integer.parseInt(properties.getProperty(maxPlayersName));
	    maxCards = Integer.parseInt(properties.getProperty(maxCardsName));
	} catch (NumberFormatException e) {
	    // we don't care if the property was of the wrong format,
	    // they've all got default values. So catch the exception
	    // and keep going.
	}
    }

    protected void updatePropertiesFromSettings() {
	properties.put(delayName, new Long(delay/ONE_SECOND).toString());
	properties.put(countDownName, new Long(countDown/ONE_SECOND).toString());
	properties.put(maxCardsName, new Integer(maxCards).toString());
	properties.put(maxPlayersName, new Integer(maxPlayers).toString());
    }

    public String toString() {
        return "["
               + "delay=" + delay + ","
               + "countDown=" + countDown + ","
               + "maxPlayers=" + maxPlayers + ","
               + "maxCards=" + maxCards + "]";
    }

    void setDelay(long delay) {
	this.delay = delay;
	saveParameters();
    }
    long getDelay() {
	return delay;
    }

    void setCountDown(long countDown) {
	this.countDown = countDown;
	saveParameters();
    }
    long getCountDown() {
	return countDown;
    }

    void setMaxPlayers(int maxPlayers) {
	this.maxPlayers = maxPlayers;
	saveParameters();
    }
    int getMaxPlayers() {
	return maxPlayers;
    }

    void setMaxCards(int maxCards) {
	this.maxCards = maxCards;
	saveParameters();
    }
    int getMaxCards() {
	return maxCards;
    }
}
