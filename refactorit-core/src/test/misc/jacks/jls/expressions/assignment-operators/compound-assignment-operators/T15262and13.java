
public class T15262and13 {
    public static void main(String[] args) {
        
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	s &= b;
	i &= b;
	i &= s;
	i &= c;
	l &= b;
	l &= s;
	l &= c;
	l &= i;
    
    }
}
