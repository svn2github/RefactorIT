
public class T1418l1 {
    public static void main(String[] args) {
        
        Object t = new Object();
        synchronized (t) {
            synchronized (t) {
                System.out.println("made it!");
            }
        }
    
    }
}
