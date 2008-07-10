
class T1420expression2 {
    
        void die() { throw new RuntimeException(); }
        void foo() {
            System.exit(1); // never returns
            die(); // always abrupt exit with exception
            Object o = new Object[Integer.MAX_VALUE][Integer.MAX_VALUE][Integer.MAX_VALUE]; // almost a guaranteed OutOfMemoryError
            foo(); // recursion short-circuits rest of method
            int i; // will never get here, but it is reachable
        }
    
}
