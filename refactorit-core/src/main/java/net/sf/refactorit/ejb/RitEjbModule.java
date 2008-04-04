/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ejb;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ejb.om.LocationAwareEjbPart;
import net.sf.refactorit.ejb.om.LocationRange;
import net.sf.refactorit.ejb.om.RitEjbAppData;
import net.sf.refactorit.ejb.om.RitEjbData;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.vfs.FileChangeListener;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import org.xml.sax.Locator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author jura
 */
public class RitEjbModule {

  public static ArrayList ejbs = new ArrayList();

  public static class RitFileChangeListener implements FileChangeListener {

    public void fileCreated(Source source) {
      if(isEJBDescriptor(source)) {
        filesChanged=true;
      }
    }

    public void fileDeleted(Source source) {
      if(isEJBDescriptor(source)) {
        filesChanged=true;
      }
    }

    public void fileContentsChanged(Source source) {
      if(isEJBDescriptor(source)) {
        filesChanged=true;
      }
    }

    public void fileRenamed(Source newSource, String oldName) {
      if(isEJBDescriptor(newSource)) {
        filesChanged=true;
      }
    }

    public void unknownChangesHappened() {
      filesChanged=true;
    }

  }

  public static boolean isEJBDescriptor(Source xmlSource) {
    String absolutePath = xmlSource.getAbsolutePath();
    if (/*!absolutePath.endsWith("META-INF" + File.separator + "ejb-jar.xml")*/
        !absolutePath.endsWith("/ejb-jar.xml")
        && !absolutePath.endsWith("\\ejb-jar.xml")) {
      return false;
    }
    return true;
  }

  public static boolean isInEjbDeclarations(String fullyQuolifiedTypeName) {
    checkUpToDate();
    if (ejbAppDataFor(fullyQuolifiedTypeName) != null)
      return true;
    return false;
  }

  private static RitEjbAppData ejbAppDataFor(String fullyQuolifiedTypeName) {
    checkUpToDate();
    Iterator i = RitEjbModule.ejbs.iterator();
    while (i.hasNext()) {
      RitEjbAppData ritEjbDataObj = (RitEjbAppData) i.next();
      if (ritEjbDataObj.isInEjbDeclarations(fullyQuolifiedTypeName))
        return ritEjbDataObj;
    }
    return null;
  }

  /**
   * @param fullyQualifiedOldName
   * @param fullyQualifiedNewName
   * @param partType -
   *          one of RitEjbConstants ()
   */
  protected static void renameBeanPart(String fullyQualifiedOldName,
          String fullyQualifiedNewName, int partType) {
    Collection relatedBeans;
    Iterator i = RitEjbModule.ejbs.iterator();
    while (i.hasNext()) {
      RitEjbAppData ritEjbAppData = (RitEjbAppData) i.next();
      relatedBeans = ritEjbAppData.getRelatedEjbs(partType,
              fullyQualifiedOldName);
      Iterator ii = relatedBeans.iterator();
      while (ii.hasNext()) {
        RitEjbData ritEjbData = (RitEjbData) ii.next();
        ritEjbData.renameDeclarationPart(partType, fullyQualifiedNewName);
      }
    }
  }

  public static void renameEjbClass(String fullyQualifiedOldName,
          String fullyQualifiedNewName) {
    renameBeanPart(fullyQualifiedOldName, fullyQualifiedNewName,
            RitEjbConstants.EJB_CLASS);
  }

  public static void renameHomeInterface(String fullyQualifiedOldName,
          String fullyQualifiedNewName) {
    renameBeanPart(fullyQualifiedOldName, fullyQualifiedNewName,
            RitEjbConstants.HOME);
  }

  public static void renameLocalInterface(String fullyQualifiedOldName,
          String fullyQualifiedNewName) {
    renameBeanPart(fullyQualifiedOldName, fullyQualifiedNewName,
            RitEjbConstants.LOCAL);
  }

  public static void renameLocalHomeInterface(String fullyQualifiedOldName,
          String fullyQualifiedNewName) {
    renameBeanPart(fullyQualifiedOldName, fullyQualifiedNewName,
            RitEjbConstants.LOCAL_HOME);
  }

  public static void renamePrimaryKeyClass(String fullyQualifiedOldName,
          String fullyQualifiedNewName) {
    renameBeanPart(fullyQualifiedOldName, fullyQualifiedNewName,
            RitEjbConstants.PRIM_KEY_CLASS);
  }

  public static void renameRemoteInterface(String fullyQualifiedOldName,
          String fullyQualifiedNewName) {
    renameBeanPart(fullyQualifiedOldName, fullyQualifiedNewName,
            RitEjbConstants.REMOTE);
  }

  public static void renameServiceEnpointInterface(
          String fullyQualifiedOldName, String fullyQualifiedNewName) {
    renameBeanPart(fullyQualifiedOldName, fullyQualifiedNewName,
            RitEjbConstants.SERVICE_ENDPOINT);
  }

