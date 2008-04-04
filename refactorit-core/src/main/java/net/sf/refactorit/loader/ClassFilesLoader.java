/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;

import net.sf.refactorit.classfile.ClassData;
import net.sf.refactorit.classfile.ClassDataSource;
import net.sf.refactorit.classfile.ClassDataWithStaticInnerSupport;
import net.sf.refactorit.classfile.ClassFormatException;
import net.sf.refactorit.classfile.DataSource;
import net.sf.refactorit.classfile.ClassData.MyInnerType;
import net.sf.refactorit.classfile.ClassData.MyTypeData;
import net.sf.refactorit.classmodel.BinAnnotation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCITypeRef;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinEnum;
import net.sf.refactorit.classmodel.BinEnumConstant;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinGenericTypeRef;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariableArityParameter;
import net.sf.refactorit.classmodel.BinWildcardTypeRef;
import net.sf.refactorit.classmodel.DependencyParticipant;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.source.Resolver;

import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



/**
 * Defines class for extracting all class properties
 */
public class ClassFilesLoader {
  /* The project we're associated with */
  private Project project;
  private Logger log = AppRegistry.getLogger(ClassFilesLoader.class);

  private final ClassDataWithStaticInnerSupport classDataCreator
      = new ClassDataWithStaticInnerSupport();

  public ClassFilesLoader(final Project project) {
    setProject(project);
  }

  //
  // Accessor methods
  //

//  private ClassPath getClassPath(){
//    return getProject().getClassPath();
//  }

  public final Project getProject() {
    return this.project;
  }

  final void setProject(final Project project) {
    this.project = project;
  }

  public final boolean existsTypeForQualifiedName(final String qName) {
    return findDataSourceForQualifiedName(qName) != null;
  }

  public BinType findTypeForQualifiedName(final String qName) {
    final DataSource classSource = findDataSourceForQualifiedName(qName);

    if (classSource == null) {
      return null;
    }

    try {
      return loadBinType(classSource, qName);
    } catch (FileNotFoundException e) {
      //file doesn't exist anymore, must show error for user, but return null
      log.debug("Not found: " + qName, e);
      return null;
    } catch (IOException e) {
      //file is corrupted, must show error for user, but return null
      log.debug("Not found: " + qName, e);
      return null;
    } catch (ClassFormatException e) {
      //file is corrupted, must show error for user, but return null
      log.debug("Not found: " + qName, e);
      return null;
    }
  }

  private final Object PSEUDO_NULL = new Object();
  private final HashMap foundSources = new HashMap(2000);

  private final DataSource findDataSourceForQualifiedName(final String name) {
    final Object cached = foundSources.get(name);
    if (cached == PSEUDO_NULL) {
      return null;
    }
    if (cached != null) {
      return (DataSource) cached;
    }

    final String qualifiedName = name.replace('.', '/') + CLASS_FILE_EXT;

    DataSource result = null;
    // Check if that fully-qualified type exists
    if (getProject().getPaths().getClassPath().exists(qualifiedName)) {

      // Compose new ClassDataSource as a result
      result = new ClassDataSource(getProject(), qualifiedName);

    } else {
      // The requested entity does not exist on classpath
      result = null;
    }

    if (result == null) {
      foundSources.put(name, PSEUDO_NULL);
    } else {
      foundSources.put(name, result);
    }

    return result;
  }

  public void putToCacheAsNotFound(String qualifiedName) {
    foundSources.put(qualifiedName, PSEUDO_NULL);
  }

  private static final HashMap typeParametersMap = new HashMap();
  private static final HashMap ownersMap = new HashMap();

