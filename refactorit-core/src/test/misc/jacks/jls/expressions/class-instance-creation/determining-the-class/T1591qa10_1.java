
class T1591qa10_1 {
    private class Inner{}
}
class T1591qa10_2 extends T1591qa10_1 {
    Object o = new T1591qa10_1().new Inner(){};
}
    