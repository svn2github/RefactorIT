package DemoStringConcatOrder;

public class A{
	
	
	/**	
	*  @audit StringConcatViolation
	*/
	public void test1(){
		System.out.println(1+2+"Test1");
	}
	
	
	/**	
	*  @audit StringConcatViolation
	*/
	public void test2(){
		
		System.out.println(1+Math.PI+"Test2");
	}
	
	/**	
	*  @audit StringConcatViolation
	*/
	public void test3(){
		
		System.out.println((1+2)+4+"Test2");
	}
	
}