interface I {
}	
public class A implements I{
	
	
}

public class B extends A {

	void test(I a) {}
	void test(B b) 	{
		test((I)b);
	}
	
}

