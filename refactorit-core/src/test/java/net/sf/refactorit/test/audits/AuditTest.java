/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.rules.j2se5.J2Se5AuditRule;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.loader.JavadocComment;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.query.DelegatingVisitor;
import net.sf.refactorit.refactorings.javadoc.Javadoc;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.loader.ProjectLoadTest;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;


/**
 * @author Villu Ruusmann
 */
public final class AuditTest extends TestCase {
  private int oldJvmMode;

  // TODO reimplement
  private final AuditRule rule;

  public AuditTest(AuditRule rule) {
    super("testAudit");

    this.rule = rule;
  }

  protected void setUp() throws Exception {
    super.setUp();

    if (rule instanceof J2Se5AuditRule) {
      oldJvmMode = Project.getDefaultOptions().getJvmMode();
      Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
    }
  }

  protected void tearDown() throws Exception {
    super.tearDown();

    if (rule instanceof J2Se5AuditRule) {
      Project.getDefaultOptions().setJvmMode(oldJvmMode);
    }
  }


  public final void testAudit() throws Exception {
    try {
      // Create project for testing the current audit
      Project project = createProject(rule.getClass());
      
      // Check if project has no errors
      ProjectLoadTest.processProject(project);
      //project.getProjectLoader().build();
      
      List compilationUnitsToVisit = project.getCompilationUnits();
      DelegatingVisitor supervisor = new DelegatingVisitor(true);
      supervisor.registerDelegate(rule);
      
      rule.setSupervisor(supervisor);

      Iterator it = compilationUnitsToVisit.iterator();
      while (it.hasNext()) {
        CompilationUnit cu = (CompilationUnit) it.next();
        MultiValueMap auditTags = findAuditTags(cu);
        
        rule.clearViolations();
        supervisor.visit(cu);
        rule.postProcess();

        List violations = rule.getViolations();
        
        Iterator iter = violations.iterator();
        while (iter.hasNext()) {
          RuleViolation violation = (RuleViolation) iter.next();
          
          performTargetItemPresenceTest(violation);

          JavadocComment comment = null;
          if (violation.getOwnerMember() != null){
             comment = Comment.findJavadocFor(
                 (violation.getOwnerMember() instanceof BinCIType) ?
                 (BinCIType) violation.getOwnerMember():
                 violation.getOwnerMember()
             );
          } else if (violation instanceof AwkwardSourceConstruct) {
            // Method-level javadoc
            AwkwardSourceConstruct asc = (AwkwardSourceConstruct) violation;
            BinSourceConstruct construct = asc.getSourceConstruct();
            comment = Comment.findJavadocFor(construct.getParentMember());
          } else {
            // Type-level javadoc
            BinTypeRef binCITypeRef = violation.getBinTypeRef();
            if(binCITypeRef.getBinCIType() != null) {
              comment = Comment.findJavadocFor(binCITypeRef.getBinCIType());
            }
          }
          
          boolean success = (comment!=null 
            && auditTags.containsKey(comment));
          
          assertTrue("Couldn't find @audit javadoc tag for " + violation +    
              " (test file: " + cu.getName() +
              ", line " + violation.getLine() + ")", success);
          
          String linkText = violation.getClass().getName();

          if (linkText.indexOf('.') > -1) {
            linkText = linkText.substring(linkText.lastIndexOf('.') + 1);
          }

          success = false;

          // Remove matching tag
          List tags = auditTags.get(comment);
          Iterator ti = tags.iterator();
          while (ti.hasNext()) {
            Javadoc.Tag tag = (Javadoc.Tag) ti.next();

            if (linkText.equals(tag.getLinkName())) {
              ti.remove();
              success = true;
              break;
            }
          }

          assertTrue("Couldn't find '" + linkText + "' in @audit tag" +
              " for " + violation +
              "(test file: " + violation.getCompilationUnit().getName() +
              ", line " + violation.getLine() + ")", success);
        }
        
        // All @audit tags must have been consumed
        if (!auditTags.isEmpty()) {
          for (Iterator ci = auditTags.keySet().iterator(); ci.hasNext();) {
            Comment c = (Comment) ci.next();

            List cTags = auditTags.get(c);
            for (int i = 0; i < cTags.size(); i++) {
              Javadoc.Tag tag = (Javadoc.Tag) cTags.get(i);

              assertTrue("No violation for @audit " + tag.getLinkName() 
                  + " tag (" + c.getStartLine() +":" +c.getStartColumn() + " - " 
                  + c.getEndLine() + ":" + c.getEndColumn()+ ")  '" 
                  + c.getCompilationUnit() + "'.  AuditRule:" + rule.getClass() + ", " 
                  + rule.getKey(), false);
            }
          }  
        }
      }

      rule.finishedRun();
    } catch (Exception e) {
      throw new Error("Rule: " + rule.getClass().getName(), e);
    }

    rule.setTestRun(false);
  }
  
