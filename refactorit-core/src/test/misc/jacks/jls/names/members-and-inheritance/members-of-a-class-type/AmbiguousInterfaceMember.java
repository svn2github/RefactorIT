
interface One {
  int VAL = 1;
}

interface Two {
  int VAL = 2;
}

class AmbiguousInterfaceMember implements One, Two {
  int i = VAL;
}
