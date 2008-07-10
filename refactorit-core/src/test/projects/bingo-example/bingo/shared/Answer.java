
package bingo.shared;

public class Answer implements java.io.Serializable {
    public boolean didIWin;
    public String message;

    public Answer(boolean didIWin, String message) {
        this.didIWin = didIWin;
        this.message = message;
    }
}
