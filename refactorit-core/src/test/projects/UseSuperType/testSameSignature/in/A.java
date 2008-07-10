interface I {
}	
public class A implements I{
	
	
}

public class B extends A {

	void test(A a) {}
	void test(B b) 	{
		test((A)b);
	}
	
}

