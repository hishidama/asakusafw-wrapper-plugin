package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class TestSheetHelperTask extends ParserWrapperTask implements IRunnableWithProgress {

	private Map<String, List<Object[]>> map;

	public TestSheetHelperTask(IProject project, Map<String, List<Object[]>> map) {
		super(project);
		this.map = map;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			cancelCheck(monitor);
			copySheets(monitor, map);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}

	public void copySheets(IProgressMonitor monitor, Map<String, List<Object[]>> map) throws Exception {
		monitor.beginTask("copy sheet", 100);
		try {
			cancelCheck(monitor);
			if (wrapper == null) {
				return;
			}
			wrapper.copySheets(map);
		} finally {
			monitor.done();
		}
	}
}
