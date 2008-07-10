public class B {

 public void setOwners(A a, ARef myRef) {

   for (int i = 0; i < a.declaredMethods.length; ++i) {
     myRef.setOwner();
   }

 }
}
