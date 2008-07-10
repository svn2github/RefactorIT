package in;

/**
 *
 * @author Arseni Grigorjev
 * @violations 6
 */
public class TestA {
  
  public void test1(){
    {}
  }
  
  public void test2(){
    {}// simple comment
  }
  
  public void test3(){
    /* comment before */{}
  }
  
  public void test4(){
    {/* comment inside */}
  }
  
  public void test5(){
    {
      
    } // comment
  }
  
  public void test6(){
    {//comment
      
    }
  }
}
