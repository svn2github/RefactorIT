
class T1591ua23_1 {
    class Inner{}
}
interface T1591ua23_2 {
    class Inner{}
}
class T1591ua23_3 extends T1591ua23_1 implements T1591ua23_2 {
    static Object o = new T1591ua23_3.Inner(){};
}
    