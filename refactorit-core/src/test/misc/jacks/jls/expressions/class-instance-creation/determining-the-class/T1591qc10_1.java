
class T1591qc10_1 {
    private class Inner{}
}
class T1591qc10_2 extends T1591qc10_1 {
    Object o = new T1591qc10_1().new Inner();
}
    