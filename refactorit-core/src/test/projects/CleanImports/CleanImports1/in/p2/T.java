package p2;

public class TestInnerClasses{
	public static class InnerClass{
		public static InnerClass INSTANCE = new InnerClass();
		
		public int m(){
			return 0;
		}
	}
}