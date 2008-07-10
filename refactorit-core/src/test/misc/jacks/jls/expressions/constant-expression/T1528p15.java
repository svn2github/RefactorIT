
public class T1528p15 {
    public static void main(String[] args) {
        
        boolean t = true, f = false;
        switch (args.length) {
            case 0:
            case ((false && t) ? 0 : 1):
            case ((true || f) ? 2 : 0):
        }
    
    }
}
