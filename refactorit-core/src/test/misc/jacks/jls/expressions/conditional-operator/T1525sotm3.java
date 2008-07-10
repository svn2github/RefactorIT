
class T1525sotm3 {
    
        void foo(short s) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            int i = 0;
            foo(bool ? (short) 0 : i);
        }
    
}
