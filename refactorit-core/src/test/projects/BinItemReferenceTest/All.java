import java.io.*;
import java.util.*;

public class All {
	private int     field1;
	private int     field2;
	private String  field3;
	
  public All( int parameter ){}
  
  public void method_1( String parameter ) {
  }

  public void method_1( int parameter ) {
  }

  public void method_2( String parameter1 ) {
  }

  public void method_2( String parameter, All parameter2 ) {
  }

  public void testBinParameterForCatchClause() {
    try {
    }
    catch( Exception parameter ) {
      parameter.printStackTrace();
    }
  }
  
  public void testThrowStatement_1( int x ) {
  	if( x == 1 )
  		throw new IllegalArgumentException();
  	
  	throw new IllegalArgumentException();
  }

	public void testThrowStatement_2( int x ) {		
		if( x == 1 )
			throw new IllegalArgumentException();
		else if( x == 2 ) 
		  throw new RuntimeException();
		  
		throw new RuntimeException();
	}
  
  public static void testNestedTryStatements() {
    try {
      System.out.println();
    } 
    finally {
      try {
        System.out.println();
      }
      catch (Exception e) {
        System.out.println();
      }
    }
  }
	
	public void testLocalVariable_and_methodInvocation_1() {
		int i=0;
		int h=0;
		String x="";
		
		for( int j = 0; j < 0; j++ )
			{ i+=j; h+=i; x+=h; }
			
	  All all = new All( 0 );
	}
	
	public void testLocalVariable_and_methodInvocation_2() {
		int i=2;
		int h=8;
		String x="d";
		
		for( int j = 0; j < 0; j++ )
			{ i+=j; h+=i; x+=h; }
			
		All all = new All( 0 );
		all.method_1( "string" );
		all.method_1( 0 );
		all.method_2( "string", all );
	}
	
	public void testFieldInvocation_1() {
		this.field1 = 1;
	  this.field2 = 2;
	  this.field3 = "3";
	}	
	
	public void testFieldInvocation_2() {
		this.field1 = 1;
	  this.field2 = 2;
	  this.field3 = "3";
	}
	
  public static class StaticInnerClass{
    public static class DoubleStaticInnerClass {}
    public class DynamicInnerClassInsideStaticInnerClass {}
  }
  
  public static class DynamicInnerClass{
    public class DoubleStaticDynamicClass {}
    public static class StaticInnerClassInsideDynamicInnerClass {}
  }
  
	public interface AnInterfaceTest{}
  
  protected String twoAnonymousClasses() {
    return 
      "" +
      new Object() {
        public String toString() { return "blaah"; }
      } +
      new Object() {
        public String toString() { return "blaah2"; }
      };
  }
  
  public String testNestedAnonymousClasses() {
    return "" + new Object() {
      public String toString() {
        return new Object() {}.toString();
      }
    };
  }
  
  public void testLocalTypes() {
  	class X {}
    class Y {}
  }
  
  public void testNestedLocalTypes() {
    class X {
      public void stuff() {
        class Y{};
      }
    }
  }
  
  public void testDirectlyNestedLocalTypes() {
    class X {
      class Y{
        class Z{}
      }
    }
  }
  
  // Anonymous classes outside methods
  Object x = new Object() {};
  { Object x = new Object() {}; }
  static { Object x = new Object() {}; }
  
  // Static initializer test
  static int staticInt;
  static {
    staticInt=0;
  }
  static {
    staticInt=2;
  }
  
  // "Dynamic" initializer test
  int dynamicInt;
  {
    dynamicInt=0;
  }
  {
    dynamicInt=2;
  }
  
  // Array type test
  int intArray[];
  float floatArray[];
  
  void localArrayTypes() {
    String nestedArrays[][];
  }
  
  public void testAnonymousInsideNamedLocal() {
    class X {
      Object o = new Object() {};
    }
  }
}
