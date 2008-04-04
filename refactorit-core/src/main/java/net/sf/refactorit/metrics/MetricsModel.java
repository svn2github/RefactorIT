/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.metrics;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.ui.options.profile.Profile;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;
import net.sf.refactorit.ui.treetable.TreeTableModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * TreeTableModel for JTreeTable component
 *
 * @author Anton Safonov
 * @author Vladislav Vislogubov (implemented columns moving support)
 */
public class MetricsModel extends BinTreeTableModel {
  private static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(MetricsModel.class);

  private ArrayList names = new ArrayList();
  private int[] indexes = new int[MetricsAction.METRICS];

  public static final String ABSTRACTNESS = "abstractness";
  public static final String CA = "afferent-coupling";
  public static final String CLOC = "comment-lines-of-code";
  public static final String CYC = "cyclic-dependencies";
  public static final String CC = "cyclomatic-complexity";
  public static final String DC = "density-of-comments";
  public static final String DIP = "dependency-inversion-principle";
  public static final String DIT = "depth-in-tree";
  public static final String DCYC = "direct-cyclic-dependencies";
  public static final String DISTANCE = "distance-from-main-secuence";
  public static final String CE = "efferent-coupling";
  public static final String EP = "encapsulation-principle";
  public static final String EXEC = "executable-statements";
  public static final String INSTABILITY = "instability";
  public static final String LSP = "limited-size-principle";
  public static final String NCLOC = "noncomment-lines-of-code";
  public static final String NOTA = "number-of-abstract-types";
  public static final String NOC = "number-of-children-in-tree";
  public static final String NOTC = "number-of-concrete-types";
  public static final String NOTE = "number-of-exported-types";
  public static final String NP = "number-of-parameters";
  public static final String NOT = "number-of-types";
  public static final String RFC = "response-for-class";
  public static final String LOC = "total-lines-of-code";
  public static final String WMC = "weighted-methods-per-class";
  public static final String MQ = "modularization-quality";
  public static final String NT = "number-of-tramps";
  public static final String LCOM = "lack-of-cohesion";
  public static final String NOF = "number-of-fields";
  public static final String NOA = "number-of-attributes";

  public Map packageDepsMap = null;
  public Map typesDepsMap = null;

  private State state;

  public static class State {
    private Profile profile;
    private HashMap settingsCache = new HashMap();

    private class ItemProfile {
      boolean active;
      double min;
      double max;
    }

    public final void setProfile(final Profile profile) {
      this.profile = profile;
    }

    private final ItemProfile getItemProfile(final String metricName) {
      ItemProfile itemProfile = (ItemProfile) settingsCache.get(metricName);

      if (itemProfile == null) {
        itemProfile = new ItemProfile();
        itemProfile.active
            = profile.isActiveItem(profile.getMetrics(false), metricName);
        itemProfile.min = getMinMax(profile, metricName, "min"); // TODO extract const
        itemProfile.max = getMinMax(profile, metricName, "max"); // TODO extract const
        settingsCache.put(metricName, itemProfile);
      }

      return itemProfile;
    }

    private double getMinMax(Profile profile, String id, String attrKey) {
      String attr = profile.getAttribute(profile.getMetrics(false), id, attrKey);
      if (attr == null || attr.length() == 0) {
        return Double.NaN;
      } else {
        return Double.valueOf(attr).doubleValue();
      }
    }

    public boolean isActive(final String metricName) {
      try {
        return getItemProfile(metricName).active;
      } catch (NullPointerException e) {
        return false;
      }
    }

    public double getMin(final String metricName) {
      return getItemProfile(metricName).min;
    }

    public double getMax(final String metricName) {
      return getItemProfile(metricName).max;
    }

    public boolean isCcRun() {
      return isActive(CC);
    }

    public boolean isLocRun() {
      return isActive(LOC);
    }

    public boolean isNclocRun() {
      return isActive(NCLOC);
    }

    public boolean isClocRun() {
      return isActive(CLOC);
    }

    public boolean isDcRun() {
      return isActive(DC);
    }

    public boolean isNpRun() {
      return isActive(NP);
    }

    public boolean isExecRun() {
      return isActive(EXEC);
    }

