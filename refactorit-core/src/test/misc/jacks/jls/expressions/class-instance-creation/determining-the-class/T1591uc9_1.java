
class T1591uc9_1 {
    class Inner{}
}
class T1591uc9_2 extends T1591uc9_1 {
    Object o1 = new T1591uc9_1.Inner();
    Object o2 = new Inner();
}
    