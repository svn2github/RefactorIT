
public class T1528p17 {
    public static void main(String[] args) {
        
        int i1 = 1, i2 = 2;
        switch (args.length) {
            case 0:
            case (true ? 1 : i1):
            case (false ? i2 : 2):
        }
    
    }
}
