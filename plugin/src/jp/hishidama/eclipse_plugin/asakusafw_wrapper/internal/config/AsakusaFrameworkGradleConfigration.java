package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.config;

import java.io.IOException;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.config.AsakusafwProperties;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util.BuildGradleUtil;

import org.eclipse.core.resources.IProject;

public abstract class AsakusaFrameworkGradleConfigration extends AsakusafwConfiguration {

	@Override
	public boolean acceptable(IProject project) {
		List<String> gradle = BuildGradleUtil.loadText(project);
		String version = BuildGradleUtil.getAsakusaFrameworkVersion(gradle);
		if (version == null) {
			return false;
		}
		return version.startsWith(getVersionPrefix());
	}

	protected abstract String getVersionPrefix();

	@Override
	public String getDefaultBuildPropertiesPath() {
		return "build.gradle";
	}

	@Override
	public AsakusafwProperties getAsakusafwProperties(IProject project, String propertyFilePath) throws IOException {
		List<String> gradle = BuildGradleUtil.loadText(project, propertyFilePath);
		return new EasyBuildGradle(gradle);
	}
}
