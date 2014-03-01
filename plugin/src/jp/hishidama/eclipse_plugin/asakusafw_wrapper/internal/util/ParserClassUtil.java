package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration.Library;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;

public class ParserClassUtil {

	public static final String CONFIGURATION_NAME = "AsakusafwWrapper.Configuration.name";
	public static final String PARSER_BUILD_PROPERTIES = "AsakusafwWrapper.Parser.buildProperties";
	public static final String PARSER_JAR_FILE_COUNT = "AsakusafwWrapper.Parser.jarFile.count";
	public static final String PARSER_JAR_FILE = "AsakusafwWrapper.Parser.jarFile.";

	public static void getClassPath(List<URL> list, IProject project) {
		List<Library> libs = getLibraries(project);

		for (Library lib : libs) {
			if (lib.selected) {
				try {
					IPath pp = Path.fromPortableString(lib.path);
					URL url = toURL(project, pp);
					if (url != null) {
						list.add(url);
					}
				} catch (MalformedURLException e) {
					LogUtil.logWarn(ParserClassUtil.class.getSimpleName(), e);
				}
			}
		}
	}

	private static URL toURL(IProject project, IPath path) throws MalformedURLException {
		if (path == null) {
			return null;
		}
		if (path.toFile().exists()) {
			URL url = path.toFile().toURI().toURL();
			return url;
		}
		IPath vp = JavaCore.getResolvedVariablePath(path);
		if (vp != null) {
			URL url = vp.toFile().toURI().toURL();
			return url;
		}
		try {
			IFile file = project.getFile(path);
			if (file.exists()) {
				URI uri = file.getLocationURI();
				if (uri != null) {
					return uri.toURL();
				}
			}
		} catch (Exception e) {
			LogUtil.logWarn("toURL()", e);
		}
		try {
			IFile file = project.getParent().getFile(path);
			if (file.exists()) {
				URI uri = file.getLocationURI();
				if (uri != null) {
					return uri.toURL();
				}
			}
		} catch (Exception e) {
			LogUtil.logWarn("toURL()", e);
		}
		return null;
	}

	public static void getProjectClassPath(List<URL> list, IJavaProject project) {
		try {
			IClasspathEntry[] cp = project.getRawClasspath();
			getClassPath(list, project, cp);
		} catch (JavaModelException e) {
			LogUtil.logWarn("getProjectClassPath()", e);
		}
	}

	private static void getClassPath(List<URL> list, IJavaProject project, IClasspathEntry[] cp) {
		for (IClasspathEntry ce : cp) {
			URL url = null;
			try {
				switch (ce.getEntryKind()) {
				case IClasspathEntry.CPE_SOURCE:
					url = toURL(project.getProject(), ce.getOutputLocation());
					break;
				case IClasspathEntry.CPE_VARIABLE:
					url = toURL(project.getProject(), JavaCore.getResolvedVariablePath(ce.getPath()));
					break;
				case IClasspathEntry.CPE_LIBRARY:
					url = toURL(project.getProject(), ce.getPath());
					break;
				case IClasspathEntry.CPE_CONTAINER:
					if (!ce.getPath().toPortableString().contains("JRE_CONTAINER")) {
						IClasspathContainer cr = JavaCore.getClasspathContainer(ce.getPath(), project);
						getClassPath(list, project, cr.getClasspathEntries());
					}
					break;
				default:
					break;
				}
			} catch (Exception e) {
				LogUtil.logWarn("getClassPath()", e);
			}
			if (url != null) {
				list.add(url);
			}
		}
	}

	public static void setConfigurationName(IProject project, String cname) {
		setValue(project, CONFIGURATION_NAME, cname);
	}

	public static String getConfigurationName(IProject project) {
		initializeProjectParser(project);

		String cname = getValue(project, CONFIGURATION_NAME);
		return cname;
	}

	public static void setLibraries(IProject project, List<Library> list) {
		setIntValue(project, PARSER_JAR_FILE_COUNT, list.size());

		int i = 0;
		for (Library lib : list) {
			setValue(project, PARSER_JAR_FILE + i, lib.toString());
			i++;
		}
	}

	public static List<Library> getLibraries(IProject project) {
		initializeProjectParser(project);

		List<Library> libs = new ArrayList<Library>();

		Integer sizeObject = getIntValue(project, PARSER_JAR_FILE_COUNT);
		int size = (sizeObject != null) ? sizeObject : 0;
		for (int i = 0; i < size; i++) {
			String lib = getValue(project, PARSER_JAR_FILE + i);
			if (lib != null) {
				libs.add(Library.valudOf(lib));
			}
		}
		return libs;
	}

