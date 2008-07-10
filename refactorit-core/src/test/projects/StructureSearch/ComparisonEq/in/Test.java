class Test{
	A aObj = new A();
	B bObj = new B();
	C cObj = new C();
	D dObj = new D();
	boolean eq;
	
	public void test1(){
		eq = (aObj == aObj); // true
		eq = (aObj == bObj); // false
		eq = (aObj == cObj); // false
		eq = (aObj == dObj); // false
	}
	
	public void test2(){
		if(dObj == aObj); 
		if(dObj == bObj); 
		if(dObj == cObj); 
		if(dObj == dObj); 
	}
	
	public void test3(){
		int i = 10;
		int k = 20;
 		if(i == 10);
 		if(i == k);
	}
	
	public void test4(){
		String s1 = "String 1";
		String s2 = s1;
 		if(s1 == s2);
	}
	
	public void test5(){
		char c1 = 'c';
		char c2 = c1;
 		if(c1 == 'c');
 		if(c2 == c1);
 		
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
