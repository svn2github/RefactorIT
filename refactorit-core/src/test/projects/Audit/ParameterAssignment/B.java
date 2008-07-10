package ParameterAssignment;


public class B {
  /**
   * @audit ParameterAssignment a
   */
  void test1(String a){
    a = "null";
  }

  /**
   * @audit ParameterAssignment a
   */
  void test2(String a){
    while(a.length() < 10){
      a += "0";
    }
  }
  
  /**
   * @audit ParameterAssignment e
   */
  void test3(){
    try {
      throw new RuntimeException();
    } catch(Exception e){
       e = null;
    }
  }
}
