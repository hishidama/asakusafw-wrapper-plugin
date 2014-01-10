package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.config.AsakusafwProperties;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util.GradlePrefsUtil;

import org.eclipse.core.resources.IProject;

public class AsakusaFramework053GradleConfigration extends AsakusafwConfiguration {

	@Override
	public String getConfigurationName() {
		return "Asakusa Framework 0.5.3 gradle";
	}

	@Override
	public String getVersionMin() {
		return "0.5.3";
	}

	@Override
	public String getVersionMax() {
		return "ANY";
	}

	@Override
	public boolean acceptable(IProject project) {
		Properties gradle = GradlePrefsUtil.loadText(project);
		String version = GradlePrefsUtil.getAsakusaFrameworkVersion(gradle);
		if (version == null) {
			return false;
		}
		return containsVersion(version, getVersionMin(), getVersionMax());
	}

	@Override
	public String getDefaultBuildPropertiesPath() {
		return ".settings/com.asakusafw.asakusafw.prefs";
	}

	@Override
	public List<Library> getDefaultLibraries(IProject project) {
		return Collections.emptyList();
	}

	@Override
	public AsakusafwProperties getAsakusafwProperties(IProject project, String propertyFilePath) throws IOException {
		Properties gradle = GradlePrefsUtil.loadText(project, propertyFilePath);
		return new GradlePrefs(gradle);
	}
}
