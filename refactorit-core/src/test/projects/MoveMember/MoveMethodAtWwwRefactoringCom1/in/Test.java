class Project {
  Person[] participants;
}

class Person {
  int id;
  boolean participate(Project p) {
    for(int i=0; i<p.participants.length; i++) {
	  if (p.participants[i].id == id) return(true);
    }
    return(false);
  }
}

class Test {
  {
    Person x;
    Project p;

    if (x.participate(p)) {
    }
  }
}
