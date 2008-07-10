package FloatEqualComparision;


public class A {
  /* */
  float var_float1 = 1;
  float var_float2 = 2;
  double var_double1 = 1;
  double var_double2 = 2;
  int var_int1 = 1;
  int var_int2 = 2; 

  /**
   *@audit FloatEqualComparision
   */
  public void test1(){
    if (var_float2 == var_float2) {
    	var_float1 = 1;
    }
  }
  
  /**
   *@audit FloatEqualComparision
   */
  public void test2(){
    if (var_double1 == var_float2) {
    	var_float1 = 1;
    }
  }

  /**
   *@audit FloatEqualComparision
   */
  public void test3(){
    if (var_float1 == var_double2) {
    	var_float1 = 1;
    }
  }
  
  /**
   *@audit FloatEqualComparision
   */
  public void test4(){
    if (var_float1 == var_int2) {
    	var_float1 = 1;
    }
  }
  
  /**
   */
  public void test5(){
    if (var_int1 == var_int2) {    // int and int, everything is ok
    	var_float1 = 1;
    }
  }

  /**
   *@audit FloatEqualComparision
   */
  public void test6(){
  	float a = 5;
	float b = 3;
	int g = 2;
	
	if (g * (a - b) == g)
	{
		g = 3;
	}
	
  }
  
  /**
   *@audit FloatEqualComparision
   */
  public void test7(){
  	float a = 5;
	float b = 3;
	int g = 2;
	
	if (returnsFloat() == a + b)
	{
		g = 3;
	}
	
  }
  
  /**
   *@audit FloatEqualComparision
   */
  public void test8(){
  	float a = 5;
	A first = new A();
		
	if (A.returnsFloat() + a  == a + returnsFloat())
	{
		a = 3;
	}
	
  }
  
    /**
   *@audit FloatEqualComparision
   */
  public void test9(){
  	float a = 5.5;
	float b = 6.6;
		
	if ( (a = 4.4) == b )
	{
		a = 3;
	}
	
  }
  
  public float returnsFloat() {
       float a = 7.5;
       return a;
  }
}
