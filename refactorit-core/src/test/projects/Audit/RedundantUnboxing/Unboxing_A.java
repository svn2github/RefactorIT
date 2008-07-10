package RedundantUnboxing;

/**
 *
 * @author  ars
 */
public class Unboxing_A {
  int a;
  
  public Unboxing_A(Integer a){
    this.a = a;
  }
  
  public Unboxing_A(int a){
    this.a = a;
  }
  
  public static Integer returnsWrapperType(){
    return 5;
  }
  
  public static void takesPrimitiveInt(int a){
    
  }
  
  public static void takesPrimitiveLong(long a){
    
  }
  
  public static void takesWhateverIntType(int a){
    
  }
  
  public static void takesWhateverIntType(Integer a){
    
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void a(){
    Integer a = 5;
    takesPrimitiveInt(a.intValue());
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void b(){
    Integer a = 5;
    int b = 5;
    System.out.println("a + b = " + (a.intValue() + b));
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void c(){
    Long a = 5L;
    takesPrimitiveLong(a.longValue());
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void d(){
    Long a = 5L;
    int b = 5;
    System.out.println("a + b = " + (a.longValue() + b));
  }
  
  /**
   * @audit ProbablyRedundantUnboxing
   */
  public void e(){
    Integer a = 5;
    takesWhateverIntType(a.intValue());
    takesWhateverIntType(a);
  }
  
  /**
   * @audit ProbablyRedundantUnboxing
   */
  public void f(){
    Integer a = 1;
    Integer b = 2;
    double c = a.doubleValue() / b;
  }
  
  /**
   * @audit ProbablyRedundantUnboxing
   */
  public void g(){
    Integer a = 1;
    new Unboxing_A(a);
    new Unboxing_A(a.intValue());
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void h(){
    int a = returnsWrapperType().intValue();
  }
  
  /**
   *
   */
  public void i(){
    int a = ((Number) returnsWrapperType()).intValue();
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void j(){
    Integer b = 3;
    int a = b.intValue();
    takesPrimitiveInt(a);
    int c = a;
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void k(){
    Integer b = 3;
    int a = b.intValue();
    takesWhateverIntType(a);
    int c = a;
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void l(){
    Integer b = 3;
    int a = b.intValue();
    takesPrimitiveInt(a);
    a++;
    takesPrimitiveInt(a);
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void m(){
    Number f = 3;
    int t = ((Integer) f).intValue();
  }
  
  /**
   * @audit RedundantUnboxing
   * @audit RedundantUnboxing
   * @audit RedundantUnboxing
   * @audit RedundantUnboxing
   */
  public void n(){
    Integer b = 3, c = 4;
    int a = b.intValue(), d = 3;
    int f = b.intValue(), g = c.intValue();
    int h = 3, m = b.intValue();
  }
  
  /**
   * @audit RedundantUnboxing
   * @audit RedundantUnboxing
   */
  public void o(){
    int temp;
    Integer b = 3, c = 4;
    temp = b.intValue();
    takesPrimitiveInt(temp);
    temp = c.intValue();
    takesWhateverIntType(temp);
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void p(){
    int temp;
    Integer b = 3;
    temp = b.intValue();
    takesPrimitiveInt(temp);
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void q(){
    int temp;
    takesPrimitiveInt(temp);
    Integer b = 3;
    temp = b.intValue();
    takesPrimitiveInt(temp);
  }
}
