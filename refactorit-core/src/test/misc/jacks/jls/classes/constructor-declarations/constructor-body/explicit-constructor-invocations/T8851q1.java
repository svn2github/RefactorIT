
class T8851q1 {
    class Inner {}
}
class Sub1 extends T8851q1.Inner {
    Sub1() {
        new T8851q1().super();
    }
}
    