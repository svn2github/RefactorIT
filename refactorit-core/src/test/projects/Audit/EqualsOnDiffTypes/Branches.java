public class Branches{

	/**
	 * @audit EqualsOnDiffTypes
	 * @audit EqualsOnDiffTypes
   * @audit EqualsOnDiffTypesSameBranch
   * @audit EqualsOnDiffTypesSameBranch
	 * @audit EqualsOnDiffTypes
	 * @audit EqualsOnDiffTypes
	 */
	public void aaa (){
	
		D2 d2 = new D2();
		C1 c1 = new C1();
		
		if (d2.equals(c1)){
		
		}
		
		if (c1.equals(d2)){
		
		}
		
		B1 b1 = new B1();
		
		if (b1.equals(c1)){
		
		}
		
		if (c1.equals(b1)){
		
		}		
		
		A a = new A();
		Z z = new Z();
		
		if (a.equals(z)){
		
		}
		
		if (z.equals(a)){
		
		}
	
	}

}

class A {

	public boolean equals(Object o){
		return	this == o;
	}

}

class Z {
	
	public boolean equals (Object o){
	
		return this == o;
	}

}

class B1 extends A {



}

class B2 extends A {


}

class C1 extends B1 {


}

class C2 extends B2 {


}

class D2 extends C2 {


}
