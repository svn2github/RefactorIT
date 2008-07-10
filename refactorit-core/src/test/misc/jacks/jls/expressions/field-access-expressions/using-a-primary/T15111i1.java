
class T15111i1 {
    
        static class A {
            static class B {
                static int i = 1;
            }
        }
        int j = new A().B.i;
    
}
