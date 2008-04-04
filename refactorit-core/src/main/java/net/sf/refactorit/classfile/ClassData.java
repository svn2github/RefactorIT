/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classfile;


import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.loader.Settings;

import java.util.ArrayList;


public final class ClassData {
  public static final String CONSTRUCTOR_NAME = "<init>";
  public static final String STATIC_INIT_NAME = "<clinit>";

  private static final String DEPRECATED_ATTR = "Deprecated";
  private static final String INNER_CLASSES_ATTR = "InnerClasses";
  private static final String SYNTHETIC_ATTR = "Synthetic";
  private static final String EXCEPTIONS_ATTR = "Exceptions";
  private static final String SIGNATURE_ATTR = "Signature";

  private static final int[] CP_TAG_SIZES = {
      0, // 0
      0, // 1
      0, // 2
      4, // 3
      4, // 4
      8, // 5
      8, // 6
      2, // 7
      2, // 8
      4, // 9
      4, // 10
      4, // 11
      4, // 12
  };

  // -------------------------

  byte[] data;
  int position = 0;

//  int magic;
//  int minor_version;
//  int major_version;

  String[] constantPoolStrings;
  String[] slashDotString;
  int[] constantPoolStringLengths;
  int[] constantPoolStringPositions;
  int[] constantPoolPositions;

  // ---------------------

  int modifiers;

  String this_class;
  String super_class;

  String[] interface_names;

  private ArrayList typeParameters;
  private ArrayList parentsInfoFromSignature;

  private ArrayList fields;

  private StringBuffer myLog;

  // Sort of a 'clever thing here' - MyField is itselves MyTypeData
  public final class MyField extends MyTypeData {
    public final String name;
    public final int modifiers;
    public final boolean isdeprecated;
    public StringBuffer signature = null;

    public MyField(final int modifiers, final String name,
        final String descriptor, final boolean isdeprecated) {
      super(descriptor);
      this.modifiers = modifiers;
      this.name = name;
      this.isdeprecated = isdeprecated;
    }
  }


  public class MyTypeData {
    public final String typeName;
    public int dimension;

    public MyTypeData(final String descriptor) {
      while (descriptor.charAt(dimension) == '[') {
        ++dimension;
      }

      this.typeName = ClassUtil.getNameForDescriptor(
          descriptor.substring(dimension)).intern();
    }
  }

  public static class MyTypeParameter {
    public final String parameterName;
    public final ArrayList supers;

    MyTypeParameter(final String name, final ArrayList supers){
      this.parameterName = name;
      this.supers = supers;
    }

    public final String toString(){
      return this.getClass() + ", name: " + parameterName +
          ", supers : " + supers;
    }
  }

  private ArrayList methods;

  public final class MyMethod extends MyConstructor {
    public MyMethod(final int modifiers, final String name,
        /*boolean isdeprecated,*/ final String[] exceptions) {
      super(modifiers, /*isdeprecated,*/ exceptions);
      this.name = name;
    }

    public final int init(final String name, final String descriptor)
        throws ClassFormatException {
      final int parenthesisEnd = super.init(name, descriptor);
      returnType = new MyTypeData(descriptor.substring(parenthesisEnd + 1)); //+1 for ')'
      return parenthesisEnd;
    }

    public String name;
    public MyTypeData returnType;
  }

  private ArrayList ctors;
  public class MyConstructor {
    public ArrayList typeParameterSignatures = null;
    public StringBuffer returnTypeSignature = null;
    public ArrayList methodParameterSignatures = null;

    public MyConstructor(final int modifiers, /*boolean isdeprecated,*/
        final String[] exceptions) {
      this.modifiers = modifiers;
//      this.isdeprecated = isdeprecated;
      this.exceptions = exceptions;
    }

