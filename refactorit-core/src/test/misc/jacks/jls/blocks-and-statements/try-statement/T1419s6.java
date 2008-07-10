
public class T1419s6 {
    public static void main(String[] args) {
        
        int j = 1;
        for (final int i = 1; j < 1; )
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
