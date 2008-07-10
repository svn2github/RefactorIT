package corrective.UnusedLocalVariable.DeleteFromSignatureAction.in;


class TestDeleteFromSignatureAction{
	A aObj = new A();
	B bObj = new B();
}

/**
*  @violations 6
*/
public class A{

	void myMeth(){
	}
	
	void myMethNew2(){ 
	}
	
	void myMeth(Ab bx){
	}
	
	void myMethNew4(){
	}
	
	void myMethNew5(){
	}
	
	void myMethNew1(){
	}
	
	void myMethNew3(){
	}
}

public class B extends A{

	void myMethNew2(){
	}
	
	void myMeth(Ab bx){
		bx = new Ab();
	}
	
	void myMethNew4(){
	}
}

public class Aa{
}

public class Ab{
}

public class Ac extends Aa{
}



