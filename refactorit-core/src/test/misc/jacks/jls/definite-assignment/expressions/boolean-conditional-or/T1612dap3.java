
public class T1612dap3 {
    public static void main(String[] args) {
        
        boolean x, y = false;
        if (false || (false ? y : false))
            y = x;
    
    }
}
