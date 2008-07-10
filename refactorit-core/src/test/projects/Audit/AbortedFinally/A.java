package AbruptCompletionOfFinallyBlock;


public class A {
  /**
   * @audit ReturnInsideFinallyBlock
   */
  public String test1(){
    String result = null;
    try {
      result = usefulText();
    } finally {
      if(result == null){
        return "error";
      }
    }
    return result;
  }

  /**
   * @audit ThrowInsideFinallyBlock
   */
  public String test2(){
    String result = null;
    try {
      result = usefulText();
    } finally {
      if(result == null){
        throw new IllegalArgumentException();
      }
      result = "error";
    }
    return result;
  }

  /**
   */
  public String test3(){
    String result = null;
    try {
      result = usefulText();
    } finally {
      result = "error";
    }
    return result;
  }
  
  /**
   */
  private String usefulText(){
    return "Hello World!";
  }
}
