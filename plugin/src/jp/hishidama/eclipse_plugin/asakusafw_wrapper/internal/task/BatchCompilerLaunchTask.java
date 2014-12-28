package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.util.BuildPropertiesUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public class BatchCompilerLaunchTask implements IWorkspaceRunnable {
	private IProject project;
	private List<IType> typeList;

	public BatchCompilerLaunchTask(List<IType> typeList) {
		this.project = typeList.get(0).getJavaProject().getProject();
		this.typeList = typeList;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Batch Compile", 100);
		try {
			String arguments = createArguments(new SubProgressMonitor(monitor, 10));
			if (arguments == null) {
				return;
			}
			ILaunchConfiguration config = createConfiguration(new SubProgressMonitor(monitor, 10), arguments);
			launch(new SubProgressMonitor(monitor, 70), config, false);
			refresh(new SubProgressMonitor(monitor, 10));
			return;
		} catch (CoreException e) {
			e.printStackTrace();
			throw e;
		} finally {
			monitor.done();
		}
	}

	private String createArguments(IProgressMonitor monitor) throws CoreException {
		assert monitor != null;
		monitor.beginTask("create arguments", 3);
		try {
			monitor.subTask("create arguments");
			checkCancel(monitor);

			StringBuilder sb = new StringBuilder(256);
			{
				String fileName = BuildPropertiesUtil.getBuildPropertiesFileName(project);
				if (fileName == null) {
					return null;
				}
				IFile file = project.getFile(fileName);
				String path = file.getRawLocation().toOSString();

				sb.append('"');
				sb.append(path);
				sb.append('"');
				monitor.worked(1);
			}
			{
				sb.append(' ');

				String path = project.getLocation().toOSString();
				sb.append('"');
				sb.append(path);
				sb.append('"');
				monitor.worked(1);
			}
			{
				for (IType type : typeList) {
					sb.append(' ');
					sb.append(type.getFullyQualifiedName());
				}
				monitor.worked(1);
			}
			return sb.toString();
		} finally {
			monitor.done();
		}
	}

	private ILaunchConfigurationWorkingCopy createConfiguration(IProgressMonitor monitor, String programArguments)
			throws CoreException {
		monitor.beginTask("create configuration", 1);
		try {
			monitor.subTask("create configuration");
			checkCancel(monitor);

			String mainClassName = "jp.hishidama.asakusafw_wrapper.batch.BatchCompilerDriver";
			String classpathProviderId = DmdlClasspathProvider.ID;
			IPath workingDirectory = project.getLocation();
			String vmArguments = "";

			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			ILaunchConfigurationWorkingCopy config = type.newInstance(null, Activator.PLUGIN_ID);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, true);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, classpathProviderId);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainClassName);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArguments);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, programArguments);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workingDirectory.toOSString());
			return config;
		} finally {
			monitor.done();
		}
	}

	private void launch(IProgressMonitor monitor, ILaunchConfiguration config, boolean debugMode) throws CoreException {
		monitor.beginTask("launch", 100);
		try {
			monitor.subTask("launch Batch compiler");
			checkCancel(monitor);

			String mode = debugMode ? ILaunchManager.DEBUG_MODE : ILaunchManager.RUN_MODE;
			boolean build = false;
			boolean register = false;
			ILaunch launch = config.launch(mode, new SubProgressMonitor(monitor, 20), build, register);
			if (!launch.hasChildren()) {
				throw new OperationCanceledException();
			}

			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			launchManager.addLaunch(launch);

			monitor.worked(10);
			while (!launch.isTerminated()) {
				checkCancel(monitor);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					launch.terminate();
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			}
			launch.terminate();

			for (IProcess process : launch.getProcesses()) {
				int r = process.getExitValue();
				if (r != 0) {
					String message = MessageFormat.format(
							"Batch compile failed. exitValue={0},\nconfig={1},\nclassPath={2}", r,
							config.getAttributes(),
							Arrays.toString(new DmdlClasspathProvider().computeUnresolvedClasspath(config)));
					IStatus status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, message, null);
					Activator.getDefault().getLog().log(status);
				}
			}
		} finally {
			monitor.done();
		}
	}

	protected void refresh(IProgressMonitor monitor) {
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (CoreException e) {
			LogUtil.logWarn("refresh error.", e);
		}
	}

	private void checkCancel(IProgressMonitor monitor) throws OperationCanceledException {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}
}
