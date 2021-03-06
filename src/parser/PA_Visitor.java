package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import utils.FileUtil;

//extends ASTVisitor

public class StatParamVisitor extends ASTVisitor {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private static final boolean PARSE_INDIVIDUAL_SRC = false,
			SCAN_FILES_FRIST = false;
	private String inPath, outPath;
	private boolean testing = false;
	private PrintStream stLocations, stSourceSequences, stTargetSequences,
			stLog;
	private HashSet<String> badFiles = new HashSet<>();
	private static final boolean USE_SIMPLE_METHOD_NAME = false;
	private String className, superClassName;
	private int numOfExpressions = 0, numOfResolvedExpressions = 0;
	private StringBuilder fullTokens = new StringBuilder();// , partialTokens =
															// new
															// StringBuilder();
	private String fullSequence = null, partialSequence = null;
	private String[] fullSequenceTokens, partialSequenceTokens;
	private HashMap<ASTNode, String> abstractSequences;

	public StatParamVisitor(String className, String superClassName) {
		super(false);
		this.className = className;
		this.superClassName = superClassName;
		this.abstractSequences = new HashMap<ASTNode, String>();
	}

	public String[] getFullSequenceTokens() {
		if (fullSequenceTokens == null)
			buildFullSequence();
		return fullSequenceTokens;
	}

	// public String[] getPartialSequenceTokens() {
	// // if (partialSequenceTokens == null)
	// // buildPartialSequence();
	// return partialSequenceTokens;
	// }

	public String getFullSequence() {
		if (fullSequence == null)
			buildFullSequence();
		return fullSequence;
	}

	// public String getPartialSequence() {
	// if (partialSequence == null)
	// buildPartialSequence();
	// return partialSequence;
	// }

	private void buildFullSequence() {
		ArrayList<String> parts = buildSequence(fullTokens);
		this.fullSequence = parts.get(0);
		this.fullSequenceTokens = new String[parts.size() - 1];
		for (int i = 1; i < parts.size(); i++)
			this.fullSequenceTokens[i - 1] = parts.get(i);
	}

	// private void buildPartialSequence() {
	// ArrayList<String> parts = buildSequence(partialTokens);
	// this.partialSequence = parts.get(0);
	// this.partialSequenceTokens = new String[parts.size() - 1];
	// for (int i = 1; i < parts.size(); i++)
	// this.partialSequenceTokens[i-1] = parts.get(i);
	// }

	private ArrayList<String> buildSequence(StringBuilder tokens) {
		tokens.append(" ");
		ArrayList<String> l = new ArrayList<>();
		StringBuilder sequence = new StringBuilder(), token = null;
		for (int i = 0; i < tokens.length(); i++) {
			char ch = tokens.charAt(i);
			if (ch == ' ') {
				if (token != null) {
					String t = token.toString();
					l.add(t);
					sequence.append(t + " ");
					token = null;
				}
			} else {
				if (token == null)
					token = new StringBuilder();
				token.append(ch);
			}
		}
		l.add(0, sequence.toString());
		return l;
	}

	public int getNumOfExpressions() {
		return numOfExpressions;
	}

	public int getNumOfResolvedExpressions() {
		return numOfResolvedExpressions;
	}

	private Type getType(VariableDeclarationFragment node) {
		ASTNode p = node.getParent();
		if (p instanceof VariableDeclarationExpression)
			return ((VariableDeclarationExpression) p).getType();
		if (p instanceof VariableDeclarationStatement)
			return ((VariableDeclarationStatement) p).getType();
		return null;
	}

