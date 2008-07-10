package DangerousCatch;


public class A {
  /**
   * @audit DangerousCatchThrowable
   */
  void test1(){
    try {
      exceptionalMethod();
    } catch(Throwable throwable){
    }
  }

  /**
   * @audit DangerousCatchError
   */
  void test2(){
    try {
      exceptionalMethod();
    } catch(Error error){
    }
  }

  /**
   */
  void test3(){
    try {
      exceptionalMethod();
    } catch(Exception exception){
    }
  }

  /**
   */
  void test4(){
    try {
       exceptionalMethod();
    } catch(RuntimeException exception){
    }
  }

  /**
   * @throws NumberFormatException Always.
   */
  private void exceptionalMethod(){
    Long.parseLong("error");
  }
}
