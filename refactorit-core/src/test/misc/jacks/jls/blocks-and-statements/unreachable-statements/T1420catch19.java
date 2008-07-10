
class T1420catch19 {
    
        class MyException extends java.io.IOException {}
        void foo() throws Exception {
            java.io.IOException io = new MyException();
            try {
                throw io;
            } catch (MyException e) {
                // reachable, as variable reference can contain subclass
            }
        }
    
}