  // FIXME: assumes that all dependencies are to .class files for now
  private final BinCIType loadBinType(final DataSource aSource,
      final String qName) throws FileNotFoundException, IOException,
      ClassFormatException {
//System.err.println("--> loading binary class:'" + qName + "'");

    if (!aSource.exists()) {
      return null;
    }

    final ClassData classData = classDataCreator.get(DataSource.getInputData(
        aSource));

    final String qClassname = classData.getName();

    if (!qClassname.equals(qName)) {
      throw new ClassFormatException(
          "class defined in file had different name! In file:'" + qClassname
          + "' expected:'" + qName + "'");
    }

    final String className = classData.getTypeName();

    final String packageName = classData.getPackageName();
    final BinPackage aPackage = project.createPackageForName(packageName);
    final BinTypeRef curTypeRef = project.createCITypeRefForName(
        qClassname, this);
    curTypeRef.setResolver(Resolver.getForClassFile(curTypeRef));

    if (qName.equals("java.lang.Object")){
      project.objectRef = curTypeRef;
    }

     //It is possible that class is inner, but owner name is not defined
    //anonymous inner class and method inner classes
    //owner CANNOT be identified by taking first part of qualified name
    //we don't read them at all
    final String ownerName = classData.getDeclaringClassName();

    // Jbuilder.jar obfuscator. CAUTION. CHECK. FIXME
    //if(ownerName == null && classData.isInner()) {
    //	throw new ClassFormatException("Reference to anonymous or method inner:"+qName);
    //}

    BinTypeRef b_owner = null;
    if (ownerName != null) {
      b_owner = getFakeOrRealTypeRef(ownerName);
      if (b_owner == null) {
        throw new ClassFormatException("Unable to locate owner:'" + ownerName
            + "' for class:'" + qName + "'");
      }
    }

    ownersMap.put(qName, ownerName);

    // LOAD TYPE PARAMETERS
    final ArrayList typeParameters = classData.getTypeParameters();
    BinTypeRef[] typeParameterRefs = null;
    if (typeParameters != null){
      typeParameterRefs = buildTypeParameters(typeParameters,
          curTypeRef, aPackage, b_owner, ownerName);
      typeParametersMap.put(qName, typeParameterRefs);
    }

    // LOAD SUPERCLASS AND SUPERINTERFACES
    BinTypeRef b_superclass = null;
    BinTypeRef[] b_interfaces = null;
    final ArrayList parentsInfo = classData.getParentsInfoFromSignature();
    boolean builtSupertypes = false;
    try {
      if (parentsInfo != null) {
        // build ineritance info using J2SE5 signature
//System.err.println("qName: " + qName + " - " + parentsInfo);
        b_superclass
            = getRefForTypeInSignature((StringBuffer) parentsInfo.get(0),
            typeParameterRefs, b_owner, ownerName, false);
        final int max = parentsInfo.size();
        b_interfaces = new BinTypeRef[max - 1];
        for (int i = 1; i < max; i++) {
          b_interfaces[i - 1] = getRefForTypeInSignature(
              (StringBuffer) parentsInfo.get(i),
              typeParameterRefs, b_owner, ownerName, false);
        }
        builtSupertypes = true;
      }
    } catch (Exception e) {
      log.debug("Failed to build J2SE5 supertype", e);
    }
    if (!builtSupertypes) {
      // build inheritance info using the old way
      b_superclass = null;
      final String superclassName = classData.getSuperclassName();
      if (classData.isClass()) {
        if (superclassName != null) {
          b_superclass = getFakeOrRealTypeRef(superclassName);
        }
      }
      final String[] interfaceNames = classData.getInterfaceNames();
      b_interfaces = new BinTypeRef[interfaceNames.length];
      for (int i = 0; i < interfaceNames.length; ++i) {
        b_interfaces[i] = getFakeOrRealTypeRef(interfaceNames[i], true);
      }
    }

    // LOAD METHODS
    final BinMethod[] b_methods;
    final ArrayList methods = classData.getMethods();
    if (methods == null) {
      b_methods = BinMethod.NO_METHODS;
    } else {
      int max = methods.size();
      b_methods = new BinMethod[max];

      BinTypeRef[] methodTypeParameters = null;

      for (int q = 0; q < max; q++) {
        final ClassData.MyMethod method = (ClassData.MyMethod) methods.get(q);
        boolean buildedMethod = false;
        try {
          if (method.methodParameterSignatures != null
              || method.returnTypeSignature != null
              || method.typeParameterSignatures != null) {
            // try to build method type parameters if any
            methodTypeParameters = (method.typeParameterSignatures != null)
                ? buildTypeParameters(method.typeParameterSignatures, curTypeRef,
                aPackage, curTypeRef, qName)
                : null;
            // build method using J2SE5 signature
            b_methods[q] = buildMethodJ2SE5(curTypeRef, method.modifiers,
                method.name, method.methodParameterSignatures,
                method.returnTypeSignature, methodTypeParameters,
                method.exceptions, qName);
            buildedMethod = b_methods[q] != null;
          }
        } catch (Exception e) {
          log.debug("Failed to build J2SE5 method", e);
        }

        if (!buildedMethod) {
          // build method the old way
          b_methods[q] = buildMethod(curTypeRef, method.modifiers, method.name,
              method.paramTypes, method.returnType,
              method.exceptions/*, method.isdeprecated*/);
        }
      }
    }

    // LOAD MODIFIERS
    final int b_modifiers = classData.getModifiers();

    // LOAD CONSTRUCTORS
    final BinConstructor[] b_constructors;
    final ArrayList ctors = classData.getConstructors();
    if (ctors == null) {
      b_constructors = BinConstructor.NO_CONSTRUCTORS;
    } else {
      int max = ctors.size();
      b_constructors = new BinConstructor[max];
      int defaultCnstrs = 0;
      final boolean isStaticInner = BinModifier.hasFlag(b_modifiers,
          BinModifier.STATIC) && ownerName != null;
      BinTypeRef[] ctorTypeParameters = null;
      for (int q = 0; q < max; q++) {
        final ClassData.MyConstructor ctor = (ClassData.MyConstructor) ctors.get(q);
        boolean buildedCnstr = false;
        try {
          if (ctor.returnTypeSignature != null
              || ctor.methodParameterSignatures != null
              || ctor.typeParameterSignatures != null) {
            // try to build method type parameters if any
            ctorTypeParameters = (ctor.typeParameterSignatures != null)
                ? buildTypeParameters(ctor.typeParameterSignatures,
                curTypeRef,
                aPackage, curTypeRef, qName)
                : null;
            // build method using J2SE5 signature
            b_constructors[q] = buildConstructorJ2SE5(curTypeRef,
                ctor.modifiers,
                ctor.methodParameterSignatures, ctorTypeParameters,
                ctor.exceptions, qName);
            buildedCnstr = b_constructors[q] != null;
          }
        } catch (Exception e) {
          log.debug("Failed to build J2SE5 constructor", e);
        }

        if (!buildedCnstr) {
          b_constructors[q] = buildConstructor(
              curTypeRef,
              isStaticInner ? null : ownerName,
              ctor.modifiers, ctor.paramTypes,
              ctor.exceptions /*, ctor.isdeprecated*/);

          // XXX: this is a hack to avoid removing owner type parameter of the
          // second constructor of inner class
          // it doesn't work when default cnstr is not first
          // or when programmer written something like this
          // Inner(int)
          // Inner(Owner, int)
          // then we remove Owner and a get a conflict here :(
          if (b_constructors[q].getParameters().length == 0) {
            ++defaultCnstrs;
          }
          if (defaultCnstrs > 1) {
            b_constructors[q] = buildConstructor(
                curTypeRef,
                null,
                ctor.modifiers, ctor.paramTypes,
                ctor.exceptions /*, ctor.isdeprecated*/);
          }
        }
      }
    }

    // LOAD FIELDS
    final BinField[] b_fields;
    final ArrayList fields = classData.getFields();
    BinTypeRef fieldType = null;
    if (fields == null) {
      b_fields = BinField.NO_FIELDS;
    } else {
      b_fields = new BinField[fields.size()];
      for (int q = 0, max = fields.size(); q < max; q++) {
        final ClassData.MyField field = (ClassData.MyField) fields.get(q);
        try {
          if (field.signature != null) {
            fieldType = getRefForTypeInSignature(field.signature,
                typeParameterRefs, b_owner, ownerName, true);
          } else {
            fieldType = null;
          }
        } catch (Exception e) {
          log.debug("Failed to build J2SE5 type of field", e);
          fieldType = null;
        }
        b_fields[q] = buildField(curTypeRef, field.modifiers, field.name, field,
            fieldType, field.isdeprecated);
      }
    }

    // LOAD INNER CLASSES
    final BinTypeRef[] b_inners;
    final ArrayList inners = classData.getDeclaredTypes();
    if (inners == null) {
      b_inners = BinTypeRef.NO_TYPEREFS;
    } else {
      int max = inners.size();
      b_inners = new BinTypeRef[max];
      for (int q = 0; q < max; q++) {
        final MyInnerType inner = (MyInnerType) inners.get(q);
        final BinTypeRef curInner = getFakeOrRealTypeRef(inner.name);
        if (curInner == null) {
          throw new ClassFormatException("Could not locate inner ref for name: "
              + inner.name);
        }
        b_inners[q] = curInner;
        // TODO add dependables
      }
    }

    // BUILD TYPE
    BinCIType bc;
    if (classData.isEnum()) {
      bc = new BinEnum(aPackage, className,
          b_methods, b_fields, null, b_constructors,
          BinInitializer.NO_INITIALIZERS, b_inners, b_owner, b_modifiers, project);
    } else if (classData.isAnnotation()) { 
      // order matters! Should go before the isInterface() call
      
      // pretend that we are using the fields instead of methods:
      final BinField[] annotationFields = new BinField[methods.size()];
      for (int q = 0, max = methods.size(); q < max; q++) {
        final ClassData.MyMethod method = (ClassData.MyMethod) methods.get(q);
        try {
          if (method.returnTypeSignature != null) {
            fieldType = getRefForTypeInSignature(method.returnTypeSignature,
                    typeParameterRefs, b_owner, ownerName, true);
          } else {
            fieldType = null;
          }
        } catch (Exception e) {
          log.debug("Failed to build J2SE5 type of field", e);
          fieldType = null;
        }
        BinTypeRef type = fieldType;
        if (type == null) {
          type = getRefForMyType(method.returnType);
        }
        annotationFields[q] = buildField(curTypeRef, method.modifiers,
                method.name, method.returnType, fieldType, false);
      }
      bc = new BinAnnotation(aPackage, className, b_methods, annotationFields,
              null, b_constructors, BinInitializer.NO_INITIALIZERS, b_inners,
              b_owner, b_modifiers, project);
     
    } else if (classData.isInterface()) {
      bc = new BinInterface(aPackage, className,
          b_methods, b_fields, null, b_inners, b_owner, b_modifiers, project);
    } else {
      bc = new BinClass(aPackage, className,
          b_methods, b_fields, null, b_constructors,
          BinInitializer.NO_INITIALIZERS, b_inners, b_owner, b_modifiers, project);
    }

    /*if (Assert.enabled) {
      //JBuilder.jar had a strange pattern for inner classes that breaks this rule
      Assert.must(qName.equals(bc.getQualifiedName()),
      "Constructed class name differs from requested name.constructed:'"
      +bc.getQualifiedName()+"' requested:'"+qName+"'");
    }*/

    if (b_owner == null) {
      aPackage.addType(curTypeRef);
    }

    if (b_owner == null || b_owner.isResolved()){
      ownersMap.clear();
      typeParametersMap.clear();
    }

    bc.setOwners(curTypeRef);
    bc.setDeprecated(classData.isDeprecated());
    bc.setTypeParameters(typeParameterRefs);

    if (!classData.isInterface()) { // FIXME: annotation?
      curTypeRef.setSuperclass(b_superclass);
    }
    curTypeRef.setInterfaces(b_interfaces);
    return bc;
  }

