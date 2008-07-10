
class T1591qc9_1 {
    class Inner{}
}
class T1591qc9_2 extends T1591qc9_1 {
    Object o = new T1591qc9_1().new Inner();
}
    