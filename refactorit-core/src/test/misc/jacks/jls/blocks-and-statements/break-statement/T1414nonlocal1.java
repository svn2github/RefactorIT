
public class T1414nonlocal1 {
    public static void main(String[] args) {
        
        do {
            new Object() {
                {
                    break;
                }
            };
        } while (false);
    
    }
}
