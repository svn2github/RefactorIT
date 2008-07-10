class Test{
	A aObj = new A();
	B bObj = new B();
	C cObj = new C();
	D dObj = new D();
	boolean inherits;
	
	
	public void test1(){
		inherits = (aObj instanceof A); // true
		inherits = (aObj instanceof B); // false
		inherits = (aObj instanceof C); // false
		inherits = (aObj instanceof D); // false
	}
	
	public void test2(){
		inherits = (bObj instanceof A); // false
		inherits = (bObj instanceof B); // true
		inherits = (bObj instanceof C); // false
		inherits = (bObj instanceof D); // false
	}
	
	public void test3(){
		inherits = (cObj instanceof A); // true
		inherits = (cObj instanceof B); // false
		inherits = (cObj instanceof C); // true
		inherits = (cObj instanceof D); // false
	}
	
	public void test4(){
		inherits = (dObj instanceof A); // true
		inherits = (dObj instanceof B); // false
		inherits = (dObj instanceof C); // true
		inherits = (dObj instanceof D); // true
	}
}

public class A{
}

public class B{
}

public class C extends A{
}

public class D extends C{
}
