import java.math.BigDecimal;

abstract class Test {
  private static String testString(){ }
  
  protected abstract String[][][] testStringArray();
	
	public static void main(){
	  String thisIsNotField;
	  Object thisAlsoIsNotField;
	  int blah;
	}
	
	Object testObject(){
	  //Should also find in anonymous inner classes
	  return new Object(){
	    public Object[][] testObjectArray(){ }
	  };
	}
	
	private class TestInnerClass{
	  private BigDecimal testBigDecimal(){ }
	}
	
	public BigDecimal[][][] testBigDecimalArray(){ }
	
	private void testVoid(){ }
	
	static abstract double testPrimitive1();
	
	private abstract short[] testPrimitive1();
	static short[][] testPrimitive2(){ }
	protected short[][][] testPrimitive3(){ }
	public static short[][][] testPrimitive3(){ }
}
