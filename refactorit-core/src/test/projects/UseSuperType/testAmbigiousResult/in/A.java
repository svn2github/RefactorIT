// ambiguous test for I1 and I2 hierarchy
// @author tonis
interface I1 {
 void m(I2 i);


}
interface I2 {

}

class A implements I1,I2 {
 public void m(A a) {
 }
 public void m(I2 i) {
 }
}

class B extends A {
  B []arr;

  void test(B b) {
     arr[0].m(b);
  }

}

