package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task.TestSheetHelperTask;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableContext;

public class TestSheetUtil {

	public static void copySheets(IProject project, Map<String, List<Object[]>> map, IRunnableContext runner) {
		TestSheetHelperTask task = new TestSheetHelperTask(project, map);
		try {
			if (runner != null) {
				runner.run(true, true, task);
			} else {
				task.run(new NullProgressMonitor());
			}
		} catch (InvocationTargetException e) {
			IStatus status = LogUtil.logWarn("test sheet copy error.", e);
			ErrorDialog.openError(null, "error", status.getMessage(), status);
		} catch (InterruptedException e) {
			// return Status.CANCEL_STATUS;
		}
	}
}
