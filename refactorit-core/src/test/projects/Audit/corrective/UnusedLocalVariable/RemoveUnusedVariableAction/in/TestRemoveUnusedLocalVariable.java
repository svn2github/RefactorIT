package corrective.UnusedLocalVariable.RemoveUnusedLocalVariable.in;

/**
*  @violations 5
*/
public class TestRemoveUnusedLocalVariable{

	public void test1(){
		int k=0;
	}
	
	public void test2(){
		String f = "testString";
	}
	
	public void test3(){
		boolean b = true;
		if(b){
			Object o = new Object();	
		}
	}
	
	public void test4(){
		int x = 10;
		int y = 8;
		if( x == 2){
			x = 1;
		}
	}
	
	public void test5(){
		int f = "abc".charAt(1);
	}
	
}