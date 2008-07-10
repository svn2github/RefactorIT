
class T8851q9 {
    static class Inner {}
}
class Sub9 extends T8851q9.Inner {
    Sub9() {
        new T8851q9().super();
    }
}
    