/**
 *
 * @author  ars
 */
public class HeavyUnused {
  
  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void a(){
    int a = 6;
    a = 9;
    a++;
  }

  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */  
  public void b(){
    int a = 7;
    
    if (true){
      return;
    }
    
    a = 8;
  }

  /**
   * @audit UnusedAssignment
   */  
  public void c1(){
    int c = 5;
    
    for (int i = 0; i < 5; i++){
      c = 3;
      
      if (i % 2 == 0){
        break;
      }
      
      c = 5;
    }
  }

  
  /**
   * @audit UnusedAssignment
   */
  public void c2(){
    int c = 5;
    
    for (int i = 0; i < 5; i++){
      c = 3;
      
      if (i % 2 == 0){
        continue;
      }
      
      c = 5;
    }
  }

  
  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void c3(){
    int c = 5;
    
    for (int i = 0; i < 5; i++){
      c = 3;
      
            
      c = 5;
    }
  }

  
  /**
   *
   */
  public void d(){
    int zi = 9;
    
    while (zi < 20){
      System.out.println("zi: " + zi);
      ++zi;
    }
  }

  
  /**
   *
   */
  public Object e1(){
    Object returnValue = null;
    try{
      returnValue = new String("");
    } catch (Exception e) {
      
    }
    
    return returnValue;
  }
  
  
  /**
   *
   */
  public Object e2(){
    Object returnValue = null;
    try{
      returnValue = new String("");
      System.out.println("some doing");
      returnValue = new Integer("");
    } catch (Exception e){
      
    }
    return returnValue;
  }

  
  /**
   *
   */
  public Object e3(){
    Object returnValue = null;
    try{
      returnValue = new String("");
      // no doings
      returnValue = new Integer("");
    } catch (Exception e){
      
    }
    return returnValue;
  }

  
  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void f(){
    int a = 5;
    while (true){
      a = 7;
      
      if (false){
        break;
      }
      
      a = 6;
    }
  }
}
