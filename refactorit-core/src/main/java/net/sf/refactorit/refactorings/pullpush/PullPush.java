/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.pullpush;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinEnumConstant;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.refactorings.movemember.MoveMemberConflictsResolver;
import net.sf.refactorit.refactorings.movemember.ReferenceUpdater;
import net.sf.refactorit.source.edit.MoveEditor;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 *
 * @author vadim
 */
public class PullPush extends AbstractRefactoring {
  public static String key = "refactoring.pullpush";

  private ConflictResolver conflictResolver;
  private ReferenceUpdater referenceUpdater = new ReferenceUpdater();

  public PullPush(
      RefactorItContext context, BinCIType nativeType, List targetAsList
  ) {
    super("Pull Up / Push Down", context);

    conflictResolver = new MoveMemberConflictsResolver(
        targetAsList, nativeType, null, this.referenceUpdater);
  }

  public ConflictResolver getResolver() {
    return this.conflictResolver;
  }

  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = new RefactoringStatus();
    BinCIType nativeType = conflictResolver.getNativeType();
    List members = conflictResolver.getBinMembersToMove();

    for (int i = 0, max = members.size(); i < max; i++) {
      BinMember member = (BinMember) members.get(i);
      BinTypeRef owner = member.getOwner();

      if (owner.getBinCIType() != nativeType) {
        status.addEntry("Selected members are not from the single class",
            RefactoringStatus.ERROR);
        return status;
      }
    }

//    if (nativeType.isInterface()) {
//      status.addEntry(
//          nativeType.getQualifiedName() + " is interface.\n" +
//          "Current version doesn't support \n" +
//          "Pull Up / Push Down for interfaces.",
//          RefactoringStatus.ERROR);
//    }

    if (!nativeType.isFromCompilationUnit()) {
      status.addEntry(
          StringUtil.capitalizeFirstLetter(nativeType.getMemberType()) + " " +
          nativeType.getQualifiedName() +
          "\nis outside of the source path and its members cannot be pulled/pushed!",
          RefactoringStatus.ERROR);
    }

    if (nativeType.getDeclaredFields().length == 0 &&
        nativeType.getDeclaredMethods().length == 0) {
      status.addEntry(StringUtil.capitalizeFirstLetter(nativeType.getMemberType())
          + " " +
          nativeType.getQualifiedName() + "\ncontains neither fields " +
          "nor methods.\nNothing to pull up / push down.",
          RefactoringStatus.ERROR);
    }

    if (nativeType.getTypeRef().getDirectSubclasses().size() == 0 &&
        !nativeType.getTypeRef().getSuperclass().getBinCIType().
        isFromCompilationUnit() &&
        nativeType.getTypeRef().getInterfaces().length == 0) {
      status.addEntry(StringUtil.capitalizeFirstLetter(nativeType.getMemberType())
          + " " +
          nativeType.getQualifiedName() + "\ndoes not have subclasses," +
          "\nits superclass is outside of the source path and " +
          "\nit does not implement interfaces.",
          RefactoringStatus.ERROR);
    }

    for (int i = 0, max = members.size(); i < max; i++) {
      if (members.get(i) instanceof BinEnumConstant) {

        status.addEntry("Enum constants cannot be  pulled up / pushed down",
            RefactoringStatus.ERROR);
        return status;
      }
    }
    
    return status;
  }

  public RefactoringStatus checkUserInput() {
    return new RefactoringStatus();
  }

  /**
   * @see net.sf.refactorit.refactorings.Refactoring#performChange
   */
  public TransformationList performChange() {
    TransformationList transList = new TransformationList();

    List toMove = conflictResolver.addConflictEditors(transList);
    referenceUpdater.createEditors(transList);
    if (transList.getStatus().isErrorOrFatal()) {
      conflictResolver.clearConflictRepository();
//      ConflictFactory.clear();
      return transList;
    }

    List comments = new ArrayList();

    for (int i = 0; i < toMove.size(); i++) {
      final LocationAware member = (LocationAware) toMove.get(i);
      comments.addAll(Comment.findAllFor(member));
    }
    toMove.addAll(comments);

    Collections.sort(toMove, LocationAware.PositionSorter.getInstance());

    transList.add(new MoveEditor(toMove, conflictResolver.getTargetType()));

    conflictResolver.clearConflictRepository();
//    ConflictFactory.clear();
    return transList;
  }

