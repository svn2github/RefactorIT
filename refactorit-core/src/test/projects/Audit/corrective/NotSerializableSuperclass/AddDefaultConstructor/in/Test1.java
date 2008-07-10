package corrective.NotSerializableSuperclass.AddDefaultConstructor.in;
import java.io.*;

/**
 * @violations 4
 */
public class Test1 implements Serializable{
  
}

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

class Test3 extends Test1{
  
}

class Test4 extends Test1 implements Serializable{
  
}

class Test5 extends Test2 implements Serializable{
  
}

class Test6 extends Test3 implements Serializable{
  
}

class Test7 extends Test3{
  
}

class Test8 extends HasPublicArgConstr implements Serializable{
  
}

class Test9 extends HasPrivateArgConstr implements Serializable{
    
  public Test9(int aaa){
    super(aaa);
    int bbb = aaa;
    bbb = 4;
  }
}

class Test10 extends HasProtectedArgConstr implements Serializable{
  
}

class Test11 extends Test10{
  
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
