package corrective.BoxingUnboxing.RemoveRedundantUnboxingAction.in;

/**
 *
 * @author  ars
 * @violations 20
 */
public class TestUnboxing_A {
  int a;
  
  public TestUnboxing_A(Integer a){
    this.a = a;
  }
  
  public TestUnboxing_A(int a){
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
  
  public void a(){
    Integer a = 5;
    takesPrimitiveInt(a.intValue());
  }
  
  public void b(){
    Integer a = 5;
    int b = 5;
    System.out.println("a + b = " + (a.intValue() + b));
  }
  
  public void c(){
    Long a = 5L;
    takesPrimitiveLong(a.longValue());
  }
  
  public void d(){
    Long a = 5L;
    int b = 5;
    System.out.println("a + b = " + (a.longValue() + b));
  }
  
  public void e(){
    Integer a = 5;
    takesWhateverIntType(a.intValue());
    takesWhateverIntType(a);
  }
  
  public void f(){
    Integer a = 1;
    Integer b = 2;
    double c = a.doubleValue() / b;
  }
  
  public void g(){
    Integer a = 1;
    new TestUnboxing_A(a);
    new TestUnboxing_A(a.intValue());
  }
  
  public void h(){
    int a = returnsWrapperType().intValue();
  }
  
  public void i(){
    int a = ((Number) returnsWrapperType()).intValue();
  }
  
  // can remove temp variable
  public void j(){
    Integer b = 3;
    int a = b.intValue();
    takesPrimitiveInt(a);
    int c = a;
  }
  
  // cannot remove temp variable
  public void k(){
    Integer b = 3;
    int a = b.intValue();
    takesWhateverIntType(a);
    int c = a;
  }
  
  // cannot remove temp variable
  public void l(){
    Integer b = 3;
    int a = b.intValue();
    takesPrimitiveInt(a);
    a++;
    takesPrimitiveInt(a);
  }
  
  public void m(){
    Number f = 3;
    int t = ((Integer) f).intValue();
  }
  
  public void n(){
    Integer b = 3, c = 4;
    int a = b.intValue(), d = 3;
    int f = b.intValue(), g = c.intValue();
    int h = 3, m = b.intValue();
  }
  
  public void o(){
    int temp;
    Integer b = 3, c = 4;
    temp = b.intValue();
    takesPrimitiveInt(temp);
    temp = c.intValue();
    takesWhateverIntType(temp);
  }
  
  public void p(){
    int temp;
    Integer b = 3;
    temp = b.intValue();
    takesPrimitiveInt(temp);
  }
  
  public void q(){
    int temp;
    takesPrimitiveInt(temp);
    Integer b = 3;
    temp = b.intValue();
    takesPrimitiveInt(temp);
  }
}
