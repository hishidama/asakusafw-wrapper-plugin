package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.dmdl.DmdlParserWrapper;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public abstract class ParserWrapperTask {
	public static final QualifiedName PASER_KEY = new QualifiedName(Activator.PLUGIN_ID, "ParserWrapperTask.parser");
	public static final QualifiedName TIME_KEY = new QualifiedName(Activator.PLUGIN_ID, "ParserWrapperTask.parser.time");

	protected final IProject project;

	protected final DmdlParserWrapper wrapper;

	public ParserWrapperTask(IProject project) {
		this.project = project;
		this.wrapper = createWrapper(project);
	}

	private DmdlParserWrapper createWrapper(IProject project) {
		IJavaProject jproject = JavaCore.create(project);
		if (jproject == null) {
			return null;
		}

		DmdlParserWrapper wrapper = null;
		try {
			IFile file = project.getFile(".classpath");
			long time = file.getLocalTimeStamp();
			Long cache = (Long) project.getSessionProperty(TIME_KEY);

			wrapper = (DmdlParserWrapper) project.getSessionProperty(PASER_KEY);
			if (wrapper == null || (cache != null && cache < time)) {
				wrapper = new DmdlParserWrapper(jproject);
				project.setSessionProperty(PASER_KEY, wrapper);
				project.setSessionProperty(TIME_KEY, time);
			}
		} catch (CoreException e) {
			if (wrapper == null) {
				wrapper = new DmdlParserWrapper(jproject);
			}
		}
		return wrapper.isValid() ? wrapper : null;
	}

	protected final void cancelCheck(IProgressMonitor monitor) throws InterruptedException {
		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}
	}
}
