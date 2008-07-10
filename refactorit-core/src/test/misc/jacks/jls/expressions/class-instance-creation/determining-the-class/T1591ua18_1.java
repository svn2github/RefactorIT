
class T1591ua18_1 {
    protected interface Inner{}
}
class T1591ua18_2 extends T1591ua18_1 {
    Object o1 = new T1591ua18_1.Inner(){};
    Object o2 = new Inner(){};
}
    