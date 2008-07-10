
public class T1420try29 {
    public static void main(String[] args) {
        
        try {
            throw new RuntimeException();
        } finally {
        }
        { int x=3; }
    
    }
}