	// need modify
	static String getUnresolvedType(Type type) {
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			return getUnresolvedType(t.getElementType())
					+ getDimensions(t.getDimensions());
		} else if (type.isIntersectionType()) {
			IntersectionType it = (IntersectionType) type;
			@SuppressWarnings("unchecked")
			ArrayList<Type> types = new ArrayList<>(it.types());
			String s = getUnresolvedType(types.get(0));
			for (int i = 1; i < types.size(); i++)
				s += " & " + getUnresolvedType(types.get(i));
			return s;
		} else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getUnresolvedType(t.getType());
		} else if (type.isUnionType()) {
			UnionType it = (UnionType) type;
			@SuppressWarnings("unchecked")
			ArrayList<Type> types = new ArrayList<>(it.types());
			String s = getUnresolvedType(types.get(0));
			for (int i = 1; i < types.size(); i++)
				s += " | " + getUnresolvedType(types.get(i));
			return s;
		} else if (type.isNameQualifiedType()) {
			NameQualifiedType qt = (NameQualifiedType) type;
			return qt.getQualifier().getFullyQualifiedName() + "."
					+ qt.getName().getIdentifier();
		} else if (type.isPrimitiveType()) {
			return type.toString();
		} else if (type.isQualifiedType()) {
			QualifiedType qt = (QualifiedType) type;
			return getUnresolvedType(qt.getQualifier()) + "."
					+ qt.getName().getIdentifier();
		} else if (type.isSimpleType()) {
			return type.toString();
		} else if (type.isWildcardType()) {
			WildcardType wt = (WildcardType) type;
			String s = "?";
			if (wt.getBound() != null) {
				if (wt.isUpperBound())
					s += "extends ";
				else
					s += "super ";
				s += getUnresolvedType(wt.getBound());
			}
			return s;
		}

		return null;
	}

	private static String getDimensions(int dimensions) {
		String s = "";
		for (int i = 0; i < dimensions; i++)
			s += "[]";
		return s;
	}

	static String getResolvedType(Type type) {
		ITypeBinding tb = type.resolveBinding();
		if (tb == null || tb.isRecovered())
			return getUnresolvedType(type);
		tb = tb.getTypeDeclaration();
		if (tb.isLocal() || tb.getQualifiedName().isEmpty())
			return getUnresolvedType(type);
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			return getResolvedType(t.getElementType())
					+ getDimensions(t.getDimensions());
		} else if (type.isIntersectionType()) {
			IntersectionType it = (IntersectionType) type;
			@SuppressWarnings("unchecked")
			ArrayList<Type> types = new ArrayList<>(it.types());
			String s = getResolvedType(types.get(0));
			for (int i = 1; i < types.size(); i++)
				s += " & " + getResolvedType(types.get(i));
			return s;
		} else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getResolvedType(t.getType());
		} else if (type.isUnionType()) {
			UnionType it = (UnionType) type;
			@SuppressWarnings("unchecked")
			ArrayList<Type> types = new ArrayList<>(it.types());
			String s = getResolvedType(types.get(0));
			for (int i = 1; i < types.size(); i++)
				s += " | " + getResolvedType(types.get(i));
			return s;
		} else if (type.isNameQualifiedType()) {
			return tb.getQualifiedName();
		} else if (type.isPrimitiveType()) {
			return type.toString();
		} else if (type.isQualifiedType()) {
			return tb.getQualifiedName();
		} else if (type.isSimpleType()) {
			return tb.getQualifiedName();
		} else if (type.isWildcardType()) {
			WildcardType wt = (WildcardType) type;
			String s = "?";
			if (wt.getBound() != null) {
				if (wt.isUpperBound())
					s += "extends ";
				else
					s += "super ";
				s += getResolvedType(wt.getBound());
			}
			return s;
		}

		return null;
	}

	@Override
	public void preVisit(ASTNode node) {
		if (node instanceof Expression) {
			numOfExpressions++;
			Expression e = (Expression) node;
			if (e.resolveTypeBinding() != null
					&& !e.resolveTypeBinding().isRecovered())
				numOfResolvedExpressions++;
		} else if (node instanceof Statement) {
			if (node instanceof ConstructorInvocation) {
				numOfExpressions++;
				if (((ConstructorInvocation) node).resolveConstructorBinding() != null
						&& !((ConstructorInvocation) node)
								.resolveConstructorBinding().isRecovered())
					numOfResolvedExpressions++;
			} else if (node instanceof SuperConstructorInvocation) {
				numOfExpressions++;
				if (((SuperConstructorInvocation) node)
						.resolveConstructorBinding() != null
						&& !((SuperConstructorInvocation) node)
								.resolveConstructorBinding().isRecovered())
					numOfResolvedExpressions++;
			}
		} else if (node instanceof Type) {
			numOfExpressions++;
			Type t = (Type) node;
			if (t.resolveBinding() != null && !t.resolveBinding().isRecovered())
				numOfResolvedExpressions++;
		}
	}

	@Override
	public boolean visit(ArrayAccess node) {
		Expression exp = node.getArray();
		Expression e1 = node.getIndex();
		// In definition, ArrayAccess has only one dimension
		exp.accept(this);
		e1.accept(this);
		return false;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		// String utype = getUnresolvedType(node.getType()), rtype =
		// getResolvedType(node.getType());
		// this.partialTokens.append(" new " + utype + " ");
		// this.fullTokens.append(" new " + rtype + " ");
		if (node.getInitializer() != null)
			node.getInitializer().accept(this);
		else
			for (int i = 0; i < node.dimensions().size(); i++)
				((Expression) (node.dimensions().get(i))).accept(this);
		return false;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(AssertStatement node) {
		// this.fullTokens.append(" assert ");
		// this.partialTokens.append(" assert ");
		return super.visit(node);
	}

	@Override
	public boolean visit(Assignment node) {
		node.getLeftHandSide().accept(this);
		// this.fullTokens.append(" = ");
		// this.partialTokens.append(" = ");
		node.getRightHandSide().accept(this);
		return false;
	}

	@Override
	public boolean visit(Block node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		// this.fullTokens.append(" boolean ");
		// this.partialTokens.append(" boolean ");
		return false;
	}

	@Override
	public boolean visit(BreakStatement node) {
		return false;
	}

	@Override
	public boolean visit(CastExpression node) {
		String result = "(" + resolveType(node.getType().resolveBinding()) + ")"
				+ resolveType(node.getExpression().resolveTypeBinding());
		node.getExpression().accept(this);
		return false;
	}

	@Override
	public boolean visit(CatchClause node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		// this.fullTokens.append(" char ");
		// this.partialTokens.append(" char ");
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ITypeBinding tb = node.getType().resolveBinding();
		if (tb != null && tb.getTypeDeclaration().isLocal())
			return false;
		String utype = getUnresolvedType(node.getType()), rtype = getResolvedType(node
				.getType());
		this.fullTokens.append(" new " + rtype + "() ");
		// this.partialTokens.append(" new " + utype + "() ");
		for (Iterator it = node.arguments().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			e.accept(this);
		}
		if (node.getAnonymousClassDeclaration() != null)
			node.getAnonymousClassDeclaration().accept(this);
		return false;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		IMethodBinding b = node.resolveConstructorBinding();
		ITypeBinding tb = null;
		if (b != null && b.getDeclaringClass() != null)
			tb = b.getDeclaringClass().getTypeDeclaration();
		if (tb != null) {
			if (tb.isLocal() || tb.getQualifiedName().isEmpty())
				return false;
		}
		String name = "." + className + "()";
		// this.partialTokens.append(" " + name + " ");
		if (tb != null)
			name = getQualifiedName(tb) + name;
		this.fullTokens.append(" " + name + " ");
		for (int i = 0; i < node.arguments().size(); i++)
			((ASTNode) node.arguments().get(i)).accept(this);
		return false;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		return false;
	}

	@Override
	public boolean visit(CreationReference node) {
		return false;
	}

	@Override
	public boolean visit(Dimension node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(EmptyStatement node) {
		return false;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(ExpressionMethodReference node) {
		return false;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		ITypeBinding tb = node.resolveTypeBinding();
		IVariableBinding vb = node.resolveFieldBinding();
		Expression exp=node.getExpression();
		exp.accept(this);
		String result = resolveType(tb) + "|||"
				+ resolveType(vb.getDeclaringClass())
				+ node.getName().getIdentifier();
		this.fullTokens.append(" " + result + " ");
		return false;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(ForStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(InfixExpression node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(Initializer node) {
		return false;
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		// this.fullTokens.append(" ");
		// this.partialTokens.append(" ");
		node.getLeftOperand().accept(this);
		// this.fullTokens.append(" <instanceof> ");
		// this.partialTokens.append(" <instanceof> ");
		String rtype = getResolvedType(node.getRightOperand()), utype = getUnresolvedType(node
				.getRightOperand());
		// this.fullTokens.append(rtype + " ");
		// this.partialTokens.append(utype + " ");
		return false;
	}

	@Override
	public boolean visit(LabeledStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(LambdaExpression node) {
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.getBody() != null && !node.getBody().statements().isEmpty())
			node.getBody().accept(this);
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {

		IMethodBinding b = node.resolveMethodBinding();
		ITypeBinding tb = null;
		if (b != null) {
			if (tb != null) {
				tb = tb.getTypeDeclaration();
				if (tb.isLocal() || tb.getQualifiedName().isEmpty())
					return false;
			}
		}
		String typeResult=resolveType(tb);
		
		if(typeResult.equals("")){
			return false;
		}
		this.fullTokens.append(" ");
		// this.partialTokens.append(" ");
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		} 
		String name = "." + node.getName().getIdentifier() + "()";
		
		name = resolveType(tb) + name;
		this.fullTokens.append(" " + name + " ");
		
		for (int i = 0; i < node.arguments().size(); i++){
			String result=getS(node);
			ASTNode nodeReturnType=(ASTNode)node.arguments().get(i);
			String strReturnType=getReturnType(nodeReturnType);
			if(!strReturnType.isEmpty()){				
				this.fullTokens.append(" " + strReturnType + "|||"+result+" ");
			}
			
		}
			
		
		
		for (int i = 0; i < node.arguments().size(); i++)
			((ASTNode) node.arguments().get(i)).accept(this);
		return false;
	}

	@Override
	public boolean visit(Modifier node) {
		return false;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		// this.fullTokens.append(" null ");
		// this.partialTokens.append(" null ");
		return false;
	}

	@Override
	public boolean visit(NumberLiteral node) {
		// this.fullTokens.append(" number ");
		// this.partialTokens.append(" number ");
		return false;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(PostfixExpression node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(PrefixExpression node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		QualifiedName obj = (QualifiedName) node;
		ITypeBinding tb = obj.resolveTypeBinding();
		this.fullTokens.append(" "+resolveType(tb) + "|||"
				+ resolveType(obj.getName().resolveTypeBinding())+"|||"+obj.getName().getIdentifier()+" ");
		return false;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleName node) {
		return false;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		ITypeBinding tb = node.getType().resolveBinding();
		if (tb != null && tb.getTypeDeclaration().isLocal())
			return false;
		String utype = getUnresolvedType(node.getType()), rtype = getResolvedType(node
				.getType());
		// this.partialTokens.append(" " + utype + " ");
		// this.fullTokens.append(" " + rtype + " ");
		if (node.getInitializer() != null) {
			// this.partialTokens.append("= ");
			// this.fullTokens.append("= ");
			node.getInitializer().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(StringLiteral node) {
		// this.fullTokens.append(" java.lang.String ");
		// this.partialTokens.append(" java.lang.String ");
		return false;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		IMethodBinding b = node.resolveConstructorBinding();
		ITypeBinding tb = null;
		if (b != null && b.getDeclaringClass() != null)
			tb = b.getDeclaringClass().getTypeDeclaration();
		if (tb != null) {
			if (tb.isLocal() || tb.getQualifiedName().isEmpty())
				return false;
		}
		String name = "." + superClassName + "()";
		// this.partialTokens.append(" " + name + " ");
		if (tb != null)
			name = getQualifiedName(tb) + name;
		// this.fullTokens.append(" " + name + " ");
		for (int i = 0; i < node.arguments().size(); i++)
			((ASTNode) node.arguments().get(i)).accept(this);
		return false;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		IVariableBinding b = node.resolveFieldBinding();
		ITypeBinding tb = null;
		if (b != null && b.getDeclaringClass() != null) {
			tb = b.getDeclaringClass().getTypeDeclaration();
			if (tb.isLocal() || tb.getQualifiedName().isEmpty())
				return false;
			// this.partialTokens.append(" " + getName(tb) + " ");
			// this.fullTokens.append(" " + getQualifiedName(tb) + " ");
		} else {
			// this.partialTokens.append(" super ");
			// this.fullTokens.append(" super ");
		}
		String name = "." + node.getName().getIdentifier();
		// this.partialTokens.append(" " + name + " ");
		if (tb != null)
			name = getQualifiedName(tb) + name;
		// this.fullTokens.append(" " + name + " ");
		return false;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		IMethodBinding b = node.resolveMethodBinding();
		ITypeBinding tb = null;
		if (b != null && b.getDeclaringClass() != null)
			tb = b.getDeclaringClass().getTypeDeclaration();
		if (tb != null) {
			if (tb.isLocal() || tb.getQualifiedName().isEmpty())
				return false;
			// this.partialTokens.append(" " + getName(tb) + " ");
			this.fullTokens.append(" " + getQualifiedName(tb) + " ");
		} else {
			// this.partialTokens.append(" super ");
			this.fullTokens.append(" super ");
		}
		String name = "." + node.getName().getIdentifier() + "()";
		// this.partialTokens.append(" " + name + " ");
		if (!USE_SIMPLE_METHOD_NAME && tb != null
		// && !name.equals(".toString()")
		// && !name.equals(".equals()")
		// && !name.equals(".clone()")
		// && !name.equals(".getClass()")
		// && !name.equals(".hashCode()")
		// && !name.equals(".valueOf()")
		)
			name = getQualifiedName(tb) + name;
		this.fullTokens.append(" " + name + " ");
		for (int i = 0; i < node.arguments().size(); i++)
			((ASTNode) node.arguments().get(i)).accept(this);
		return false;
	}

	@Override
	public boolean visit(SuperMethodReference node) {
		return false;
	}

	@Override
	public boolean visit(SwitchCase node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {

		return false;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		return false;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		return false;
	}

	@Override
	public boolean visit(TypeMethodReference node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeParameter node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		ITypeBinding tb = node.getType().resolveBinding();
		if (tb != null && tb.getTypeDeclaration().isLocal())
			return false;
		// String utype = getUnresolvedType(node.getType()), rtype =
		// getResolvedType(node.getType());
		// this.partialTokens.append(" " + utype + " ");
		// this.fullTokens.append(" " + rtype + " ");
		for (int i = 0; i < node.fragments().size(); i++)
			((ASTNode) node.fragments().get(i)).accept(this);
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding tb = node.getType().resolveBinding();
		if (tb != null && tb.getTypeDeclaration().isLocal())
			return false;
		// String utype = getUnresolvedType(node.getType()), rtype =
		// getResolvedType(node.getType());
		// this.partialTokens.append(" " + utype + " ");
		// this.fullTokens.append(" " + rtype + " ");
		for (int i = 0; i < node.fragments().size(); i++)
			((ASTNode) node.fragments().get(i)).accept(this);
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		Type type = getType(node);
		String utype = getUnresolvedType(type), rtype = getResolvedType(type);
		// this.partialTokens.append(" " + utype + " ");
		// this.fullTokens.append(" " + rtype + " ");
		if (node.getInitializer() != null) {
			// this.partialTokens.append("= ");
			// this.fullTokens.append("= ");
			node.getInitializer().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(WhileStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayType node) {
		return false;
	}

	@Override
	public boolean visit(IntersectionType node) {
		return false;
	}

	@Override
	public boolean visit(ParameterizedType node) {
		return false;
	}

	@Override
	public boolean visit(UnionType node) {
		return false;
	}

	@Override
	public boolean visit(NameQualifiedType node) {
		return false;
	}

	@Override
	public boolean visit(PrimitiveType node) {
		return false;
	}

	@Override
	public boolean visit(QualifiedType node) {
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		return false;
	}

	@Override
	public boolean visit(WildcardType node) {
		return false;
	}

	private boolean isAPI() {
		boolean result = false;
		return result;
	}

	// need to override
	private boolean isAPI(ITypeBinding tb) {
		boolean check = false;
		return check;
	}

	private String resolveType(ITypeBinding tb) {
		String result = "";
		if(tb==null){
			return "";
		}
		if (tb.isArray())
			return getQualifiedName(tb.getComponentType().getTypeDeclaration())
					+ getDimensions(tb.getDimensions());
		return tb.getQualifiedName();
	}

	private String getMethodSignature(MethodInvocation node) {
		return "";
	}

	// need to override
	private String getQualifiedName(ITypeBinding tb) {

		if (tb.isArray())
			return getQualifiedName(tb.getComponentType().getTypeDeclaration())
					+ getDimensions(tb.getDimensions());
		return tb.getQualifiedName();
	}

	private String getName(ITypeBinding tb) {
		if (tb.isArray())
			return getName(tb.getComponentType().getTypeDeclaration())
					+ getDimensions(tb.getDimensions());
		return tb.getName();
	}

	public String getS(ASTNode node) {
		String cachedResult = abstractSequences.get(node);
		String result = "";
		if (cachedResult != null) {
			return cachedResult;
		} else if (node instanceof SimpleName) {
			SimpleName obj = (SimpleName) node;
			IBinding b = obj.resolveBinding();
			if (b != null) {
				if (b instanceof IVariableBinding) {
					IVariableBinding vb = (IVariableBinding) b;
					ITypeBinding tb = vb.getType();
					if (tb != null) {
						tb = tb.getTypeDeclaration();
						if (tb.isLocal() || tb.getQualifiedName().isEmpty())
							return "";
						return resolveType(tb);
						// this.partialTokens.append(" " + getName(tb) + " ");
					}
				} else if (b instanceof ITypeBinding) {
					ITypeBinding tb = (ITypeBinding) b;
					tb = tb.getTypeDeclaration();
					if (tb.isLocal() || tb.getQualifiedName().isEmpty())
						return "";
					return getQualifiedName(tb);
				}
			} else {
				return "";
				// this.partialTokens.append(" " + node.getIdentifier() + " ");
			}
		} else if (node instanceof MethodInvocation) {
			MethodInvocation obj = (MethodInvocation) node;
			IMethodBinding b = obj.resolveMethodBinding();
			ITypeBinding tb = null;
			if (b != null) {
				tb = b.getDeclaringClass();
				if (tb != null) {
					tb = tb.getTypeDeclaration();
					if (tb.isLocal() || tb.getQualifiedName().isEmpty()) {
						result = "";
					} else {
						result = resolveType(tb);
					}
				}
			}

		} else if (node instanceof ClassInstanceCreation) {
			ClassInstanceCreation obj = (ClassInstanceCreation) node;
			ITypeBinding tb = obj.resolveTypeBinding();
			result = resolveType(tb);
		} else if (node instanceof ConstructorInvocation) {
			result = "this()";
		} else if (node instanceof SuperConstructorInvocation) {
			result = "super()";
		} else if (node instanceof QualifiedName) {
			QualifiedName obj = (QualifiedName) node;
			ITypeBinding tb = obj.resolveTypeBinding();
			result = resolveType(tb) + "|||"
					+ resolveType(obj.getName().resolveTypeBinding())+"|||"+obj.getName().getIdentifier();
		} else if (node instanceof FieldAccess) {
			// ReturnType(E)|||S(e).f
			FieldAccess obj = (FieldAccess) node;
			ITypeBinding tb = obj.resolveTypeBinding();
			IVariableBinding vb = obj.resolveFieldBinding();
			result = resolveType(tb) + "|||"
					+ resolveType(vb.getDeclaringClass())
					+ obj.getName().getIdentifier();

		} else if (node instanceof ArrayAccess) {
			// S(a)[n]
			ArrayAccess obj = (ArrayAccess) node;
			Expression exp = obj.getArray();
			// In definition, ArrayAccess has only one dimension
			result = resolveType(exp.resolveTypeBinding()) + "[1]";
		} else if (node instanceof CastExpression) {
			CastExpression obj = (CastExpression) node;
			// (Type(TypeName))S(e)
			result = "(" + resolveType(obj.getType().resolveBinding()) + ")"
					+ resolveType(obj.getExpression().resolveTypeBinding());
		} else if (node instanceof StringLiteral) {
			result = "String#lit";
		} else if (node instanceof NumberLiteral) {
			result = "Number#lit";
		} else if (node instanceof CharacterLiteral) {
			result = "Character#lit";
		} else if (node instanceof TypeLiteral) {
			TypeLiteral obj = (TypeLiteral) node;
			result = resolveType(obj.resolveTypeBinding()) + ".class#lit";
		} else if (node instanceof BooleanLiteral) {
			result = "Boolean#lit";
		} else if (node instanceof NullLiteral) {
			result = "Null#lit";
		}
		abstractSequences.put(node, result);
		return result;
	}
	
	public String getReturnType(ASTNode node) {
		String result = "";
		ITypeBinding tb=null;
		if (node instanceof SimpleName) {
			SimpleName obj = (SimpleName) node;
			tb = obj.resolveTypeBinding();
			
		} else if (node instanceof MethodInvocation) {
			MethodInvocation obj = (MethodInvocation) node;
			tb = obj.resolveTypeBinding();
			
		} else if (node instanceof ClassInstanceCreation) {
			ClassInstanceCreation obj = (ClassInstanceCreation) node;
			tb = obj.resolveTypeBinding();
		} else if (node instanceof ConstructorInvocation) {
			result = "this()";
			return result;
		} else if (node instanceof SuperConstructorInvocation) {
			result = "super()";
			return result;
		} else if (node instanceof QualifiedName) {
			QualifiedName obj = (QualifiedName) node;
			tb = obj.resolveTypeBinding();
			
		} else if (node instanceof FieldAccess) {
			// ReturnType(E)|||S(e).f
			FieldAccess obj = (FieldAccess) node;
			tb = obj.resolveTypeBinding();
			

		} else if (node instanceof ArrayAccess) {
			// S(a)[n]
			ArrayAccess obj = (ArrayAccess) node;
			tb=obj.resolveTypeBinding();
		} else if (node instanceof CastExpression) {
			CastExpression obj = (CastExpression) node;
			tb=obj.resolveTypeBinding();
		} else if (node instanceof StringLiteral) {
			result = "String#lit";
			return result;
		} else if (node instanceof NumberLiteral) {
			result = "Number#lit";
			return result;
		} else if (node instanceof CharacterLiteral) {
			result = "Character#lit";
			return result;
		} else if (node instanceof TypeLiteral) {
			TypeLiteral obj = (TypeLiteral) node;
			tb=obj.resolveTypeBinding();
		} else if (node instanceof BooleanLiteral) {
			result = "Boolean#lit";
			return result;
		} else if (node instanceof NullLiteral) {
			result = "Null#lit";
			return result;
		}
		if(tb!=null){
			result=resolveType(tb);			
		}
		return result;
	}

}
