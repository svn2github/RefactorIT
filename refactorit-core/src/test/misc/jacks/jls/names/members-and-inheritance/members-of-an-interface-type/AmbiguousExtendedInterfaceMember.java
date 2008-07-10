
interface One {
  int VAL = 1;
}

interface Two {
  int VAL = 2;
}

interface AmbiguousExtendedInterfaceMember extends One, Two {
  int i = VAL;
}
