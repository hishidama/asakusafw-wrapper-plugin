package jp.hishidama.asakusafw_wrapper.batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.BatchDriver;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectBatchCompiler;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.BatchDescription;

// @see com.asakusafw.compiler.bootstrap.BatchCompilerDriver
// @see com.asakusafw.compiler.bootstrap.AllBatchCompilerDriver
public class BatchCompilerDriver {

	static final Logger LOG = LoggerFactory.getLogger(BatchCompilerDriver.class);

	/**
	 * プログラムエントリー.
	 * 
	 * @param args
	 *            コマンドライン引数
	 */
	public static void main(String... args) {
		BatchCompilerDriver driver = new BatchCompilerDriver();
		try {
			if (!driver.start(args)) {
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}
	}

	public boolean start(String[] args) throws Exception {
		if (args.length < 3) {
			System.err.println("args: property-file base-directory className...");
			return false;
		}
		String fileName = args[0];
		String baseDir = args[1];
		List<String> classNameList = getClassName(args);

		BatchCompilerArguments arguments = createBatchCompilerArguments(fileName);
		arguments.initialize();
		File outputDirectory = arguments.getCompiledSourceDirectory(baseDir);
		String packageName = arguments.getCompiledSourcePackage();
		Location hadoopWorkLocation = arguments.getHadoopWorkDirectory();
		File compilerWorkDirectory = arguments.getCompilerWorkDirectory(baseDir);
		List<File> linkingResources = Lists.create();
		String link = getLinkingResources();
		String plugin = getPluginLocations();

		if (link != null) {
			for (String s : link.split(File.pathSeparator)) {
				linkingResources.add(new File(s));
			}
		}
		List<URL> pluginLocations = Lists.create();
		if (plugin != null) {
			for (String s : plugin.split(File.pathSeparator)) {
				if (s.trim().isEmpty()) {
					continue;
				}
				try {
					File file = new File(s);
					if (!file.exists()) {
						throw new FileNotFoundException(file.getAbsolutePath());
					}
					URL url = file.toURI().toURL();
					pluginLocations.add(url);
				} catch (IOException e) {
					LOG.warn(MessageFormat.format("プラグイン{0}をロードできませんでした", s), e);
				}
			}
		}

		Set<String> errorBatches = Sets.create();
		boolean succeeded = true;
		for (String className : classNameList) {
			Class<? extends BatchDescription> batchDescription = loadIfBatchClass(className);
			if (batchDescription == null) {
				continue;
			}
			boolean singleSucceeded = compile(outputDirectory, batchDescription, packageName, hadoopWorkLocation,
					compilerWorkDirectory, linkingResources, pluginLocations);
			succeeded &= singleSucceeded;
			if (singleSucceeded) {
				LOG.info("{}をコンパイルしました", className);
			} else {
				errorBatches.add(className);
			}
		}
		if (succeeded) {
			LOG.info("バッチアプリケーションの生成が完了しました");
		} else {
			LOG.error("バッチをコンパイルする際にエラーが発生しました: {}", errorBatches);
		}
		return succeeded;
	}

	protected List<String> getClassName(String[] args) {
		List<String> list = Lists.create();
		for (int i = 2; i < args.length; i++) {
			list.add(args[i]);
		}
		return list;
	}

	protected BatchCompilerArguments createBatchCompilerArguments(String fileName) {
		return new BatchCompilerArguments(fileName);
	}

	protected String getLinkingResources() {
		return null;
	}

	protected String getPluginLocations() {
		return null;
	}

	protected static Class<? extends BatchDescription> loadIfBatchClass(String className) {
		try {
			Class<?> aClass = Class.forName(className);
			if (BatchDescription.class.isAssignableFrom(aClass) == false) {
				LOG.warn("{}はBatchDescriptionを継承していません", aClass.getName());
				return null;
			}
			if (aClass.isAnnotationPresent(Batch.class) == false) {
				LOG.warn("{}には@Batchの指定がありません", aClass.getName());
				return null;
			}
			return aClass.asSubclass(BatchDescription.class);
		} catch (ClassNotFoundException e) {
			LOG.debug("クラスのロードに失敗しました", e);
			return null;
		}
	}

	protected boolean compile(File outputDirectory, Class<? extends BatchDescription> batchDescription,
			String packageName, Location hadoopWorkLocation, File compilerWorkDirectory, List<File> linkingResources,
			final List<URL> pluginLibraries) {
		assert outputDirectory != null;
		assert batchDescription != null;
		assert packageName != null;
		assert hadoopWorkLocation != null;
		assert compilerWorkDirectory != null;
		assert linkingResources != null;
		try {
			BatchDriver analyzed = BatchDriver.analyze(batchDescription);
			if (analyzed.hasError()) {
				for (String diagnostic : analyzed.getDiagnostics()) {
					LOG.error(diagnostic);
				}
				return false;
			}

			String batchId = analyzed.getBatchClass().getConfig().name();
			ClassLoader serviceLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
				@Override
				public ClassLoader run() {
					URLClassLoader loader = new URLClassLoader(
							pluginLibraries.toArray(new URL[pluginLibraries.size()]), BatchCompilerDriver.class
									.getClassLoader());
					return loader;
				}
			});
			DirectBatchCompiler.compile(analyzed.getDescription(), packageName, hadoopWorkLocation, new File(
					outputDirectory, batchId), new File(compilerWorkDirectory, batchId), linkingResources,
					serviceLoader, FlowCompilerOptions.load(System.getProperties()));
			return true;
		} catch (Exception e) {
			LOG.error(MessageFormat.format("コンパイルはエラーにより中断しました ({0})", batchDescription.getName()), e);
			return false;
		}
	}
}
