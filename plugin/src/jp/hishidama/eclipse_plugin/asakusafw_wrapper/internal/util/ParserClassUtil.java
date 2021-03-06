package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util;

import static jp.hishidama.eclipse_plugin.util.StringUtil.isEmpty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration.Library;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.util.JdtUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;

public class ParserClassUtil {

	public static final String CONFIGURATION_NAME = "AsakusafwWrapper.Configuration.name";
	public static final String PARSER_BUILD_PROPERTIES = "AsakusafwWrapper.Parser.buildProperties";
	public static final String PARSER_JAR_FILE_COUNT = "AsakusafwWrapper.Parser.jarFile.count";
	public static final String PARSER_JAR_FILE = "AsakusafwWrapper.Parser.jarFile.";

	public static void readDmdlPrefsClassPath(List<URL> list, IProject project) {
		IFile file = project.getFile(".settings/com.asakusafw.dmdl.prefs");
		if (!file.exists()) {
			return;
		}

		Properties properties = new Properties();
		{
			InputStream is;
			try {
				is = file.getContents();
			} catch (CoreException e) {
				LogUtil.logWarn("ParserClassUtil#readDmdlPrefsClassPath() error", e);
				return;
			}
			try {
				properties.load(is);
			} catch (IOException e) {
				LogUtil.logWarn("ParserClassUtil#readDmdlPrefsClassPath() error", e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					LogUtil.logWarn("ParserClassUtil#readDmdlPrefsClassPath() error", e);
				}
			}
		}

		Map<Integer, String> map = new TreeMap<Integer, String>();
		final int INDEX = "classpath.".length();
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String) entry.getKey();
			if (key.startsWith("classpath.")) {
				int n;
				try {
					n = Integer.parseInt(key.substring(INDEX));
				} catch (NumberFormatException e) {
					LogUtil.logWarn("ParserClassUtil#readDmdlPrefsClassPath() error", e);
					continue;
				}
				map.put(n, (String) entry.getValue());
			}
		}
		for (String path : map.values()) {
			try {
				list.add(new File(path).toURI().toURL());
			} catch (MalformedURLException e) {
				LogUtil.logWarn("ParserClassUtil#readDmdlPrefsClassPath() error", e);
			}
		}
	}

	public static void getClassPath(List<URL> list, IProject project) {
		List<Library> libs = getLibraries(project);

		for (Library lib : libs) {
			if (lib.selected) {
				try {
					IPath pp = Path.fromPortableString(lib.path);
					URL url = JdtUtil.toURL(project, pp);
					if (url != null) {
						list.add(url);
					}
				} catch (MalformedURLException e) {
					LogUtil.logWarn(ParserClassUtil.class.getSimpleName(), e);
				}
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
		IFile file = project.getFile(".classpath");
		long time = file.getLocalTimeStamp();

		final String key = "ParserClassPath.initialize";
		check: {
			String s = getValue(project, key);
			if (isEmpty(s)) {
				break check;
			}

			try {
				long cache = Long.parseLong(s);
				if (cache < time) {
					break check;
				}
			} catch (NumberFormatException e) {
				break check;
			}
			return;
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

		setValue(project, key, Long.toString(time));
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
}
