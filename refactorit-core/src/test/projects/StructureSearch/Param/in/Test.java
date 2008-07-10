import java.math.BigDecimal;

/**
 * All methods here should have only one parameter, so JUnit test can get it 
 * from method.
 */
abstract class Test {
  private static void testString(String first){ 
  }
  
  protected abstract void testObject(Object object1);
	
	public static void main(String args[][]){
	  String thisIsNotField;
	  Object thisAlsoIsNotField;
	}
	
	Object test3(){
	  //Should also find in anonymous inner classes
	  return new Object(){
	    public void testObjectArray(Object[][] objectArray1){
	    }
	  };
	}
	
	private class TestInnerClass{
	  private void testMultipleParameters(Object[][][][][] testString){
	  }
	}
	
	public void testPrimitive1(int primitive){ 
	}
	
	public void testPrimitive2(int[] primitive){ 
	}
	
	public void testPrimitive3(int[][] primitive){ 
	}
	
	public void testPrimitive4(int[][][] primitive){ 
	}
	
	private void testBigDecimal(BigDecimal bigDecimal){ 
	}
	
	static abstract void testWithoutParameters();
}
