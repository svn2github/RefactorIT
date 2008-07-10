
class T1591qa22_1 {}
class T1591qa22_2 extends T1591qa22_1 {
    class Inner{}
    Object o = new T1591qa22_1().new Inner(){};
}
    