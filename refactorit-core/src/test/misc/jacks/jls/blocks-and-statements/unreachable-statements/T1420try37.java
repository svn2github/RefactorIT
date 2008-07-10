
public class T1420try37 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } finally {
            return;
        }
        { int x=3; }
    
    }
}
