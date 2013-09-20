package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import java.io.FileNotFoundException;
import java.text.MessageFormat;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.config.AsakusafwProperties;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util.ParserClassUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

public class BuildPropertiesUtil {

	private static final String PARSER_BUILD_PROPERTIES = ParserClassUtil.PARSER_BUILD_PROPERTIES;

	public static String getBuildPropertiesFileName(IProject project) {
		ParserClassUtil.initializeProjectParser(project);

		return ParserClassUtil.getValue(project, PARSER_BUILD_PROPERTIES);
	}

	public static String getDefaultBuildPropertiesFileName(IProject project) {
		ParserClassUtil.initializeProjectParser(project);

		AsakusafwConfiguration c = ParserClassUtil.getConfiguration(project);
		if (c != null) {
			return c.getDefaultBuildPropertiesPath();
		}

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getString(PARSER_BUILD_PROPERTIES);
	}

	public static void saveBuildPropertiesFileName(IProject project, String path) {
		ParserClassUtil.setValue(project, PARSER_BUILD_PROPERTIES, path);
	}

	public static AsakusafwProperties getBuildProperties(IProject project, boolean putError) {
		String path = getBuildPropertiesFileName(project);
		try {
			AsakusafwConfiguration c = ParserClassUtil.getConfiguration(project);
			if (c == null) {
				if (putError) {
					openMessageDialog("Asakusa Frameworkのバージョンをプロパティーページで設定して下さい。");
				}
				return null;
			}
			return c.getAsakusafwProperties(project, path);
		} catch (FileNotFoundException e) {
			if (putError) {
				openFileNotFoundErrorDialog(path);
			}
			return null;
		} catch (Exception e) {
			IStatus status = LogUtil.logError("build.properties read error", e);
			if (putError) {
				openErrorDialog(status);
			}
			return null;
		}
	}

	private static void openMessageDialog(final String message) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openWarning(null, "build.properties error", message);
			}
		});
	}

	private static void openFileNotFoundErrorDialog(String fname) {
		final String message = MessageFormat.format("プロジェクト内にAsakusa Frameworkのbuild.propertiesが見つかりません。\n"
				+ "プロパティーページでbuild.propertiesの場所を指定して下さい。\n\n" + "現在指定されているパス={0}", fname);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openWarning(null, "build.properties error", message);
			}
		});
	}

	private static void openErrorDialog(final IStatus status) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				ErrorDialog.openError(null, "build.properties error", "build.propertiesの読み込み中にエラーが発生しました。", status);
			}
		});
	}
}
