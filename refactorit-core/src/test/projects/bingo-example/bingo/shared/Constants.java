package bingo.shared;

public interface Constants {

	// various constants for the game

    public static final long ONE_SECOND = 1000;
    public static final long FIVE_SECONDS = ONE_SECOND * 5;
    public static final long TWENTY_SECONDS = ONE_SECOND * 20;
    public static final long ONE_MINUTE = ONE_SECOND * 60;
    public static final long FIVE_MINUTES = ONE_MINUTE * 5;

    public static final int MAX_WOLF_CRIES = 3;

	// this is the port number to which all information
	// is sent. this is an arbitrarily chosen number
	// based on my kid's birthday (5/25/96)
    public static final int portNumber = 52596;

	// when a message is sent to a multicast socket, it's
	// sent to a particular group on that socket.
	// a group is identified by an InetAddress which can be
	// specified by a string of the form %d.%d.%d.%d
	//
	// the bingo game sends information to three groups.
	// these inetaddresses are arbitrarily chosen based on nothing
    public static final String BallListeningGroup = "230.0.0.1";
    public static final String GameListeningGroup = "230.0.0.2";
    public static final String PlayerListeningGroup = "230.0.0.3";

}
