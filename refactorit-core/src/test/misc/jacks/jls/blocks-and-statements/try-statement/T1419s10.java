
public class T1419s10 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (Exception e) {
            try {
                throw new Exception();
            } catch (Exception e) {
            }
        }
    
    }
}