    public int init(final String name, final String descriptor)
        throws ClassFormatException {

      if (descriptor.length() == 0 || descriptor.charAt(0) != '(') {
        throw new ClassFormatException("invalid descriptor '" +
            descriptor + "' for method:'" + name + "'");
      }

      final int parenthesisEndI = descriptor.indexOf(')', 1);
      if (parenthesisEndI == -1) {
        throw new ClassFormatException("invalid descriptor '" +
            descriptor + "' for method:'" + name + "'");
      }

      final ArrayList paramDescriptors
          = ClassUtil.splitDescriptors(descriptor.substring(1, parenthesisEndI));
      if (paramDescriptors != null) {
        paramTypes = new MyTypeData[paramDescriptors.size()];
        for (int i = 0, max = paramDescriptors.size(); i < max; ++i) {
          paramTypes[i] = new MyTypeData((String) paramDescriptors.get(i));
        }
      }

      return parenthesisEndI;
    }

    public final int modifiers;
//    public final boolean isdeprecated;
    public MyTypeData[] paramTypes;
    public final String[] exceptions;
  }

  public final class MyInnerType {
    public String name;
    public boolean isStatic = false;
  }


  boolean has_static_init = false;
  boolean class_isdeprecated;
  boolean class_isinner;

  ArrayList class_InnerTypes;
  String class_declaringClassName;

  // ------------------------

  protected ClassData(final byte[] data) throws ClassFormatException {
    this.data = data;

    try {
      resolve();

    } catch (Throwable throwable) {
      //System.out.println(myLog);
      throw new ClassFormatException(throwable);
    } finally {
      // free the memory
      this.data = null;
      constantPoolStrings = null;
      slashDotString = null;
      constantPoolStringLengths = null;
      constantPoolStringPositions = null;
      constantPoolPositions = null;
    }
  }

  public final int getModifiers() {
    return modifiers;
  }

  final void markStatic() {
    modifiers = BinModifier.setFlags(modifiers, BinModifier.STATIC);
  }

  public final boolean isEnum() {
    return BinModifier.hasFlag(modifiers, BinModifier.ENUM);
  }

  public final boolean isAnnotation() {
    return BinModifier.hasFlag(modifiers, BinModifier.ANNOTATION);
  }

  public final boolean isInterface() {
    return BinModifier.hasFlag(modifiers, BinModifier.INTERFACE);
  }

  public final boolean isClass() {
    return !isEnum() && !isAnnotation() && !isInterface();
  }

  public final String getName() {
    return this_class;
  }

  public final String getTypeName() {
    String className = this_class;

    int pos = className.lastIndexOf('.');
    if (pos != -1) {
      int tmp = pos;

      tmp = className.indexOf('$', tmp);
      if (tmp != -1) {
        pos = tmp;
      }

      className = className.substring(pos + 1);
    }

    return className;
  }

  public final String getPackageName() {
    return extractPackageName(this_class);
  }

  private static final void parseMethodSignature(final String signature,
      final MyConstructor myMethod){
//    System.out.println("[ARS>>] Method signature: " + signature);
    /*try {*/
      int pos = 0;
      char c = signature.charAt(pos);

      // TRY TO READ TYPE PARAMETERS
      if (c == '<'){
        myMethod.typeParameterSignatures = new ArrayList(2);
        pos = readTypeParameters(pos + 1, myMethod.typeParameterSignatures,
            signature);
      }

      // TRY TO READ FORMAL METHOD PARAMETERS
      c = signature.charAt(pos++);
      if (c == '('){
        c = signature.charAt(pos);
        myMethod.methodParameterSignatures = new ArrayList(3);
        while(c != ')'){
          pos = readRawTypeName(pos, myMethod.methodParameterSignatures,
              signature, true);
          c = signature.charAt(pos);
        }

        pos++;
      }

      // TRY TO READ RETURN TYPE SIGNATURE
      final ArrayList tmp = new ArrayList(1);
      readRawTypeName(pos, tmp, signature, true);
      myMethod.returnTypeSignature = (StringBuffer) tmp.get(0);

    /*} catch (StringIndexOutOfBoundsException e){
      System.out.println("Exception " + e.getMessage());
      //e.printStackTrace(System.out);
    }*/
  }

