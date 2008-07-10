
class T8851q5 {
    class Inner {}
}
class Sub5 extends T8851q5.Inner {
    T8851q5 t;
    Sub5() {
        // using a member declared in this class is illegal
        t.super();
    }
}
    