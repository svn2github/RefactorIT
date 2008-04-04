/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.LoadingASTUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TypeNameIndexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author tanel
 *
 * Represents static imports of a compilation unit
 */
public final class StaticImports {
	final CompilationUnit compilationUnit;

  private List singleStaticImports;
	private List onDemandStaticImports;


	public StaticImports(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public void addSingleStaticImport(int nodeNumber, String qualifiedName) {
		if (singleStaticImports == null) {
			singleStaticImports = new ArrayList(5);
		}
		singleStaticImports.add(new SingleStaticImport(nodeNumber, qualifiedName));
	}

	public void addOnDemandStaticImport(int nodeNumber, String typeName) {
		if (onDemandStaticImports == null) {
			onDemandStaticImports = new ArrayList(5);
		}
		onDemandStaticImports.add(new OnDemandStaticImport(nodeNumber, typeName));
	}

	public List getTypeUsageInfos() {
		List result = new ArrayList();
		collecTypeUsageInfos(singleStaticImports, result);
		collecTypeUsageInfos(onDemandStaticImports, result);
		return result;
	}

	private static void collecTypeUsageInfos(List staticImports, List result) {
		if (staticImports != null) {
			for (Iterator iter = staticImports.iterator(); iter.hasNext();) {
				StaticImport staticImport = (StaticImport) iter.next();
				BinTypeRef ref = staticImport.getTypeUsageInfo();
				if (ref != null) {
					result.add(ref);
				}
			}
		}
	}

	public BinField getField(String name, BinCIType context) {
		BinField field = getField(singleStaticImports, name, context);
		if (field == null) {
			field = getField(onDemandStaticImports, name, context);
		}
		return field;
	}

	public BinField getSingleStaticImportField(String name, BinCIType context) {
		return getField(singleStaticImports, name, context);
	}


	private static BinField getField(List staticImports, String name, BinCIType context) {
		if (staticImports != null) {
			for (Iterator iter = staticImports.iterator(); iter.hasNext();) {
				StaticImport staticImport = (StaticImport) iter.next();
				BinField field = staticImport.getField(name, context);
				if (field != null) {
					return field;
				}
			}
		}
		return null;

	}

	public List getMethods(BinCIType context) {
		List result = new ArrayList();
		collectMethods(result, singleStaticImports, context);
		collectMethods(result, onDemandStaticImports, context);
		return result;
	}

	public List getSingleStaticImportMethods(BinCIType context) {
		List result = new ArrayList();
		collectMethods(result, singleStaticImports, context);
		return result;
	}

	private static void collectMethods(List methods, List staticImports, BinCIType context) {
		if (staticImports != null) {
			for (Iterator iter = staticImports.iterator(); iter.hasNext();) {
				StaticImport staticImport = (StaticImport) iter.next();
				methods.addAll(staticImport.getMethods(context));
			}
		}
	}

	public SingleStaticImport getSingleStaticImport(String memberName) {
		if (singleStaticImports != null) {
			for (Iterator iter = singleStaticImports.iterator(); iter.hasNext();) {
				SingleStaticImport single = (SingleStaticImport) iter.next();
				if (single.getMemberName().equals(memberName)) {
					return single;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a static import that will import a static member with the
	 * given name, if such member exists. Returns <code>null</code> if
	 * the no such import exists.
	 *
	 * @param fqn
	 * @return
	 */
	public StaticImport getImport(String fqn) {
		String normalizedFqn = fqn.replace('$', '.');
		if (normalizedFqn.indexOf('.') == -1) {
			return null;
		}
		if (singleStaticImports != null) {
			for (Iterator iter = singleStaticImports.iterator(); iter.hasNext();) {
				SingleStaticImport single = (SingleStaticImport) iter.next();
				if (single.getQualifiedName().equals(normalizedFqn)) {
					return single;
				}
			}
		}
		if (onDemandStaticImports != null) {
			for (Iterator iter = onDemandStaticImports.iterator(); iter.hasNext();) {
				OnDemandStaticImport onDemand = (OnDemandStaticImport) iter.next();
				if (onDemand.getTypeName().equals(LoadingASTUtil.extractUntilLastDot(normalizedFqn))) {
					return onDemand;
				}
			}
		}
		return null;
	}

	public List getSingleImports() {
		return singleStaticImports;
	}


	public List getOnDemandImports() {
		return onDemandStaticImports;
	}

	/**
	 *
	 */
	public void cleanUp() {
		singleStaticImports = null;
		onDemandStaticImports = null;

	}

	/**
	 * @param name
	 * @return
	 */
	public BinTypeRef getOnDemandType(String name) {
  	if (onDemandStaticImports != null) {
  		for (int i = 0; i < onDemandStaticImports.size(); i++) {
  			BinTypeRef ref = ((OnDemandStaticImport) onDemandStaticImports.get(i)).getType(name);
  			if (ref != null) {
  				return ref;
  			}
  		}
  	}
  	return null;
	}

	public BinTypeRef getSingleImportType(String shortName) {
		if (singleStaticImports != null) {
			SingleStaticImport single = getSingleStaticImport(shortName);
			if (single != null) {
				String typeName = single.getQualifiedName();
				return compilationUnit.getProject().getTypeRefForSourceName(typeName);
			}
		}
		return null;
	}

	/**
	 * @param method
	 * @param owner
	 * @return
	 */
	public SingleStaticImport getSingleStaticImportFor(BinMember member) {
		if (singleStaticImports != null) {
			for (Iterator iter = singleStaticImports.iterator(); iter.hasNext();) {
				SingleStaticImport singleStaticImport = (SingleStaticImport) iter.next();
				if (singleStaticImport.getMemberName().equals(member.getName())) {
					BinTypeRef ref = singleStaticImport.getTypeRef();
					if (member.getOwner() == ref) {
						return singleStaticImport;
					}
				}
			}
		}
		return null;
	}


	public abstract class StaticImport {
		final int nodeNumber;

		public  StaticImport(int nodeNumber) {
			this.nodeNumber = nodeNumber;
		}

		public final CompilationUnit getCompilationUnit() {
			return compilationUnit;
		}

		public abstract BinTypeRef getTypeUsageInfo();

		public abstract BinField getField(String name, BinCIType context);

		public abstract List getMethods(BinCIType context);
	}

	public final class SingleStaticImport extends StaticImport {
		private final String qualifiedName;

		public SingleStaticImport(int nodeNumber, String qualifiedName) {
			super(nodeNumber);
			this.qualifiedName = qualifiedName;
		}

		/* (non-Javadoc)
		 * @see net.sf.refactorit.source.StaticImports.StaticImport#getTypeUsageInfo()
		 */
		public BinTypeRef getTypeUsageInfo() {
      final ASTImpl node = (ASTImpl) compilationUnit.getSource().getASTByIndex(nodeNumber).getFirstChild().getFirstChild();
      final String typeName = LoadingASTUtil.extractUntilLastDot(qualifiedName);
      final BinTypeRef ref = compilationUnit.resolve(typeName, node);
      if (ref != null) {
        return BinSpecificTypeRef.create(compilationUnit, node, ref, true);
      }
			return null;
		}

		public BinField getField(String name, BinCIType context) {
			String fieldName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
			if (fieldName.equals(name)) {
  			String typeName = LoadingASTUtil.extractUntilLastDot(qualifiedName);
  			BinTypeRef ref = compilationUnit.getProject().getTypeRefForName(typeName);
  			if (ref != null) {
  				BinField field = ref.getBinCIType().getAccessibleField(fieldName, context);
	  			if ((field != null) && field.isStatic()) {
	  				return field;
	  			}
  			}
			}
			return null;
		}

		public List getMethods(BinCIType context) {
			List result = new ArrayList(2);
			String typeName = LoadingASTUtil.extractUntilLastDot(qualifiedName);
			BinTypeRef ref = compilationUnit.getProject().getTypeRefForName(typeName);
			if (ref != null) {
				BinMethod[] methods = ref.getBinCIType().getAccessibleMethods(getMemberName(), context);
				for (int j = 0; j < methods.length; j++) {
					if (methods[j].isStatic()) {
						result.add(methods[j]);
					}
				}
			}
			return result;
		}

		public String getMemberName() {
			return qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
		}

		public BinTypeRef getTypeRef() {
			String typeName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
			return  compilationUnit.getProject().getTypeRefForName(typeName);
		}

		public ASTImpl getMemberNameNode() {
			return (ASTImpl) compilationUnit.getSource().getASTByIndex(nodeNumber).getFirstChild().getFirstChild().getNextSibling();
		}

		/**
		 * @return
		 */
		public String getQualifiedName() {
			return qualifiedName;
		}

		public String getTypeName() {
			return LoadingASTUtil.extractUntilLastDot(qualifiedName);
		}

		/**
		 * Gets usages of single static import, i.e. field/method/type invocations
		 * that depend on this single static import.
		 * Usages (source constructs) are sorted by member in a 
		 * MultiValueMap
		 *
		 * @return
		 */
		public MultiValueMap getUsages() {
			final MultiValueMap result = new MultiValueMap();

			BinItemVisitor visitor = new AbstractIndexer() {

	  		public void visit(BinFieldInvocationExpression expression) {
	  			super.visit(expression);
	        if (expression.invokedViaStaticImport()) {
	        	BinField fieldViaThis = getField(expression.getField().getName(), expression.getOwner().getBinCIType());
	        	if (expression.getField() == fieldViaThis) {
	        		result.putAll(expression.getField(), expression);
	        	}
	        }
	  		}

	  		public void visit(BinMethodInvocationExpression expression) {
	  			super.visit(expression);
	        if (expression.invokedViaStaticImport()) {
	        	List methodsViaThis = getMethods(expression.getOwner().getBinCIType());
	        	if (methodsViaThis.contains(expression.getMethod())) {
	        		result.putAll(expression.getMethod(), expression);
	        	}
	        }
	  		}

	  	};
	  	visitor.visit(compilationUnit);
	  	// now find type usages, if such inner type exists
	  	BinTypeRef ref = compilationUnit.getProject().getTypeRefForSourceName(getQualifiedName());
	  	if (ref != null && ref.getBinCIType().isStatic()) {
	  		ManagingIndexer manager = new ManagingIndexer();
	  		TypeNameIndexer typeNameIndexer = new TypeNameIndexer(manager, ref.getBinCIType(), false);
	  		manager.visit(compilationUnit);
	  		for (Iterator iter = manager.getInvocations().iterator(); iter.hasNext();) {
				InvocationData invocation = (InvocationData) iter.next();
				if (isDirectUsage(invocation.getWhereAst()))
					result.putAll(ref, invocation.getInConstruct());
	  		}
	  	}
	  	
	  	
			return result;
		}
		
		private boolean isDirectUsage(ASTImpl ast) {
			if (ast.getParent().getType() == JavaTokenTypes.DOT) {
				return ast.getParent().getFirstChild().equals(ast);
			}
			return true;
		}

		public String toString() {
			return "import static " + qualifiedName + " in " + compilationUnit;
		}
	}

	public final class OnDemandStaticImport extends StaticImport {
		private final String typeName;

		public OnDemandStaticImport(int nodeNumber, String typeName) {
			super(nodeNumber);
			this.typeName = typeName;
		}

		public BinTypeRef getTypeUsageInfo() {

      final ASTImpl node = compilationUnit.getSource().getASTByIndex(nodeNumber);

      final BinTypeRef ref = compilationUnit.resolve(typeName, node);
      if (ref != null) {
        return BinSpecificTypeRef.create(compilationUnit, node, ref, true);
      }

			return null;
		}

		public BinField getField(String name, BinCIType context) {
			BinTypeRef ref = compilationUnit.getProject().getTypeRefForName(typeName);
			if (ref != null) {
  			BinField field = ref.getBinCIType().getAccessibleField(name, context);
  			if ((field != null) && (field.isStatic())) {
  				return field;
  			}
			}
			return null;
		}

		public List getMethods(BinCIType context) {
			List result = new ArrayList(10);
			BinTypeRef ref = compilationUnit.getProject().getTypeRefForName(typeName);
			if (ref != null) {
				BinMethod[] methods = ref.getBinCIType().getAccessibleMethods(context);
				for (int j = 0; j < methods.length; j++) {
					if (methods[j].isStatic()) {
						result.add(methods[j]);
					}
				}
			}
			return result;
		}

		/**
		 * @return type whose static members are imported
		 */
		public String getTypeName() {
			return typeName;
		}

		/**
		 * @param newName
		 * @return
		 */
		public BinTypeRef getType(String name) {
			String qualifiedName = getTypeName();
			BinTypeRef tmpType = compilationUnit.getProject().getTypeRefForName(qualifiedName);
			if (tmpType != null) {
  			BinTypeRef tmpResult = tmpType.getBinCIType().getDeclaredType(name);
        if ((tmpResult != null) && (tmpResult.getBinType().isStatic())) {
        	return tmpResult;
        }
			}
			return null;
		}

		/**
		 * @return
		 */
		public ASTImpl getNode() {
			return compilationUnit.getSource().getASTByIndex(nodeNumber);
		}

		public String toString() {
			return "import " + typeName + ".* in " + compilationUnit;
		}

	}
}