  private final BinTypeRef[] buildTypeParameters(
      final ArrayList typeParametersData, final BinTypeRef curTypeRef,
      final BinPackage aPackage, final BinTypeRef typeOwner,
      final String ownerName) throws ClassFormatException {

    final BinTypeRef[] typeParameterRefs = new BinTypeRef[typeParametersData
        .size()];

    for (int i = 0 ; i < typeParametersData.size(); i++){
      final ClassData.MyTypeParameter paramData = (ClassData.MyTypeParameter)
          typeParametersData.get(i);
      typeParameterRefs[i]
          = buildTypeParameterHeader(paramData.parameterName);
    }

    for (int i = 0; i < typeParameterRefs.length; i++){
      final ClassData.MyTypeParameter paramData = (ClassData.MyTypeParameter)
          typeParametersData.get(i);
      buildTypeParameter(curTypeRef, aPackage, typeParameterRefs[i],
          paramData.supers, typeParameterRefs, typeOwner, ownerName);
    }

    return typeParameterRefs;
  }

//  private final BinTypeRef createFakeWildcardArgument(){
//    final BinCIType wildcardType = new BinCIType(project.getPackageForName(""),
//          "?", BinMethod.NO_METHODS, BinField.NO_FIELDS,
//        BinFieldDeclaration.NO_FIELDDECLARATIONS, BinTypeRef.NO_TYPEREFS,
//        null, BinModifier.PUBLIC, project) {
//      public boolean isInterface() {
//        return false;
//      }
//      public boolean isClass() {
//        return false;
//      }
//      public boolean isEnum() {
//        return false;
//      }
//      public boolean isAnnotation() {
//        return false;
//      }
//      public String getMemberType() {
//        return "wildcard";
//      }
//      public boolean isWildcard() {
//        return true;
//      }
//    };
//
//    return new BinWildcardTypeRef(wildcardType);
//  }

