public class A {
   public void f1() {
   }

   public void f2() {
   }

/** 
* @USED
*/
   public void f3() {
   }

/** 
* @USED
*/
   private void f4() {
   }
}


class B {
  {
    new A().f1();
  }
}
