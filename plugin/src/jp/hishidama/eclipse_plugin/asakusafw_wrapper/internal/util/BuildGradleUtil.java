package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class BuildGradleUtil {

	public static List<String> loadText(IProject project) {
		return loadText(project, "build.gradle");
	}

	public static List<String> loadText(IProject project, String path) {
		IFile file = project.getFile(path);
		if (!file.exists()) {
			return Collections.emptyList();
		}
		try {
			List<String> list = new ArrayList<String>();

			InputStream is = file.getContents();
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				for (;;) {
					String line = r.readLine();
					if (line == null) {
						break;
					}
					if (line.contains("asakusafw") || line.contains("SourcePackage")) {
						list.add(line);
					}
				}
				return list;
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				e.printStackTrace();
				return list;
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getAsakusaFrameworkVersion(List<String> list) {
		if (list == null) {
			return null;
		}
		for (String s : list) {
			String[] ss = s.split("=");
			if (ss.length > 1) {
				if (ss[0].trim().equals("asakusafwVersion")) {
					return ss[1].replace("'", "").trim();
				}
			}
		}
		return null;
	}
}
