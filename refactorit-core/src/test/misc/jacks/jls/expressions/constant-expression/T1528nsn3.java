
class T1528nsn3 {
    
        int i1 = 0;
        final int i2 = i1++;
        void foo (int j) {
            switch (j) {
                case i2:
            }
        }
    
}
