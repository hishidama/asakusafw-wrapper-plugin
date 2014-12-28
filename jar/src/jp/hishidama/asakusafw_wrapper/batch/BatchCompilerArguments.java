package jp.hishidama.asakusafw_wrapper.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Properties;

import com.asakusafw.compiler.flow.Location;

public class BatchCompilerArguments {

	private final File file;
	private Delegate delegate;

	public BatchCompilerArguments(String fileName) {
		this.file = new File(fileName);
	}

	public void initialize() throws IOException {
		this.delegate = createDelegate(file);
		if (delegate == null) {
			throw new UnsupportedOperationException(MessageFormat.format("unsupported property-file. file={0}", file));
		}
	}

	protected Delegate createDelegate(File file) throws IOException {
		String name = file.getName();
		if ("com.asakusafw.asakusafw.prefs".equals(name)) {
			return new Prefs(file);
		} else if ("build.properties".equals(name)) {
			return new Build(file);
		} else if ("build.gradle".equals(name)) {
			return new Gradle(file);
		}
		return null;
	}

	public File getCompiledSourceDirectory(String baseDir) {
		return delegate.getCompiledSourceDirectory(baseDir);
	}

	public String getCompiledSourcePackage() {
		return delegate.getCompiledSourcePackage();
	}

	public Location getHadoopWorkDirectory() {
		return delegate.getHadoopWorkDirectory();
	}

	public File getCompilerWorkDirectory(String baseDir) {
		return delegate.getCompilerWorkDirectory(baseDir);
	}

	protected static interface Delegate {

		public File getCompiledSourceDirectory(String baseDir);

		public String getCompiledSourcePackage();

		public Location getHadoopWorkDirectory();

		public File getCompilerWorkDirectory(String baseDir);
	}

	protected class Build implements Delegate {
		protected Properties properties;

		public Build(File file) throws IOException {
			properties = new Properties();
			properties.load(new FileInputStream(file));
		}

		protected String getProperty(String key) {
			String value = properties.getProperty(key);
			if (value == null) {
				throw new IllegalStateException(MessageFormat.format("property not found. key={0}", key));
			}
			return value;
		}

		@Override
		public File getCompiledSourceDirectory(String baseDir) {
			String output = getProperty("asakusa.batchc.dir");
			File outputDirectory = new File(baseDir, output);
			return outputDirectory;
		}

		@Override
		public String getCompiledSourcePackage() {
			String packageName = getProperty("asakusa.package.default");
			return packageName;
		}

		@Override
		public Location getHadoopWorkDirectory() {
			String hadoopWork = getProperty("asakusa.hadoopwork.dir");
			Location hadoopWorkLocation = Location.fromPath(hadoopWork, '/');
			return hadoopWorkLocation;
		}

		@Override
		public File getCompilerWorkDirectory(String baseDir) {
			String compilerWork = getProperty("asakusa.compilerwork.dir");
			File compilerWorkDirectory = new File(baseDir, compilerWork);
			return compilerWorkDirectory;
		}
	}

	protected class Gradle implements Delegate {
		protected Properties properties;

		public Gradle(File file) throws IOException {
			properties = new Properties();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			try {
				for (;;) {
					String line = reader.readLine();
					if (line == null) {
						break;
					}
					if (line.contains("compiledSourcePackage")) {
						int n = line.indexOf('\'');
						if (n >= 0) {
							int m = line.indexOf('\'', n + 1);
							if (m >= 0) {
								String value = line.substring(n + 1, m);
								properties.setProperty("compiledSourcePackage", value);
							}
						}
					}
				}
			} finally {
				reader.close();
			}
		}

		protected String getProperty(String key) {
			String value = properties.getProperty(key);
			if (value == null) {
				throw new IllegalStateException(MessageFormat.format("property not found. key={0}", key));
			}
			return value;
		}

		@Override
		public File getCompiledSourceDirectory(String baseDir) {
			String output = "build/batchc";
			File outputDirectory = new File(baseDir, output);
			return outputDirectory;
		}

		@Override
		public String getCompiledSourcePackage() {
			String packageName = getProperty("compiledSourcePackage");
			return packageName;
		}

		@Override
		public Location getHadoopWorkDirectory() {
			String hadoopWork = "target/hadoopwork/${execution_id}";
			Location hadoopWorkLocation = Location.fromPath(hadoopWork, '/');
			return hadoopWorkLocation;
		}

		@Override
		public File getCompilerWorkDirectory(String baseDir) {
			String compilerWork = "build/tmp/compileBatchapp/build";
			File compilerWorkDirectory = new File(baseDir, compilerWork);
			return compilerWorkDirectory;
		}
	}

	protected class Prefs implements Delegate {
		protected Properties properties;

		public Prefs(File file) throws IOException {
			properties = new Properties();
			properties.load(new FileInputStream(file));
		}

		protected String getProperty(String key) {
			String value = properties.getProperty(key);
			if (value == null) {
				throw new IllegalStateException(MessageFormat.format("property not found. key={0}", key));
			}
			return value;
		}

		@Override
		public File getCompiledSourceDirectory(String baseDir) {
			String output = getProperty("com.asaksuafw.asakusafw.compiler.compiledSourceDirectory");
			File outputDirectory = new File(baseDir, output);
			return outputDirectory;
		}

		@Override
		public String getCompiledSourcePackage() {
			String packageName = getProperty("com.asaksuafw.asakusafw.compiler.compiledSourcePackage");
			return packageName;
		}

		@Override
		public Location getHadoopWorkDirectory() {
			String hadoopWork = getProperty("com.asaksuafw.asakusafw.compiler.hadoopWorkDirectory");
			Location hadoopWorkLocation = Location.fromPath(hadoopWork, '/');
			return hadoopWorkLocation;
		}

		@Override
		public File getCompilerWorkDirectory(String baseDir) {
			String compilerWork = getProperty("com.asaksuafw.asakusafw.compiler.compilerWorkDirectory");
			if (compilerWork.isEmpty()) {
				compilerWork = "build/tmp/compileBatchapp/build";
			}
			File compilerWorkDirectory = new File(baseDir, compilerWork);
			return compilerWorkDirectory;
		}
	}
}
