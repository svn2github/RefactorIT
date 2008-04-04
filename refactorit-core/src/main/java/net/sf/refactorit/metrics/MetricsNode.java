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
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import java.util.Comparator;


/**
 * @author Anton Safonov
 */
public class MetricsNode extends BinTreeTableNode {

  /** To get settings for e.g. coloring */
  private MetricsModel model;

  private final int complexity;
  private final int numberOfParameters;
  private int executableStatements;
  private int loc = -1;
  private int ncloc = -1;
  private int cloc = -1;
  private double dc = Double.NaN;
  private int weightedMethodsPerClass = -1;
  private int rfc = -1;
  private int dit = -1;
  private int noc = -1;
  private int ca = -1;
  private int ce = -1;
  private double instability = Double.NaN;
  private double abstractness = Double.NaN;
  private double distance = Double.NaN;
  private int not = -1;
  private int nota = -1;
  private int notc = -1;
  private int note = -1;
  private int cyc = -1;
  private int dcyc = -1;
  private int lsp = -1;
  private double dip = Double.NaN;
  private double ep = Double.NaN;
  private double mq = Double.NaN;
  private int nt = -1;
  private double lcom = Double.NaN;
  private int nof = -1;
  private int noa = -1;

  public MetricsNode(Object bin) {
    this(bin, -1, -1, -1, -1, -1, -1, -1, -1, Double.NaN);
  }

  public MetricsNode(Object bin,
      int complexity,
      int executableStatements,
      int numberOfParameters,
      int nt,
      int nof,
      int loc,
      int ncloc,
      int cloc,
      double dc) {
    super(bin);

    this.complexity = complexity;
    this.executableStatements = executableStatements;
    this.numberOfParameters = numberOfParameters;
    this.nt = nt;
    this.nof = nof;
    this.loc = loc;
    this.ncloc = ncloc;
    this.cloc = cloc;
    this.dc = dc;
  }

  public final void setModel(final MetricsModel model) {
    this.model = model;
  }

  public final MetricsModel getModel() {
    return this.model;
  }

  public String getDisplayName() {
    if (this.name == null && getBin() instanceof BinCIType) {
      this.name = ((BinCIType) getBin()).getNameWithAllOwners();
    }

    return super.getDisplayName();
  }

  public static final class MetricsNodeComparator implements Comparator {
    public static final MetricsNodeComparator instance
        = new MetricsNodeComparator();
    private MetricsNodeComparator() {
    }

    public int compare(Object o1, Object o2) {
      if (!(o1 instanceof BinTreeTableNode) || !(o2 instanceof BinTreeTableNode)) {
        return 0;
      }
      final BinTreeTableNode node1 = (BinTreeTableNode) o1;
      final BinTreeTableNode node2 = (BinTreeTableNode) o2;

      int res = 0;

      if (node1 instanceof MetricsNode && node2 instanceof MetricsNode) {
        if (((MetricsNode) node1).getModel().getState().isCcRun()) {
          // bad methods go first
          res = ((MetricsNode) node2).getComplexity()
              - ((MetricsNode) node1).getComplexity();
        }
      }

      if (res == 0) {
        res = node1.getDisplayName().compareToIgnoreCase(node2.getDisplayName());
      }

      return res;
    }
  }

  public int getComplexity() {
    return this.complexity;
  }

  public int getExecutableStatements() {
    return this.executableStatements;
  }

  public void setExecutableStatements(int executableStatements) {
    this.executableStatements = executableStatements;
  }

  public int getNumberOfParameters() {
    return this.numberOfParameters;
  }

  public int getNumberOfTramps() {
    return this.nt;
  }

  public int getNumberOfFields() {
    return this.nof;
  }

  public int getNumberOfAttributes() {
    return this.noa;
  }

  public int getWeightedMethodsPerClass() {
    return weightedMethodsPerClass;
  }

  public void setWeightedMethodsPerClass(int weightedMethodsPerClass) {
    this.weightedMethodsPerClass = weightedMethodsPerClass;
  }

  public void setDit(int dit) {
    this.dit = dit;
  }

  public int getDit() {
    return dit;
  }

