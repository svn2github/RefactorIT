
class T1591qa8_1 {
    protected class Inner{}
}
class T1591qa8_2 extends T1591qa8_1 {
    Object o = new T1591qa8_1().new Inner(){};
}
    