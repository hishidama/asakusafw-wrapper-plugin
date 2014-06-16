package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class TestSheetHelperTask extends ParserWrapperTask implements IRunnableWithProgress {

	private Map<String, Object[]> map;

	public TestSheetHelperTask(IProject project) {
		super(project);
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			cancelCheck(monitor);
			getRuleItem(monitor);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}

	private void getRuleItem(IProgressMonitor monitor) throws Exception {
		monitor.beginTask("get rule validation", 100);
		try {
			cancelCheck(monitor);
			if (wrapper == null) {
				throw new Exception("DmdlParserWrapperの作成に失敗しました。");
			}
			this.map = wrapper.getRuleItem();
		} finally {
			monitor.done();
		}
	}

	public Map<String, Object[]> getRuleItem() {
		return map;
	}
}
