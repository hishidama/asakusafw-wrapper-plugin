package jp.hishidama.eclipse_plugin.asakusafw_wrapper.dmdl;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util.ParserClassUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.util.TestSheetUtil.SheetInfo;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

/**
 * DMDLパーサー.
 * 
 * @since 2013.08.26
 */
public class DmdlParserWrapper {
	private static final String CALLER_CLASS = "jp.hishidama.asakusafw_wrapper.dmdl.DmdlParserCaller";

	private IProject project;

	public DmdlParserWrapper(IJavaProject project) {
		initClassLoader(project);
	}

	private ClassLoader parserLoader;

	public boolean isValid() {
		return parserLoader != null;
	}

	private List<URL> parserClassList;

	protected void initClassLoader(IJavaProject project) {
		parserClassList = new ArrayList<URL>();

		findClassPath(parserClassList, project);
		findMyClassPath(parserClassList, "resource/dmdlparser-caller.jar");

		LogUtil.logInfo(MessageFormat.format("DmdlParser caller classpath={0}", toString(parserClassList)));

		parserLoader = URLClassLoader.newInstance(parserClassList.toArray(new URL[parserClassList.size()]));
	}

	protected void findClassPath(List<URL> list, IJavaProject javaProject) {
		ParserClassUtil.getProjectClassPath(list, javaProject);
		this.project = javaProject.getProject();
		ParserClassUtil.getClassPath(list, project);
	}

	protected URL findMyClassPath(List<URL> list, String jarName) {
		try {
			Bundle bundle = Activator.getDefault().getBundle();
			IPath path = Path.fromPortableString(jarName);
			URL bundleUrl = FileLocator.find(bundle, path, null);
			URL url = FileLocator.resolve(bundleUrl);

			list.add(url);
			return url;
		} catch (Exception e) {
			LogUtil.logWarn("DmdlParser#findMyClassPath() error.", e);
			return null;
		}
	}

	public List<DmdlParseErrorInfo> parse(List<IFile> ifiles) {
		List<Object[]> files = convertFiles(ifiles);

		String taskName = "DMDLパーサーのロード中";
		try {
			Class<?> c = parserLoader.loadClass(CALLER_CLASS);
			taskName = "DMDLパーサーの準備中";
			Object caller = c.newInstance();
			taskName = "DMDLパーサーのメソッド準備中";
			Method method = c.getMethod("parse", List.class);
			taskName = "DMDLパーサーの実行中";
			@SuppressWarnings("unchecked")
			List<Object[]> list = (List<Object[]>) method.invoke(caller, files);

			taskName = "DMDLパーサーの実行結果のまとめ中";
			List<DmdlParseErrorInfo> result = new ArrayList<DmdlParseErrorInfo>(list.size());
			for (Object[] r : list) {
				DmdlParseErrorInfo pe = new DmdlParseErrorInfo();
				pe.file = (URI) r[0];
				pe.level = (Integer) r[1];
				pe.message = (String) r[2];
				pe.beginLine = (Integer) r[3];
				pe.beginColumn = (Integer) r[4];
				pe.endLine = (Integer) r[5];
				pe.endColumn = (Integer) r[6];
				result.add(pe);
			}
			return result;
		} catch (final Throwable e) {
			{
				String message = MessageFormat.format("DmdlParser#parse() error. classpath={0}",
						toString(parserClassList));
				LogUtil.logWarn(message, e);
			}
			final String taskName0 = taskName;
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					Throwable t = e;
					while (t.getCause() != null) {
						t = t.getCause();
					}
					IStatus status = LogUtil.warnStatus(MessageFormat.format(
							"DmdlParser#parse() error.\nexception={0}\nmessage={1}", t.getClass().getName(),
							t.getMessage()), t);
					String message = MessageFormat
							.format("{0}にエラーが発生しました。\nDMDLパーサーに必要なライブラリーが指定されていない可能性があります。\nプロパティーページでAsakusa FrameworkおよびDmdlParserの設定を確認してください。",
									taskName0);
					ErrorDialog.openError(null, "error", message, status);
				}
			});
		}
		return null;
	}

	private static String toString(List<?> list) {
		if (list == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(512);
		for (Object obj : list) {
			if (sb.length() != 0) {
				sb.append("\n");
			}
			sb.append(obj);
		}
		return sb.toString();
	}

	public void generateTestSheet(String asakusaFwVersion, List<IFile> ifiles, List<SheetInfo> sheetInfoList)
			throws CoreException {
		String version = convertTestSheetGeneratorVersion(asakusaFwVersion);
		List<Object[]> files = convertFiles(ifiles);

		List<String[]> names = new ArrayList<String[]>(sheetInfoList.size());
		for (SheetInfo info : sheetInfoList) {
			String[] name = { info.srcModelName, info.srcSheetName, info.dstBookName, info.dstSheetName };
			names.add(name);
		}

		String taskName = "DMDLパーサーのロード中";
		try {
			Class<?> c = parserLoader.loadClass(CALLER_CLASS);
			taskName = "DMDLパーサーの準備中";
			Object caller = c.newInstance();
			taskName = "テストシート作成のメソッド準備中";
			Method method = c.getMethod("generateTestSheet", String.class, List.class, List.class);
			taskName = "テストシートの作成中";
			method.invoke(caller, version, files, names);
		} catch (Throwable e) {
			{
				String message = MessageFormat.format("DmdlParser#generateTestSheet() error. classpath={0}",
						toString(parserClassList));
				LogUtil.logWarn(message, e);
			}
			String message = MessageFormat
					.format("{0}にエラーが発生しました。\nDMDLパーサーに必要なライブラリーが指定されていない可能性があります。\nプロパティーページでAsakusa FrameworkおよびDmdlParserの設定を確認してください。",
							taskName);
			IStatus status = LogUtil.warnStatus(message, e);
			throw new CoreException(status);
		}
	}

	private List<Object[]> convertFiles(List<IFile> ifiles) {
		List<Object[]> files = new ArrayList<Object[]>(ifiles.size());
		for (IFile f : ifiles) {
			try {
				Object[] arr = { f.getLocationURI(), f.getCharset() };
				files.add(arr);
			} catch (Exception e) {
				LogUtil.logWarn(MessageFormat.format("DmdlParser#convertFiles({0}) error.", f), e);
			}
		}
		return files;
	}

	private String convertTestSheetGeneratorVersion(String asakusaFwVersion) {
		if (AsakusafwConfiguration.compareVersion(asakusaFwVersion, "0.5.3") >= 0) {
			return "053";
		}
		return "02";
	}
}
