
class T1525sotm2 {
    
        void foo(short s) {}
        void foo(int i) throws Exception {}

        void bar() {
            boolean bool = true;
            foo(bool ? (short) 0 : 0);
        }
    
}
