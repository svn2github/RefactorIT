
class T1591qc8_1 {
    protected class Inner{}
}
class T1591qc8_2 extends T1591qc8_1 {
    Object o = new T1591qc8_1().new Inner();
}
    