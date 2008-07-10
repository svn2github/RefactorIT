
class T1525cotm2 {
    
        void foo(char c) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            int i = 0;
            foo(bool ? '0' : i);
        }
    
}