    public boolean isWmcRun() {
      return isActive(WMC);
    }

    public boolean isRfcRun() {
      return isActive(RFC);
    }

    public boolean isDitRun() {
      return isActive(DIT);
    }

    public boolean isNocRun() {
      return isActive(NOC);
    }

    public boolean isCaRun() {
      return isActive(CA);
    }

    public boolean isCeRun() {
      return isActive(CE);
    }

    public boolean isInstabilityRun() {
      return isActive(INSTABILITY);
    }

    public boolean isAbstractnessRun() {
      return isActive(ABSTRACTNESS);
    }

    public boolean isDistanceRun() {
      return isActive(DISTANCE);
    }

    public boolean isNotRun() {
      return isActive(NOT);
    }

    public boolean isNotaRun() {
      return isActive(NOTA);
    }

    public boolean isNotcRun() {
      return isActive(NOTC);
    }

    public boolean isNoteRun() {
      return isActive(NOTE);
    }

    public boolean isCycRun() {
      return isActive(CYC);
    }

    public boolean isDcycRun() {
      return isActive(DCYC);
    }

    public boolean isLspRun() {
      return isActive(LSP);
    }

    public boolean isDipRun() {
      return isActive(DIP);
    }

    public boolean isEpRun() {
      return isActive(EP);
    }

    public boolean isMqRun() {
      return isActive(MQ);
    }

    public boolean isNtRun() {
      return isActive(NT);
    }

    public boolean isLcomRun() {
      return isActive(LCOM);
    }

    public boolean isNofRun() {
      return isActive(NOF);
    }

    public boolean isNoaRun() {
     return isActive(NOA);
   }

  }


  public void populate(Project project, Object target) {
long start = System.currentTimeMillis();
    ProgressListener listener = (ProgressListener)
        CFlowContext.get(ProgressListener.class.getName());
    // FIXME: (Jaanek Oja) This is a hack. we remove the listener from
    // CFlowContex because other
    // objects are created below this point of control flow that also notify
    // about progress by quering progress listener from CFlowContext, and we
    // want to elimineate that. Remove this hack if
    // metrics package are refactored so that it uses single ManagingIndexer.
    if (listener != null) {
      CFlowContext.remove(ProgressListener.class.getName());
    }

    addItems(project, target, listener);

    if (getState().isMqRun()) {
      MetricsNode root = (MetricsNode) getRoot();
      List children = root.getChildren();
      List pkgs = new ArrayList(children.size());
      for (int i = 0, max = children.size(); i < max; i++) {
        Object child = children.get(i);
        if (child instanceof BinTreeTableNode
            && ((BinTreeTableNode) child).getBin() instanceof BinPackage) {
          pkgs.add(((BinTreeTableNode) child).getBin());
        }
      }
      root.setMq(ModularizationQualityMetric.calculate(this, pkgs));
    }


    // Add the progress listener back to control flow. It was removed
    // in the beginning of this function (see above).
    if (listener != null) {
      CFlowContext.add(ProgressListener.class.getName(), listener);
    }

    ((MetricsNode) getRoot()).sortAllChildren(
        MetricsNode.MetricsNodeComparator.instance);
//System.err.println("Metrics: " + (System.currentTimeMillis() - start) + " ms");
  }

  /**
   * @return map of all packages with their coresponding dependencies
   * (given that it has been created by user selecting the CYC metric)
   */
  public Map getPackageDepsMap(Project project) {
    if (packageDepsMap == null) {
      packageDepsMap = DependenciesIndexer.createPackageDepsMap(project);
    }

    return packageDepsMap;
  }

  /** Sets packageDepsMap when doing JUnit testing for CYC metric */
  public void setPackageDepsMap(Map testMap) {
    packageDepsMap = testMap;
  }

  /**
   * @return map of all types with their coresponding dependencies
   */
  public Map getTypesDepsMap(Project project) {
    if (typesDepsMap == null) {
      typesDepsMap = DependenciesIndexer.createTypesDepsMap(project);
    }

    return typesDepsMap;
  }

  public void setTypesDepsMap(Map testMap) {
    typesDepsMap = testMap;
  }

