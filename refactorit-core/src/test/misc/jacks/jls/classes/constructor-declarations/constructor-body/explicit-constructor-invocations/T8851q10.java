
class T8851q10 {
    
        class One {}
        class Two extends One {
            Two() {
                new T8851q10().super();
            }
            Two(int i) {}
        }
    
}
