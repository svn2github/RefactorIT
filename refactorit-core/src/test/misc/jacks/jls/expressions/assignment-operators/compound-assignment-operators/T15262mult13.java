
public class T15262mult13 {
    public static void main(String[] args) {
        
	byte b = 1;
	short s = 1;
	char c = 1;
	int i = 1;
	long l = 1;
	float f = 1;
	double d = 1;
	s *= b;
	i *= b;
	i *= s;
	i *= c;
	l *= b;
	l *= s;
	l *= c;
	l *= i;
	f *= b;
	f *= s;
	f *= c;
	f *= i;
	f *= l;
	d *= b;
	d *= s;
	d *= c;
	d *= i;
	d *= l;
	d *= f;
    
    }
}