//	public PullPush(final RefactorItContext context,
//                  final Object bin) {
//		super("Pull Up / Push Down", context, parent);
//
//		this.selectedMember = (BinMember)bin;
//	}
//
//	private static class AccessData {
//		private BinMember member;
//		private int newAccess;
//		private int oldAccess;
//		private int accessLine;
//		private int accessColumn;
//
//		AccessData(BinMember member, int newAccess, int accessLine, int accessColumn) {
//			this.member = member;
//			this.oldAccess = member.getAccessModifier();
//			this.newAccess = newAccess;
//			this.accessLine = accessLine;
//			this.accessColumn = accessColumn;
//		}
//	}
//
//	private static class ExtractedSource {
//		private String comments;
//		private String memberDecalration;
//
//		ExtractedSource(String comments, String memberDecalration) {
//			this.comments = comments;
//			this.memberDecalration = memberDecalration;
//		}
//
//		public String getCombinedSource() {
//			return (comments + memberDecalration);
//		}
//	}

//	public RefactoringStatus performChange() {
//    RefactoringStatus status = new RefactoringStatus();
//		SourceEditor editor = new SourceEditor();
//
//		for (int i = 0; i < pullPushData.length; i++) {
//			BinMember member = pullPushData[i].getMember();
//      BinCIType targetType = pullPushData[i].getTypeToMoveIn();
//
//			SourceCoordinate position;
//      if (member instanceof BinMethod) {
//        position = findNewMethodPosition(targetType);
////        position = targetType.findNewMethodPosition();
//      } else {
//        position = findNewFieldPosition(targetType);
////        position = targetType.findNewFieldPosition(targetType);
//      }
//
//			insertImportIfNecessary(pullPushData[i], editor);
//
//			ExtractedSource extractedSource = extractPartFromSource(pullPushData[i], editor);
////      System.out.println("position.getLine():" + position.getLine()); //innnnn
////      System.out.println("position.getColumn():" + position.getColumn()); //innnnn
////      System.out.println("extractedSource.getCombinedSource():" + extractedSource.getCombinedSource()); //innnnn
//
//			if (pullPushData[i].isImplementationForClass()) {
//				editor.addEditor(new StringInserter(pullPushData[i].getTypeToMoveIn().getCompilationUnit(),
//																						position.getLine(), 0,
//																						extractedSource.getCombinedSource()));
//			}
//
//			if (isErasePermitted(pullPushData[i])) {
//				editor.addEditor(new StringEraser(member.getCompilationUnit(),
//																					startToExtract, 0,
//																					member.getEndLine(),
//																					member.getEndColumn()));
//			}
//
//			if (pullPushData[i].isDeclarationForInterface() ||
//					pullPushData[i].isDeclarationForClass()) {
//				String interfaceDeclaration =
//								composeDeclarationForInterface(pullPushData[i], extractedSource);
//				editor.addEditor(
//            new StringInserter(pullPushData[i].getTypeToMoveIn().getCompilationUnit(),
//                position.getLine(), 0,
//                interfaceDeclaration));
//
//				if (member instanceof BinMethod) {
//					List implementers = pullPushData[i].getImplementers();
//
//					if (implementers.size() > 0) {
//						String methodDefinition =
//											composeMethodDefinitionForImplementer((BinMethod)member);
//
//						for (int j = 0, maxJ = implementers.size(); j < maxJ; j++) {
//							BinCIType implementer = (BinCIType)implementers.get(j);
//							SourceCoordinate implementerPosition
//                  = implementer.findNewMethodPosition();
//
//							editor.addEditor(new StringInserter(implementer.getCompilationUnit(),
//																									implementerPosition.getLine(),
//                                                  0,
//																									methodDefinition));
//						}
//					}
//				}
//			}
//		}
//
//		status.merge(editor.performEdit());
//
//		return status;
//	}
//
//  public SourceCoordinate findNewFieldPosition(BinCIType targetType) {
//    return findNewMemberPosition(JavaTokenTypes.VARIABLE_DEF, targetType);
//  }
//
//  public SourceCoordinate findNewMethodPosition(BinCIType targetType) {
//    return findNewMemberPosition(JavaTokenTypes.METHOD_DEF, targetType);
//  }
//
//  private SourceCoordinate findNewMemberPosition(int tokenType, BinCIType targetType) {
//    ASTImpl objBlock = ASTUtil.getFirstChildOfType(targetType.getOffsetNode(),
//        JavaTokenTypes.OBJBLOCK);
//
//    ASTImpl lastChild = ASTUtil.getLastChildOfTypeInConsecutiveList(
//        objBlock, tokenType);
//
//    int line, column;
//
//    if (lastChild == null) {
//      if (tokenType == JavaTokenTypes.METHOD_DEF) {
//        // just before closing brace
//        line = objBlock.getEndLine();
//        column = objBlock.getEndColumn();
//      } else { // VARIABLE_DEF and all the rest
//        // just after opening brace
//        line = objBlock.getStartLine() + 1;
//        column = objBlock.getStartColumn();
//      }
//    } else {
//      line = lastChild.getEndLine() + 1;
//      column = lastChild.getEndColumn();
//    }
//
//    return new SourceCoordinate(line, column);
//  }
//
//
//	private String composeMethodDefinitionForImplementer(BinMethod method) {
//		StringBuffer result = new StringBuffer();
//		String modifiers = BinModifier.toString(method.getModifiers(), false, true);
//
//		if (modifiers.length() > 0) {
//			modifiers += " ";
//		}
//
//		result.append("\n\tpublic " + modifiers);
//		result.append(composeMethodSignature(method) + " {\n");
//		result.append("\t\tthrow new RuntimeException(\"method " + method.getName() +
//									" is not implemented\");\n\t}\n");
//
//		return result.toString();
//	}
//
//	private boolean isErasePermitted(PullPushData data) {
//		return (data.getMembersToSplit().size() == 0 &&
//						(data.isImplementationForClass() ||
//						(data.isDeclarationForInterface() && (data.getMember() instanceof BinField))));
//	}
//
//	private int findEndColumnOfPreviousLine(PullPushData data, SourceCoordinate position) {
//		String content = data.getTypeToMoveIn().getCompilationUnit().getContent();
//		LineIndexer indexer = new LineIndexer(content, 0);
//
//		int posStart = indexer.lineColToPos(position.getLine() - 1, 1);
//		int posEnd = content.indexOf('\n', posStart);
//		int colEnd = indexer.posToLineCol(posEnd).getColumn();
//
//		return colEnd;
//	}
//
//	private String composeDeclarationForInterface(PullPushData data,
//																								ExtractedSource extractedSource) {
//		BinMember member = data.getMember();
//		String comments = extractedSource.comments;
//
//		/* comments length may be zero in the following situation:
//		 * /@ comment string @/ int a;
//		 */
//		if (comments.trim().length() == 0) {
//			comments = collectCommentsFor(member, new StringBuffer("\n"));
//		}
//
//		StringBuffer result;
//		if (comments.trim().length() == 0) {
//			result = new StringBuffer("\t");
//		}
//		else {
//			result = new StringBuffer(comments +
//																(comments.endsWith("\n") ? "\t" : "\n\t"));
//		}
//
//		if (data.isDeclarationForClass()) {
//			String accessModifier = BinModifier.toString(data.getAccessModifier());
//			if (accessModifier.length() > 0) {
//				result.append(accessModifier + " ");
//			}
//			result.append("abstract ");
//		}
//
//		if (member instanceof BinField) {
//			BinField field = (BinField)member;
//
//			BinType binType = field.getTypeRef().getBinType();
//			if (binType instanceof BinArrayType) {
//				result.append(((BinArrayType)binType).getArrayType().getName());
//				result.append(((BinArrayType)binType).getDimensionString() + " ");
//			}
//			else {
//				result.append(binType.getName() + " ");
//			}
//
//			result.append(field.getName());
//
//			String initString = data.getStaticInit();
//			if (initString != null && initString.length() > 0) {
//				result.append(" = " + initString + ";\n");
//			}
//			else {
//				String memberDecalration = extractedSource.memberDecalration;
//				result.append(" " + memberDecalration.substring(
//																								memberDecalration.indexOf('='),
//																								memberDecalration.length()));
//			}
//		}
//		else if (member instanceof BinMethod) {
//			result.append(composeMethodSignature((BinMethod)member) + ";\n");
//		}
//		else {
//			throw new RuntimeException("member is neither BinField nor BinMethod");
//		}
//
//		return result.toString();
//	}
//
//	private String composeMethodSignature(BinMethod method) {
//		StringBuffer result = new StringBuffer();
//
//		result.append(method.getReturnType().getName() + " ");
//		result.append(method.getName() + "(");
//
//		BinParameter[] params = method.getParameters();
//		for (int i = 0; i < params.length; i++) {
//			result.append(params[i].getTypeRef().getName() + " ");
//			result.append(params[i].getName());
//
//			if ((params.length - i) > 1) {
//				result.append(", ");
//			}
//		}
//
//		result.append(")");
//
//		return result.toString();
//	}
//
//	private String changeAccessOfMember(PullPushData data, String sourceLine,
//																					int startAccessColumn) {
//		int nodeAccess = data.getAccessModifier();
//		int memberAccess = data.getMember().getAccessModifier();
//
//		if (nodeAccess != memberAccess) {
//			return changeAccess(memberAccess, nodeAccess, sourceLine, startAccessColumn);
//		}
//
//		return sourceLine;
//	}
//
//	private String changeSuperToThis(ASTImpl superAST, String sourceLine, int count) {
//		if (superAST == null) {
//			return sourceLine;
//		}
//
//		return StringUtil.replace(sourceLine, "this.",
//															(superAST.getStartColumn() - 1) - count,
//															(superAST.getEndColumn() - 1) - count);
//	}
//
//	private String addCastForThis(ASTImpl thisAST, String sourceLine, int count) {
//		if (thisAST == null) {
//			return sourceLine;
//		}
//
//		String cast = "(" + selectedMember.getOwner().getName() + ")";
//		StringBuffer buffer = new StringBuffer(sourceLine);
//		buffer.insert(((thisAST.getStartColumn() - 1) + (cast.length() * count)), cast);
//
//		return buffer.toString();
//	}
//
//	private String changeAccess(int oldAccess, int newAccess, String sourceLine,
//															int startAccessColumn) {
//		if (oldAccess == BinModifier.PACKAGE_PRIVATE) {
//			StringBuffer buffer = new StringBuffer(sourceLine);
//			return buffer.insert(startAccessColumn - 1,
//														BinModifier.toString(newAccess) + " ").toString();
//		}
//		else {
//			return StringUtil.replace(sourceLine,
//									getOldAccessString(oldAccess, newAccess, sourceLine),
//									BinModifier.toString(newAccess));
//		}
//	}
//
//	private String getOldAccessString(int oldAccess, int newAccess, String extracted) {
//		String oldAccessString = BinModifier.toString(oldAccess);
//		char afterAccess = extracted.charAt(extracted.indexOf(oldAccessString) +
//																				oldAccessString.length());
//		return newAccess ==
//					BinModifier.PACKAGE_PRIVATE ? oldAccessString + afterAccess
//																			: oldAccessString;
//	}
//
//	private void insertImportIfNecessary(PullPushData data, SourceEditor editor) {
//		List imports = data.getImports();
//
//		if (imports.size() > 0) {
//			BinCIType typeToMoveIn = data.getTypeToMoveIn();
//			SourceCoordinate importPosition =
//							findImportPositionInNewClass(typeToMoveIn);
//
//			StringBuffer importString = new StringBuffer();
//			for (int i = 0, max = imports.size(); i < max; i++) {
//				importString.append("import ");
//				importString.append(imports.get(i));
//				importString.append(";\n");
//
//				editor.addEditor(new StringInserter(typeToMoveIn.getCompilationUnit(),
//																						importPosition.getLine(), 0,
//																						importString.toString()));
//				importString.setLength(0);
//			}
//		}
//	}
//
//	private HashMap collectAccessDataOfImmovables(PullPushData data) {
//		HashMap accessData = new HashMap();
//		HashMap accessOfImmovables = data.getAccessModifiersOfImmovables();
//
//		if (accessOfImmovables.size() > 0) {
//			Set keySet = accessOfImmovables.keySet();
//
//			for (Iterator i = keySet.iterator(); i.hasNext();) {
//				BinMember member = (BinMember)i.next();
//				int access = ((Integer)accessOfImmovables.get(member)).intValue();
//
//				ASTImpl astNode = member.getVisibilityNode();
//
//				accessData.put(new Integer(astNode.getStartLine()),
//											new AccessData(member, access, astNode.getStartLine(),
//																			astNode.getStartColumn()));
//			}
//		}
//
//		return accessData;
//	}
//
//	private void changeAccessOfImmovables(AccessData accessData, String sourceLine,
//																				SourceEditor editor) {
//		String result = changeAccess(accessData.oldAccess, accessData.newAccess,
//																	sourceLine, accessData.accessColumn);
//
//		editor.addEditor(new StringEraser(accessData.member.getCompilationUnit(),
//																			accessData.accessLine, 0,
//																			sourceLine.length()));
//
//		editor.addEditor(new StringInserter(accessData.member.getCompilationUnit(),
//																				accessData.accessLine, 0,
//																				result));
//	}
//
//	private String collectCommentsFor(LocationAware la, StringBuffer result) {
//		Comment comment = Comment.findFor(la);
//		if (comment != null) {
//			result.insert(1, "\t" + comment.getText() + "\n");
//
//			collectCommentsFor(comment, result);
//		}
//
//		return result.toString();
//	}
//
//	private Comment findCommentFor(LocationAware la) {
//		Comment comment = Comment.findFor(la);
//
//		if (comment != null) {
//			Comment upperComment = findCommentFor(comment);
//
//			if (upperComment != null) {
//				return upperComment;
//			}
//		}
//
//		return comment;
//	}
//
//	private String splitMembers(PullPushData data, String sourceLine,
//															SourceEditor editor) {
//		BinMember member = data.getMember();
//		List membersToSplit = data.getMembersToSplit();
//		StringBuffer modifierBuffer = new StringBuffer();
//		String type = "";
//
//		ASTImpl typeNode = ASTUtil.getFirstChildOfType(member.getOffsetNode(),
//																										JavaTokenTypes.TYPE);
//		if (typeNode != null) {
//			type = typeNode.getFirstChild().getText();
//		}
//
//    List modifierNodes = member.getModifierNodes();
//    for (int i = 0, max = modifierNodes.size(); i < max; i++) {
//      final ASTImpl modifier = (ASTImpl) modifierNodes.get(i);
//			modifierBuffer.append(modifier.getText() + " ");
//    }
//
//		String newSourceLine = modifierBuffer.toString() + type + " " +
//														member.getName() + ";\n";
//
//		StringBuffer stringWithoutMember = new StringBuffer(modifierBuffer.toString());
//		stringWithoutMember.append(type + " ");
//
//		for (int i = 0, max = membersToSplit.size(); i < max; i++) {
//			stringWithoutMember.append(((BinMember)membersToSplit.get(i)).getName());
//			if ((max - i) > 1) {
//				stringWithoutMember.append(", ");
//			}
//		}
//		stringWithoutMember.append(";");
//
//		editor.addEditor(new StringEraser(member.getCompilationUnit(),
//																			member.getStartLine(), member.getStartColumn() - 1,
//																			member.getEndLine(), member.getEndColumn() - 1));
//
//		editor.addEditor(new StringInserter(data.getTypeToMoveIn().getCompilationUnit(),
//																				member.getStartLine(),
//																				member.getStartColumn() - 1,
//																				stringWithoutMember.toString()));
//
//		return newSourceLine;
//	}
//
//	private ASTImpl[] getASTsOfParameterTypes(PullPushData data) {
//		BinMember member = data.getMember();
//
//		if (member instanceof BinMethod) {
//			List list = new ArrayList();
//			String oldTypeName = member.getOwner().getName();
//			BinParameter[] parameters = ((BinMethod)member).getParameters();
//
//			for (int i = 0; i < parameters.length; i++) {
//				ASTImpl typeAst = parameters[i].getTypeAst();
//
//				if (oldTypeName.equals(typeAst.getText())) {
//					list.add(typeAst);
//				}
//			}
//
//			return (ASTImpl[])list.toArray(new ASTImpl[0]);
//		}
//
//		return new ASTImpl[0];
//	}
//
//	private String changeTypeOfParameter(ASTImpl typeAST, String newTypeName,
//																				String sourceLine, int count) {
//		if (typeAST == null) {
//			return sourceLine;
//		}
//
//		int endColumn = (typeAST.getEndColumn() - 1);
//		endColumn =
//						Character.isWhitespace(sourceLine.charAt(endColumn)) ? (endColumn - 1)
//																																 : endColumn;
//		return StringUtil.replace(sourceLine, newTypeName,
//															(typeAST.getStartColumn() - 1) - count,
//															endColumn - count);
//	}
//
//	private ExtractedSource extractPartFromSource(PullPushData data,
//																								SourceEditor editor) {
//		BinMember member = data.getMember();
//		CompilationUnit compilationUnit = member.getCompilationUnit();
//		Comment comment = findCommentFor(member);
//		HashMap accessDataOfImmovables = collectAccessDataOfImmovables(data);
//		ASTImpl[] superASTs = (ASTImpl[])data.getSuperReferenceASTList().toArray(new ASTImpl[0]);
//		ASTImpl[] thisASTs = (ASTImpl[])data.getThisReferenceASTList().toArray(new ASTImpl[0]);
//		List membersToSplit = data.getMembersToSplit();
//		StringBuffer commentResult = new StringBuffer();
//		StringBuffer declarationResult = new StringBuffer();
//    LineReader reader = null;
//		String buffer = null;
//
//		int startLine = member.getStartLine();
//		int endLine = member.getEndLine();
//		startToExtract = startLine;
//
//		ASTImpl astNode = member.getVisibilityNode();
//		int startAccessLine = astNode.getStartLine();
//		int startAccessColumn = astNode.getStartColumn();
//
//		ASTImpl[] typeASTs = new ASTImpl[0];
//		if (data.isChangeParameterType()) {
//			typeASTs = getASTsOfParameterTypes(data);
//		}
//
//		try {
//			int len = (int)compilationUnit.getSource().length();
//			if (len < 1024) {
//				len = 1024;
//			}
//
//			try {
//				reader = new LineReader(
//												new InputStreamReader(compilationUnit.getSource().getInputStream(),
//																							Main.getEncoding()), len);
//			}
//			catch (UnsupportedEncodingException e) {
//				throw new RuntimeException( "Unsupported encoding: " + e.getMessage() );
//			}
//
//      int lineNum = 0;
//
//			while ((buffer = reader.readLine()) != null || lineNum == 0) {
//				++lineNum;
//
//				if (comment != null) {
//					if (lineNum + 1 == comment.getStartLine()) {
//						if (buffer.trim().length() == 0) {
//							commentResult.append(buffer);
//							startToExtract = lineNum;
//						}
//						else {
//							startToExtract = lineNum + 1;
//						}
//					}
//
//					if (lineNum >= comment.getStartLine() && lineNum < startLine) {
//						commentResult.append(buffer);
//					}
//				}
//				else {
//					if (lineNum + 1 == startLine) {
//						if (buffer.trim().length() == 0) {
//							declarationResult.append(buffer);
//							startToExtract = lineNum;
//						}
//					}
//				}
//
//				if (lineNum >= startLine && lineNum <= endLine) {
//					if (membersToSplit.size() > 0) {
//						buffer = splitMembers(data, buffer, editor);
//					}
//
//					if (!data.isDeclarationForClass()) {
//						if (lineNum == startAccessLine) {
//							buffer = changeAccessOfMember(data, buffer, startAccessColumn);
//						}
//					}
//
//					for (int i = 0, count = 0; i < typeASTs.length; i++) {
//						String newTypeName = data.getTypeToMoveIn().getName();
//						if (lineNum == typeASTs[i].getStartLine()) {
//							buffer = changeTypeOfParameter(typeASTs[i], newTypeName,
//																							buffer, count);
//							count += (typeASTs[i].getTextLength() - newTypeName.length());
//						}
//					}
//
//					for (int i = 0, count = 0; i < superASTs.length; i++) {
//						if (lineNum == superASTs[i].getStartLine()) {
//							buffer = changeSuperToThis(superASTs[i], buffer, count);
//							count++;
//						}
//					}
//
//					for (int i = 0, count = 0; i < thisASTs.length; i++) {
//						if (lineNum == thisASTs[i].getStartLine()) {
//							buffer = addCastForThis(thisASTs[i], buffer, count);
//							count++;
//						}
//					}
//
//					declarationResult.append(buffer);
//				}
//
//				Integer integer = new Integer(lineNum);
//				if (accessDataOfImmovables.containsKey(integer)) {
//					AccessData accessData = (AccessData)accessDataOfImmovables.get(integer);
//					changeAccessOfImmovables(accessData, buffer, editor);
//				}
//			}
//
//			if (reader != null) {
//				reader.close();
//			}
//    }
//		catch (Exception e) {
//			throw new RuntimeException("Problems in Pull/Push");
//		}
//
//		return new ExtractedSource(commentResult.toString(), declarationResult.toString());
//	}
//
//	private SourceCoordinate findImportPositionInNewClass(BinCIType typeToMoveIn) {
//		ASTImpl firstNode = (ASTImpl)typeToMoveIn.getCompilationUnit().getFirstNode();
//
//		if (firstNode.getType() != JavaTokenTypes.PACKAGE_DEF &&
//				firstNode.getType() != JavaTokenTypes.IMPORT) {
//      return new SourceCoordinate(1, 0);
//		}
//
//		ASTImpl result = null;
//		ASTImpl sibling = (ASTImpl)firstNode.getNextSibling();
//		while (sibling != null && sibling.getType() == JavaTokenTypes.IMPORT) {
//			result = sibling;
//			sibling = (ASTImpl)sibling.getNextSibling();
//		}
//
//    int line = (result != null) ? result.getLine() + 1
//                                : firstNode.getLine() + 1;
//    return new SourceCoordinate(line, 0);
//	}
//
//	public void setPullPushData(PullPushData[] pullPushData) {
//		this.pullPushData = pullPushData;
//	}

  public String getDescription() {
    return super.getDescription();
  }

  public String getKey() {
    return key;
  }

}
