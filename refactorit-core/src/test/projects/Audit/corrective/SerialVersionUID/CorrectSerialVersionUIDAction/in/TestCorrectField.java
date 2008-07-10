package corrective.SerialVersionUID.CorrectSerialVersionUIDAction.in;
import java.io.Serializable;

/**
 * @violations 4
 */
public class TestCorrectField implements Serializable{
  
/**
 *
 */
  private static final long serialVersionUID = 342342432;
  
  public TestCorrectField (){
    
  }
  
}

class Test3 implements Serializable{

  private final long serialVersionUID = 342342432;
  
  public Test3 (){
    
  }
  
}

class Test4 implements Serializable{
 
  private long serialVersionUID = 342342432;
  
  public Test4 (){
    
  }
  
}

class Test5 implements Serializable{
 
  private static final int serialVersionUID = 342342432;
  
  public Test5 (){
    
  }
  
}

class Test6 implements Serializable{
  
  private static long serialVersionUID = 342342432;
  
  public Test6 (){
    
  }
  
}
