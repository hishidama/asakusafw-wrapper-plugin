package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.config;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;

public class AsakusaFramework052GradleConfigration extends AsakusaFrameworkGradleConfigration {

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
	public List<Library> getDefaultLibraries(IProject project) {
		return Collections.emptyList();
	}
}
