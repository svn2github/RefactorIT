
public class T1419s5 {
    public static void main(String[] args) {
        
        final int i = 1;
        new Object() {
            {
                try {
                    int j = i;
                    throw new Exception();
                } catch (Exception i) {
                }
            }
        };
    
    }
}
