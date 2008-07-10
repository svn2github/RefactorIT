package bingo.shared;

import javax.swing.table.*;
import java.util.Vector;

class PlayerInfoModel extends AbstractTableModel {
    protected static int NUM_COLUMNS = 4;
    protected static int START_NUM_ROWS = 5;
    protected int nextEmptyRow = 0;
    protected int numRows = 0;

    static final public String idName = "ID";
    static final public String playerName = "Player Name";
    static final public String cardNumName = "# of Cards";
    static final public String wolfNumName = "# of Wolf Cries";

    protected Vector data = null;

    public PlayerInfoModel() {
        data = new Vector();
    }

    public String getColumnName(int column) {
	switch (column) {
	  case 0:
	    return idName;
	  case 1:
	    return playerName;
	  case 2:
	    return cardNumName;
	  case 3:
	    return wolfNumName;
	}
	return "";
    }

    //XXX Should this really be synchronized?
    public synchronized int getColumnCount() {
        return NUM_COLUMNS;
    }

    public synchronized int getRowCount() {
        if (numRows < START_NUM_ROWS) {
            return START_NUM_ROWS;
        } else {
            return numRows;
        }
    }

    public synchronized Object getValueAt(int row, int column) {
	try {
            PlayerRecord p = (PlayerRecord)data.elementAt(row);
            switch (column) {
              case 0:
                return new Integer(p.ID);
              case 1:
                return p.name;
              case 2:
                return new Integer(p.numCards);
              case 3:
                return new Integer(p.wolfCries);
            }
	} catch (Exception e) {
	}
	return "";
    }

    public synchronized void updatePlayer(PlayerRecord playerRecord) {
        int ID = playerRecord.ID; //find the ID
        PlayerRecord p = null;
        int index = -1; 
        boolean found = false;
	boolean addedRow = false;
        
        int i = 0;
        while (!found && (i < nextEmptyRow)) {
            p = (PlayerRecord)data.elementAt(i);
            if (p.ID == ID) {
                found = true;
                index = i;
            } else {
                i++;
            }
        }

        if (found) { //update old player
	    data.setElementAt(playerRecord, index);
        } else { //add new player
	    if (numRows <= nextEmptyRow) {
		//add a row
                numRows++;
		addedRow = true;
            }
            index = nextEmptyRow;
	    data.addElement(playerRecord);
	}
    
        nextEmptyRow++;

	//Notify listeners that the data changed.
	if (addedRow) {
	    fireTableRowsInserted(index, index);
	} else {
	    fireTableRowsUpdated(index, index);
	}
    }

    public synchronized void clear() {
	int oldNumRows = numRows;

        numRows = START_NUM_ROWS;
	data.removeAllElements();
        nextEmptyRow = 0;

	if (oldNumRows > START_NUM_ROWS) {
	    fireTableRowsDeleted(START_NUM_ROWS, oldNumRows - 1);
	}
	fireTableRowsUpdated(0, START_NUM_ROWS - 1);
    }
}