	public static void initializeProjectParser(IProject project) {
		final String key = "ParserClassPath.initialize";
		{
			String s = getValue(project, key);
			if (nonEmpty(s)) {
				return;
			}
		}

		AsakusafwConfiguration c = null;
		{
			String cname = getValue(project, CONFIGURATION_NAME);
			if (isEmpty(cname)) {
				c = getDefaultConfiguration(project);
				if (c != null) {
					try {
						cname = c.getConfigurationName();
					} catch (Exception e) {
						LogUtil.logError("initializeProjectParser()", e);
					}
				}
				if (cname == null) {
					cname = "";
				}
				setConfigurationName(project, cname);
			} else {
				c = getConfiguration(cname);
			}
		}

		String bpath = null;
		List<Library> libs = null;
		if (c != null) {
			try {
				bpath = c.getDefaultBuildPropertiesPath();
			} catch (Exception e) {
				LogUtil.logWarn("initializeProjectParser()", e);
			}
			try {
				libs = c.getDefaultLibraries(project);
			} catch (Exception e) {
				LogUtil.logWarn("initializeProjectParser()", e);
			}
		}
		if (bpath == null) {
			bpath = "";
		}
		setValue(project, PARSER_BUILD_PROPERTIES, bpath);

		if (libs == null) {
			libs = Collections.emptyList();
		}
		setLibraries(project, libs);

		setValue(project, key, "initialized");
	}

	public static String getValue(IProject project, String key) {
		try {
			String value = project.getPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, key));
			if (value != null) {
				return value;
			}
			return getDefaultValue(project, key);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
			return null;
		}
	}

	private static Integer getIntValue(IProject project, String key) {
		try {
			String value = project.getPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, key));
			if (value != null) {
				return Integer.valueOf(value);
			}
			return getDefaultIntValue(project, key);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
			return null;
		}
	}

	private static String getDefaultValue(IProject project, String key) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getString(key);
	}

	private static Integer getDefaultIntValue(IProject project, String key) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getInt(key);
	}

	public static void setValue(IProject project, String key, String value) {
		try {
			project.setPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, key), value);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	private static void setIntValue(IProject project, String key, int value) {
		try {
			project.setPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, key), Integer.toString(value));
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	public static AsakusafwConfiguration getConfiguration(IProject project) {
		String cname = getConfigurationName(project);
		return getConfiguration(cname);
	}

	private static AsakusafwConfiguration getConfiguration(String cname) {
		if (cname == null) {
			return null;
		}
		List<AsakusafwConfiguration> list = Activator.getExtensionLoader().getConfigurations();
		for (AsakusafwConfiguration c : list) {
			if (cname.equals(c.getConfigurationName())) {
				return c;
			}
		}
		return null;
	}

	public static AsakusafwConfiguration getDefaultConfiguration(IProject project) {
		List<AsakusafwConfiguration> list = new ArrayList<AsakusafwConfiguration>();

		for (AsakusafwConfiguration c : Activator.getExtensionLoader().getConfigurations()) {
			if (c.acceptable(project)) {
				list.add(c);
			}
		}
		if (list.isEmpty()) {
			return null;
		}
		if (list.size() == 1) {
			return list.get(0);
		}

		Collections.sort(list, new Comparator<AsakusafwConfiguration>() {
			@Override
			public int compare(AsakusafwConfiguration o1, AsakusafwConfiguration o2) {
				String min1 = o1.getVersionMin();
				String min2 = o2.getVersionMin();
				int c = AsakusafwConfiguration.compareVersion(min1, min2);
				if (c != 0) {
					return c;
				}

				String max1 = o1.getVersionMax();
				String max2 = o2.getVersionMax();
				return AsakusafwConfiguration.compareVersion(max1, max2);
			}
		});
		return list.get(list.size() - 1);
	}

	public static List<Library> getDefaultLibraries(IProject project) {
		AsakusafwConfiguration c = getConfiguration(project);
		return getDefaultLibraries(project, c);
	}

	public static List<Library> getDefaultLibraries(IProject project, AsakusafwConfiguration c) {
		if (c == null) {
			return Collections.emptyList();
		}
		try {
			List<Library> list = c.getDefaultLibraries(project);
			if (list == null) {
				return Collections.emptyList();
			}
			return list;
		} catch (Exception e) {
			LogUtil.logError("getDefaultLibraries()", e);
			return Collections.emptyList();
		}
	}

	private static boolean nonEmpty(String s) {
		return s != null && !s.isEmpty();
	}

	private static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
}