  private void addItems(final Project project, final Object target,
      final ProgressListener listener) {
    if (target instanceof Object[]) {
      for (int i = 0; i < ((Object[]) target).length; i++) {
        addItems(project, ((Object[]) target)[i], listener);
      }
    } else {
      if (target instanceof BinMethod) {
        MetricsNode methodNode = createMethodNode((BinMethod) target);
        ((MetricsNode) getRoot()).addChild(methodNode);
      } else if (target instanceof BinClass) {
        addClass((BinClass) target);
      } else if (target instanceof BinInterface) {
        addInterface((BinInterface) target);
      } else if (target instanceof BinPackage) {
        final String packageName = ((BinPackage) target).getQualifiedName();

        List types = project.getDefinedTypes();

        // get the nr of types in project.
        int max = types.size();

        // iterate over the types
        for (int i = 0; i < max; ) {
          final BinTypeRef typeRef = (BinTypeRef) types.get(i);
          if (typeRef.getQualifiedName().startsWith(packageName)) {
            final BinCIType type = typeRef.getBinCIType();
            if (type instanceof BinClass) {
              addClass((BinClass) type);
            } else if (type instanceof BinInterface) {
              addInterface((BinInterface) type);
            }
          }

          notifyMetricsProgress(listener, max, ++i);
        }
      } else if (target instanceof Project) {

        // whole project
        List all_types = project.getDefinedTypes();

        // get the nr of types in project.
        int max = all_types.size();

        for (int iType = 0; iType < max; ) {
          BinCIType type = ((BinTypeRef) all_types.get(iType)).getBinCIType();
          if (type instanceof BinClass) {

            addClass((BinClass) type);
          } else if (type instanceof BinInterface) {

            addInterface((BinInterface) type);
          }

          notifyMetricsProgress(listener, max, ++iType);
        }
      }
    }
  }

  /**
   * Notifies the progress to progress listener.
   *
   * @param max the maximum value.
   * @param currentValue the current value.
   */
  private void notifyMetricsProgress(ProgressListener listener,
      int max, int currentValue) {
    if (listener != null) {
      float percentage = (currentValue / (float) max) * 100;
      listener.progressHappened(percentage);
    }
  }

  public MetricsModel(ArrayList columnNames, int[] actionIndexes) {
    super(new MetricsNode(resLocalizedStrings.getString("tree.root")));
    ((MetricsNode) getRoot()).setModel(this);

    if (columnNames != null) { // in tests
      names = (ArrayList) columnNames.clone();
      System.arraycopy(actionIndexes, 0, indexes, 0, actionIndexes.length);
    }
  }

  public State getState() {
    if (this.state == null) {
      this.state = new State();
    }

    return this.state;
  }

  public void setState(State state) {
    this.state = state;
  }

  private MetricsNode createMethodNode(final BinMethod method) {
    int complexity = -1;

    BinStatement methodBody = null;

    if (!method.isAbstract() && !method.isNative()) {
      methodBody = method.getBody();
      if (methodBody != null) {
        if (getState().isCcRun() || getState().isWmcRun()) {
          complexity = CyclomaticComplexityMetric.calculate(methodBody);
        }
      }
    }

    int executableStatements = -1;
    if (methodBody != null) {
      if (getState().isExecRun()) {
        executableStatements = ExecutableStatementsMetric.calculate(methodBody);
      }
    }

    final int numberOfParameters =
        ((getState().isNpRun()) ? NumberOfParametersMetric.calculate(method)
        : -1);

    final int numberOfTramps =
        ((getState().isNtRun()) ? NumberOfTrampsMetric.calculate(method)
        : -1);

    final int numberOfFields =
        ((getState().isNofRun()) ? NumberOfFieldsMetric.calculate(method)
        : -1);

    final int loc = ((getState().isLocRun()) ? LocMetric.calculate(method) : -1);

    final int ncloc =
        ((getState().isNclocRun()) ? NclocMetric.calculate(method) : -1);

    final int cloc = ((getState().isClocRun()) ? ClocMetric.calculate(method)
        : -1);

    final double dc =
        ((getState().isDcRun()) ? DcMetric.calculate(method) : Double.NaN);

    final MetricsNode metricsNode = new MetricsNode(
        method, complexity, executableStatements,
        numberOfParameters, numberOfTramps, numberOfFields,
        loc, ncloc, cloc, dc);
    metricsNode.setModel(this);
    return metricsNode;
  }

