package multiDeclarationMove;

public class A {

  // koks

  public static final int ONE = 1,
      TWO = 2, // this is entity
      THREE = 3,
      FOUR // i
      = // did
      4 // it
      ;
  public void meth1() {
    int i = ONE;
    int k = TWO;
    int[] f = new int[this.THREE];
    f[FOUR - 2] = 10;
  }
}
