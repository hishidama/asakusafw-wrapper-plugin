package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * dmdlファイルユーティリティー.
 * 
 * @since 2013.08.26
 */
public class DmdlFileUtil {

	private DmdlFileUtil() {
	}

	public static List<IFile> getDmdlFiles(IProject project) {
		if (project == null) {
			return Collections.emptyList();
		}
		List<IFile> list = new ArrayList<IFile>();
		collectDmdlFiles(list, project);
		return list;
	}

	private static void collectDmdlFiles(List<IFile> list, IContainer folder) {
		IResource[] members;
		try {
			members = folder.members();
		} catch (CoreException e) {
			return;
		}
		for (IResource r : members) {
			if (r instanceof IFile) {
				if (r.getFullPath().getFileExtension().equals("dmdl")) {
					list.add((IFile) r);
				}
			} else if (r instanceof IContainer) {
				collectDmdlFiles(list, (IContainer) r);
			}
		}
	}
}
