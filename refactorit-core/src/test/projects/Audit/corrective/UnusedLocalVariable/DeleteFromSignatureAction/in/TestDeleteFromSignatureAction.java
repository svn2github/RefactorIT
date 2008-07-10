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
	
	void myMeth(Aa ax){ 
	}
	
	void myMeth(Ab bx){
	}
	
	void myMeth(Ac cx){
	}
	
	void myMeth(Ab bx, Ac cx){
	}
	
	void myMethNew1(){
	}
	
	void myMethNew3(){
	}
}

public class B extends A{

	void myMeth(Aa ax){
	}
	
	void myMeth(Ab bx){
		bx = new Ab();
	}
	
	void myMeth(Ac cx){
	}
}

public class Aa{
}

public class Ab{
}

public class Ac extends Aa{
}



