
class T1591qc16_1 {
    class Inner{}
}
interface T1591qc16_2 {
    class Inner{}
}
class T1591qc16_3 extends T1591qc16_1 implements T1591qc16_2 {
    Object o = new T1591qc16_3().new Inner();
}
    