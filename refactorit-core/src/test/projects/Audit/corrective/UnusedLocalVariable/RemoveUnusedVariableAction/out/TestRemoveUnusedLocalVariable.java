package corrective.UnusedLocalVariable.RemoveUnusedLocalVariable.in;

/**
*  @violations 5
*/
public class TestRemoveUnusedLocalVariable{

	public void test1(){
		
	}
	
	public void test2(){
		
	}
	
	public void test3(){
		boolean b = true;
		if(b){
				
		}
	}
	
	public void test4(){
		int x = 10;
		
		if( x == 2){
			x = 1;
		}
	}
	
	public void test5(){
		
	}
	
}