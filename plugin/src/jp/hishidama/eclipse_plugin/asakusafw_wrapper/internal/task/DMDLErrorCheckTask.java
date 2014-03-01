package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.dmdl.DmdlParseErrorInfo;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.dmdl.DmdlParserWrapper;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.util.DMDLFileUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;

public class DMDLErrorCheckTask implements IRunnableWithProgress {

	public static final QualifiedName KEY = new QualifiedName(Activator.PLUGIN_ID, "DMDLErrorCheckTask.parser");

	private IProject project;

	private DmdlParserWrapper wrapper;

	public DMDLErrorCheckTask(IProject project) {
		this.project = project;
		this.wrapper = createWrapper(project);
	}

	protected DmdlParserWrapper createWrapper(IProject project) {
		IJavaProject jproject = JavaCore.create(project);
		if (jproject == null) {
			return null;
		}

		DmdlParserWrapper wrapper = null;
		try {
			wrapper = (DmdlParserWrapper) project.getSessionProperty(KEY);
			if (wrapper == null) {
				wrapper = new DmdlParserWrapper(jproject);
				project.setSessionProperty(KEY, wrapper);
			}
		} catch (CoreException e) {
			if (wrapper == null) {
				wrapper = new DmdlParserWrapper(jproject);
			}
		}
		return wrapper.isValid() ? wrapper : null;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("DMDL error check", 100);
		try {
			cancelCheck(monitor);
			List<IFile> list = DMDLFileUtil.getDmdlFiles(project);
			monitor.worked(10);

			cancelCheck(monitor);
			parse(new SubProgressMonitor(monitor, 90), list);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}

	protected void parse(IProgressMonitor monitor, List<IFile> files) throws InterruptedException {
		monitor.beginTask("", files.size());
		try {
			FileDocumentProvider provider = new FileDocumentProvider();
			checkMark(monitor, files, provider);
		} finally {
			monitor.done();
		}
	}

	protected void checkMark(IProgressMonitor monitor, List<IFile> files, FileDocumentProvider provider)
			throws InterruptedException {
		cancelCheck(monitor);
		if (wrapper == null) {
			return;
		}
		Map<URI, IFile> fileMap = new HashMap<URI, IFile>();
		for (IFile file : files) {
			monitor.subTask(file.getName());
			cancelCheck(monitor);
			try {
				file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
				fileMap.put(file.getLocationURI(), file);
			} catch (Exception e) {
			}
			monitor.worked(1);
		}
		List<DmdlParseErrorInfo> list = wrapper.parse(files);
		if (list != null) {
			cancelCheck(monitor);
			for (DmdlParseErrorInfo pe : list) {
				cancelCheck(monitor);
				IFile file = fileMap.get(pe.file);
				if (file != null) {
					IDocument document = getDocument(provider, file);
					createErrorMarker(file, document, pe);
				}
			}
		}
	}

	protected IDocument getDocument(FileDocumentProvider provider, IFile file) {
		FileEditorInput input = new FileEditorInput(file);
		try {
			provider.connect(input);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		return provider.getDocument(input);
	}

	protected void createErrorMarker(IFile file, IDocument document, DmdlParseErrorInfo pe) {
		try {
			int beginOffset = document.getLineOffset(pe.beginLine - 1) + pe.beginColumn - 1;
			int endOffset = document.getLineOffset(pe.endLine - 1) + pe.endColumn;

			IMarker marker = file.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.SEVERITY, pe.level); // IMarker.SEVERITY_ERROR
			marker.setAttribute(IMarker.MESSAGE, pe.message);
			// marker.setAttribute(IMarker.LINE_NUMBER, pe.beginLine - 1);
			marker.setAttribute(IMarker.CHAR_START, beginOffset);
			marker.setAttribute(IMarker.CHAR_END, endOffset);
			marker.setAttribute(IMarker.LOCATION,
					String.format("%d:%d-%d:%d", pe.beginLine, pe.beginColumn, pe.endLine, pe.endColumn));
			marker.setAttribute(IMarker.TRANSIENT, false);
		} catch (Exception e) {
			LogUtil.logWarn("DMDLErrorCheckTask#createErrorMarker() error.", e);
		}
	}

	protected void cancelCheck(IProgressMonitor monitor) throws InterruptedException {
		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}
	}
}
