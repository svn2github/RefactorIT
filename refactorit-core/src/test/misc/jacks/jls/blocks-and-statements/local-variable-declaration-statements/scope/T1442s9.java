
class T1442s9 {
    
        void foo(final byte b) {
            class One {
                final int i = 1;
            }
            Object i;
            class Two extends One {
                {
                    switch (b) {
                        case 0:
                        case (i == 1) ? 1 : 0:
                    }
                }
            }
        }
    
}
