
class T1591qa9_1 {
    class Inner{}
}
class T1591qa9_2 extends T1591qa9_1 {
    Object o = new T1591qa9_1().new Inner(){};
}
    