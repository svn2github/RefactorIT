
class T1528nsn2 {
    
        int i1 = 1;
        final int i2 = i1;
        void foo (int j) {
            switch (j) {
                case i2:
            }
        }
    
}
