// 5:21 - 5:24
public class A {
  public int foo() {
  	int a = 0;
    return sum(++a, --a);
  }
  
  public int sum(int a, int b) {
  	return a + b;
  }
}
