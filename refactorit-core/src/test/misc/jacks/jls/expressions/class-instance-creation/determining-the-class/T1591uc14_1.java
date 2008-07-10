
class T1591uc14_1 {
    class Inner{}
}
interface T1591uc14_2 {
    class Inner{}
}
class T1591uc14_3 extends T1591uc14_1 implements T1591uc14_2 {
    static Object o = new T1591uc14_3.Inner();
}
    