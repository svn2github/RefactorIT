package corrective.EmptyBlocksAndBodies.InsertEmptyBlockCommentAction.in;

  /**
   *@violations 4
   */
public class TestAddComments {

  public void test1(){
    for (int i = 0; i < 10; i++)
    {
    	int k = 0;
	k = i;
        k++;
    }
  }
  
  public void test2(){    /* CAUTION: empty block! */
 
  
  
  }
  
  public void test3(){
    
    if ( true ) { 
        int a = 5;
    }
    else {      /* CAUTION: empty block! */

	
    }
  
  }
  
  public void test4(){
  
  	if (true) ; // empty statement, but not block
  }
  
  public void test5(){
  
  	if (true) {      /* CAUTION: empty block! */
 
	
	}
  }
  
  public void test6(){
  
  	for ( ;true; ) {      /* CAUTION: empty block! */
 
	
	}
  }
    
  public void test7(){
  
  	int b = 1;
  	switch (b)  {
		case 1: 
		
		case 2:
		 break;
	
	}
  }
  
}