  private final BinTypeRef getRefForTypeInSignature(final StringBuffer data,
      final BinTypeRef[] myTypeParameterRefs, final BinTypeRef ownerRef,
      final String ownerName, final boolean expectPrimitives)
      throws ClassFormatException {
//System.err.println("[ARS>>] data: " + data);
    BinTypeRef typeRef = null;
    final StringBuffer typeName = new StringBuffer();
    int i = 0, bracesCount;
    char c;
    try {
      int parametrizedOwnerEnd = 1;
      while (true){
        parametrizedOwnerEnd = data.indexOf(">$", parametrizedOwnerEnd + 1);
        if (parametrizedOwnerEnd >= 0){
          i = parametrizedOwnerEnd;
        } else {
          break;
        }
      }
      if (i > 1){
        bracesCount = 0;
        for (int k = 1; k < i + 1; k++){
          c = data.charAt(k);
          if (c == '<'){
            ++bracesCount;
          } else if (c == '>'){
            --bracesCount;
          } else if (bracesCount == 0){
            typeName.append(c);
          }
        }
      }
      for (i++; (c = data.charAt(i)) != '<'; i++){
        typeName.append(c);
      }
    } catch (StringIndexOutOfBoundsException e) {
//System.err.println(e);
    }

    c = data.charAt(0);
    if (expectPrimitives){
      switch(c){
        case 'I': // int
          return BinPrimitiveType.INT_REF;
        case 'Z': // boolean
          return BinPrimitiveType.BOOLEAN_REF;
        case 'F': // float
          return BinPrimitiveType.FLOAT_REF;
        case 'J': // long
          return BinPrimitiveType.LONG_REF;
        case 'D': // double
          return BinPrimitiveType.DOUBLE_REF;
        case 'S': // short
          return BinPrimitiveType.SHORT_REF;
        case 'B': // byte
          return BinPrimitiveType.BYTE_REF;
        case 'C': // char
          return BinPrimitiveType.CHAR_REF;
        case 'V': // void descriptor
          return BinPrimitiveType.VOID_REF;
        default:
          // it is not a primitive type -> go ahead with analysis
          break;
      }
    }

    switch (c){

      case 'L':
        String typeNameStr = typeName.toString();
        if ("java.lang.Object".equals(typeNameStr)){
          typeRef = project.getObjectRef();
        } else {
          typeRef = getFakeOrRealTypeRef(typeNameStr);
        }

        if (i != data.length()){ // there are arguments to read
          final ArrayList arguments = new ArrayList(2);
          try{
            i++;
            c = data.charAt(i++);
            while(true){
              bracesCount = 0;
              final StringBuffer argumentData = new StringBuffer();
              for ( ; c != ';'  || bracesCount != 0;
                  c = data.charAt(i++)){
                if (c == '<'){
                  ++bracesCount;
                } else if (c == '>'){
                  --bracesCount;
                }
                argumentData.append(c);
              }
              BinTypeRef argRef = getRefForTypeInSignature(argumentData,
                  myTypeParameterRefs, ownerRef, ownerName, expectPrimitives);

              if (!argRef.isSpecific()) {
                argRef = BinSpecificTypeRef.create(argRef);
              }
              ((BinSpecificTypeRef) argRef)
                  .setTypeParameterResolver(typeRef, arguments.size());

              arguments.add(argRef);

              c = data.charAt(i++);
            }
          } catch (StringIndexOutOfBoundsException e) {
//System.err.println(e);
          }
          if (!arguments.isEmpty()){
            typeRef = new BinGenericTypeRef(typeRef);
            typeRef.setTypeArguments(
                (BinTypeRef[]) arguments.toArray(new BinTypeRef[arguments.size()]));
//System.err.println("setArgs: " + arguments);
          }
        }
        break;

      case 'T':
        boolean insideMyself = true;
        BinTypeRef curOwnerRef = ownerRef;
        String curOwnerName = ownerName;
        BinTypeRef[] curTypeParameters = myTypeParameterRefs;
        while(true){
          typeRef = findTypeParameterRefForName(typeName, curTypeParameters);

          if (typeRef == null){ // check owners upwards till possible
            if (curOwnerRef.isResolved()){
              curOwnerRef = (insideMyself)
                ? ownerRef
                : curOwnerRef.getBinCIType().getOwner();

              if (curOwnerRef == null){
                break; // could not find
              }

              curOwnerName = curOwnerRef.getQualifiedName();

              insideMyself = false;
              curTypeParameters = curOwnerRef.getBinCIType().getTypeParameters();

            } else {
              curOwnerName = (insideMyself)
                  ? ownerName
                  : (String) ownersMap.get(curOwnerName);

              insideMyself = false;

              if (curOwnerName == null){
                break; // could not find
              }

              curOwnerRef = project.getTypeRefForName(curOwnerName);
              if (curOwnerRef.isResolved()){
                curTypeParameters = curOwnerRef.getBinCIType().getTypeParameters();
              } else {
                curTypeParameters = (BinTypeRef[]) typeParametersMap.get(
                    curOwnerName);
              }
            }
          } else {
            break; // found what we wanted
          }
        }
        break;

      case '*': // argument <?>
        typeRef = new BinWildcardTypeRef(project.getPackageForName(""),
            ownerRef, project);//createFakeWildcardArgument();
        typeRef.setUpperBound(project.getObjectRef());
        break;

      case '+': // argument <? extends T>
        typeRef = new BinWildcardTypeRef(project.getPackageForName(""),
            ownerRef, project);//createFakeWildcardArgument();
        typeRef.setUpperBound(getRefForTypeInSignature(new StringBuffer(
            data.substring(1)), myTypeParameterRefs, ownerRef, ownerName,
            expectPrimitives));
        break;

      case '-': // argument <? super T>
        typeRef = new BinWildcardTypeRef(project.getPackageForName(""),
            ownerRef, project);//createFakeWildcardArgument();
        typeRef.setLowerBound(getRefForTypeInSignature(new StringBuffer(
            data.substring(1)), myTypeParameterRefs, ownerRef, ownerName,
            expectPrimitives));
        break;

      case '[': // array
        int dimensions = 0;
        do {
          c = data.charAt(++dimensions);
        } while (c == '[');
        typeRef = project.createArrayTypeForType(getRefForTypeInSignature(
            new StringBuffer(data.substring(dimensions)), myTypeParameterRefs,
            ownerRef, ownerName, expectPrimitives), dimensions);
        break;

      default:
        throw new ClassFormatException("Unknown type argument format: type "
            + "argument starts with '" + c + "' character");
    }

    if (typeRef == null){
      throw new ClassFormatException("Can`t find typeRef for name: " + typeName);
    }

//System.err.println("typeRef: " + typeRef);

    return typeRef;
  }

