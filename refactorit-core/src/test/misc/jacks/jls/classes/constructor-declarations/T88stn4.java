
public class T88stn4 {
    public static void main(String[] args) {
        
        class Local {
            Local() {} // trigger jikes bug 179
            not_ctor() {}
        }
    
    }
}