  private void addClass(final BinClass type) {
    MetricsNode typeNode = createTypeNode(type);

    int weightedMethodsPerClass = 0;

    final BinMethod[] methods = type.getDeclaredMethods();

    for (int i = 0; i < methods.length; i++) {
      MetricsNode methodNode = createMethodNode(methods[i]);
      typeNode.addChild(methodNode);

      final int executableStatements = methodNode.getExecutableStatements();
      if (executableStatements >= 0) {
        int execs = typeNode.getExecutableStatements();
        if (execs < 0) {
          execs = 0;
        }
        typeNode.setExecutableStatements(execs + executableStatements);
      }

      if (methodNode.getComplexity() != -1) {
        weightedMethodsPerClass += methodNode.getComplexity();
      }
    }

    final BinConstructor[] constructors = type.getDeclaredConstructors();
    for (int i = 0; i < constructors.length; i++) {
      MetricsNode methodNode = createMethodNode(constructors[i]);
      typeNode.addChild(methodNode);

      final int executableStatements = methodNode.getExecutableStatements();
      if (executableStatements >= 0) {
        int execs = typeNode.getExecutableStatements();
        if (execs < 0) {
          execs = 0;
        }
        typeNode.setExecutableStatements(execs + executableStatements);
      }

      if (methodNode.getComplexity() != -1) {
        weightedMethodsPerClass += methodNode.getComplexity();
      }
    }

    if (getState().isWmcRun()) {
      typeNode.setWeightedMethodsPerClass(weightedMethodsPerClass);
    }

    if (getState().isRfcRun()) {
      typeNode.setRfc(RfcMetric.calculate(type));
    }

    if (getState().isLocRun()) {
      typeNode.setLoc(LocMetric.calculate(type));
    }

    if (getState().isNclocRun()) {
      typeNode.setNcloc(NclocMetric.calculate(type));
    }

    if (getState().isClocRun()) {
      typeNode.setCloc(ClocMetric.calculate(type));
    }

    if (getState().isDcRun()) {
      typeNode.setDc(DcMetric.calculate(type));
    }

    if (getState().isDitRun()) {
      typeNode.setDit(DitMetric.calculate(type));
    }

    if (getState().isNocRun()) {
      typeNode.setNoc(NocMetric.calculate(type));
    }

    if (getState().isNotRun()) {
      typeNode.setNot(TypeCountMetric.calculate(type));
    }

    if (getState().isNotaRun()) {
      typeNode.setNota(AbstractTypeCountMetric.calculate(type));
    }

    if (getState().isNotcRun()) {
      typeNode.setNotc(ConcreteTypeCountMetric.calculate(type));
    }

    if (getState().isNoteRun()) {
      typeNode.setNote(ExportedTypeCountMetric.calculate(type));
    }

    if (getState().isDipRun()) {
      typeNode.setDip(DipMetric.calculate(type));
    }

    if (getState().isLcomRun()) {
      typeNode.setLcom(LackOfCohesionMetric.calculate(type));
    }

    if (getState().isNoaRun()) {
      typeNode.setNoa(NumberOfAttributesMetric.calculate(type));
    }

    addExecutableStatementsToParent(typeNode);
  }

  private void addInterface(final BinInterface type) {
    final State curState = getState();

    MetricsNode typeNode = createTypeNode(type);

    final BinMethod[] methods = type.getDeclaredMethods();

    for (int i = 0; i < methods.length; i++) {
      MetricsNode methodNode = createMethodNode(methods[i]);
      typeNode.addChild(methodNode);
    }

    if (curState.isLocRun()) {
      typeNode.setLoc(LocMetric.calculate(type));
    }

    if (curState.isClocRun()) {
      typeNode.setCloc(ClocMetric.calculate(type));
    }

    if (curState.isNclocRun()) {
      typeNode.setNcloc(NclocMetric.calculate(type));
    }

    if (curState.isDcRun()) {
      typeNode.setDc(DcMetric.calculate(type));
    }

    if (curState.isNotRun()) {
      typeNode.setNot(TypeCountMetric.calculate(type));
    }

    if (curState.isNotaRun()) {
      typeNode.setNota(AbstractTypeCountMetric.calculate(type));
    }

    if (curState.isNotcRun()) {
      typeNode.setNotc(ConcreteTypeCountMetric.calculate(type));
    }

    if (curState.isNoteRun()) {
      typeNode.setNote(ExportedTypeCountMetric.calculate(type));
    }

    if (curState.isDipRun()) {
      typeNode.setDip(DipMetric.calculate(type));
    }

    if (curState.isLcomRun()) {
      typeNode.setLcom(LackOfCohesionMetric.calculate(type));
    }

    if (getState().isNoaRun()) {
      typeNode.setNoa(NumberOfAttributesMetric.calculate(type));
    }

    addExecutableStatementsToParent(typeNode);
  }

