package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task.TestSheetGenerateTask;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class TestSheetUtil {

	public static class SheetInfo {
		public String srcModelName;
		public String srcSheetName;
		public String dstBookName;
		public String dstSheetName;
	}

	public static void generateTestSheet(IProject project, String version, List<SheetInfo> names,
			IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		TestSheetGenerateTask task = new TestSheetGenerateTask(project, version, names);
		task.run(monitor);
	}
}
