
class T1591ua10_1 {
    private class Inner{}
}
class T1591ua10_2 extends T1591ua10_1 {
    Object o1 = new T1591ua10_1.Inner(){};
    Object o2 = new Inner(){};
}
    