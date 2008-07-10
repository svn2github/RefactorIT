package MethodCallsMethod;

public class Child extends Parent{
	
	Child myChild = new Child();

	/**
	 * @audit MethodCallsSuper
	 */
	public void setGrusha(int a, Child smth){
		super.setGrusha(a, this);
	}

	/**
	 *
	 */	
	public void getMulti(){
	
		a().b(); 
	}
	
	/**
	 *
	 */	
	public Child a (){
		
		return this;
	}

	/**
	 *
	 */	
	public int b () {
		return 5;
	}

	/**
	 *
	 */	
	public void delegate(){
		myChild.a();
	}
}

class Parent {

	private int grusha = 2;
	public void setGrusha(int b, Child asdsmth) {
	    this.grusha = b;
	}

}
