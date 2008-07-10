// tests constructor usage
interface I {
 public void m(I i[]);


}

class A implements I {
  void m3() {
  }
  public void m(I i[]) {

  }
  public void m1(A a2[]) {
    a2[0].m3();
  }

  public void test() {
   I []a=new A[3];
   A []a1 = new A[3];

   m(a);
   m1(a1);

  }

}
