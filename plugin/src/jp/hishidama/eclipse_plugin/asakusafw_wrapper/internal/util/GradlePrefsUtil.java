package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class GradlePrefsUtil {

	public static Properties loadText(IProject project) {
		return loadText(project, ".settings/com.asakusafw.asakusafw.prefs");
	}

	public static Properties loadText(IProject project, String path) {
		Properties properties = new Properties();

		IFile file = project.getFile(path);
		if (!file.exists()) {
			return properties;
		}
		try {
			InputStream is = file.getContents();
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				properties.load(reader);
				return properties;
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				e.printStackTrace();
				return properties;
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

	public static String getAsakusaFrameworkVersion(Properties properties) {
		if (properties == null) {
			return null;
		}

		return properties.getProperty("com.asaksuafw.asakusafw.asakusafwVersion");
	}
}
