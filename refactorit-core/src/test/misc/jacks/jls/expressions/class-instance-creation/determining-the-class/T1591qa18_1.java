
class T1591qa18_1 {
    class Inner{}
}
interface T1591qa18_2 {
    class Inner{}
}
class T1591qa18_3 extends T1591qa18_1 implements T1591qa18_2 {
    Object o = new T1591qa18_3().new Inner(){};
}
    