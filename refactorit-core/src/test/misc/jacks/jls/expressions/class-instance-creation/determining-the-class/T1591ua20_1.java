
class T1591ua20_1 {
    private interface Inner{}
}
class T1591ua20_2 extends T1591ua20_1 {
    Object o1 = new T1591ua20_1.Inner(){};
    Object o2 = new Inner(){};
}
    