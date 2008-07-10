package StaticMethodProposal;


/**
 */
class BSuper {
  /**
   */
  public void test1(){
    int a = 0;
  }
}

/**
 */
public class B extends BSuper {
  /**
   * Cannot staticalize - {@link BSuper#test1}.
   */
  public void test1(){
    int a = 0;
  }

  /**
   * Cannot staticalize - {@link #test2(String)}.
   */
  public void test2(Object object){
    int a = 0;
  }

  /**
   * Cannot staticalize - {@link #test2(Object)}.
   */
  public void test2(String string){
    int a = 0;
  }

  /**
   * Cannot staticalize - {@link BChild#test2}.
   */
  public void test3(){
    int a = 0;
  }
}

/**
 */
class BChild extends B {
  /**
   */
  public void test3(){
    int a = 0;
  }
}
