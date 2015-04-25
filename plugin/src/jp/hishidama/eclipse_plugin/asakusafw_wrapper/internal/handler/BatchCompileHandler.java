package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.handler;

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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;

public class BatchCompileHandler extends AbstractHandler {

	private List<IType> beforeTypeList = Collections.emptyList();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!PlatformUI.getWorkbench().saveAllEditors(true)) {
			return null;
		}

		List<IType> typeList = getBatchType(event);
		if (typeList.isEmpty()) {
			typeList = beforeTypeList;
		}
		if (typeList.isEmpty()) {
			MessageDialog.openInformation(null, "Batch compile", "クラスが見つかりませんでした。\nバッチクラスを選択してから実行して下さい。");
			return null;
		}
		beforeTypeList = typeList;

		IProject project = typeList.get(0).getJavaProject().getProject();
		launchCompileJava(project);
		launchCompileBatch(typeList);

		return null;
	}

	private List<IType> getBatchType(ExecutionEvent event) {
		List<IJavaElement> elements = JdtUtil.getJavaElements(event);
		List<IType> typeList = new ArrayList<IType>(elements.size());
		for (IJavaElement element : elements) {
			List<IType> list = getBatchType(element);
			typeList.addAll(list);
		}
		return typeList;
	}

	private List<IType> getBatchType(IJavaElement element) {
		if (element instanceof IType) {
			IType type = (IType) element;
			if (BatchUtil.isBatch(type)) {
				return Arrays.asList(type);
			}
		} else if (element instanceof ICompilationUnit) {
			ICompilationUnit unit = (ICompilationUnit) element;
			try {
				for (IType type : unit.getTypes()) {
					if (BatchUtil.isBatch(type)) {
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

		IServiceLocator serviceLocator = PlatformUI.getWorkbench();

		try {
			ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
			Command command = commandService.getCommand("com.asakusafw.shafu.ui.buildProject");
			Parameterization[] params = { new Parameterization(command.getParameter("taskNames"), "compileJava") };
			ParameterizedCommand parametrizedCommand = new ParameterizedCommand(command, params);

			IHandlerService handlerService = (IHandlerService) serviceLocator.getService(IHandlerService.class);
			handlerService.executeCommand(parametrizedCommand, null);
		} catch (Exception e) {
			LogUtil.logWarn("Shafu execute error", e);
			MessageDialog.openWarning(null, "Batch compile : compileJava",
					"ShafuのcompileJavaコマンドの実行に失敗しました。\nShafuがインストールされているかどうか確認して下さい。");
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
}