  private final MultiValueMap findAuditTags(CompilationUnit cu) {
    MultiValueMap result = new MultiValueMap();

    List comments = cu.getJavadocComments();
    for (Iterator it = comments.iterator(); it.hasNext();) {
      JavadocComment c = (JavadocComment) it.next();

      Javadoc javadoc = Javadoc.createJavadocInstance(JavadocComment.AUDIT_TAG, c, 0);

      List tags = javadoc.getStandaloneTags();
      for (int i = 0; i < tags.size(); i++) {
        Javadoc.Tag tag = (Javadoc.Tag) tags.get(i);

        if (tag.getName().equals("audit")) {
          result.put(c, tag);
        }
      }
    }

    return result;
  }
  
  
  /*
    // old Arseni`s code
    public final void testAudit() {
    try {
      // Create project for testing the current audit
      Project project = createProject(rule.getClass());
      project.getProjectLoader().build();

      List compilationUnitsToVisit = project.getCompilationUnits();
      DelegatingVisitor supervisor = new DelegatingVisitor(true);
      supervisor.registerDelegate(rule);
      rule.setSupervisor(supervisor);

      Iterator it = compilationUnitsToVisit.iterator();
      while (it.hasNext()) {
        supervisor.visit((CompilationUnit) it.next());
        rule.postProcess();
      }
      rule.finishedRun();
      List violations = rule.getViolations();

      it = violations.iterator();
      while (it.hasNext()) {
        RuleViolation violation = (RuleViolation) it.next();
        
        performTargetItemPresenceTest(violation);

        List javadocTags;

        if (violation.getOwnerMember() != null){
          javadocTags = (violation.getOwnerMember() instanceof BinCIType)
              ? findTypeTags(((BinCIType) violation.getOwnerMember())
                  .getTypeRef())
              : findMemberTags(violation.getOwnerMember());
        } else if (violation instanceof AwkwardSourceConstruct) {
          // Method-level javadoc
          AwkwardSourceConstruct asc = (AwkwardSourceConstruct) violation;
          BinSourceConstruct construct = asc.getSourceConstruct();
          javadocTags = findMemberTags(construct.getParentMember());
        } else {
          // Type-level javadoc
          BinTypeRef binCITypeRef = violation.getBinTypeRef();
          javadocTags = findTypeTags(binCITypeRef);
        }

        String linkText = violation.getClass().getName();

        if (linkText.indexOf('.') > -1) {
          linkText = linkText.substring(linkText.lastIndexOf('.') + 1);
        }

        // Indicates whether the mathing @audit tag was removed or not
        boolean success = false;

        // Remove matching tag
        Iterator tags = javadocTags.iterator();
        while (tags.hasNext()) {
          Javadoc.Tag tag = (Javadoc.Tag) tags.next();

          boolean isAuditTag = tag.getName().equals("audit");

          if (isAuditTag && linkText.equals(tag.getLinkName())) {
            tags.remove();

            success = true;

            // Bail out
            break;
          }
        }
        assertTrue("Couldn't find @audit for " + violation +
            "(test file: " + violation.getCompilationUnit().getName() +
            ", line " + violation.getLine() + ")", success);
      }

      // All @audit tags must have been consumed
      it = getMap().values().iterator();
      while (it.hasNext()) {
        List javadocTags = (List) it.next();

        Iterator tags = javadocTags.iterator();
        while (tags.hasNext()) {
          Javadoc.Tag tag = (Javadoc.Tag) tags.next();

          boolean isAuditTag = tag.getName().equals("audit");

          assertFalse("Undetected @audit " + tag.getLinkName(), isAuditTag);
        }
      }
    } catch (Exception e) {
      e.printStackTrace(System.out);

      fail(rule.getClass().getName() + ": " + e.toString());
    }
    rule.setTestRun(false);
  }

  private List findMemberTags(BinMember member) {
    List result = (List) getMap().get(member);

    // Singleton javadoc
    if (result == null) {
      JavadocComment comment = Comment.findJavadocFor(member);
      if (comment == null) {
        throw new RuntimeException("Did not find comment for: " + member);
      }

      Javadoc javadoc = Javadoc.parseIntoFakeClassmodel(member, comment, 0);

      // mutable list
      result = new ArrayList(javadoc.getStandaloneTags());

      getMap().put(member, result);
    }

    return result;
  }

  private List findTypeTags(BinTypeRef typeRef) {
    List result = (List) getMap().get(typeRef);

    // Singleton javadoc
    if (result == null) {
      JavadocComment comment = Comment.findJavadocFor(typeRef.getBinCIType());
      if (comment == null) {
        fail("failed to find comment for: " + typeRef);
      }

      Javadoc javadoc = Javadoc.parseIntoFakeClassmodel(
          comment, typeRef, typeRef.getBinCIType().getCompilationUnit(), 0);

      // mutable list
      result = new ArrayList(javadoc.getStandaloneTags());

      getMap().put(typeRef, result);
    }

    return result;
  }
  */
  private Project createProject(Class clazz) throws Exception {
    Project result;

    try {
      String suffix = clazz.getName();

      // Extract the last identifier from class' FQN
      if (suffix.indexOf('.') > -1) {
        suffix = suffix.substring(suffix.lastIndexOf('.') + 1);
      }

      result = Utils.createTestRbProject(
          Utils.getTestProjects().getProject("Audit_" + suffix));

      // Load sources
      result.getProjectLoader().build();
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }

    return result;
  }

  /** 
   * If violation has CorrectiveActions, then it should also have target item
   */
  private void performTargetItemPresenceTest(RuleViolation violation) {
    assertNotNull(violation.getCorrectiveActions());

    if (violation.getCorrectiveActions().size() > 0){
      assertNotNull(
          "Violation that has corrective actions should have target item" +
          " reference defined (call setTargetItem(..) method in violation" +
          " constructor)", violation.getTargetItemReference());
    }
  }
}
