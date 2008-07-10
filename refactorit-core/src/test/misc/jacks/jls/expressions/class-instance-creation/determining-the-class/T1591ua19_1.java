
class T1591ua19_1 {
    interface Inner{}
}
class T1591ua19_2 extends T1591ua19_1 {
    Object o1 = new T1591ua19_1.Inner(){};
    Object o2 = new Inner(){};
}
    