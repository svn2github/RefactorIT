
class T151221am1 {
    public int foo(String s) { return 0; }
    private int foo(Integer i) { return 0; }
}
class T151221am1_Test {
    int i = new T151221am1().foo(null);
}
