class Test {
	private int[] i1 = {2,3,4};
	protected int[] i2 = {2,3,4};
  int[] i3 = {2,3,4};
  public int[] i4 = {2,3,4};
	
  private float f1;
  private float f2;
  private boolean b1;
  private boolean b2;
  
	private static double[][] d1 = {2,3,4};
	private static double[][] d2 = {2,3,4};
	
	private char[][][][][] c;

	private static String[][][] testArrayString;
	private String testString;
	private static Object testObject;
	private Object[] testObjectArray;
	public static java.math.BigDecimal b;
	
	public static void main(String args[]){
	  String thisIsNotField;
	  Object thisAlsoIsNotField;
	  byte b1;
	}
	
	private class PrivateInnerClass{
	  private String thisShouldAlsoBeFound;
	}
}