  private static final BinTypeRef findTypeParameterRefForName(final StringBuffer name,
      final BinTypeRef[] myTypeParameterRefs){
    if (myTypeParameterRefs == null){
      return null;
    }

    for (int j = 0; j < myTypeParameterRefs.length; j++){
      if (name.toString().equals(myTypeParameterRefs[j].getName())){
        return myTypeParameterRefs[j];
      }
    }
    return null;
  }

  private final void buildTypeParameter(final BinTypeRef ownerRef,
      final BinPackage aPackage, final BinTypeRef parameterRef,
      final List supersData, final BinTypeRef[] myTypeParameterRefs,
      final BinTypeRef typeOwner, final String ownerName)
      throws ClassFormatException {

    BinTypeRef superClass = getRefForTypeInSignature(
        (StringBuffer) supersData.get(0), myTypeParameterRefs, typeOwner,
        ownerName, false);
    final BinTypeRef[] superInterfaces = new BinTypeRef[supersData.size()-1];
    for (int i = 1, max = supersData.size(); i < max; i++){
      superInterfaces[i-1] = getRefForTypeInSignature(
          (StringBuffer) supersData.get(i), myTypeParameterRefs, typeOwner,
          ownerName, false);
    }

    // JAVA5: this is a hack, must be reimplemented later
    BinCIType type;
    if (superInterfaces.length > 0 && superClass == project.getObjectRef()){

      superClass = null; // for sync with CompilationUnitsLoader

      type = new BinInterface(aPackage,
          parameterRef.getName(), BinMethod.NO_METHODS, BinField.NO_FIELDS,
          BinFieldDeclaration.NO_FIELDDECLARATIONS,
          BinTypeRef.NO_TYPEREFS, ownerRef, BinModifier.PUBLIC, project) {
        public final String getMemberType() {
          return "type parameter";
        }

        public boolean isTypeParameter() {
          return true;
        }
      };
    } else {
      type = new BinClass(aPackage,
          parameterRef.getName(), BinMethod.NO_METHODS,
          BinField.NO_FIELDS,
          BinFieldDeclaration.NO_FIELDDECLARATIONS,
          BinConstructor.NO_CONSTRUCTORS, BinInitializer.NO_INITIALIZERS,
          BinTypeRef.NO_TYPEREFS,
          ownerRef, BinModifier.PUBLIC, project) {
        public String getMemberType() {
          return "type parameter";
        }

        public boolean isTypeParameter() {
          return true;
        }
      };
    }

    type.setTypeRef(parameterRef);
    parameterRef.setBinType(type);
    parameterRef.setSuperclass(superClass);
    parameterRef.setInterfaces(superInterfaces);

    if (type.isClass()) {
      ((BinClass) type).ensureCopiedMethods();
    }
  }