  private final void parseClassSignature(final String signature){
//    System.out.println("[ARS>>] Class signature: "+signature);
    ArrayList superTypesList = null;
    try {
      int pos = 0;
      final char c = signature.charAt(pos);

      // TRY TO READ TYPE PARAMETERS
      if (c == '<'){
        typeParameters = new ArrayList(2);
        pos = readTypeParameters(pos + 1, typeParameters, signature);
      }

      // NOW SAVE SUPERCLASS AND SUPERINTERFACES INFO
      superTypesList = new ArrayList(3);
      while(true){ // the cycle brakes when SIOOBE is thrown :)
        pos = readRawTypeName(pos, superTypesList, signature, false);
      }

    } catch (StringIndexOutOfBoundsException e){
      if (superTypesList != null && superTypesList.size() > 0){
        parentsInfoFromSignature = superTypesList;
      }
    }
  }

  /** @skip paramAsgn */
  private static final int readTypeParameters(int pos, final ArrayList container,
      final String signature){

    boolean endReached = false;
    ArrayList supersList = null;
    StringBuffer curParameterName;
    char c = signature.charAt(pos++);
    while(!endReached){
      supersList = new ArrayList(3);
      curParameterName = new StringBuffer();

      // read parameter name
      while (c != ':'){
        curParameterName.append(c);
        c = signature.charAt(pos++);
      }

      // put type parameter into container
      container.add(new MyTypeParameter(curParameterName.toString(),
          supersList));

      // read supers
      boolean readingSuperName = false;
      while(true){
        if (c == ':'){
          if (readingSuperName){
            supersList.add(new StringBuffer("Ljava.lang.Object"));
          }
          c = signature.charAt(pos++);
          readingSuperName = true;
        } else if (c == '>'){
          // no more param info
          endReached = true;
          break;
        } else {
          if (readingSuperName){
            pos = readRawTypeName(pos - 1, supersList, signature, false);
            c = signature.charAt(pos++);
            readingSuperName = false;
          } else {
            // next type param
            break;
          }
        }
      }
    }
    return pos;
  }

  /** @skip paramAsgn */
  private static final int readRawTypeName(int pos, final ArrayList container,
      final String signature, final boolean expectPrimitives){
    int countBraces = 0;
    boolean insideArguments = false;

    final StringBuffer typeName = new StringBuffer();

    char c = signature.charAt(pos++);

    if (expectPrimitives){
      while (c == '['){ // read array type
        typeName.append(c);
        c = signature.charAt(pos++);
      }

      switch(c){
        case 'I': // int
        case 'Z': // boolean
        case 'F': // float
        case 'J': // long
        case 'D': // double
        case 'S': // short
        case 'B': // byte
        case 'C': // char
        case 'V': // void descriptor
          typeName.append(c);
          container.add(typeName);
          return pos;
        default:
          break;
      }
    }

    // read T or L types (TT1;, Ljava.lang.Object;)
    while(c != ';' || insideArguments){
      switch(c){
        case '.':
          c = '$';
          break;
        case '/':
          c = '.';
          break;
        case '<':
          insideArguments = true;
          countBraces++;
          break;
        case '>':
          countBraces--;
          if (countBraces == 0){
            insideArguments = false;
          }
          break;
        default:
          break;
      }
      typeName.append(c);
      c = signature.charAt(pos++);
    }
    container.add(typeName);
    return pos;
  }

  public static final String extractPackageName(final String fqn) {
    String retVal;
    final int lastDotI = fqn.lastIndexOf('.');
    if (lastDotI != -1) {
      retVal = fqn.substring(0, lastDotI);
    } else {
      retVal = "";
    }

    return retVal;
  }

  public final String getSuperclassName() {
    return super_class;
  }

  public final ArrayList getDeclaredTypes() {
    return class_InnerTypes;
  }

  public final boolean isDeprecated() {
    return class_isdeprecated;
  }

  public final boolean isInner() {
    return class_isinner;
  }

  public final String[] getInterfaceNames() {
    return interface_names;
  }

  public final String getDeclaringClassName() {
    return class_declaringClassName;
  }

  /** @return methods array, doesn't contain synthetic methods */
  public final ArrayList getMethods() {
    return methods;
  }

  /** @return constructors array, doesn't contain synthetic constructors */
  public final ArrayList getConstructors() {
    return ctors;
  }

  /** @return fields array, doesn't contain synthetic fields */
  public final ArrayList getFields() {
    return fields;
  }

