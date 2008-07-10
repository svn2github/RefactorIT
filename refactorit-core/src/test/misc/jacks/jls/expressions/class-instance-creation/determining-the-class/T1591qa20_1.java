
class T1591qa20_1 {}
class T1591qa20_2 extends T1591qa20_1 {
    class Inner{}
    Object o = new T1591qa20_1().new Inner();
}
    