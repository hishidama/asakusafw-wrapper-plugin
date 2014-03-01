package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Properties;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.config.AsakusafwProperties;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util.PomXmlUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public abstract class AsakusaFrameworkConfigration extends AsakusafwConfiguration {

	@Override
	public boolean acceptable(IProject project) {
		String pom = PomXmlUtil.loadText(project);
		String version = PomXmlUtil.getAsakusaFrameworkVersion(pom);
		if (version == null) {
			return false;
		}
		return containsVersion(version, getVersionMin(), getVersionMax());
	}

	@Override
	public String getDefaultBuildPropertiesPath() {
		return "build.properties";
	}

	@Override
	public AsakusafwProperties getAsakusafwProperties(IProject project, String propertyFilePath) throws IOException {
		Properties p = loadProperties(project, propertyFilePath);
		return new BuildProperties(p);
	}

	private static Properties loadProperties(IProject project, String fname) throws IOException {
		if (fname == null) {
			throw new FileNotFoundException("null");
		}

		IFile file = null;
		try {
			file = project.getFile(fname);
		} catch (Exception e) {
			LogUtil.logWarn("loadProperties()", e);
		}
		if (file == null || !file.exists()) {
			IStatus status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, MessageFormat.format(
					"not found property file. file={0}", fname));
			Activator.getDefault().getLog().log(status);
			throw new FileNotFoundException(fname);
		}

		InputStream is = null;
		Reader reader = null;
		try {
			is = file.getContents();
			String cs;
			try {
				cs = file.getCharset();
			} catch (Exception e) {
				cs = "UTF-8";
			}
			reader = new InputStreamReader(is, cs);
			Properties p = new Properties();
			p.load(reader);
			return p;
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
			throw new IOException(e);
		} catch (IOException e) {
			IStatus status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, MessageFormat.format(
					"property file read error. file={0}", fname), e);
			Activator.getDefault().getLog().log(status);
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LogUtil.logWarn(AsakusaFrameworkConfigration.class.getSimpleName(), e);
				}
			} else if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LogUtil.logWarn(AsakusaFrameworkConfigration.class.getSimpleName(), e);
				}
			}
		}
	}
}