  // ---------------------

  private final void resolve() throws ClassFormatException {
    consumeU4(); // skip 'magic'

    final int minor_version = consumeU2();
    final int major_version = consumeU2();

    if (Settings.debugLevel > 50) {
      log("DD version is " + major_version + '.' + minor_version);
    }

    final int constantPoolCount = consumeU2();
    if (Settings.debugLevel > 50) {
      log("DD Constant pool count is : " + constantPoolCount);
    }
    readConstantPool(constantPoolCount);

    modifiers = consumeU2();

    final int thisClassIndex = consumeU2();
    this_class = getCPClassName(thisClassIndex);

    if (Settings.debugLevel > 50) {
      log("This class is : " + this_class);
    }

    final int super_classNdx = consumeU2();
    super_class = (super_classNdx == 0) ? null : getCPClassName(super_classNdx);

    if (Settings.debugLevel > 50) {
      log("Superclass is : " + super_class);
    }

    final int interfaces_count = consumeU2();
    if (Settings.debugLevel > 50) {
      log(position + " DD interfaces count is : " + interfaces_count);
    }
    readInterfaces(interfaces_count);

    final int fields_count = consumeU2();
    if (Settings.debugLevel > 50) {
      log(position + " DD fields count is : " + fields_count);
    }
    readFields(fields_count);

    final int methods_count = consumeU2();
    if (Settings.debugLevel > 50) {
      log(position + " DD method count is : " + methods_count);
    }
    readMethods(methods_count);

    final int attributesCount = consumeU2();

    for (int i = 0; i < attributesCount; ++i) {
      final int result = findNextAttribute(DEPRECATED_ATTR, SIGNATURE_ATTR,
          INNER_CLASSES_ATTR, null);
      int dlength;

      switch (result) {
        case 0: // Deprecated
          dlength = consumeU4();
          position += dlength;
          class_isdeprecated = true;
          break;

        case 1: // Signature
          dlength = consumeU4();
          parseClassSignature(getCPString(consumeU2()));
          break;

        case 2: // Inner Classes
          dlength = consumeU4();
          final int innerCount = consumeU2();

          class_InnerTypes = new ArrayList(innerCount);

          for (int ci = 0; ci < innerCount; ++ci) {
            final int innerClassIndex = consumeU2();
            final int outerClassIndex = consumeU2();
            final int innerNameIndex = consumeU2();
            final int innerAccessFlags = consumeU2();

            final MyInnerType curInner = new MyInnerType();
            curInner.name = getCPClassName(innerClassIndex);
            curInner.isStatic = BinModifier.hasFlag(
                innerAccessFlags, BinModifier.STATIC);

            if (curInner.name.equals(this_class)) {
              class_isinner = true;
              if (outerClassIndex != 0) {
                class_declaringClassName = getCPClassName(outerClassIndex);
                //if (innerNameIndex == 0) {
                //System.out.println("found owner for unnamed:" +
                //    this_class + " owner:" + class_declaringClassName);
                //}
              }
              continue;
            }

            // we need only inners of this class, not inherited from interfaces
            if (thisClassIndex != outerClassIndex) {
              continue;
            }

            //if outerClassIndex is not 0 then this is not anonymous inner
            //but javac generated class
            if (innerNameIndex == 0 && outerClassIndex == 0) {
              continue; // skiping the anonymous because we dont need them EVER
            }

            if (ignoreInner(curInner.name)) {
              continue;
            }

            if (Settings.debugLevel > 50) {
              log("DD found inner:" + curInner.name);
            }

            class_InnerTypes.add(curInner);
          }
          break;

        default:
      }
    }

    if (Settings.debugLevel > 50) {
      log("DD Class is " + (class_isdeprecated ? "" : "NOT ") + "deprecated.");
      log("DD Class is " + (class_isinner ? "" : "NOT ") + "inner.");
    }
  }