  private void addExecutableStatementsToParent(MetricsNode typeNode) {
    final int executableStatements = typeNode.getExecutableStatements();
    ParentTreeTableNode parent = typeNode.getParent();
    if (executableStatements >= 0 && parent instanceof MetricsNode) {
      int execs = ((MetricsNode) parent).getExecutableStatements();
      if (execs < 0) {
        execs = 0;
      }
      ((MetricsNode) parent).setExecutableStatements(execs
          + executableStatements);
    }

    ParentTreeTableNode nextParent = parent.getParent();

    if (executableStatements >= 0 && nextParent instanceof MetricsNode) {
      int execs = ((MetricsNode) nextParent).getExecutableStatements();
      if (execs < 0) {
        execs = 0;
      }
      ((MetricsNode) nextParent).setExecutableStatements(execs
          + executableStatements);
    }

  }

  private MetricsNode createTypeNode(BinCIType type) {
    final ParentTreeTableNode parentNode
        = ((MetricsNode) getRoot()).findParent(type.getPackage(), true);

    MetricsNode typeNode = new MetricsNode(type);
    parentNode.addChild(typeNode);
    typeNode.setModel(this);
    return typeNode;
  }

  /**
   * Returns the number of available column.
   */
  public int getColumnCount() {
    return names.size();
  }

  /**
   * Returns the name for column number <code>column</code>.
   */
  public String getColumnName(int column) {
    return (String) names.get(column);
  }

