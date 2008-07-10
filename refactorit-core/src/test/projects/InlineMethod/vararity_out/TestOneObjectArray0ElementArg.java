package vararity_out;
public class TestOneObjectArg {
	public void methodA(){
		Object[] vararityParam = new Object[] {};
		System.out.println(vararityParam);
		System.out.println(vararityParam[0]);
	}
	static void vararityMethod(Object... vararityParam){
		System.out.println(vararityParam);
		System.out.println(vararityParam[0]);
	}
}