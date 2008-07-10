
class T1525otm8 {
    
        void foo(String s) {}
        void foo(int i) {}
        void bar() {
            foo(true ? "" : 42);
        }
    
}
