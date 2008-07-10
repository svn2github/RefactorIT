
class T1591qa19 {
    
        class Inner{}
        int foo() { return 1; }
        Object o = foo().new Inner(){};
    
}
