
class T1525cotm4 {
    
        void foo(char c) throws Exception {}
        void foo(int i) {}

        void bar() {
            foo(true ? '0' : -1);
        }
    
}
