package corrective.DemoLoopCondition.ReplaceBoolEquationAssignment.in;

/**
*  @violations 3
*/
public class TestReplaceBoolEquation{
	boolean a = true;
	boolean b = true; 
	
	public void test1(){
		while(a == b){
			b = false;
		}
	}
	
	public void test2(){
		for(;a == b;){
			b = false;
		}
	}
	
	public void test3(){
		do{
			b = false;
		}
		while(a == b);
	}
	
	public void test4(){
		boolean c = false;
		while((a = b) == c){
			c = true;
		}
	}
	
}