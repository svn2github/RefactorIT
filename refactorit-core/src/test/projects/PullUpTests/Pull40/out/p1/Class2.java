// fails, not implemented
package p1;

class Class1 {
	public Class2.Inner tmp1;
}

public class Class2 extends Class1 {

// use qualified name of Inner
	public class Inner {
	}	
}
