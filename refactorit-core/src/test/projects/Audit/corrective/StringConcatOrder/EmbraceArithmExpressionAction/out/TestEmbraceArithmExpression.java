package corrective.DemoStringConcat.EmbraceArithmExpression.in;

/**
*	@violations 3
*/
public class TestEmbraceArithmExpression{

	public void test1(){
		System.out.println((1 + 2) + "Test1");
	}
	
	public void test2(){
		System.out.println((0 + Math.PI) + "Test2");
	}
	
	public void test3(){
		String str = (3 + 5 + 8 + 12 + 144) + "Test3";
	}
	
	public void test4(){
		String str = ((3 + 5) + 8) + "Test4";
	}
	
	public void test5(){
		String str = (8 + (3 + 5)) + "Test5";
	}
}