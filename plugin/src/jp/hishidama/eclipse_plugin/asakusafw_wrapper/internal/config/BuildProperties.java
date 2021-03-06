package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.config;

import java.util.Properties;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.config.AsakusafwProperties;

public class BuildProperties extends AsakusafwProperties {

	private Properties properties;

	public BuildProperties(Properties properties) {
		this.properties = properties;
	}

	@Override
	public String getPackageDefault() {
		return getProperty("asakusa.package.default");
	}

	@Override
	public String getDmdlDir() {
		return getProperty("asakusa.dmdl.dir");
	}

	@Override
	public String getModelgenOutput() {
		return getProperty("asakusa.modelgen.output");
	}

	@Override
	public String getModelgenPackage() {
		return getProperty("asakusa.modelgen.package");
	}

	@Override
	public String getDmdlEncoding() {
		return getProperty("asakusa.dmdl.encoding");
	}

	@Override
	public String getTestDataSheetOutput() {
		return getProperty("asakusa.testdatasheet.output");
	}

	@Override
	public String getCompiledSourceDirectory() {
		return getProperty("asakusa.batchc.dir");
	}

	@Override
	public String getCompiledSourcePackage() {
		return getProperty("asakusa.package.default");
	}

	@Override
	public String getHadoopWorkDirectory() {
		return getProperty("asakusa.hadoopwork.dir");
	}

	@Override
	public String getCompilerWorkDirectory() {
		return getProperty("asakusa.compilerwork.dir");
	}

	private String getProperty(String key) {
		if (properties == null) {
			return null;
		}
		return properties.getProperty(key);
	}
}
