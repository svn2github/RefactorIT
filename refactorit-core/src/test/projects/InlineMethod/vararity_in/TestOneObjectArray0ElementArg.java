package vararity_in;
public class TestOneObjectArg {
	public void methodA(){
		/*]*/vararityMethod(new Object[] {});/*[*/
	}
	static void vararityMethod(Object... vararityParam){
		System.out.println(vararityParam);
		System.out.println(vararityParam[0]);
	}
}