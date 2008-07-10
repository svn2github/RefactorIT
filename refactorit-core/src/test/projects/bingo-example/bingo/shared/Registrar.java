package bingo.shared;

import java.rmi.*;

public interface Registrar extends Remote {

    public String whatsHappening()
        throws RemoteException;

    public Ticket mayIPlay(String playerName,
                           int numCards, long seed)
        throws RemoteException;

    public Answer BINGO(int playerID, Card c)
        throws RemoteException;
}
