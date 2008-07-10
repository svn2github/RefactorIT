package SelfAssignment;


public class A {
  /* */
  String string = null;

  /**
   */
  public void test1(){
    string = (new A()).string;
  }

  /**
   */
  public void test2(){
    A interim = new A();
    string = interim.string;
    interim.string = string;
  }

  /**
   */
  public void test3(){
    this.string = (new A()).string;
  }
  
  /**
   */
  public void test4(){
    A interim = new A();
    this.string = interim.string;
    interim.string = this.string;
  }

  /**
   * @audit SelfAssignmentOnField string
   * @audit SelfAssignmentOnField string
   */
  public void test5(){
    A left = new A();
    A right = new A();
    left.string = right.string;
    left.string = left.string; // bug
    right.string = right.string; // bug
  }
}
