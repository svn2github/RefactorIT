package multiDeclarationMove;

public class A {



  public void meth1() {
    int i = B.ONE;
    int k = B.TWO;
    int[] f = new int[B.THREE];
    f[B.FOUR - 2] = 10;
  }
}
