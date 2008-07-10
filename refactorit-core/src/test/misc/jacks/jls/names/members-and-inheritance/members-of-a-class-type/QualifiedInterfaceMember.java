
interface One {
  int VAL = 1;
}

interface Two {
  int VAL = 2;
}

class QualifiedInterfaceMember implements One, Two {
  int i = One.VAL;
}
