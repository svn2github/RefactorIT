package StaticMethodProposal;


public class A {
  /* */
  int instField = 0;

  /* */
  static int statField = 0;


  /**
   * @audit StaticMethodProposal
   */
  public void test1() {
    int a = 0;
  }

  /**
   */
  public void test2() {
    int a = 0;

    //
    this.instField = 1;
  }

  /**
   * @audit StaticMethodProposal
   */
  public void test3() {
    int a = 0;
    
    //
    statField = 1;
  }

  /**
   */
  public void test4() {
    setInstField(getInstField() + 1);
  }

  /**
   * @audit StaticMethodProposal
   */
  public void test5() {
    setStatField(getStatField() + 1);
  }

  /** 
   * @audit StaticMethodProposal
   */
  public void test6() {
    String test = "Hello World";
    test = test.substring(0, test.length());
  }

  /**
   */
  public void test7() {
    /**
     */
    class Inner {
      /**
       * @audit StaticMethodProposal
       */
      public void test1() {
        int a = 0;
      }
      
      /**
       */
      public void test2() {
        setInstField(getInstField() + 1);
      }
      
      /**
       * @audit StaticMethodProposal
       */
      public void test3() {
        setStatField(getStatField() + 1);
      }
    }
  }

  /**
   */
  private int getInstField() {
    return this.instField;
  }

  /**
   */
  private void setInstField(int instField) {
    this.instField = instField;
  }

  /**
   */
  private static int getStatField() {
    return statField;
  }

  /**
   */
  private static void setStatField(int statField) {
    A.statField = statField;
  }
}
