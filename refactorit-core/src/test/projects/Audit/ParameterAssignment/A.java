package ParameterAssignment;


public class A {
  /**
   * @audit ParameterAssignment a.
   */
  void test1(int a){
    a = 1;
  }

  /**
   * @audit ParameterAssignment a
   */
  void test2(int a){
    while(a < 10){
       a++;
    }
  }
}
