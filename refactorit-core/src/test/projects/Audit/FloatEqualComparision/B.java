package FloatEqualComparision;


public class B {
  /* */
  float var_float1 = 1;
  float var_float2 = 2;
  double var_double1 = 1;
  double var_double2 = 2;
  int var_int1 = 1;
  int var_int2 = 2; 

	public B ()
	{
	
	}

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
    if ((new B()).var_float1 == var_float2){
    	var_float1 = 1;
    }
  }

  /**
   *@audit FloatEqualComparision
   */
  public void test3(){    
    if (this.var_float1 == var_float2){
    	var_float1 = 1;
    }
  } 
    
  /**
   *@audit FloatEqualComparision
   */
  public void test4(){
    if (((var_float1 - var_double1)) == var_float2){
    	var_float1 = 1;
    }
  }
 
  /**
   *@audit FloatEqualComparision
   */
  public void test5(){
 
    boolean hz;
    if (hz = (var_double1 + 0.1 == var_double2) ) {
    	var_float1 = 1;    	
    }
  }
  
  /**
   *@audit FloatEqualComparision
   */
  public void test6(){
   
    if ((double) var_int1 == var_double1) {
    	var_double1 = 1;
    }
  }
   
  /**
   *@audit FloatEqualComparision
   */
  public void test7(){
       
    if (retFloat() == var_double1){
    	var_double1 = 1;
    }
  }
  
  /**
   *@audit FloatEqualComparision
   */
  public void test8(){
     if (this.retFloat() == var_double1){
    	var_double1 = 1;
    }
  }
  
  /**
   *@audit FloatEqualComparision
   */
  public void test9(){    
     if ((new B()).retFloat() == var_double1){
    	var_double1 = 1;
    }
  }
  
  /**
   *@audit FloatEqualComparision
   */
  public void test10(){    
    float a = 5;
    if (a == var_float1)
    {
    	var_float2 = 4;
    }
  }
 
   /**
   *@audit FloatEqualComparision
   */
  public void test11(){ 
   float a = 5;
    if (a == 5 + 6)
    {
    	var_float2 = 4;
    }
    
 }
 
  /**
   *@audit FloatEqualComparision
   */
  public void test12(){
    float a = 5;
    if (a == 5)
    {
    	var_float2 = 4;
    }
    
  }
  
  /**
   *@audit FloatEqualComparision
   */
  public void test13(){
  
    float[] arr = new float[1];
    if (arr[0] == var_float2++)
    {
    	var_float1 = 4;
    }
  }
  
  
  public float retFloat()
  {
  	float a = 4;
  	return a;
  }
  
}
