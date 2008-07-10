
class T1591uc10_1 {
    private class Inner{}
}
class T1591uc10_2 extends T1591uc10_1 {
    Object o1 = new T1591uc10_1.Inner();
    Object o2 = new Inner();
}
    