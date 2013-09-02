package jp.hishidama.eclipse_plugin.asakusafw_wrapper.handler;

import java.lang.reflect.InvocationTargetException;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task.DMDLErrorCheckTask;
import jp.hishidama.eclipse_plugin.util.ProjectUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ui.PlatformUI;

public class DMDLErrorCheckHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!PlatformUI.getWorkbench().saveAllEditors(true)) {
			return null;
		}

		IProject project = ProjectUtil.getProject(event);
		if (project == null) {
			MessageDialog.openInformation(null, "DMDL error check", "IProjectが見つかりませんでした。\nプロジェクトを選択してから実行して下さい。");
			return null;
		}

		execute(project, new ProgressMonitorDialog(null));
		return null;
	}

	public void execute(IProject project, IRunnableContext runner) {
		final DMDLErrorCheckTask task = new DMDLErrorCheckTask(project);
		// IWorkbench workbench = PlatformUI.getWorkbench();
		// IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		try {
			if (runner != null) {
				runner.run(true, true, task);
			} else {
				task.run(new NullProgressMonitor());
			}
		} catch (InvocationTargetException e) {
			IStatus status = LogUtil.logWarn("DMDL error check error.", e);
			ErrorDialog.openError(null, "error", status.getMessage(), status);
		} catch (InterruptedException e) {
			// return Status.CANCEL_STATUS;
		}
	}
}
