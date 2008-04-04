/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.loader.JavadocComment;
import net.sf.refactorit.query.DelegatingVisitor;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.reports.ReportGeneratorFactory;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.ParsingMessageDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.audit.AuditTreeTable;
import net.sf.refactorit.ui.audit.AuditTreeTableModel;
import net.sf.refactorit.ui.errors.ErrorsTab;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.module.audit.AuditAction;
import net.sf.refactorit.ui.options.profile.Profile;
import net.sf.refactorit.ui.options.profile.ProfileDialog;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.utils.ParsingInterruptedException;

import javax.swing.JLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



/**
 *
 *
 * @author Sander Magi
 * @author Villu Ruusmann
 * @author Igor Malinin
 */
public class AuditRunner implements AuditReconciler {
  // print auditrule analyzing time to console?
  private boolean debugPerformance = false;
  
  final TreeRefactorItContext context;

  private ResultArea results;

//  private AuditTreeTable table;
  private final AuditTreeTableModel model;

  private BinItemReference targetObject;

  private long lastAuditRunTime;

  public AuditRunner(TreeRefactorItContext context) {
    this(context, new AuditTreeTableModel());
  }

  public AuditRunner(TreeRefactorItContext context, AuditTreeTableModel model) {
    this.context = context;
    this.model = model;
  }

  /** For CLI (and perhaps for some tests?) */
  public AuditRunner(AuditTreeTableModel model) {
    this(null, model);
  }

  public boolean reconcile(Set sources, TreeRefactorItContext context) {
    return reconcile(sources, context, true);
  }

  boolean reconcile(Set sources, final TreeRefactorItContext context,
      boolean partial){

    final Set modifiedSources = new HashSet(10);
    modifiedSources.addAll(getTargetSources(context));
    modifiedSources.addAll(sources);
    
    filterReallyModified(modifiedSources);
    
    if (modifiedSources.size() == 0){
      return false;
    }

    results.saveTreeExpansionAndScrollState(context);

    if (partial) {
      model.saveBinItemReferences(context);
    } else {
      model.clearModel();
    }

    BinItemReference compilationUnitsReferences = BinItemReference.create(
        modifiedSources);

    // do not display table (temporary)
    JLabel reRunLabel = new JLabel("Refreshing results ...");
    results.setContent(reRunLabel);

    doRebuild(context);

    Collection compilationUnits = (Collection) compilationUnitsReferences
         .restore(context.getProject());

    if (partial) {
      model.restoreFromBinItemReferences(context);
    }

    BinTreeTableNode rootNode = (BinTreeTableNode) model.getRoot();

    rootNode.removeChildrenOf(compilationUnits);

    model.resort();

    rootNode.removeSecondaryText();

    try {
      runAuditsOnCompilationUnits(new HashSet(compilationUnits));
    } catch (SearchingInterruptedException ex) {
      return false;
    }

    model.sort();
    rootNode.reflectLeafNumberToParentName();

    results.setContent(new AuditTreeTable(model, context, this));

    return true;
  }


  Set getTargetSources(RefactorItContext context) {
    Object target = targetObject.restore(context.getProject());
    if (target == null){
      return Collections.EMPTY_SET;
    }
    return getCompilationUnits(target, context);
  }

  private void filterReallyModified(Set compilationUnits) {
    for (Iterator i = compilationUnits.iterator(); i.hasNext(); ){
      CompilationUnit compilationUnit = (CompilationUnit) i.next();
      if (compilationUnit.getSource().lastModified() <= compilationUnit
          .getProject().getLastRebuilded()/*lastAuditRunTime*/) {
        i.remove();
      }
    }
  }

  private static void doRebuild(RefactorItContext context) {
    try {
      ParsingMessageDialog dlg = new ParsingMessageDialog(context);
      dlg.setDialogTask(
          new ParsingMessageDialog.RebuildProjectTask(context.getProject()));
      dlg.show(true);
    } catch (ParsingInterruptedException ex) {
    } catch (Exception e) {
    } finally {
      ErrorsTab.addNew(context);
    }
  }

