    /**
     * @violations 4
     */

public class Test2 {

    public void method1() {
    int ii;
    int c;
    c++;
    ii++;
  }

  public void method2() {
    int iii;
    int c;
    iii++;
  }

  public void method3(int a) {
    a++;
  }


  public void method4() {
      int c;
      int a[] = new int[5];
      c++;
      a[0] = 3;
  }

  public void method5() {
      int a = 0;
      int iiii = 1;
      while (iiii < a)
        a++;
  }


  public void method6() {
    int iiiii;
    {
      {
        iiiii++;
      }
      iiiii++;
    }
  }

  public void method7() {
    int o;
    for (int i=0; 0<2; i++) {
      o++;
    }
  }

  public void method8() {
    int i = 0;
    {
      {
        while (i < 10)
          i++;
      }
    }
  }

  public void method10() {
    int i = 0;
    String a = (new Integer(i)).toString();
    int o = i;
    i++;
    o++;
    a += "bu";
  }
}
