
class T1525cotm1 {
    
        void foo(char c) {}
        void foo(int i) throws Exception {}

        void bar() {
            boolean bool = true;
            foo(bool ? '0' : 0);
        }
    
}