  private final BinTypeRef buildTypeParameterHeader(final String name){
    final BinCIType stubType = new BinCIType(
        null, name, null, 0, project) {
      public boolean isInterface() {
        return false;
      }
      public boolean isClass() {
        return false;
      }
      public boolean isEnum() {
        return false;
      }

      public boolean isAnnotation() {
        return false;
      }

      public String getMemberType() {
        return "type parameter";
      }
      public boolean isTypeParameter() {
        return true;
      }
    };

    return project.createLocalTypeRefForType(stubType);
  }

  private final BinMethod buildMethod(final BinTypeRef owner,
      final int modifiers, final String name,
      final ClassData.MyTypeData[] paramTypes,
      final ClassData.MyTypeData returnType,
      final String[] exceptionNames/*,
      boolean isDeprecated*/) throws ClassFormatException {

    final int paramsNum = paramTypes == null ? 0 : paramTypes.length;
    final BinParameter[] params = new BinParameter[paramsNum];

    boolean varargs = BinModifier.hasFlag(modifiers, BinModifier.VARARGS);

    for (int q = 0; q < paramsNum; q++) {
      //FIXME: parameter name & modifier ?
      final BinTypeRef paramRef = getRefForMyType(paramTypes[q]);
      if (varargs && q == paramsNum - 1) {
        params[q] = new BinVariableArityParameter(null, paramRef, 0);
      } else {
        params[q] = new BinParameter(null, paramRef, 0);
      }
      if (paramRef.isReferenceType()) {
        ((DependencyParticipant) paramRef).addDependableWithoutCheck(owner);
      }
    }

    final BinTypeRef _return = getRefForMyType(returnType);
    if (_return.isReferenceType()) {
      ((DependencyParticipant) _return).addDependableWithoutCheck(owner);
    }

    BinMethod.Throws[] exceptions;
    if (exceptionNames != null) {
      exceptions = new BinMethod.Throws[exceptionNames.length];
      for (int q = 0; q < exceptionNames.length; q++) {
        final BinTypeRef exc = getFakeOrRealTypeRef(exceptionNames[q]);
        exceptions[q] = new BinMethod.Throws(exc);

        ((DependencyParticipant) exc).addDependableWithoutCheck(owner);
      }
    } else {
      exceptions = BinMethod.Throws.NO_THROWS;
    }

    final BinMethod retVal = new BinMethod(name, params, _return, modifiers,
        exceptions, true);
    retVal.setBody(null);
    return retVal;
  }

