
public class T1420switch24 {
    public static void main(String[] args) {
        
        switch (args.length) {
            default:
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        }
        { int x=3; }
    
    }
}
