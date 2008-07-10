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
    takesPrimitiveInt(a);
  }
  
  public void b(){
    Integer a = 5;
    int b = 5;
    System.out.println("a + b = " + (a + b));
  }
  
  public void c(){
    Long a = 5L;
    takesPrimitiveLong(a);
  }
  
  public void d(){
    Long a = 5L;
    int b = 5;
    System.out.println("a + b = " + (a + b));
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
    int a = returnsWrapperType();
  }
  
  public void i(){
    int a = ((Number) returnsWrapperType()).intValue();
  }
  
  // can remove temp variable
  public void j(){
    Integer b = 3;
    int a = b;
    takesPrimitiveInt(a);
    int c = a;
  }
  
  // cannot remove temp variable
  public void k(){
    Integer b = 3;
    int a = b;
    takesWhateverIntType(a);
    int c = a;
  }
  
  // cannot remove temp variable
  public void l(){
    Integer b = 3;
    int a = b;
    takesPrimitiveInt(a);
    a++;
    takesPrimitiveInt(a);
  }
  
  public void m(){
    Number f = 3;
    int t = (Integer) f;
  }
  
  public void n(){
    Integer b = 3, c = 4;
    int a = b, d = 3;
    int f = b, g = c;
    int h = 3, m = b;
  }
  
  public void o(){
    int temp;
    Integer b = 3, c = 4;
    temp = b;
    takesPrimitiveInt(temp);
    temp = c;
    takesWhateverIntType(temp);
  }
  
  public void p(){
    int temp;
    Integer b = 3;
    temp = b;
    takesPrimitiveInt(temp);
  }
  
  public void q(){
    int temp;
    takesPrimitiveInt(temp);
    Integer b = 3;
    temp = b;
    takesPrimitiveInt(temp);
  }
}