  public static Source getDescriptorForPart(String fullyQualifiedTypeName) {
    RitEjbAppData appData = ejbAppDataFor(fullyQualifiedTypeName);
    Source descriptorFile = appData.getDescriptorXml();
    return descriptorFile;
  }

  private static void parseForEjbs(SourcePath sourcePathObj) {
    ejbs = new ArrayList();
    WildcardPattern[] xmlPattern = {new WildcardPattern("*.xml")};
    Iterator i = sourcePathObj.getNonJavaSources(xmlPattern).iterator();
    while (i.hasNext()) {
      Source xmlSource = (Source) i.next();
      if (isEjbDescriptorSource(xmlSource)) {
        RitEjbAppData ritEjbAppData = new RitEjbAppData(xmlSource);
        RitEjbModule.ejbs.add(ritEjbAppData);
      }
    }
  }

  private static boolean isEjbDescriptorSource(Source xmlSource) {
    return xmlSource.getName().equals("ejb-jar.xml");
  }

  private static FileChangeListener fileChangeListener = null;
  private static net.sf.refactorit.classmodel.Project activeProject = null;
  private static boolean filesChanged=false;

  private static void checkUpToDate() {
    if (fileChangeListener == null) {
      initFileChangeListener();
      parseForEjbs();
    } else if (isProjectAltered()) {
      parseForEjbs();
    }else if(filesChanged) {
      filesChanged=false;
      parseForEjbs();
    }
  }


  private static void parseForEjbs() {
    activeProject=IDEController.getInstance().getActiveProject();
    parseForEjbs(activeProject.getPaths().getSourcePath());
  }

  private static boolean isProjectAltered() {
    return !IDEController.getInstance().getActiveProject().equals(activeProject);
  }

  private static void initFileChangeListener() {
    fileChangeListener = new RitFileChangeListener();
    IDEController.getInstance().getActiveProject().getPaths().getSourcePath()
            .getFileChangeMonitor().addFileChangeListener(fileChangeListener);
  }

  public static RitEjbAppData getAppDataForDescriptor(Source xmlDescriptorSrc) {
    Iterator i = ejbs.iterator();
    while (i.hasNext()) {
      RitEjbAppData element = (RitEjbAppData) i.next();
      if (xmlDescriptorSrc.getAbsolutePath().equals(element.getDescriptorXml().getAbsolutePath())) {
        return element;
      }
    }
    return null;
  }

  public static BinItem getTypeFor(Source xmlDescriptorSrc,
          SourceCoordinate coordinate) {
    if(!isEjbDescriptorSource(xmlDescriptorSrc)) {
      return null;
    }
    checkUpToDate();
    RitEjbAppData appData = getAppDataForDescriptor(xmlDescriptorSrc);
    if (appData == null)
      throw new NullPointerException(
              "assertion failed: no RitEjbAppData object found for"
                      + xmlDescriptorSrc);
    String ret=(String)appData.getTypeValueAt(coordinate);
    BinTypeRef ref = fqNameToBinTypeRef(ret);
    if(ref==null)return null;
    return ref.getBinType();
  }

  private static BinTypeRef fqNameToBinTypeRef(String ret) {
    Project activeProject=IDEController.getInstance().getActiveProject();
    BinTypeRef ref=activeProject.findTypeRefForName(ret);
    return ref;
  }

  public static Collection getApps() {
    return ejbs;
  }

  public static Map getRelatedEjbParts(String fullyQualifiedName) {
    Map ret=new LinkedHashMap();
    RitEjbAppData appData = ejbAppDataFor(fullyQualifiedName);
    Map binCITypesEjbParts = getRelatedBinCITypes(fullyQualifiedName, appData);
    for (Iterator i = binCITypesEjbParts.entrySet().iterator(); i.hasNext();) {
      Map.Entry ejbPartEntry=(Entry) i.next();
      String partName=(String) ejbPartEntry.getKey();
      BinCIType binCIType = (BinCIType)ejbPartEntry.getValue();
      ret.put(binCIType.getName()+ " (EJB "+partName+")",binCIType);
    }
    LocationRange locationRange=appData.getStartLocatorFor(fullyQualifiedName);
    Locator startLocator = locationRange.getStartLocator();
    Locator endLocator = locationRange.getEndLocator();
    BinSelection binSelection = new BinSelection("-", startLocator
            .getLineNumber(), startLocator.getColumnNumber(), endLocator
            .getLineNumber(), endLocator.getColumnNumber());
    Source descriptorXml=appData.getDescriptorXml();
    binSelection.setCompilationUnit(new CompilationUnit(descriptorXml,IDEController.getInstance().getActiveProject()));
    ret.put(binSelection.getCompilationUnit().getName()+" (EJB descriptor)", binSelection);
    return ret;
  }

  public static Map getRelatedBinCITypes(String fqName) {
    RitEjbAppData appData = ejbAppDataFor(fqName);
    return appData==null?new HashMap():getRelatedBinCITypes(fqName,appData);
  }

