package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

/**
 * dmdlファイルユーティリティー.
 * 
 * @since 2013.08.26
 */
public class DMDLFileUtil {

	private DMDLFileUtil() {
	}

	public static List<IFile> getDmdlFiles(IProject project) {
		if (project == null) {
			return Collections.emptyList();
		}
		List<IFile> list = new ArrayList<IFile>();
		collectDmdlFiles(list, project);
		return list;
	}

	public static List<IFile> getSelectionDmdlFiles() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
		return getSelectionDmdlFiles(selection);
	}

	public static List<IFile> getSelectionDmdlFiles(ISelection selection) {
		IProject project = null;

		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Set<IFile> set = new HashSet<IFile>();
			for (Iterator<?> i = ss.iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof IJavaElement) {
					IJavaElement java = (IJavaElement) obj;
					obj = java.getResource();
					if (project == null) {
						project = java.getJavaProject().getProject();
					}
				}
				if (obj instanceof IFile) {
					IFile file = (IFile) obj;
					if ("dmdl".equals(file.getFileExtension())) {
						set.add(file);
					} else {
						if (project == null) {
							project = file.getProject();
						}
					}
				} else if (obj instanceof IContainer) {
					collectDmdlFiles(set, (IContainer) obj);
					if (project == null) {
						project = ((IContainer) obj).getProject();
					}
				}
			}

			List<IFile> list = new ArrayList<IFile>(set);
			if (list.isEmpty()) {
				list = getDmdlFiles(project);
			}
			sort(list);
			return list;
		}

		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFileEditorInput finput = (IFileEditorInput) input;
				IFile file = finput.getFile();
				if ("dmdl".equals(file.getFileExtension())) {
					return Arrays.asList(file);
				}
			}
		}

		return Collections.emptyList();
	}

	private static void sort(List<IFile> list) {
		Collections.sort(list, new Comparator<IFile>() {
			@Override
			public int compare(IFile o1, IFile o2) {
				String s1 = o1.getFullPath().toPortableString();
				String s2 = o2.getFullPath().toPortableString();
				return s1.compareTo(s2);
			}
		});
	}

	private static void collectDmdlFiles(Collection<IFile> list, IContainer folder) {
		IResource[] members;
		try {
			members = folder.members();
		} catch (CoreException e) {
			return;
		}
		for (IResource r : members) {
			if (r instanceof IFile) {
				if ("dmdl".equals(r.getFileExtension())) {
					list.add((IFile) r);
				}
			} else if (r instanceof IContainer) {
				collectDmdlFiles(list, (IContainer) r);
			}
		}
	}
}
