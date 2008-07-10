package EmptyBlocksAndBodies;


public class A {
  /* */


  /**
   *
   */
  public void test1(){
    for (int i = 0; i < 10; i++)
    {
    	int k = 0;
	k = i;
        k++;
    }
  }
  
  /**
   *@audit EmptyBlocksAndBodies
   */
  
  public void test2(){ 
  
  
  }
  
  /**
   *@audit EmptyBlocksAndBodies
   */
  
  public void test3(){
    
    if ( true ) { 
        int a = 5;
    }
    else {
	
    }
  
  }
  
  /**
   *
   */
  
  public void test4(){
  
  	if (true) ; // empty statement, but not block
  }
  
  /**
   *@audit EmptyBlocksAndBodies
   */
  
  public void test5(){
  
  	if (true) { 
	
	}
  }
  
  /**
   *@audit EmptyBlocksAndBodies
   */
  
  public void test6(){
  
  	for ( ;true; ) { 
	
	}
  }
    
  /**
   *
   */
  
  public void test7(){
  
  	int b = 1;
  	switch (b)  {
		case 1: 
		
		case 2:
		 break;
	
	}
  }
  
}
