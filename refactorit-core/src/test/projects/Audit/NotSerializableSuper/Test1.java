package NotSerializableSuperclass;
import java.io.*;

// ALL OK, Test1 is serilizable, doesn`t extend anything
/**
 *
 */
public class Test1 implements Serializable{
  
}

// ALL OK, Test2 is not serializable, doesnt` extend anything
/**
 *
 */
class Test2 implements java.awt.event.MouseListener{
  
  public void mouseClicked(java.awt.event.MouseEvent e) {
  }
  
  public void mouseEntered(java.awt.event.MouseEvent e) {
  }
  
  public void mouseExited(java.awt.event.MouseEvent e) {
  }
  
  public void mousePressed(java.awt.event.MouseEvent e) {
  }
  
  public void mouseReleased(java.awt.event.MouseEvent e) {
  }
  
}

// ALL OK, Test3 is serializable, extends serializable Test1
/**
 *
 */
class Test3 extends Test1{
  
}

// ALL OK, Test4 is serilizable, extends serializable Test1
/**
 *
 */
class Test4 extends Test1 implements Serializable{
  
}

// VIOLATION: Test5 serializable, extends Test2 (not serializable)
/**
 * @audit NotSerializableSuperWithConstr
 */
class Test5 extends Test2 implements Serializable{
  
}

// ALL OK: Test6 is serilizable, extends serializable Test3
/**
 *
 */
class Test6 extends Test3 implements Serializable{
  
}

// ALL OK: Test7 is serilizable because extends serializable Test3
/**
 *
 */
class Test7 extends Test3{
  
}

/**
 * @audit NotSerializableSuperWithConstr
 */
class Test8 extends HasPublicArgConstr implements Serializable{
  
}

/**
 * @audit NotSerializableSuper
 */
class Test9 extends HasPrivateArgConstr implements Serializable{
    
  public Test9(int aaa){
    super(aaa);
    int bbb = aaa;
    bbb = 4;
  }
}

/**
 * @audit NotSerializableSuperWithConstr
 */
class Test10 extends HasProtectedArgConstr implements Serializable{
  
}

/**
 *
 */
class Test11 extends Test10{
  
}

// extends a class from file, witch has no proper import for Serializable
/**
 * @audit NotSerializableSuperWithConstr
 */
class Test12 extends TestAdditional implements Serializable{
  
}

class HasPublicArgConstr {
  public HasPublicArgConstr(){
    
  }
}

class HasProtectedArgConstr {
  protected HasProtectedArgConstr(){
    
  }
}

class HasPrivateArgConstr {
  
  private HasPrivateArgConstr(){
    
  }
  
  public HasPrivateArgConstr(int aaa){
    int bbb = aaa;
    bbb = 3;
  }
}
