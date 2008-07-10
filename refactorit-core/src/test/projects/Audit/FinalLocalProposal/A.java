package FinalLocalProposal;

/**
 * @author ars
 */
public class A {

  /**
   * @audit FinalLocalVarProposal
   */
  public int a() {
    int a = 6;
    if (a > 4) {
      return a;
    } else {
      return 7;
    }
  }

  /**
   * @audit FinalParamProposal
   * @audit FinalParamProposal
   */
  public void b(int c, int d) {
    System.out.println((c + d));
  }

  /**
   *
   */
  public void c1(int a) {
    if (a == 5) {
      return;
    } else {
      a++;
      System.out.println(a);
    }
  }

  /**
   *
   */
  public void c2(int a) {
    if (a == 5) {
      return;
    } else {
      a = 7;
      System.out.println(a);
    }
  }

  /**
   *
   */
  public void d() {
    int a = 7;

    for (int i = 0; i < 5; i++) {
      if (a == 7) {
        a = 3;
        return;
      }
    }
  }

  /**
   *
   */
  public void e1() {
    final int a = 7;

    for (int i = 0; i < 5; i++) {
      if (a == 7) {
        return;
      }
    }
  }

  /**
   *
   */
  public void e2() {
    final int a = 7, b = 5;

    for (int i = 0; i < 5; i++) {
      if (a == 7) {
        return;
      }
    }
  }

  /**
   * @audit FinalParamProposal
   */
  public void f(int a) {
    for (int i = 0; i < 5; i++) {
      if (a == 7) {
        return;
      }
    }
  }

  /**
   *
   */
  public void g() {
    int a = 0;
    for (int i = 0; i < 5; i++) {
      a = 5;
    }
  }

  /**
   *
   */
  public void h() {
    for (int a = 6, b = 9; a < b; a++) {

    }
  }

  /**
   * @audit FinalLocalVarProposal
   */
  public void i() {
    int b = 3;
    for (int a = 6; a < b; b++) {

    }
  }

  /**
   * @audit FinalLocalVarProposal
   */

  public void j() {
    int type;
    final boolean b = true;
    if (b) {
      type = 0;
    } else {
      type = 1;
    }
  }

  /**
   * @audit FinalLocalVarProposal
   */
  public void k() {
    int i;
    {
      i = 1;
    }
  }

  /**
   * @audit FinalLocalVarProposal
   */
  public void l() {
    int k;
    k = 1;
  }

  /**
   * 
   */
  public void m() {
    int k;
    for (int z = 0; z < 1; z++) {
      if (z < 5) {
        k = z;
      }
    }
  }

  /**
   * 
   */
  public void n(final String[] lines) {
    String[] tmp;
    for (int i = 0; i < lines.length; i++) {
      tmp = lines[i].split("\t");
    }
  }

  /**
   * 
   */
  public void o() {
    for (int i = 10, k; i < 10; i++) {
      k = i - 1;
    }
  }

}
