package DemoLoopCondition;

public class A{
	boolean a=true,b=true;
	/**	
	*  @audit DemoBoolViolation
	*/
	public void test1(){
		while(a = b){
			b=false;
		}
	}
	
	/**	
	*  @audit DemoBoolViolation
	*/
	public void test2(){
		for(; a = b; ){
			b=false;
		}
		
	}
	
	/**	
	*  @audit DemoBoolViolation
	*/
	public void test3(){
		do
		{
			b=false;
		}
		while(a = b);
	}
	
}