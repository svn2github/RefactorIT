
class T1591uc8_1 {
    protected class Inner{}
}
class T1591uc8_2 extends T1591uc8_1 {
    Object o1 = new T1591uc8_1.Inner();
    Object o2 = new Inner();
}
    