  public void doAudit(
      final AuditAction action, final Set sources, final Object target
      ) {
    // FIXME this should be somewhere else, but not here if needed...
    // We need the same Model instance after ReRuns because
    // the Model instance keeps a per-column sort state.
//    if (action.getModel() == null) {
//      BinTreeTableNode rootNode = createSimpleModel();
//      action.setModel(model);
//    } else {
//      model = action.getModel();
//      model.clearModel();
//
//      rootNode = (BinTreeTableNode) model.getRoot();
//      rootNode.removeHtmlAndLeafNumbers();
//    }

    BinTreeTableNode rootNode = (BinTreeTableNode) model.getRoot();

    targetObject = BinItemReference.create(target);

    lastAuditRunTime = System.currentTimeMillis();

    try {
      runAuditsOnCompilationUnits(sources);
    } catch (SearchingInterruptedException ex) {
      return;
    }

    model.sort();

    rootNode.reflectLeafNumberToParentName();

    AuditTreeTable table = new AuditTreeTable(model, context, this);
    results = ResultArea.create(table, context, action);
    results.setTargetBinObject(target);

    // results.setTargetBinObject(new FindRerunInfo(object, request));
    BinPanel panel = BinPanel.getPanel(context, "Audits", results);

    // Register default help for panel's current toolbar
    panel.setDefaultHelp("refact.audit");

    final BinItemReference objectReference = BinItemReference
        .create(target);

    panel.setFilterActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object object = objectReference.restore(context.getProject());

        if (object == null) {
          DialogManager.getInstance().showCustomError(
              context, "Info", "Cannot rediscover the target.");
          return;
        }

        Profile selected = (Profile) context.getState();
        selected = ProfileDialog.showAudit(selected);
        if (selected == null) {
          // User dismissed this dialog
          return;
        }

        context.setState(selected);

        reconcile(getTargetSources(context), context, false);
      }
    });
    
    panel.addToolbarButton(ReportGeneratorFactory.getAuditsReportGenerator().getReportButton(new BinTreeTableModel[] {model}, context, target));
  }

  private void runAuditsOnCompilationUnits(final Set compilationUnitsToVisit) throws
      SearchingInterruptedException {
    Profile selected = (Profile) context.getState();

    final AuditRule[] audits = Audit.createActiveRulesAndSetToModel(selected, model);
    if (audits == null) {
      throw new IllegalStateException();
    }

    // Define task
    Runnable task = new Runnable() {
      public void run() {
        if (debugPerformance){
          findViolationsToModelDebug(compilationUnitsToVisit, audits);
        } else {
          findViolationsToModel(compilationUnitsToVisit, audits);
        }
      }
    };

    // Run the predefined task
    JProgressDialog.run(context, task, true);
  }

  private static void attachDensity(CompilationUnit compilationUnit, List itsViolations) {
    int score = 0;

    for (int i = 0; i < itsViolations.size(); i++) {
      RuleViolation violation = (RuleViolation) itsViolations.get(i);
      score += violation.getPriority().getScoreForDensityCalculation();
    }

    int lines = compilationUnit.getLineIndexer().getLineCount();
    float density = (float) score / (float) lines;

    for (Iterator i = itsViolations.iterator(); i.hasNext(); ) {
      ((RuleViolation) i.next()).setDensity(density);
    }
  }

  /**
   * REUSE: This can be reused I'm sure
   *
   * @pre getAllCompilationUnits returned list doesn't contain null element
   */
  public static Set getCompilationUnits(Object target, RefactorItContext context) {
    // a modifiable copy
    Set sources = new HashSet(getAllCompilationUnits(target, context));

    Iterator iter = sources.iterator();
    while (iter.hasNext()) {
      CompilationUnit src = (CompilationUnit) iter.next();
      if (!FileUtil.isJavaFile(src.getName())) {
        iter.remove();
      }
    }

    return sources;
  }

  public static Set getAllCompilationUnits(Object target, RefactorItContext context) {
    if (target instanceof Object[]) {
      Object[] targets = (Object[]) target;

      Set finalResult = new HashSet();

      for (int i = 0; i < targets.length; ++i) {
        finalResult.addAll(getCompilationUnits(targets[i], context));
      }

      return finalResult;
    }

    if (target instanceof CompilationUnit) {
      return Collections.singleton(target);
    }

    if (target instanceof Project) {
      return new HashSet(((Project) target).getCompilationUnits());
    }

    if (target instanceof BinPackage) {
      String prefix = ((BinPackage) target).getQualifiedName();
      HashSet result = new HashSet();

      BinPackage packageList[] = context.getProject().getAllPackages();
      for (int i = 0; i < packageList.length; ++i) {
        BinPackage item = packageList[i];
        if (item.hasTypesWithSources()
            && item.getQualifiedName().startsWith(prefix)) {
          for (Iterator t = item.getAllTypes(); t.hasNext(); ) {
            BinTypeRef ref = (BinTypeRef) t.next();
            CompilationUnit compilationUnit = ref.getBinType().getCompilationUnit();
            if (compilationUnit != null) {
              result.add(compilationUnit);
            }
          }
        }
      }

      return result;
    }

    if (target instanceof BinCIType) {
      CompilationUnit srcFile = ((BinCIType) target).getCompilationUnit();
      if (srcFile != null) {
        return Collections.singleton(srcFile);
      }

      return Collections.EMPTY_SET;
    }

    if (Assert.enabled) {
      Assert.must(false, "Invalid target " + target + " for Audit Action");
    }

    return Collections.EMPTY_SET; 
  }

  private AuditTreeTableModel findViolationsToModelDebug(
    final Set compilationUnitsToVisit, final AuditRule[] audits){

    ProgressListener listener = (ProgressListener)
        CFlowContext.get(ProgressListener.class.getName());

    DelegatingVisitor supervisor = new DelegatingVisitor(true);
    AuditRule firstRule = new AuditRule() {};
    firstRule.setSupervisor(supervisor);
    supervisor.setDelegates(new AuditRule[] { firstRule });
    for (Iterator cu = compilationUnitsToVisit.iterator(); cu.hasNext(); ){
      findViolationsToModel((CompilationUnit) cu.next(), supervisor);
    }

    int max = compilationUnitsToVisit.size() * audits.length;
    for (int a = 0; a < audits.length; a++){
      supervisor = new DelegatingVisitor(true);
      audits[a].setSupervisor(supervisor);
      supervisor.setDelegates(new AuditRule[] { audits[a] });

      Iterator i = compilationUnitsToVisit.iterator();
      for (int pos = 0; i.hasNext(); pos++){
        findViolationsToModel((CompilationUnit) i.next(), supervisor);

        if (listener != null) {
          listener.progressHappened(
              ProgressMonitor.Progress.FULL.getPercentage(pos + a *
                  compilationUnitsToVisit.size(), max));
        }
      }
    }

    System.out.println("Audits performed: ");
    for (int k = 0; k < audits.length; k++){
      System.out.println("" + audits[k].debugInfo());
    }

    return model;
  }

  public AuditTreeTableModel findViolationsToModel(
      final Set compilationUnitsToVisit, final AuditRule[] audits) {

    ProgressListener listener = (ProgressListener)
        CFlowContext.get(ProgressListener.class.getName());

    DelegatingVisitor supervisor = new DelegatingVisitor(true);
    for (int i = 0, max = audits.length; i < max; i++) {
      audits[i].setSupervisor(supervisor);
    }
    supervisor.setDelegates(audits);

    Iterator i = compilationUnitsToVisit.iterator();
    for (int pos = 0, max = compilationUnitsToVisit.size(); i.hasNext(); pos++){
      findViolationsToModel((CompilationUnit) i.next(), supervisor);

      if (listener != null) {
        listener.progressHappened(
            ProgressMonitor.Progress.FULL.getPercentage(pos, max));
      }
    }
    
    for (int k = 0, max = audits.length; k < max; k++) {
      audits[k].finishedRun();
    }
    
    return model;
  }

  private void findViolationsToModel(CompilationUnit compilationUnit,
      final DelegatingVisitor supervisor) {
    ArrayList violationsInFile = new ArrayList();

    AuditRule[] audits = (AuditRule[]) supervisor.getDelegates();
    final int max = audits.length;

    for (int j = 0; j < max; j++) {
      audits[j].clearViolations();
    }

    supervisor.visit(compilationUnit);
    
    
    for (int j = 0; j < max; j++) {
        audits[j].postProcess();
    }

    List auditKeys = new ArrayList();
    for (int j = 0; j < max; j++) {
      ArrayList violations = audits[j].getViolations();
      auditKeys.add(audits[j].getKey());
      insertViolationsToTreeModel(violations);
      violationsInFile.addAll(violations);
    }
    
    List violations = handleUnusedTags(compilationUnit, auditKeys);
    insertViolationsToTreeModel(violations);
    violationsInFile.addAll(violations);
    
    attachDensity(compilationUnit, violationsInFile);
  }
  
  private List handleUnusedTags(CompilationUnit compilationUnit, List auditKeys) {
    MultiValueMap usedSkips = RuleViolation.getUsedTags(); 
    
    List allComments = new ArrayList(compilationUnit.getJavadocComments());
    
    List violations = new ArrayList();
    
    for(int i = 0; i < allComments.size(); i++) {
      JavadocComment comment = (JavadocComment) allComments.get(i);
      if(SkipTagHelper.isSkipTag(comment)) {
        List options = new ArrayList(SkipTagHelper.getSkippedOptions(comment.getText()));
        
        List usedOptions = usedSkips.get(comment);
        if(usedOptions != null) {
          // find unused skips list
          options.removeAll(usedOptions);
        }
      
        // create key list of unused skips for audits, that were executed
        List significant = new ArrayList();
        for(int k=0; k<options.size(); k++) {
          String key = (String)options.get(k);
          if(auditKeys.contains(key)){
            significant.add(key);
          }
        }
        options.clear();
        
        if(!significant.isEmpty()) {
          BinItem owner = comment.getOwner();
          if(owner instanceof BinCIType) {
            violations.add(new UnusedTagViolation(comment, significant, (BinCIType)owner));
          } else if(owner instanceof BinMethod) {
            violations.add(new UnusedTagViolation(comment, significant, (BinMethod)owner));
          }
        }
      }
    }
    
    return violations;
  }
  

  
  
  private void insertViolationsToTreeModel(List ruleViolations) {
    for (int i = 0; i < ruleViolations.size(); i++) {
      model.addViolation((RuleViolation) ruleViolations.get(i));
    }
  }

  public AuditTreeTableModel getAuditModel() {
    return model;
  }
  
  public List revalidateViolations(List violations, RefactoringStatus status){
    RuleViolationValidator validator = new RuleViolationValidator(model);
    status.merge(validator.validate(violations));
    return validator.getValid();
  }
}
