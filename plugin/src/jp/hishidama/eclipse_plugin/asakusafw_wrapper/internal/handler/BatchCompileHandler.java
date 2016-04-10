package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.handler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task.BatchCompilerLaunchTask;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.property.AsakusafwBatchCompilerPropertyPage;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.util.BatchUtil;
import jp.hishidama.eclipse_plugin.util.JdtUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;

public class BatchCompileHandler extends AbstractHandler {

	private BatchList beforeTypeList = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!PlatformUI.getWorkbench().saveAllEditors(true)) {
			return null;
		}

		BatchList result = getBatchType(event);
		List<IType> typeList = result.typeList;
		List<IJavaElement> elements = result.elementList;
		if (typeList.isEmpty()) {
			if (beforeTypeList != null) {
				result = beforeTypeList;
				typeList = beforeTypeList.typeList;
				elements = beforeTypeList.elementList;
			}
		}
		if (typeList.isEmpty()) {
			MessageDialog.openInformation(null, "Batch compile", "クラスが見つかりませんでした。\nバッチクラスを選択してから実行して下さい。");
			return null;
		}
		beforeTypeList = result;

		String action = event.getParameter("jp.hishidama.asakusafwWrapper.command.batchCompile.action");
		if (action != null) {
			launchCompileBatchapp(elements, action);
			return null;
		}

		IProject project = typeList.get(0).getJavaProject().getProject();
		launchCompileJava(project);
		launchCompileBatch(typeList);

		return null;
	}

	private static class BatchList {
		public List<IType> typeList;
		public List<IJavaElement> elementList;
	}

	private BatchList getBatchType(ExecutionEvent event) {
		List<IJavaElement> elements = JdtUtil.getJavaElements(event);
		List<IType> typeList = new ArrayList<IType>(elements.size());
		for (IJavaElement element : elements) {
			List<IType> list = getBatchType(element);
			typeList.addAll(list);
		}

		BatchList result = new BatchList();
		result.typeList = typeList;
		result.elementList = elements;
		return result;
	}

	private List<IType> getBatchType(IJavaElement element) {
		if (element instanceof IType) {
			IType type = (IType) element;
			if (BatchUtil.isBatch(type) || BatchUtil.isIterativeBatch(type)) {
				return Arrays.asList(type);
			}
		} else if (element instanceof ICompilationUnit) {
			ICompilationUnit unit = (ICompilationUnit) element;
			try {
				for (IType type : unit.getTypes()) {
					if (BatchUtil.isBatch(type) || BatchUtil.isIterativeBatch(type)) {
						return Arrays.asList(type);
					}
				}
			} catch (JavaModelException e) {
				// fall through
			}
		} else {
			IResource resource = element.getResource();
			if (resource instanceof IContainer) {
				List<IType> list = getBatchType((IContainer) resource);
				if (!list.isEmpty()) {
					return list;
				}
			}
		}

		return Collections.emptyList();
	}

	private List<IType> getBatchType(IContainer container) {
		List<IType> list = new ArrayList<IType>();
		collectBatchType(list, container);
		return list;
	}

	private void collectBatchType(List<IType> list, IContainer container) {
		try {
			for (IResource member : container.members()) {
				if (member instanceof IFile) {
					ICompilationUnit unit = JdtUtil.getJavaUnit((IFile) member);
					if (unit != null) {
						for (IType type : unit.getTypes()) {
							if (BatchUtil.isBatch(type)) {
								list.add(type);
							}
						}
					}
				} else if (member instanceof IContainer) {
					collectBatchType(list, (IContainer) member);
				}
			}
		} catch (CoreException e) {
			// fall through
		}
	}

	void launchCompileJava(IProject project) throws ExecutionException {
		if (project == null) {
			return;
		}
		if (!AsakusafwBatchCompilerPropertyPage.getCompileJava(project)) {
			return;
		}

		launchAsakusafwTask("compileJava");
	}

	private void launchAsakusafwTask(String taskName, String... args) {
		StringBuilder sb = new StringBuilder(taskName);
		for (String arg : args) {
			sb.append(' ');
			sb.append(arg);
		}

		IServiceLocator serviceLocator = PlatformUI.getWorkbench();
		try {
			ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
			Command command = commandService.getCommand("com.asakusafw.shafu.ui.buildProject");
			Parameterization[] params = { new Parameterization(command.getParameter("taskNames"), sb.toString()) };
			ParameterizedCommand parametrizedCommand = new ParameterizedCommand(command, params);

			IHandlerService handlerService = (IHandlerService) serviceLocator.getService(IHandlerService.class);
			handlerService.executeCommand(parametrizedCommand, null);
		} catch (Exception e) {
			LogUtil.logWarn("Shafu execute error", e);
			MessageDialog.openWarning(null, "Batch compile : " + taskName,
					MessageFormat.format("Shafuの{0}コマンドの実行に失敗しました。\nShafuがインストールされているかどうか確認して下さい。", taskName));
		}
	}

	void launchCompileBatch(List<IType> typeList) {
		final BatchCompilerLaunchTask task = new BatchCompilerLaunchTask(typeList);

		WorkspaceJob job = new WorkspaceJob("Batch compile") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				// IWorkspace workspace = ResourcesPlugin.getWorkspace();
				// workspace.run(task, monitor);
				task.run(monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	void launchCompileBatchapp(List<IJavaElement> elements, String taskName) {
		CompileBatchLauncher launcher = new CompileBatchLauncher(taskName);
		launcher.launch(elements);
	}

	private class CompileBatchLauncher {
		private final String taskName;

		public CompileBatchLauncher(String taskName) {
			this.taskName = taskName;
		}

		public void launch(List<IJavaElement> elements) {
			for (IJavaElement element : elements) {
				if (element instanceof IJavaProject || element instanceof IPackageFragmentRoot) {
					launchAsakusafwTask(taskName);
					return;
				}
			}
			StringBuilder sb = new StringBuilder(128);
			for (IJavaElement element : elements) {
				String name = getBatchClassName(element);
				if (name != null) {
					if (sb.length() != 0) {
						sb.append(",");
					}
					sb.append(name);
				}
			}
			if (sb.length() > 0) {
				launchAsakusafwTask(taskName, "--update", sb.toString());
			}
		}

		private String getBatchClassName(IJavaElement element) {
			if (element instanceof IType) {
				IType type = (IType) element;
				if (BatchUtil.isBatch(type) || BatchUtil.isIterativeBatch(type)) {
					return type.getFullyQualifiedName();
				}
			} else if (element instanceof ICompilationUnit) {
				ICompilationUnit unit = (ICompilationUnit) element;
				try {
					for (IType type : unit.getTypes()) {
						if (BatchUtil.isBatch(type) || BatchUtil.isIterativeBatch(type)) {
							return type.getFullyQualifiedName();
						}
					}
				} catch (JavaModelException e) {
					// fall through
				}
			} else if (element instanceof IPackageFragment) {
				IPackageFragment pack = (IPackageFragment) element;
				return pack.getElementName() + ".*";
			}
			return null;
		}
	}
}
