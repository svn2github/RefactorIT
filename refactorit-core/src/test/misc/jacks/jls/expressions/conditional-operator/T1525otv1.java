
class T1525otv1 {
    
        void foo() {}
        void bar() {
            String s = true ? foo() : foo();
        }
    
}
