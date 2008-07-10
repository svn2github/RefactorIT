package SelfAssignment;


public class B {
  /* */
  public String string = null;

  /**
   * @audit SelfAssignmentOnField string
   */
  public void test1(){
    string = string;
  }

  /**
   * @audit SelfAssignmentOnField string
   */
  public void test2(){
    this.string = string;
  }

  /**
   * @audit SelfAssignmentOnField string
   */
  public void test3(){
    string = this.string;
  }

  /**
   * @audit SelfAssignmentOnField string
   */
  public void test4(){
    this.string = this.string;
  }
}

class BA extends B {
  /**
   * @audit SelfAssignmentOnField string
   */
  public void test5(){
    super.string = string;
  }
  
  /**
   * @audit SelfAssignmentOnField string
   */
  public void test6(){
    super.string = this.string;
  }
  
  /**
   * @audit SelfAssignmentOnField string
   */
  public void test7(){
    super.string = super.string;
  }
}
