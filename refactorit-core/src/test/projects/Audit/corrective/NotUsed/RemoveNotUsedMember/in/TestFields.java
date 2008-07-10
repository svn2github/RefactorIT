package corrective.NotUsed.RemoveNotUsedMember;

/**
 * @violations 12
 */
public class TestFields {
  private static int k = 0;
  boolean NOT_USED1;
  boolean NOT_USED2;
  boolean NOT_USED3;
  boolean NOT_USED4;
  boolean NOT_USED5;
  boolean NOT_USED6;
  
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
    NOT_USED1 = true; // right expression changes, comment only assignment
  }
  
  public void meth2() {
    NOT_USED2 = true & false; // can comment all
  }

  public void meth3() {
    NOT_USED3/**Javadoc*//*simple*/ = false; // right expression changes, comment only assignment
  }
  
  public void meth4() {
    NOT_USED4 /**Javadoc*/
    /*simple*/=/**Javadoc*/
    /*simple*/true /*comment*/&/*comment*/ false/*comment*/; // can comment all
  }
  
  public void meth5() {
    for(NOT_USED5 = (true ^ false);;) {
      NOT_USED5 = false;
    }
  }
  
  public void meth6() {
    for(NOT_USED6 = true;;NOT_USED6 = false) {
      NOT_USED6 = false;
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
  boolean NOT_USED1 = false, // notused1
          NOT_USED2 = false, // notused2
	        NOT_USED3 = true,  // notused3
          USED1 = true; // used1
  
  int a = 1/*a comment*/, b=a=2 /*b comment*/, c=b=(a=3)/*c comment*/, d=c=b=a=4/*d comment*/, e=d=c=b=a=5/*e comment*/;
  
  public static void main(String[] args) {
    new G().meth1();
    new G().meth2();
  }
  
  public void meth1() {
    NOT_USED1 = true && true && (false || true);
    NOT_USED3 = USED1 = true;
    NOT_USED3 = USED1;
  }
  
  public void meth2() {
    e = (USED1)?1:2;
  }
}

class H {
  public static void main(String[] args) {
  } 
  
  public void meth1() {
/**
 * meth1 contains important javadoc comment
 */
   /**
    * meth1 contains important javadoc comment
    */
    /**
     * @javadoc
     */
   /*
    * simple comment
    */
  }
  
  public void meth2() { /** javadoc */
    /* additional comment*/
  }
}
