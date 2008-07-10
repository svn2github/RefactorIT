
public class T1420label11 {
    public static void main(String[] args) {
        
        a: try {
            break a;
        } finally {
            return;
        }
        { int x=3; }
    
    }
}
