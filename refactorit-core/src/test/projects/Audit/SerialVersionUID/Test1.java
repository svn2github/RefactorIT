package SerialVersionUID;
import java.io.Serializable;

// test1: Test1 implements Serializable, but has no serialVersionUID declared

/**
 *@audit NoSerialVersionUID
 */
public class Test1 implements Serializable{

  public Test1 (){
    
  }
  
}

// test2: Everything is ok, B_OK implements Serializable and has 
// serialVersionUID

/**
 *
 */
class Test2 implements Serializable{
  
/**
 *
 */
  private static final long serialVersionUID = 342342432;
  
  public Test2 (){
    
  }
  
}

// test3: serialVersionUID is not static

class Test3 implements Serializable{

 /**
  * @audit BadSerialVersionUID
  */
  private final long serialVersionUID = 342342432;
  
  public Test3 (){
    
  }
  
}

// test4: serialVersionUID is not static and not final

class Test4 implements Serializable{
 /**
  * @audit BadSerialVersionUID
  */
  private long serialVersionUID = 342342432;
  
  public Test4 (){
    
  }
  
}

// test5: serialVersionUID is not long type

class Test5 implements Serializable{
 /**
  *@audit BadSerialVersionUID
  */
  private static final int serialVersionUID = 342342432;
  
  public Test5 (){
    
  }
  
}

// test6: serialVersionUID is not final

class Test6 implements Serializable{
 /**
  *@audit BadSerialVersionUID
  */
  private static long serialVersionUID = 342342432;
  
  public Test6 (){
    
  }
  
}

// test7: Test7 doesn`t implement Serializable, but Test1 does
// Test7 has no serialVersionID declared

/**
 *@audit NoSerialVersionUID
 */
class Test7 extends Test1 {
   
    
  public Test7 (){
    
  }
  
}

// test8: Test8 doesn`t implement Serializable, but Test1 does
// Test8 has serialVersionID, but it is not static


class Test8 extends Test1 {
  /**
   * @audit BadSerialVersionUID
   */
  private final long serialVersionUID = 342342432;
    
  public Test8 (){
    
  }
  
}

// test9: Test9 doesn`t implement Serializable, but Test1 does
// Test9 is OK, all modifiers are right

/**
 *
 */
class Test9 extends Test1 {
  
  private static final long serialVersionUID = 342342432;
    
  public Test9 (){
    
  }
  
}

// test10: Test10 is not serializable
// serialVersionUID has nos static modifier

/**
 *
 */
class A {
  
}

class Test10 extends A {
  
  private final long serialVersionUID = 342342432;
    
  public Test10 (){
    
  }
  
}