  public void setNoc(int noc) {
    this.noc = noc;
  }

  public int getNoc() {
    return noc;
  }

  public int getLoc() {
    return loc;
  }

  public void setLoc(int loc) {
    this.loc = loc;
  }

  public void setNcloc(int ncloc) {
    this.ncloc = ncloc;
  }

  public int getNcloc() {
    return ncloc;
  }

  public void setCloc(int cloc) {
    this.cloc = cloc;
  }

  public int getCloc() {
    return cloc;
  }

  public void setDc(double dc) {
    this.dc = dc;
  }

  public double getDc() {
    return dc;
  }

  public double getInstability() {
    return instability;
  }

  public void setInstability(double instability) {
    this.instability = instability;
  }

  public double getAbstractness() {
    return abstractness;
  }

  public void setAbstractness(double abstractness) {
    this.abstractness = abstractness;
  }

  public double getDistance() {
    return this.distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public int getCa() {
    return ca;
  }

  public void setCa(int ca) {
    this.ca = ca;
  }

  public int getCe() {
    return ce;
  }

  public void setCe(int ce) {
    this.ce = ce;
  }

  public int getRfc() {
    return rfc;
  }

  public void setRfc(int rfc) {
    this.rfc = rfc;
  }

  public int getNot() {
    return not;
  }

  public void setNot(int not) {
    this.not = not;
  }

  public int getNota() {
    return nota;
  }

  public void setNota(int nota) {
    this.nota = nota;
  }

  public int getNotc() {
    return notc;
  }

  public void setNotc(int notc) {
    this.notc = notc;
  }

  public int getNote() {
    return note;
  }

  public void setNote(int note) {
    this.note = note;
  }

  public void setCyc(int cyc) {
    this.cyc = cyc;
  }

  public int getCyc() {
    return this.cyc;
  }

  public void setDcyc(int dcyc) {
    this.dcyc = dcyc;
  }

  public int getDcyc() {
    return this.dcyc;
  }

  public void setLsp(int lsp) {
    this.lsp = lsp;
  }

  public int getLsp() {
    return this.lsp;
  }

  public void setDip(double dip) {
    this.dip = dip;
  }

  public double getDip() {
    return this.dip;
  }

  public void setEp(double ep) {
    this.ep = ep;
  }

  public double getEp() {
    return this.ep;
  }

  public void setMq(double mq) {
    this.mq = mq;
  }

  public double getMq() {
    return this.mq;
  }

  public void setNt(int nt) {
    this.nt = nt;
  }

  public int getNt() {
    return this.nt;
  }

  public void setLcom(double lcom) {
    this.lcom = lcom;
  }

  public double getLcom() {
    return this.lcom;
  }

  public void setNof(int nof) {
    this.nof = nof;
  }

  public int getNof() {
    return this.nof;
  }

  public void setNoa(int noa) {
    this.noa = noa;
  }

  public int getNoa() {
    return this.noa;
  }

  /**
   * Creates node for the package.
   *
   * @param pkg package.
   * @param parentNode parent node of the node to be created.
   *
   * @return node. Never returns <code>null</code>.
   */
  protected ParentTreeTableNode createPackageNode(
      BinPackage pkg,
      BinTreeTableNode parentNode,
      final boolean flatPackages) {

    final MetricsNode parentMetricsNode = (MetricsNode) parentNode;
    final MetricsModel model = parentMetricsNode.getModel();
    final MetricsModel.State curState = model.getState();
    final MetricsNode node = new MetricsNode(pkg);
    node.setModel(model);

    if (curState.isLocRun()) {
      node.setLoc(LocMetric.calculate(pkg));
      if (parentMetricsNode.getLoc() == -1) {
        parentMetricsNode.setLoc(0);
      }
      parentMetricsNode.setLoc(parentMetricsNode.getLoc() + node.getLoc());
      parentMetricsNode.setDc(DcMetric.calculate(parentMetricsNode.getLoc(),
          parentMetricsNode.getCloc()));
    }

    if (curState.isNclocRun()) {
      node.setNcloc(NclocMetric.calculate(pkg));
      if (parentMetricsNode.getNcloc() == -1) {
        parentMetricsNode.setNcloc(0);
      }
      parentMetricsNode.setNcloc(parentMetricsNode.getNcloc() + node.getNcloc());
    }

    if (curState.isClocRun()) {
      node.setCloc(ClocMetric.calculate(pkg));
      if (parentMetricsNode.getCloc() == -1) {
        parentMetricsNode.setCloc(0);
      }
      parentMetricsNode.setCloc(parentMetricsNode.getCloc() + node.getCloc());
      parentMetricsNode.setDc(DcMetric.calculate(parentMetricsNode.getLoc(),
          parentMetricsNode.getCloc()));
    }

    if (curState.isDcRun()) {
      node.setDc(DcMetric.calculate(pkg));
    }

    if (curState.isNotRun()) {
      node.setNot(TypeCountMetric.calculate(pkg));
      if (node.getNot() != -1) {
        if (parentMetricsNode.getNot() == -1) {
          parentMetricsNode.setNot(0);
        }
      }
      parentMetricsNode.setNot(parentMetricsNode.getNot() + node.getNot());
    }

    if (curState.isNotaRun()) {
      node.setNota(AbstractTypeCountMetric.calculate(pkg));
      if (node.getNota() != -1) {
        if (parentMetricsNode.getNota() == -1) {
          parentMetricsNode.setNota(0);
        }
      }
      parentMetricsNode.setNota(parentMetricsNode.getNota() + node.getNota());
    }

    if (curState.isNotcRun()) {
      node.setNotc(ConcreteTypeCountMetric.calculate(pkg));
      if (node.getNotc() != -1) {
        if (parentMetricsNode.getNotc() == -1) {
          parentMetricsNode.setNotc(0);
        }
      }
      parentMetricsNode.setNotc(parentMetricsNode.getNotc() + node.getNotc());
    }

    if (curState.isNoteRun()) {
      node.setNote(ExportedTypeCountMetric.calculate(pkg));
      if (node.getNote() != -1) {
        if (parentMetricsNode.getNote() == -1) {
          parentMetricsNode.setNote(0);
        }
      }
      parentMetricsNode.setNote(parentMetricsNode.getNote() + node.getNote());
    }

    if (curState.isCaRun()) {
      node.setCa(AfferentCouplingMetric.calculate(pkg));
    }

    if (curState.isCeRun()) {
      node.setCe(EfferentCouplingMetric.calculate(pkg));
    }

    if (curState.isInstabilityRun() || curState.isDistanceRun()) {
      calculateInstability(node, pkg);
    }

    if (curState.isMqRun()) {
      node.setMq(ConnectivityMetric.calculateIntraConnectivity(model, pkg));
    }

    if (curState.isAbstractnessRun() || curState.isDistanceRun()) {
      node.setAbstractness(AbstractnessMetric.calculate(pkg));
    }

    if (curState.isDistanceRun()) {
      node.setDistance(DistanceMetric.calculate(
          node.getInstability(), node.getAbstractness()));
    }

    if (curState.isCycRun()) {
      node.setCyc(CyclicDependencyMetric.calculate(model, pkg));
    }

    if (curState.isDcycRun()) {
      node.setDcyc(DirectCyclicDependencyMetric.calculate(pkg));
    }

    if (curState.isLspRun()) {
      node.setLsp(LspMetric.calculate(pkg));
    }

    if (curState.isEpRun()) {
      node.setEp(EpMetric.calculate(pkg));
    }

    parentNode.addChild(node);

    return node;
  }

  private void calculateInstability(final MetricsNode node, BinPackage pkg) {
    final int efferentCoupling;
    if (node.getCe() == -1) {
      efferentCoupling = EfferentCouplingMetric.calculate(pkg);
    } else {
      efferentCoupling = node.getCe();
    }

    final int afferentCoupling;
    if (node.getCa() == -1) {
      afferentCoupling = AfferentCouplingMetric.calculate(pkg);
    } else {
      afferentCoupling = node.getCa();
    }

    node.setInstability(
        InstabilityMetric.calculate(efferentCoupling, afferentCoupling));
  }
}
