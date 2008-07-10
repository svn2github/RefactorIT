class Project {
  Person[] participants;

  boolean participate(Person person) {
    for(int i=0; i<this.participants.length; i++) {
	  if (this.participants[i].id == person.id) return(true);
    }
    return(false);
  }
}

class Person {
  int id;
}

class Test {
  {
    Person x;
    Project p;

    if (p.participate(x)) {
    }
  }
}
