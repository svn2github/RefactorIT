package bingo.shared;

public class PlayerRecord {
    public int ID = -1;
    public String name;
    public int numCards = 0;
    public int wolfCries = 0;

    public PlayerRecord(int ID, String name, int numCards) {

	this.ID = ID;
	this.name = name;
	this.numCards = numCards;

	this.wolfCries = 0;
    }

    public PlayerRecord(byte[] b) {
	this.ID = (int)b[0];
	this.numCards = (int)b[1];
	this.wolfCries = (int)b[2];

	byte[] nameBytes = new byte[b.length-3];
	System.arraycopy(b, 3, nameBytes, 0, nameBytes.length);
	this.name = new String(nameBytes);
    }

    public byte[] getBytes() {
        byte[] numbers = { (byte)ID, (byte)numCards, (byte)wolfCries };
	byte[] nameBytes = name.getBytes();

	byte[] answer = new byte[numbers.length + nameBytes.length];

	System.arraycopy(numbers, 0, answer, 0, numbers.length);
	System.arraycopy(nameBytes, 0,
			 answer, numbers.length, nameBytes.length);

	return answer;
    }
}
