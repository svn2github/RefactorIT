
class T1525cotm3 {
    
        void foo(char c) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            foo(bool ? '0' : -1);
        }
    
}
