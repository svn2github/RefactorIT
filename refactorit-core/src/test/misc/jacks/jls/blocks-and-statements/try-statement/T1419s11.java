
public class T1419s11 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (Exception e) {
            new Object() {
                int e;
            };
        }
    
    }
}
