
public class T1420try5 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        }
        { int x=3; }
    
    }
}
