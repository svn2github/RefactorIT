
public class T1420while15 {
    public static void main(String[] args) {
        
        while (true) {
            try {
                break;
            } finally {
                return;
            }
        }
        { int x=3; }
    
    }
}
