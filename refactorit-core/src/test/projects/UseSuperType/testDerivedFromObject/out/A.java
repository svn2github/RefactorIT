
interface A {
}
class B implements A {
  Object o=null;
  
  void test() {
    A a=null;
    
    o.equals(a);
  }
}

