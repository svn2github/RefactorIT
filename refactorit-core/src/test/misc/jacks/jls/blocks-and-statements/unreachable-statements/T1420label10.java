
public class T1420label10 {
    public static void main(String[] args) {
        
        a: try {
            break a;
        } finally {
            return;
        }
        {}
    
    }
}
