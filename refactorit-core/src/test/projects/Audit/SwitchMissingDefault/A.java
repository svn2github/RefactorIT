package MissingSwitchDefault;


public class A {
  /**
   */
  public void test1(){
    switch(value()){
      case 1:
      case 2:
      case 3:
        break;
      case 4:
        break;
      case 5:
      case 6:
        break;
      default:
        break;
    }
  }

  /**
   * @audit MissingSwitchDefault
   */
  public void test2(){
    switch(value()){
      case 1:
        break;
       case 2:
        break;
    }
  }

  private int value(){
    return (int)(System.currentTimeMillis() % 10L);
  }
}
