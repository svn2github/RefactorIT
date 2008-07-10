
public class T1420synch5 {
    public static void main(String[] args) {
        
        synchronized (args) {
            return;
        }
        { int x=3; }
    
    }
}
