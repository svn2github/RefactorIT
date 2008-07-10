
interface One {
  int VAL = 1;
}

interface Two {
  int VAL = 2;
}

interface QualifiedExtendedInterfaceMember extends One, Two {
  int i = Two.VAL;
}
