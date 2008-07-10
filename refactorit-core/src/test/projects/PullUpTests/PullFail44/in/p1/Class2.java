package p1;
import p2.Class10;

public class Class2 extends Class10 {
	public Inner tmp1;

// cannot import Inner since it's package private and 
// new class is in another package
	class Inner {
	}	
}
