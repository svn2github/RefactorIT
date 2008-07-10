
class T1525otm5 {
    
        class C1 {}
        class C2 extends C1 {}
        void foo() {
            Object o = true ? new C1() : new C2();
        }
    
}
