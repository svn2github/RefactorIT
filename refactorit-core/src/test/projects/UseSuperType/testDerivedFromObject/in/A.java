
interface A {
}
class B implements A {
  Object o=null;
  
  void test() {
    B a=null;
    
    o.equals(a);
  }
}

