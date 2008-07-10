
class T511r1 {
    
        Object o1 = (Object) new Object();
        Object o2 = (Object) null;
        Object o3 = (Object) o1;
        Object o4 = (Object) o2;
        Object foo() { return new Object(); }
        Object o5 = (Object) foo();
    
}
