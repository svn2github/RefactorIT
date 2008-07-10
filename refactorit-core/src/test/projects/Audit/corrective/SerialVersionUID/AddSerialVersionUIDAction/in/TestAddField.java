package corrective.SerialVersionUID.AddSerialVersionUIDAction.in;
import java.io.Serializable;

/**
 * @violations 3
 */
public class TestAddField implements Serializable{

  public TestAddField (){
    
  }
  
}

class Test2 implements Serializable{
  
  private static final long serialVersionUID = 342342432;
  
  public Test2 (){
    
  }
  
}

class Test7 extends TestAddField {
   
    
  public Test7 (){
    
  }
  
}

class Test8 extends TestAddField {
  
  private final long serialVersionUID = 342342432;
    
  public Test8 (){
    
  }
  
}

class Test9 extends TestAddField {
  
  private static final long serialVersionUID = 342342432;
    
  public Test9 (){
    
  }
  
}
