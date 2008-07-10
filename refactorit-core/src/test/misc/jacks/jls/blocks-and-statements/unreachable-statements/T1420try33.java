
public class T1420try33 {
    public static void main(String[] args) {
        
        try {
            new Object();
        } finally {
            return;
        }
        { int x=3; }
    
    }
}
