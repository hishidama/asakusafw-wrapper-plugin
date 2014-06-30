package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.util.DMDLFileUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.util.TestSheetUtil.SheetInfo;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class TestSheetGenerateTask extends ParserWrapperTask implements IRunnableWithProgress {

	private String version;
	private List<SheetInfo> names;

	public TestSheetGenerateTask(IProject project, String version, List<SheetInfo> names) {
		super(project);
		this.version = version;
		this.names = names;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("generate test sheet", 100);
		try {
			cancelCheck(monitor);
			List<IFile> list = DMDLFileUtil.getDmdlFiles(project);
			monitor.worked(10);

			generate(list, monitor);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}

	private void generate(List<IFile> files, IProgressMonitor monitor) throws Exception {
		cancelCheck(monitor);
		if (wrapper == null) {
			throw new Exception("DmdlParserWrapperの作成に失敗しました。");
		}
		wrapper.generateTestSheet(version, files, names);
	}
}
