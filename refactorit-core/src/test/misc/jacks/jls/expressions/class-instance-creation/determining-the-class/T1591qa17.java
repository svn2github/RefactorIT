
class T1591qa17 {
    
        class Inner {
            static final int foo = 1;
        }
        Object o = new T1591qa17(){
            class Inner {
                static final int foo = 2;
            }
        }.new Inner(){
            {
                switch (1) {
                    case 0:
                    case ((foo == 2) ? 1 : 0):
                }
            }
        };
    
}
