
public class T15262xor14 {
    public static void main(String[] args) {
        
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	b ^= s;
	b ^= c;
	b ^= i;
	b ^= l;
	s ^= c;
	s ^= i;
	s ^= l;
	c ^= b;
	c ^= s;
	c ^= i;
	c ^= l;
	i ^= l;
    
    }
}
