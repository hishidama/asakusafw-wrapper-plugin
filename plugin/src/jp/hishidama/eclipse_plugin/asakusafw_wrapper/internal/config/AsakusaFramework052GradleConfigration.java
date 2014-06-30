package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.config.AsakusafwProperties;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util.BuildGradleUtil;

import org.eclipse.core.resources.IProject;

public class AsakusaFramework052GradleConfigration extends AsakusafwConfiguration {

	@Override
	public String getConfigurationName() {
		return "Asakusa Framework 0.5.2 gradle(experimental)";
	}

	@Override
	public String getVersionMin() {
		return "0.5.2";
	}

	@Override
	public String getVersionMax() {
		return "ANY";
	}

	@Override
	public String getCurrentVersion(IProject project) {
		List<String> gradle = BuildGradleUtil.loadText(project);
		String version = BuildGradleUtil.getAsakusaFrameworkVersion(gradle);
		return version;
	}

	@Override
	public String getDefaultBuildPropertiesPath() {
		return "build.gradle";
	}

	@Override
	public List<Library> getDefaultLibraries(IProject project) {
		return Collections.emptyList();
	}

	@Override
	public AsakusafwProperties getAsakusafwProperties(IProject project, String propertyFilePath) throws IOException {
		List<String> gradle = BuildGradleUtil.loadText(project, propertyFilePath);
		return new EasyBuildGradle(gradle);
	}
}
