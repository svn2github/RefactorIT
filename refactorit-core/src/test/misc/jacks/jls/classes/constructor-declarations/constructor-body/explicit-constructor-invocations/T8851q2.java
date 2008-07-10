
class T8851q2 {
    class Inner {}
}
class Sub2 extends T8851q2.Inner {
    Sub2() {
        new T8851q2(){}.super();
    }
}
    