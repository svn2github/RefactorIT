/**
 *
 * @author  ars
 */
public class Unused {
  

  /**
   * @audit UnusedAssignment
   */  
  public void a(){
    int a;
    a = 5;
  }

  /**
   * @audit UnusedAssignment
   */
  public void b(){
    int a = 6;
  }

  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */  
  public void c(){
    int a = 5;
    a++;
    a = 3;
  }

  /** 
   * @audit UnusedAssignment
   */  
  public void d(){
    int d = 3;
    d++;
    d = d + 6;
  }

  /**
   *
   */  
  public void e(){
    int d = 3;
    d++;
    d = d + 6;
    System.out.println(d);
  }

  /**
   * @audit UnusedAssignment
   */  
  public void f(){
    int d = 3;
    d = d + 6;
  }

  /**
   * @audit UnusedAssignment
   */  
  public void g(){
    int d = 3;
    d += 6;
  }

  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */  
  public void h(){
    int a = 5;
    a = (a = 6) + (a = 7) + a;
  }

  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */  
  public void j(){
    int a = 3;
    a = ((a = 3) > 2) ? 4 : a - 2;
  }
}