  private final void readMethods(final int methods_count) throws
      ClassFormatException {

    methods = new ArrayList(methods_count);

    int modifiers;
    String name;
    String descriptor;
    String signature;
    boolean isdeprecated;
    boolean issynthetic;
    String[] exceptions;

    for (int i = 0; i < methods_count; ++i) {
      signature = null;
      modifiers = consumeU2();
      name = getCPString(consumeU2()).intern();
      descriptor = getCPSlashDotString(consumeU2());
      isdeprecated = false;
      issynthetic = false;
      exceptions = null;

      final int attributeCount = consumeU2();
      for (int z = 0; z < attributeCount; ++z) {
        final int result = findNextAttribute(DEPRECATED_ATTR, SYNTHETIC_ATTR,
            EXCEPTIONS_ATTR, SIGNATURE_ATTR);
        int dlength;

        switch (result) {
          case 0: // Deprecated
            dlength = consumeU4();
            position += dlength;
            isdeprecated = true;
            break;

          case 1: // Synthetic
            dlength = consumeU4();
            position += dlength;
            issynthetic = true;
            break;

          case 2: // Exceptions
            dlength = consumeU4();
            final int endposition = position + dlength;
            final int exceptionCount = consumeU2();
            exceptions = new String[exceptionCount];
            for (int cx = 0; cx < exceptionCount; ++cx) {
              final int exceptionNameIndex = consumeU2();
              final String exceptionName = getCPClassName(exceptionNameIndex);
              exceptions[cx] = exceptionName;
              if (Settings.debugLevel > 50) {
                log("DD Throwing " + exceptionName);
              }
            }
            position = endposition;
            break;

          case 3: // Signature
            dlength = consumeU4();
            signature = getCPString(consumeU2());
            break;

          case 4: // Code
            dlength = consumeU4();
            position += dlength;
            break;

          default:
            break;
        }
      }

      if (STATIC_INIT_NAME.equals(name)) {
        has_static_init = true;
      } else if (!issynthetic) {
        if (CONSTRUCTOR_NAME.equals(name)) {
          final MyConstructor ctor
              = new MyConstructor(modifiers, /*isdeprecated,*/ exceptions);
          ctor.init(name, descriptor);
          if (ctors == null) {
            ctors = new ArrayList(3);
          }

          if (signature != null){
            parseMethodSignature(signature, ctor);
          }

          ctors.add(ctor);
          if (Settings.debugLevel > 50) {
            log("DD Constructor: " + descriptor
                + (isdeprecated ? " Deprecated" : "")
                + (issynthetic ? " Synthetic" : " not synthetic"));
          }
        } else {
          final MyMethod method
              = new MyMethod(modifiers, name, /*isdeprecated,*/ exceptions);
          method.init(name, descriptor);

          if (signature != null){
            parseMethodSignature(signature, method);
          }

          methods.add(method);
        }
      }
    }
  }

  private final void readFields(final int fields_count) {
    int modifiers;
    MyField myField;
    String name;
    String descriptor;
    String signature;
    boolean isdeprecated;
    boolean issynthetic;

    fields = new ArrayList(fields_count);

    for (int i = 0; i < fields_count; ++i) {
      myField = null;
      signature = null;
      modifiers = consumeU2();
      name = getCPString(consumeU2()).intern();
      descriptor = getCPSlashDotString(consumeU2());
      isdeprecated = false;
      issynthetic = false;

      final int attributeCount = consumeU2();
      for (int z = 0; z < attributeCount; ++z) {
        final int result = findNextAttribute(DEPRECATED_ATTR, SYNTHETIC_ATTR,
            SIGNATURE_ATTR, null);
        int dlength;

        switch (result) {
          case 0: // Deprecated
            dlength = consumeU4();
            position += dlength;
            isdeprecated = true;
            break;

          case 1: // Synthetic
            issynthetic = true;
            dlength = consumeU4();
            position += dlength;
            break;

          case 2: // Signature
            dlength = consumeU4();
            signature = getCPString(consumeU2());
            break;

          default:
        }
      }

      if (!issynthetic) {
        myField = new MyField(modifiers, name, descriptor, isdeprecated);
        if (signature != null){
          myField.signature = new StringBuffer(signature.substring(0,
              signature.length() - 1).replace('/', '.'));
        }
        fields.add(myField);
      }

      if (Settings.debugLevel > 50) {
        log("DD Field: " + (i + 1) + " " + name + " " + descriptor
            + (isdeprecated ? " Deprecated" : ""));
      }
    }
  }

