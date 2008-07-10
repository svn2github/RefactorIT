class Test {
	int[] i = {2,3,4};

	static String[][] bonson = (String[][]) null;
	static Object bonson2 = bonson;
	private Object[][] bonson3 = (Object[][]) bonson2;

	static java.math.BigDecimal b = (java.math.BigDecimal) bonson2;

	public static void main(String[] args){
		String s = (String) m((Object) new Test(), (String) bonson2);
	}

	public static Object m(Object o, String s) {
		Test t = (Test) o;

		int[] ints = (int[]) bonson2;

		return String.valueOf(t);
	}
	
	private abstract class InnerClass{
	  public long longPrimitive;
	  private int intPrimitive = (int) longPrimitive;
	}
}