  private final BinMethod buildMethodJ2SE5(final BinTypeRef owner,
      final int modifiers, final String name,
      final ArrayList methodParameterSignatures,
      final StringBuffer returnTypeSignature,
      final BinTypeRef[] typeParameterRefs, final String[] exceptionNames,
      final String ownerQName) throws ClassFormatException {

    final int paramsNum = (methodParameterSignatures == null)
        ? 0 : methodParameterSignatures.size();
    final BinParameter[] params = new BinParameter[paramsNum];

    boolean varargs = BinModifier.hasFlag(modifiers, BinModifier.VARARGS);

    for (int q = 0; q < paramsNum; q++) {
      final StringBuffer paramTypeName
          = (StringBuffer) methodParameterSignatures.get(q);
      final BinTypeRef paramRef = getRefForTypeInSignature(paramTypeName,
          typeParameterRefs, owner, ownerQName, true);
      if (varargs && q == paramsNum - 1) {
        params[q] = new BinVariableArityParameter(null, paramRef, 0);
      } else {
        params[q] = new BinParameter(null, paramRef, 0);
      }
      if (paramRef.isReferenceType()) {
        ((DependencyParticipant) paramRef).addDependableWithoutCheck(owner);
      }
    }

    final BinTypeRef _return = getRefForTypeInSignature(returnTypeSignature,
        typeParameterRefs, owner, ownerQName, true);
    if (_return.isReferenceType()) {
      ((DependencyParticipant) _return).addDependableWithoutCheck(owner);
    }

    BinMethod.Throws[] exceptions;
    if (exceptionNames != null) {
      exceptions = new BinMethod.Throws[exceptionNames.length];
      for (int q = 0; q < exceptionNames.length; q++) {
        final BinTypeRef exc = getFakeOrRealTypeRef(exceptionNames[q]);
        exceptions[q] = new BinMethod.Throws(exc);

        ((DependencyParticipant) exc).addDependableWithoutCheck(owner);
      }
    } else {
      exceptions = BinMethod.Throws.NO_THROWS;
    }

    final BinMethod retVal = new BinMethod(name, params, _return, modifiers,
        exceptions, true);
    retVal.setTypeParameters(typeParameterRefs);
    retVal.setBody(null);
    return retVal;
  }

  private final BinConstructor buildConstructor(
      final BinTypeRef owner,
      final String ownerName,
      final int modifiers,
      final ClassData.MyTypeData[] paramTypes,
      final String[] exceptionNames/*,
      boolean isDeprecated*/) throws ClassFormatException {

    int start;
    final int paramsNum = paramTypes == null ? 0 : paramTypes.length;
    if (ownerName != null && paramsNum > 0
        && paramTypes[0].typeName.length() > 2
        && ownerName.equals(paramTypes[0].typeName.substring(1,
        paramTypes[0].typeName.length() - 1))) {
      start = 1; // first is owner type reference for local type
    } else {
      start = 0;
    }
    final BinParameter[] params = new BinParameter[paramsNum - start];

    boolean varargs = BinModifier.hasFlag(modifiers, BinModifier.VARARGS);

    for (int q = start; q < paramsNum; q++) {
      //FIXME: parameter name & modifier ?
      final BinTypeRef paramRef = getRefForMyType(paramTypes[q]);

      if (varargs && q == paramsNum - 1) {
        params[q - start] = new BinVariableArityParameter(null, paramRef, 0);
      } else {
        params[q - start] = new BinParameter(null, paramRef, 0);
      }

      if (paramRef.isReferenceType()) {
        ((DependencyParticipant) paramRef).addDependableWithoutCheck(owner);
      }
    }

    BinMethod.Throws[] exceptions;
    if (exceptionNames != null) {
      exceptions = new BinMethod.Throws[exceptionNames.length];
      for (int q = 0; q < exceptionNames.length; q++) {
        final BinTypeRef exc = getFakeOrRealTypeRef(exceptionNames[q]);
        exceptions[q] = new BinMethod.Throws(exc);

        ((DependencyParticipant) exc).addDependableWithoutCheck(owner);
      }
    } else {
      exceptions = BinMethod.Throws.NO_THROWS;
    }

    final BinConstructor retVal
        = new BinConstructor(params, modifiers, exceptions);
//    retVal.setDeprecated(isDeprecated);
    retVal.setBody(null);
    return retVal;
  }

