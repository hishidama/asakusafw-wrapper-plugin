package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.config;

import java.util.Properties;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.config.AsakusafwProperties;

/**
 * .settings/com.asakusafw.asakusafw.prefs Gradleプロパティー.
 */
public class GradlePrefs extends AsakusafwProperties {

	private Properties properties;

	public GradlePrefs(Properties properties) {
		if (properties != null) {
			this.properties = properties;
		} else {
			this.properties = new Properties();
		}
	}

	@Override
	public String getPackageDefault() {
		return getProperty("com.asaksuafw.asakusafw.compiler.compiledSourcePackage");
	}

	@Override
	public String getDmdlDir() {
		return getProperty("com.asaksuafw.asakusafw.dmdl.dmdlSourceDirectory");
	}

	@Override
	public String getModelgenOutput() {
		return getProperty("com.asaksuafw.asakusafw.modelgen.modelgenSourceDirectory");
	}

	@Override
	public String getModelgenPackage() {
		return getProperty("com.asaksuafw.asakusafw.modelgen.modelgenSourcePackage");
	}

	@Override
	public String getDmdlEncoding() {
		return getProperty("com.asaksuafw.asakusafw.dmdl.dmdlEncoding");
	}

	@Override
	public String getTestDataSheetOutput() {
		return getProperty("com.asaksuafw.asakusafw.testtools.testDataSheetDirectory");
	}

	private String getProperty(String key) {
		return properties.getProperty(key);
	}
}
