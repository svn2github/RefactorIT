package IntDivFloatContext;


public class A {

  public A (float x){
    float y = x;
  }
  
 /**
  *@audit IntDivFloatContext
  */
  public A (float x, int z, int f){
    this(f/z);    
  }
  
  /**
   *@audit IntDivFloatContext
   */
  void test1(){
    int a = 3;
    int b = 4;
    float c = 3;
    
    float d = c * (a / b) + c;
  }
  
 /**
  *@audit IntDivFloatContext
  */
  void test2(){
    int a = 3;
    int b = 4;
    float c = 3;
    
    float d = (a / b);
  }
  
 /**
  *@audit IntDivFloatContext
  */
  void test3(){
    int b = 1;
    int c = 2;
    needsFloat(b/c);
  }
  
 /**
  *@audit IntDivFloatContext
  */
  void test4(){
    int b = 1;
    int c = 2;
    int ff = 1;
    needsFloat(ff, b/c);
  }
  
 /**
  *@audit IntDivFloatContext
  */
  void test5(){
    int b = 1;
    int c = 2;
    A a = new A(b/c);
  }
  
 /**
  *@audit IntDivFloatContext
  */
  void test6(){
    int b = 1;
    int c = 2;
    A a = new A(b/c, c, b);
  }
  
 /**
  *@audit IntDivFloatContext
  */
  void test7(){
    int b = 1;
    float h = 2;
    float a = (b/2) * h;
    
  }
  
 /**
  *@audit IntDivFloatContext
  *@audit IntDivFloatContext
  *@audit IntDivFloatContext
  *@audit IntDivFloatContext
  *@audit IntDivFloatContext
  */
  void test8(){
    int b = 1;
    int c = 1;
    float h = 2;
    float u = (b == c) ? (b/2) : h;
    u = (b == c) ? (b/2) : h;
    h = (b == c) ? h : (b/2);
    h = (b == c) ? c/4 : (b/2);
  }
     
 /**
  *@audit IntDivFloatContext
  */
  void test9(){
    int b = 1;
    float h = 2;
    float a = (1/2) * h;
    
  }
  
 /**
  *@audit IntDivFloatContext
  */
  void test10(){
    int a=1;
    int b=2;
     for(int x; a/b == 0.1 && x < 10; x++ )   {
       x++;
     }
  }
  
 /**
  * @audit IntDivFloatContext
  */
  void test11(){
    int a=1, b = 4;
    float c=10, d = 20;
    int e = 5, g;
     
    float j  = a/b, k = a+b;
  }
  
  void needsFloat(float a){
    float b = a;
  }
  
  void needsFloat(float a, float b){
    float c = b ;
  }
}
