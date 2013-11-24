package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.config.AsakusafwProperties;

/**
 * build.gradleプロパティー簡易版.
 */
public class EasyBuildGradle extends AsakusafwProperties {

	private Map<String, String> properties = new HashMap<String, String>();

	public EasyBuildGradle(List<String> properties) {
		if (properties == null) {
			return;
		}
		for (String s : properties) {
			String[] ss = s.trim().split("[ \t]+");
			if (ss.length >= 2) {
				String key = ss[0].trim();
				String value = ss[1].replace("'", "").trim();
				this.properties.put(key, value);
			}
		}
	}

	@Override
	public String getPackageDefault() {
		return getProperty("compiledSourcePackage");
	}

	@Override
	public String getDmdlDir() {
		return "src/main/dmdl";
	}

	@Override
	public String getModelgenOutput() {
		return "build/generated-sources/modelgen";
	}

	@Override
	public String getModelgenPackage() {
		return getProperty("modelgenSourcePackage");
	}

	@Override
	public String getDmdlEncoding() {
		return "UTF-8";
	}

	private String getProperty(String key) {
		return properties.get(key);
	}
}
