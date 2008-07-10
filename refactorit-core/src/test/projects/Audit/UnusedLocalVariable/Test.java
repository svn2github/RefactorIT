class Test{
	A aObj = new A();
	B bObj = new B();
	C cObj = new C();
}

public class A{
	/**
	 * @audit UnusedParameter
	 */
	int myMeth(Aa ax){

		return 0;
	}


	/**
	 * @audit UnusedLocalVariable
	 * @audit UnusedLocalVariable

	 */
	int myMeth(){
		int k=1;
		String s = "myString";
		return 0;
	}

	/**
	 * @audit UnusedParameter
	 */
	int myMeth(Ac cx){
		return 0;
	}

}

public class B extends A{

	/**
	 * @audit UnusedParameter
	 * @audit UnusedParameter
	 */
	int myMeth(Aa ax, Ab cx){
		return 0;
	}
}

public class C extends B{

	/**
	 * @audit UnusedParameter
	 */
	int myMeth(Aa ax){    		// overridden
		return 0;
	}

	/**
	 * @audit UnusedParameter
	 * @audit UnusedParameter
	 */
	int myMeth(Aa ax, Ab bx){  // overridden
		return 0;
	}
}

public class D {
	void myMeth(int z){
		z = 10;
	}
}

public class Aa{

	/**
	 * @audit UnusedParameter
	 */
	int a(String f){
		return 0;
	}
}

public class Ab{

}

public class Ac extends Aa{

}



