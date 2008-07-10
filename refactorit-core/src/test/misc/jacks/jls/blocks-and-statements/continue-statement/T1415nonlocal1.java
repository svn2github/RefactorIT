
public class T1415nonlocal1 {
    public static void main(String[] args) {
        
        do {
            new Object() {
                {
                    continue;
                }
            };
        } while (false);
    
    }
}
