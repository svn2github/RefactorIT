
public class T15262sub14 {
    public static void main(String[] args) {
        
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	b -= s;
	b -= c;
	b -= i;
	b -= l;
	b -= f;
	b -= d;
	s -= c;
	s -= i;
	s -= l;
	s -= f;
	s -= d;
	c -= b;
	c -= s;
	c -= i;
	c -= l;
	c -= f;
	c -= d;
	i -= l;
	i -= f;
	i -= d;
	l -= f;
	l -= d;
	f -= d;
    
    }
}
