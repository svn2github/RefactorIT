
public class T1415nonlocal2 {
    public static void main(String[] args) {
        
        a: do {
            new Object() {
                {
                    continue a;
                }
            };
        } while (false);
    
    }
}