  private final BinConstructor buildConstructorJ2SE5(
      final BinTypeRef ownerRef,
      final int modifiers,
      final ArrayList methodParameterSignatures,
      final BinTypeRef[] typeParameterRefs,
      final String[] exceptionNames,
      final String ownerQName) throws ClassFormatException {

    final int paramsNum = (methodParameterSignatures == null)
        ? 0 : methodParameterSignatures.size();
    final BinParameter[] params = new BinParameter[paramsNum];

    boolean varargs = BinModifier.hasFlag(modifiers, BinModifier.VARARGS);

    for (int q = 0; q < paramsNum; q++) {
      final StringBuffer paramTypeName
          = (StringBuffer) methodParameterSignatures.get(q);
      final BinTypeRef paramRef = getRefForTypeInSignature(paramTypeName,
          typeParameterRefs, ownerRef, ownerQName, true);

      if (varargs && q == paramsNum - 1) {
        params[q] = new BinVariableArityParameter(null, paramRef, 0);
      } else {
        params[q] = new BinParameter(null, paramRef, 0);
      }

      if (paramRef.isReferenceType()) {
        ((DependencyParticipant) paramRef).addDependableWithoutCheck(ownerRef);
      }
    }

    BinMethod.Throws[] exceptions;
    if (exceptionNames != null) {
      exceptions = new BinMethod.Throws[exceptionNames.length];
      for (int q = 0; q < exceptionNames.length; q++) {
        final BinTypeRef exc = getFakeOrRealTypeRef(exceptionNames[q]);
        exceptions[q] = new BinMethod.Throws(exc);

        ((DependencyParticipant) exc).addDependableWithoutCheck(ownerRef);
      }
    } else {
      exceptions = BinMethod.Throws.NO_THROWS;
    }

    final BinConstructor retVal
        = new BinConstructor(params, modifiers, exceptions);
    retVal.setBody(null);
    retVal.setTypeParameters(typeParameterRefs);
    return retVal;
  }

  private final BinField buildField(final BinTypeRef owner, final int modifiers,
      final String name, final MyTypeData typeData, final BinTypeRef j2se5type,
      final boolean isDeprecated) throws ClassFormatException {
    BinTypeRef type = j2se5type;
    if (type == null){
      type = getRefForMyType(typeData);
    }

    BinField retVal;
    if (BinModifier.hasFlag(modifiers, BinModifier.ENUM)) {
      retVal = new BinEnumConstant(name, owner, modifiers, true);
    } else {
      retVal = new BinField(name, type, modifiers, true);
    }
    retVal.setDeprecated(isDeprecated);

    if (type.isReferenceType()) {
      ((DependencyParticipant) type).addDependableWithoutCheck(owner);
    }

    return retVal;
  }

  /**
   * Same as getFakeOrRealTypeRef, but also has array handling
   */
  private final BinTypeRef getRefForMyType(final ClassData.MyTypeData typeData) {
    final int dimensions = typeData.dimension;
    final String qName = typeData.typeName;

    if (Assert.enabled && qName == null) {
      Assert.must(false, "qName == null");
    }

    if (dimensions > 0) {
      final BinTypeRef arrayType = getFakeOrRealTypeRef(qName);
      if (Assert.enabled && arrayType == null) {
        Assert.must(false, "Failed to locate type for name:'" + qName + "'");
      }
      final BinTypeRef resultType = project.createArrayTypeForType(arrayType,
          dimensions);
      return resultType;
    } else {
      return getFakeOrRealTypeRef(qName);
    }
  }

  private final BinTypeRef getFakeOrRealTypeRef(final String qName) {
    return getFakeOrRealTypeRef(qName, false);
  }

  private final BinTypeRef getFakeOrRealTypeRef(final String qName, final boolean expectInterface) {
    BinTypeRef result = project.getTypeRefForName(qName);
    if (result == null) {
      result = new BinCITypeRef(qName, this);
      ((BinCITypeRef) result).setClazz(!expectInterface);
      project.addLoadedType(qName, result);
    }
    return result;
  }

  // TypeDefs by old 'n crappy impl.
  // Object[] classpathParts;
  // Project project;

  public static final String CLASS_FILE_EXT = ".class";
}