  /**
   * Returns the value to be displayed for node <code>node</code>,
   * at column number <code>column</code>.
   */
  public Object getValueAt(Object node, int column) {

    int index = indexes[column];
    if (node instanceof MetricsNode) {
      switch (index) {
        case 0:
          return node;

        case 1:
          if (((MetricsNode) node).getComplexity() != -1) {
            return new Integer(((MetricsNode) node).getComplexity());
          } else {
            return null;
          }

        case 2:
          if (((MetricsNode) node).getLoc() != -1) {
            return new Integer(((MetricsNode) node).getLoc());
          } else {
            return null;
          }

        case 3:
          if (((MetricsNode) node).getNcloc() != -1) {
            return new Integer(((MetricsNode) node).getNcloc());
          } else {
            return null;
          }

        case 4:

          if (((MetricsNode) node).getCloc() != -1) {
            return new Integer(((MetricsNode) node).getCloc());
          } else {
            return null;
          }

        case 5:
          final double dc = ((MetricsNode) node).getDc();
          if (!Double.isNaN(dc)) {
            return new Double(dc);
          } else {
            return null;
          }

        case 6:
          if (((MetricsNode) node).getNumberOfParameters() != -1) {
            return new Integer(((MetricsNode) node).getNumberOfParameters());
          } else {
            return null;
          }

        case 7:
          if (((MetricsNode) node).getExecutableStatements() != -1) {
            return new Integer(((MetricsNode) node).getExecutableStatements());
          } else {
            return null;
          }

        case 8:
          if (((MetricsNode) node).getWeightedMethodsPerClass() != -1) {
            return new Integer(
                ((MetricsNode) node).getWeightedMethodsPerClass());
          } else {
            return null;
          }

        case 9:
          if (((MetricsNode) node).getRfc() != -1) {
            return new Integer(
                ((MetricsNode) node).getRfc());
          } else {
            return null;
          }

        case 10:
          if (((MetricsNode) node).getDit() != -1) {
            return new Integer(((MetricsNode) node).getDit());
          } else {
            return null;
          }

        case 11:
          if (((MetricsNode) node).getNoc() != -1) {
            return new Integer(((MetricsNode) node).getNoc());
          } else {
            return null;
          }

        case 12:
          if (((MetricsNode) node).getNot() != -1) {
            return new Integer(((MetricsNode) node).getNot());
          } else {
            return null;
          }

        case 13:
          if (((MetricsNode) node).getNota() != -1) {
            return new Integer(((MetricsNode) node).getNota());
          } else {
            return null;
          }

        case 14:
          if (((MetricsNode) node).getNotc() != -1) {
            return new Integer(((MetricsNode) node).getNotc());
          } else {
            return null;
          }

        case 15:
          if (((MetricsNode) node).getNote() != -1) {
            return new Integer(((MetricsNode) node).getNote());
          } else {
            return null;
          }

        case 16:
          if (((MetricsNode) node).getCa() != -1) {
            return new Integer(((MetricsNode) node).getCa());
          } else {
            return null;
          }

        case 17:
          if (((MetricsNode) node).getCe() != -1) {
            return new Integer(((MetricsNode) node).getCe());
          } else {
            return null;
          }

        case 18:
          final double instability = ((MetricsNode) node).getInstability();
          if (!Double.isNaN(instability)) {
            return new Double(instability);
          } else {
            return null;
          }

        case 19:
          final double abstractness = ((MetricsNode) node).getAbstractness();
          if (!Double.isNaN(abstractness)) {
            return new Double(abstractness);
          } else {
            return null;
          }

        case 20:
          final double distance = ((MetricsNode) node).getDistance();
          if (!Double.isNaN(distance)) {
            return new Double(distance);
          } else {
            return null;
          }

        case 21:
          if (((MetricsNode) node).getCyc() != -1) {
            return new Integer(((MetricsNode) node).getCyc());
          } else {
            return null;
          }

        case 22:
          if (((MetricsNode) node).getDcyc() != -1) {
            return new Integer(((MetricsNode) node).getDcyc());
          } else {
            return null;
          }

        case 23:
          if (((MetricsNode) node).getLsp() != -1) {
            return new Integer(((MetricsNode) node).getLsp());
          } else {
            return null;
          }

        case 24:
          final double dip = ((MetricsNode) node).getDip();
          if (!Double.isNaN(dip)) {
            return new Double(dip);
          } else {
            return null;
          }

        case 25:
          final double ep = ((MetricsNode) node).getEp();
          if (!Double.isNaN(ep)) {
            return new Double(ep);
          } else {
            return null;
          }

        case 26:
          final double mq = ((MetricsNode) node).getMq();
          if (!Double.isNaN(mq)) {
            return new Double(mq);
          } else {
            return null;
          }

        case 27:
          if (((MetricsNode) node).getNumberOfTramps() != -1) {
            return new Integer(((MetricsNode) node).getNumberOfTramps());
          } else {
            return null;
          }

        case 28:
          final double lcom = ((MetricsNode) node).getLcom();
          if (!Double.isNaN(lcom)) {
            return new Double(lcom);
          } else {
            return null;
          }

        case 29:
          if (((MetricsNode) node).getNumberOfFields() != -1) {
            return new Integer(((MetricsNode) node).getNumberOfFields());
          } else {
            return null;
          }

        case 30:
          if (((MetricsNode) node).getNumberOfAttributes() != -1) {
            return new Integer(((MetricsNode) node).getNumberOfAttributes());
          } else {
            return null;
          }


        default:
          return null;
      }

    }

    return null;
  }

