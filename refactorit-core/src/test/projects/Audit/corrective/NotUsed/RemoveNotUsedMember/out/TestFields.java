package corrective.NotUsed.RemoveNotUsedMember;

/**
 * @violations 12
 */
public class TestFields {
  private static int k = 0;
  
  
  
  
  
  
  
  boolean USED1;

  public static void main(String[] args) {
    new TestFields().use();
  }
  
  public void use() {
    meth1();
    meth2();
    meth3();
    meth4();
    meth5();
    meth6();
    use1();
  }
  
  public void meth1() {
     // right expression changes, comment only assignment
  }
  
  public void meth2() {
     // can comment all
  }

  public void meth3() {
     // right expression changes, comment only assignment
  }
  
  public void meth4() {
     // can comment all
  }
  
  public void meth5() {
    for(;;) {
      
    }
  }
  
  public void meth6() {
    for(;;) {
      
    }
  }
  
  public void use1() {
    USED1 = increase();
  }
  
  public boolean increase() {
    k++;
    return true;
  }

  /*public F getThis() {
    return this;
  }*/
}

class G {
     boolean USED1 = true; // used1 
  
   int a = 1; /*a comment*/
   int b = a=2; /*b comment*/
   int c = b=(a=3); /*c comment*/
   int d = c=b=a=4; /*d comment*/

  
  public static void main(String[] args) {
    new G().meth1();
    new G().meth2();
  }
  
  public void meth1() {
    
    USED1 = true;
    
  }
  
  public void meth2() {
    
  }
}

class H {
  public static void main(String[] args) {
  } 
  
  
  
  
}