  private final void readInterfaces(final int interfaces_count) {
    interface_names = new String[interfaces_count];
    for (int i = 0; i < interfaces_count; ++i) {
      interface_names[i] = getCPClassName(consumeU2());
      if (Settings.debugLevel > 50) {
        log("DD implements " + interface_names[i]);
      }
    }
  }

  private final String getCPClassName(final int index) {
    return getCPSlashDotString(getU2At(constantPoolPositions[index]));
  }

  private final String getCPSlashDotString(final int index) {
    if (slashDotString[index] == null) {
      slashDotString[index] = new String(data,
          constantPoolStringPositions[index], constantPoolStringLengths[index]);
      slashDotString[index] = slashDotString[index].replace('/', '.').intern();
    }
    return slashDotString[index];
  }

  private final String getCPString(final int index) {
    if (constantPoolStrings[index] == null) {
      constantPoolStrings[index] = new String(data,
          constantPoolStringPositions[index],
          constantPoolStringLengths[index]).intern();
    }
    return constantPoolStrings[index];
  }

  private final int findNextAttribute(final String c1, final String c2,
      final String c3, final String c4) {
    final int nameIndex = consumeU2();
    final String attributeName = getCPString(nameIndex);
    if (attributeName.equals(c1)) {
      return 0;
    } else if (attributeName.equals(c2)) {
      return 1;
    } else if (c3 != null && attributeName.equals(c3)) {
      return 2;
    } else if (c4 != null && attributeName.equals(c4)) {
      return 3;
    }
//    else if ("Code".equals(attributeName)) {
//      return 4;
//    }
//    else {
//      System.err.println("skipping attribute: " + attributeName);
//    }

    final int length = consumeU4();
    position += length;
    if (Settings.debugLevel > 50) {
      log("DD skipping attribute: " + attributeName);
    }

    return -1;
  }

  private final void readConstantPool(final int constantPoolCount) {
    constantPoolStrings = new String[constantPoolCount];
    slashDotString = new String[constantPoolCount];
    constantPoolStringLengths = new int[constantPoolCount];
    constantPoolStringPositions = new int[constantPoolCount];
    constantPoolPositions = new int[constantPoolCount];

    // N.B! this is correct. don't ask me why
    for (int i = 1; i < constantPoolCount; ++i) {
      final int tag = consumeU1();
      if (tag == 1) {
        final int utfLength = consumeU2();
        constantPoolStringLengths[i] = utfLength;
        constantPoolStringPositions[i] = position;
        position += utfLength;
      } else {
        constantPoolPositions[i] = position;
        position += CP_TAG_SIZES[tag];

        if (tag == 5 || tag == 6) {
          ++i; // LONG and DOUBLE take 2 spaces
        }
      }
    }
  }

  // TODO: more effective binary arithmetic ???

  private static final int byteToInt(final byte b) {
    if (b >= 0) {
      return b;
    }
    return 256 + b;
  }

  private final int getU2At(final int index) {
    return byteToInt(data[index]) * 256 + byteToInt(data[index + 1]);
  }

  private final int consumeU4() {
    return consumeU2() * 65536 + consumeU2();
  }

  private final int consumeU1() {
    return byteToInt(data[position++]);
  }

  private final int consumeU2() {
    return byteToInt(data[position++]) * 256 + byteToInt(data[position++]);
  }

  private final void log(final String aMessage) {
    if (myLog == null) {
      myLog = new StringBuffer(2000);
    }
    myLog.append(aMessage);
    myLog.append("\n");
  }

  private static final boolean ignoreInner(final String innerName) {
    final int pos = innerName.lastIndexOf('$');
    if (pos != -1 && pos + 1 < innerName.length()) {
      final int ch = innerName.charAt(pos + 1);
      if (ch >= 0x30 && ch <= 0x39) {
        return true;
      }
    }

    return false;
  }

  public final ArrayList getTypeParameters() {
    return this.typeParameters;
  }

  public final ArrayList getParentsInfoFromSignature() {
    return this.parentsInfoFromSignature;
  }
}