  public final String getKey(int column) {
    switch (this.indexes[column]) {
      case 0: // node itself
        return null;

      case 1:
        return CC;

      case 2:
        return LOC;

      case 3:
        return NCLOC;

      case 4:
        return CLOC;

      case 5:
        return DC;

      case 6:
        return NP;

      case 7:
        return EXEC;

      case 8:
        return WMC;

      case 9:
        return RFC;

      case 10:
        return DIT;

      case 11:
        return NOC;

      case 12:
        return NOT;

      case 13:
        return NOTA;

      case 14:
        return NOTC;

      case 15:
        return NOTE;

      case 16:
        return CA;

      case 17:
        return CE;

      case 18:
        return INSTABILITY;

      case 19:
        return ABSTRACTNESS;

      case 20:
        return DISTANCE;

      case 21:
        return CYC;

      case 22:
        return DCYC;

      case 23:
        return LSP;

      case 24:
        return DIP;

      case 25:
        return EP;

      case 26:
        return MQ;

      case 27:
        return NT;

      case 28:
        return LCOM;

      case 29:
        return NOF;

      case 30:
        return NOA;

      default:
        return null;
    }
  }

  public static final int NONE = -1;
  public static final int ALL = 0;
  public static final int METHOD = 1;
  public static final int CLASS = 2;
  public static final int PACKAGE = 3;
  public static final int PROJECT = 4;

  public final int getApplicability(int column) {
    switch (this.indexes[column]) {
      case 0: // node itself
        return NONE;

      case 1: // CC
        return METHOD;

      case 2: // LOC
        return CLASS;

      case 3: // NCLOC
        return CLASS;

      case 4: // CLOC
        return CLASS;

      case 5: // DC
        return ALL;

      case 6: // NP
        return METHOD;

      case 7: // EXEC
        return METHOD;

      case 8: // WMC
        return CLASS;

      case 9: // RFC
        return CLASS;

      case 10: // DIT
        return CLASS;

      case 11: // NOC
        return CLASS;

      case 12: // NOT
        return PACKAGE;

      case 13: // NOTA
        return PACKAGE;

      case 14: // NOTC
        return PACKAGE;

      case 15: // NOTE
        return PACKAGE;

      case 16: // CA
        return PACKAGE;

      case 17: // CE
        return PACKAGE;

      case 18: // INST
        return PACKAGE;

      case 19: // ABSTR
        return PACKAGE;

      case 20: // DIST
        return PACKAGE;

      case 21: // CYC
        return PACKAGE;

      case 22: // DCYC
        return PACKAGE;

      case 23: // LSP
        return PACKAGE;

      case 24: // DIP
        return CLASS;

      case 25: // EP
        return PACKAGE;

      case 26: // MQ
        return PACKAGE;

      case 27: // NT
        return METHOD;

      case 28: // LCOM
        return CLASS;

      case 29: // NOF
        return METHOD;

      case 30: // NOA
        return CLASS;

      default:
        return NONE;
    }
  }

  /**
   * @see net.sf.refactorit.ui.treetable.BinTreeTableModel#isShowing(int)
   */
  public boolean isShowing(int column) {
    int index = indexes[column];
    switch (index) {
      case 1:
        return getState().isCcRun();

      case 2:
        return getState().isLocRun();

      case 3:
        return getState().isNclocRun();

      case 4:
        return getState().isClocRun();

      case 5:
        return getState().isDcRun();

      case 6:
        return getState().isNpRun();

      case 7:
        return getState().isExecRun();

      case 8:
        return getState().isWmcRun();

      case 9:
        return getState().isRfcRun();

      case 10:
        return getState().isDitRun();

      case 11:
        return getState().isNocRun();

      case 12:
        return getState().isNotRun();

      case 13:
        return getState().isNotaRun();

      case 14:
        return getState().isNotcRun();

      case 15:
        return getState().isNoteRun();

      case 16:
        return getState().isCaRun();

      case 17:
        return getState().isCeRun();

      case 18:
        return getState().isInstabilityRun();

      case 19:
        return getState().isAbstractnessRun();

      case 20:
        return getState().isDistanceRun();

      case 21:
        return getState().isCycRun();

      case 22:
        return getState().isDcycRun();

      case 23:
        return getState().isLspRun();

      case 24:
        return getState().isDipRun();

      case 25:
        return getState().isEpRun();

      case 26:
        return getState().isMqRun();

      case 27:
        return getState().isNtRun();

      case 28:
        return getState().isLcomRun();

      case 29:
        return getState().isNofRun();

      case 30:
        return getState().isNoaRun();
    }

    return false;
  }

  public Class getColumnClass(int column) {
    return (column == 0) ? TreeTableModel.class : Integer.class;
  }
}
