package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util.ParserClassUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;
import org.osgi.framework.Bundle;

public class DmdlClasspathProvider extends StandardClasspathProvider {

	public static final String ID = "jp.hishidama.asakusafwWrapper.dmdl.dmdlClasspathProvider";

	@Override
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
		List<IRuntimeClasspathEntry> results = new ArrayList<IRuntimeClasspathEntry>();
		Collections.addAll(results, super.computeUnresolvedClasspath(configuration));
		getDmdlCompilerLibraries(results, configuration);
		IRuntimeClasspathEntry[] r = results.toArray(new IRuntimeClasspathEntry[results.size()]);
		return r;
	}

	private void getDmdlCompilerLibraries(List<IRuntimeClasspathEntry> results, ILaunchConfiguration configuration)
			throws CoreException {
		IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
		IProject project = javaProject.getProject();

		List<URL> list = new ArrayList<URL>();
		ParserClassUtil.readDmdlPrefsClassPath(list, project);
		ParserClassUtil.getClassPath(list, project);
		findMyClassPath(list, "resource/dmdlparser-caller.jar");

		for (URL url : list) {
			IPath path = new Path(url.getPath());
			results.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
		}
	}

	protected URL findMyClassPath(List<URL> list, String jarName) {
		try {
			Bundle bundle = Activator.getDefault().getBundle();
			IPath path = Path.fromPortableString(jarName);
			URL bundleUrl = FileLocator.find(bundle, path, null);
			URL url = FileLocator.resolve(bundleUrl);

			list.add(url);
			return url;
		} catch (Exception e) {
			LogUtil.logWarn("DmdlClasspathProvider#findMyClassPath() error.", e);
			return null;
		}
	}
}
