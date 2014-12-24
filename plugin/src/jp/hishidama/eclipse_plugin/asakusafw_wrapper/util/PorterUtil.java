package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import java.util.HashSet;
import java.util.Set;

import jp.hishidama.eclipse_plugin.jdt.util.ReflectionUtil;
import jp.hishidama.eclipse_plugin.jdt.util.TypeUtil;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;

/**
 * Impoter/Expoterユーティリティー.
 * 
 * @since 2014.04.27
 */
public class PorterUtil {
	public static final String IMPORTER_NAME = "com.asakusafw.vocabulary.external.ImporterDescription";
	public static final String EXPORTER_NAME = "com.asakusafw.vocabulary.external.ExporterDescription";
	public static final String DIRECTIO_IMPORTER_NAME = "com.asakusafw.vocabulary.directio.DirectFileInputDescription";
	public static final String DIRECTIO_EXPORTER_NAME = "com.asakusafw.vocabulary.directio.DirectFileOutputDescription";
	public static final String WINDGATE_FS_IMPORTER_NAME = "com.asakusafw.vocabulary.windgate.FsImporterDescription";
	public static final String WINDGATE_FS_EXPORTER_NAME = "com.asakusafw.vocabulary.windgate.FsExporterDescription";
	public static final String WINDGATE_JDBC_IMPORTER_NAME = "com.asakusafw.vocabulary.windgate.JdbcImporterDescription";
	public static final String WINDGATE_JDBC_EXPORTER_NAME = "com.asakusafw.vocabulary.windgate.JdbcExporterDescription";

	public static final String DATA_FORMAT_NAME = "com.asakusafw.runtime.directio.DataFormat";
	public static final String MODEL_INPUT_NAME = "com.asakusafw.runtime.io.ModelInput";
	public static final String MODEL_OUTPUT_NAME = "com.asakusafw.runtime.io.ModelOutput";

	public static boolean isImporter(IType type) {
		return TypeUtil.isImplements(type, IMPORTER_NAME);
	}

	public static boolean isExporter(IType type) {
		return TypeUtil.isImplements(type, EXPORTER_NAME);
	}

	public static boolean isPorter(IType type) {
		return getPorterInterfaceName(type) != null;
	}

	public static String getPorterInterfaceName(IType type) {
		Set<String> set = new HashSet<String>(2);
		set.add(IMPORTER_NAME);
		set.add(EXPORTER_NAME);
		return TypeUtil.findImplements(type, set);
	}

	public static boolean isDataFormat(IType type) {
		return TypeUtil.isImplements(type, DATA_FORMAT_NAME);
	}

	public static boolean isModelInput(IType type) {
		return TypeUtil.isImplements(type, MODEL_INPUT_NAME);
	}

	public static boolean isModelOutput(IType type) {
		return TypeUtil.isImplements(type, MODEL_OUTPUT_NAME);
	}

	public static String getPorterOrFormatInterfaceName(IType type) {
		Set<String> set = new HashSet<String>(2);
		set.add(IMPORTER_NAME);
		set.add(EXPORTER_NAME);
		set.add(DATA_FORMAT_NAME);
		set.add(MODEL_INPUT_NAME);
		set.add(MODEL_OUTPUT_NAME);
		return TypeUtil.findImplements(type, set);
	}

	public static boolean isImPorterOrInput(String name) {
		return IMPORTER_NAME.equals(name) || DATA_FORMAT_NAME.equals(name) || MODEL_INPUT_NAME.equals(name);
	}

	public static boolean isExPorterOrOutput(String name) {
		return EXPORTER_NAME.equals(name) || DATA_FORMAT_NAME.equals(name) || MODEL_OUTPUT_NAME.equals(name);
	}

	public static String getModelClassName(IJavaProject javaProject, String porterClassName) {
		String name = getModelClassNameAst(javaProject, porterClassName);
		if (name != null) {
			return name;
		}
		return getModelClassNameReflection(javaProject, porterClassName);
	}

	private static String getModelClassNameReflection(IJavaProject javaProject, String porterClassName) {
		try {
			Class<?> clazz = ReflectionUtil.loadClass(javaProject, porterClassName);
			Object object = clazz.newInstance();
			Class<?> modelType = ReflectionUtil.get(object, "getModelType");
			return modelType.getName();
		} catch (Exception e) {
			return null;
		}
	}

	private static String getModelClassNameAst(IJavaProject javaProject, String porterClassName) {
		try {
			IType type = javaProject.findType(porterClassName);
			return getModelClassName(type);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getModelClassName(IType type) {
		if (type == null) {
			return null;
		}

		try {
			for (IMethod method : type.getMethods()) {
				if (method.getElementName().equals("getModelType")) {
					return getModelClassName(method);
				}
			}

			String superName = type.getSuperclassName();
			IType superType = TypeUtil.resolveType(superName, type);
			return getModelClassName(superType);
		} catch (Exception e) {
			return null;
		}
	}

	private static String getModelClassName(IMethod method) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(method.getCompilationUnit());
		try {
			ISourceRange range = method.getSourceRange();
			parser.setSourceRange(range.getOffset(), range.getLength());
		} catch (JavaModelException e) {
			// fall through
		}
		ASTNode node = parser.createAST(new NullProgressMonitor());
		FindModelTypeVisitor visitor = new FindModelTypeVisitor();
		node.accept(visitor);
		String simpleName = visitor.getModelTypeName();
		if (simpleName == null) {
			return null;
		}
		return TypeUtil.resolveTypeName(simpleName, method.getDeclaringType());
	}

	private static class FindModelTypeVisitor extends ASTVisitor {
		private String typeName;

		@Override
		public boolean visit(MethodDeclaration node) {
			String name = node.getName().getIdentifier();
			if (name.equals("getModelType")) {
				return true;
			}
			return false;
		}

		@Override
		public boolean visit(ReturnStatement node) {
			Expression expression = node.getExpression();
			if (expression instanceof TypeLiteral) {
				TypeLiteral literal = (TypeLiteral) expression;
				Type type = literal.getType();
				this.typeName = type.toString();
			}
			return false;
		}

		public String getModelTypeName() {
			return typeName;
		}
	}
}
