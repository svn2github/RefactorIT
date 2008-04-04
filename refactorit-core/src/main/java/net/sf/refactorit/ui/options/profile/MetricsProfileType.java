/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.metrics.MetricsModel;
import net.sf.refactorit.ui.checktree.CheckTreeNode;

import org.w3c.dom.Element;


public class MetricsProfileType implements ProfileType {
  IProfilePanel profileProvider;

  public String getName() {
    return "Metrics";
  }

  public String getParametersPrefix() {
    return "metrics.";
  }

  public void setProfilePanel(IProfilePanel p) {
    this.profileProvider = p;
  }

  public void createNodes(CheckTreeNode root) {
    CheckTreeNode simple = new CheckTreeNode("Simple", true);
    root.add(simple);

    CheckTreeNode oo = new CheckTreeNode("Object Oriented", true);
    root.add(oo);

    CheckTreeNode qm = new CheckTreeNode("Quality Metrics", true);
    root.add(qm);

    addNode(oo, "Abstractness (A)",
        "Abstractness (A):<br><br>This metric counts the ratio of abstract classes and interfaces for a package.<br><br>The preferred threshold values apply for a package.",
        "abstractness", new String[] {"min", "max"});
    addNode(oo, "Afferent Coupling (Ca) (slow)",
        "Afferent Coupling (Ca): Also known as Incoming Dependencies:<br><br>This metric counts the number of classes from other packages that depend on classes in the analysed package.<br><br>The preferred threshold values apply for a package.",
        "afferent-coupling", new String[] {"min", "max"});
    addNode(simple, "Comment Lines of Code (CLOC)",
        "Comment Lines of Code (CLOC):<br><br>CLOC counts all lines that contain regular comments and Javadoc comments.<br><br>Your preferred threshold values apply for a class.",
        "comment-lines-of-code", new String[] {"min", "max"});
    addNode(qm, "Cyclic Dependencies (CYC)",
        "Cyclic Dependencies (CYC):<br><br>This estimates how many cycles in which a package is involved by determining the number of times a package is repeated in the dependency graph.<br><br>The preferred threshold values apply for a package.",
        "cyclic-dependencies", new String[] {"min", "max"});
    addNode(simple, "Cyclomatic Complexity (V(G))",
        "Cyclomatic Complexity (V(G) also known as CC): McCabe's Cyclomatic Complexity:<br><br>V(G) counts the number of code conditions giving an indication of how complex the program is.<br><br>The preferred threshold values apply for a method.",
        "cyclomatic-complexity", new String[] {"min", "max"});
    addNode(simple, "Density of Comments (DC = CLOC / LOC)",
        "Density of Comments (DC = CLOC / LOC):<br><br>This determines a density value for how commented the code is.",
        "density-of-comments", new String[] {"min", "max"});
    addNode(oo, "Depth in Tree (DIT)",
        "Depth in Tree (DIT):<br><br>This is the distance from the class to the root of the inheritance tree (0 for java.lang.Object).<br><br>The preferred threshold values apply for a class.",
        "depth-in-tree", new String[] {"min", "max"});
    addNode(qm, "Dependency Inversion Principle (DIP)",
        "Dependency Inversion Principle (DIP):<br><br>This metric calculates the ratio of dependencies that have abstract classes or interfaces as a target to the total amount of dependencies for a class.<br><br>The preferred threshold values apply for a class.",
        "dependency-inversion-principle", new String[] {"min", "max"});
    addNode(qm, "Direct Cyclic Dependencies (DCYC) (very slow)",
        "Direct Cyclic Dependencies (DCYC):<br><br>Direct cyclic dependencies for a package counts every mutual dependency: The number of other packages the package depends on and which in turn also depend on the package.<br><br>The preferred threshold values apply for a package.",
        "direct-cyclic-dependencies", new String[] {"min", "max"});
    addNode(qm, "Distance from the Main Sequence (D) (very slow)",
        "Distance from the Main Sequence (D):<br><br>The perpendicular distance of a package from the main sequence from the idealized line A + I = 1. Ideal is D = 0.<br><br>The preferred threshold values apply for a package.",
        "distance-from-main-secuence", new String[] {"min", "max"});
    addNode(oo, "Efferent Coupling (Ce)",
        "Efferent Coupling (Ce): Also known as Outgoing Dependencies:<br><br>This metric is a measure for the number of types of the analysed package which depend upon types from other packages.<br><br>The preferred threshold values apply for a package.",
        "efferent-coupling", new String[] {"min", "max"});
    addNode(qm, "Encapsulation Principle (EP) (very slow)",
        "Encapsulation Principle (EP):<br><br>This metric calculates the ratio of classes that are used outside of a package to the total amount of classes that the package contains.<br><br>The preferred threshold values apply for a package.",
        "encapsulation-principle", new String[] {"min", "max"});
    addNode(simple, "Executable Statements (EXEC)",
        "Executable Statements (EXEC):<br><br>This metric counts the number of executable statements.<br><br>The preferred threshold values apply for a method/constructor.",
        "executable-statements", new String[] {"min", "max"});
    addNode(oo, "Instability (I = Ce / (Ca + Ce)) (slow)",
        "Instability (I = Ce / (Ca + Ce)): Robert C. Martin's Instability:<br><br>Check to see how stable or instable your packages are designed.<br><br>The preferred threshold values apply for a package.",
        "instability", new String[] {"min", "max"});
    addNode(qm, "Lack of Cohesion of Methods (LCOM)",
        "Lack of Cohesion of Methods (LCOM):<br><br>This is a measure for the cohesiveness of a class (calculated using the Henderson-Sellers method).<br><br>The preferred threshold values apply for a class.",
        "lack-of-cohesion", new String[] {"min", "max"});
    addNode(qm, "Limited Size Principle  (LSP)",
        "Limited Size Principle (LSP):<br><br>The number of direct subpackages of a package.<br><br>The preferred threshold values apply for a package.",
        "limited-size-principle", new String[] {"min", "max"});
    addNode(qm, "Modularization Quality (MQ)",
        "Modularization Quality (MQ):<br><br>The MQ of an Module Dependency Graph (MDG) that is\n" +
        "partitioned into k packages is the difference between the average\n" +
        "inter- and intra-connectivity of the k packages.<br><br>\n" +
        "MQ is calculatable for the project node only, for the package nodes the\n" +
        "intra-connectivity value is shown.<br>\n" +
        "Original MQ is scaled thousand times in the results table, so the range is -1000..1000.<br>\n" +
        "MQ can not be used to compare different projects, but to show the development trend\n" +
        "of the given project.",
        "modularization-quality", new String[] {"min", "max"});
    addNode(simple, "Non-Comment Lines of Code (NCLOC)",
        "Non-Comment Lines of Code (NCLOC, also known as NCSL and ELOC): Non-Comment Lines of Code / Non-Comment Source Lines / Effective Lines of Code:<br><br>This counts all the lines that do not contain comments or blank lines.<br><br>Your preferred threshold values apply for a class.",
        "noncomment-lines-of-code", new String[] {"min", "max"});
    addNode(oo, "Number of Abstract Types (NOTa)",
        "Number of Abstract Types (NOTa):<br><br>This metric counts the number of abstract classes and interfaces.<br><br>The preferred threshold values apply for a package.",
        "number-of-abstract-types", new String[] {"min", "max"});
    addNode(oo, "Number of Children in Tree (NOC)",
        "Number of Children in Tree (NOC):<br><br>This metric measures the number of direct subclasses of a class.<br><br>The preferred threshold values apply for a class.",
        "number-of-children-in-tree", new String[] {"min", "max"});
    addNode(oo, "Number of Concrete Types (NOTc)",
        "Number of Concrete Types (NOTc):<br><br>This metric counts the number of concrete classes.<br><br>The preferred threshold values apply for a package.",
        "number-of-concrete-types", new String[] {"min", "max"});
    addNode(oo, "Number of Exported Types (NOTe)",
        "Number of Exported Types (NOTe):<br><br>This metric counts the number of classes and interfaces exported outside a package.<br><br>The preferred threshold values apply for a package.",
        "number-of-exported-types", new String[] {"min", "max"});
    addNode(oo, "Number of Fields (NOF)",
        "Number of Fields (NOF):<br><br>This metric counts the number of fields in a method (in local and anonymous classes).<br><br>The preferred threshold values apply for a method.",
        "number-of-fields", new String[] {"min", "max"});
    addNode(simple, "Number of Parameters (NP)",
        "Number of Parameters (NP):<br><br>This metric counts the number of parameters for a method or a constructor.<br><br>The preferred threshold values apply for a method.",
        "number-of-parameters", new String[] {"min", "max"});
    addNode(qm, "Number of Tramps (NT)",
        "Number of Tramps (NT):<br><br>This metric counts the number of parameters in a class' methods, which are not used by its code.<br><br>The preferred threshold values apply for a method.",
        "number-of-tramps", new String[] {"min", "max"});
    addNode(oo, "Number of Types (NOT)",
        "Number of Types (NOT):<br><br>This metric counts the number of classes and interfaces.<br><br>The preferred threshold values apply for a package.",
        "number-of-types", new String[] {"min", "max"});
    addNode(oo, "Response for Class (RFC)",
        "Response for Class (RFC):<br><br>This metric counts the number of distinct methods and constructors invoked by a class.<br><br>The preferred threshold values apply for a class.",
        "response-for-class", new String[] {"min", "max"});
    addNode(simple, "Total Lines of Code (LOC)",
        "Total Lines of Code (LOC, also known as SLOC and ELOC): Total Lines of Code / Source Lines of Code / Effective Lines of Code:<br><br>The number of lines for a class including blank lines and comments.<br><br>The preferred threshold values apply for a class.",
        "total-lines-of-code", new String[] {"min", "max"});
    addNode(oo, "Weighted Methods per Class (WMC)",
        "Weighted Methods per Class (WMC):<br><br>This calculates the sum of cyclomatic complexity of methods for a class.<br><br>The preferred threshold values apply for a class.",
        "weighted-methods-per-class", new String[] {"min", "max"});
    addNode(oo, "Number of Attributes (NOA)",
        "Number of Attributes (NOA):<br><br>This metric counts the number of fields in type (class or interface).<br><br>The preferred threshold values apply for a class.",
        "number-of-attributes", new String[] {"min", "max"});
  }

  private MetricsUserObject addNode(CheckTreeNode parent, String message,
      String description, String key) {
    final MetricsUserObject metric
        = new MetricsUserObject(message, description, key);
    parent.add(new ProfilePanel.TreeNode(metric) {
      protected Element getItem(String key) {
        return profileProvider.getProfile().getMetricsItem(key);
      }
    });
    return metric;
  }

  private MetricsUserObject addNode(CheckTreeNode parent, String message,
      String description, String key, String[] options) {
    MetricsUserObject metric = addNode(parent, message, description, key);

    MetricsOptionsPanel optionsPanel = new MetricsOptionsPanel(
        key, options, this, ResourceUtil.getBundle(MetricsModel.class));
    optionsPanel.setProfile(profileProvider.getProfile());

    profileProvider.addOptionsPanel(optionsPanel, key);

    return metric;
  }

  public Profile createDefaultProfile() {
    return Profile.createDefaultMetrics();
  }

  public void refreshBranch(String key){
  }

  public void refreshBranches(){
  }

  public void refreshAudit(String key){
  }

}
