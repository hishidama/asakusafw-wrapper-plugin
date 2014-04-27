package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import java.util.HashSet;
import java.util.Set;

import jp.hishidama.eclipse_plugin.jdt.util.ReflectionUtil;
import jp.hishidama.eclipse_plugin.jdt.util.TypeUtil;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

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

	public static String getModelClassName(IJavaProject javaProject, String porterClassName) {
		try {
			Class<?> clazz = ReflectionUtil.loadClass(javaProject, porterClassName);
			Object object = clazz.newInstance();
			Class<?> modelType = ReflectionUtil.get(object, "getModelType");
			return modelType.getName();
		} catch (Exception e) {
			return null;
		}
	}
}