  private static Map getRelatedBinCITypes(String fullyQualifiedName, RitEjbAppData appData) {
    Iterator i=appData.getRelatedEjbs(fullyQualifiedName).iterator();
    Map ret=new HashMap();
    while(i.hasNext()) {
      RitEjbData ejbData=(RitEjbData)i.next();
      Map parts=ejbData.getEjbPats();
      for (Iterator partsIt = parts.entrySet().iterator(); partsIt.hasNext();) {
        Map.Entry ejbPartEntry = (Map.Entry) partsIt.next();
        String partName = RitEjbConstants.NAMES_IN_UI[((Integer)ejbPartEntry.getKey()).intValue()];
        LocationAwareEjbPart element = (LocationAwareEjbPart) ejbPartEntry.getValue();
        String fqName=(String)element.getValue();
        BinTypeRef binTypeRef=fqNameToBinTypeRef(fqName);
        if(binTypeRef!=null) {
          ret.put(partName, binTypeRef.getBinType());
        }
      }
    }
    return ret;
  }

  private static List getRelatedBinCITypeRefs(String fullyQualifiedName, RitEjbAppData appData) {
    Iterator i=appData.getRelatedEjbs(fullyQualifiedName).iterator();
    List ret=new ArrayList();
    while(i.hasNext()) {
      RitEjbData ejbData=(RitEjbData)i.next();
      Map parts=ejbData.getEjbPats();
      for (Iterator partsIt = parts.entrySet().iterator(); partsIt.hasNext();) {
        Map.Entry ejbPartEntry = (Map.Entry) partsIt.next();
        LocationAwareEjbPart element = (LocationAwareEjbPart) ejbPartEntry.getValue();
        String fqName=(String)element.getValue();
        if(fullyQualifiedName.equals(fqName)) {
          //don't need it
          continue;
        }
        BinTypeRef binTypeRef=fqNameToBinTypeRef(fqName);
        if(binTypeRef!=null) {
          ret.add(binTypeRef);
        }
      }
    }
    return ret;
  }

  private static List getRelatedEjbImplypeRefs(String fullyQualifiedName, RitEjbAppData appData) {
      Iterator i=appData.getRelatedEjbs(fullyQualifiedName).iterator();
      List ret=new ArrayList();
      while(i.hasNext()) {
        RitEjbData ejbData=(RitEjbData)i.next();
        String ejbClassFqName=ejbData.getEjbClass();
        BinTypeRef ejbImlClassTypeRef=fqNameToBinTypeRef(ejbClassFqName);
        ret.add(ejbImlClassTypeRef);
      }
      return ret;
    }
  private static List getRelatedEjbInterfacesRefs(String fullyQualifiedName, RitEjbAppData appData) {
      Iterator i=appData.getRelatedEjbs(fullyQualifiedName).iterator();
      List ret=new ArrayList();
      while(i.hasNext()) {
        RitEjbData ejbData=(RitEjbData)i.next();
        String interfaceFqName=ejbData.getEjbRemoteInterface();
        addBinTypeRef(ret, interfaceFqName);
        interfaceFqName=ejbData.getEjbLocalInterface();
        addBinTypeRef(ret,interfaceFqName);
      }
      return ret;
  }

private static void addBinTypeRef(List ret, String interfaceFqName) {
  BinTypeRef ejbImlClassTypeRef=fqNameToBinTypeRef(interfaceFqName);
  if(ejbImlClassTypeRef!=null){
    ret.add(ejbImlClassTypeRef);
  }
}

  public static class DescriptorWithLocation{
    private Source descriptorXml;
    private int row;
    private Locator start;
    private Locator end;
    public DescriptorWithLocation(Source descriptorSource, Locator start,Locator end) {
      descriptorXml=descriptorSource;
      row=start.getLineNumber();
      this.start=start;
      this.end=end;
    }
    public Source getDescriptorXml() {
      return descriptorXml;
    }
    public int getRow() {
      return row;
    }
    public Locator getEnd() {
      return end;
    }
    public Locator getStart() {
      return start;
    }
  }

  public static List getRelatedBinCITypeRefs(BinTypeRef type) {
    String fqName = type.getQualifiedName();
    RitEjbAppData appData = ejbAppDataFor(fqName);
    return appData==null?new ArrayList():getRelatedBinCITypeRefs(fqName,appData);
  }

  public static List getRelatedEJBImplTypeRefs(BinTypeRef type) {
    String fqName = type.getQualifiedName();
    RitEjbAppData appData = ejbAppDataFor(fqName);
    if(appData==null) {
      return new ArrayList();
    }
    return getRelatedEjbImplypeRefs(fqName,appData);
  }

  public static List getRelatedInterfaces(BinTypeRef type) {
  String fqName = type.getQualifiedName();
  RitEjbAppData appData = ejbAppDataFor(fqName);
  if(appData==null) {
    return new ArrayList();
  }
  return getRelatedEjbInterfacesRefs(fqName,appData);
  }

}
