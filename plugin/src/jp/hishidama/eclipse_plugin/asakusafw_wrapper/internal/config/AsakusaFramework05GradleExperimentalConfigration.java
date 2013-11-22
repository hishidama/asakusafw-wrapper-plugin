package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.config;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;

public class AsakusaFramework05GradleExperimentalConfigration extends AsakusaFrameworkGradleConfigration {

	@Override
	public String getConfigurationName() {
		return "Asakusa Framework 0.5 gradle(experimental)";
	}

	@Override
	protected String getVersionPrefix() {
		return "0.5";
	}

	@Override
	public List<Library> getDefaultLibraries(IProject project) {
		return Collections.emptyList();
	}
}
