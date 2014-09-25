package jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension;

import java.io.IOException;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.config.AsakusafwProperties;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.util.BuildPropertiesUtil;

import org.eclipse.core.resources.IProject;

/**
 * Asakusa Frameworkの設定情報.
 * 
 * @since 2013.08.26
 */
public abstract class AsakusafwConfiguration {

	/**
	 * 設定名.
	 * 
	 * @return 設定名
	 */
	public abstract String getConfigurationName();

	/**
	 * 対象最低バージョン.
	 * 
	 * @return バージョン
	 * @since 2013.11.22
	 */
	public abstract String getVersionMin();

	/**
	 * 対象最高バージョン.
	 * 
	 * @return バージョン
	 * @since 2013.11.22
	 */
	public abstract String getVersionMax();

	/**
	 * 現在のバージョン.
	 * 
	 * @param project
	 *            プロジェクト
	 * @return バージョン
	 * @since 2014.06.29
	 */
	public abstract String getCurrentVersion(IProject project);

	/**
	 * 現在のバージョン.
	 * 
	 * @param project
	 *            プロジェクト
	 * @return バージョン
	 * @since 2014.08.02
	 */
	public static String getAsakusaFwVersion(IProject project) {
		String asakusaFwVersion = null;
		AsakusafwConfiguration c = BuildPropertiesUtil.getAsakusafwConfiguration(project);
		if (c != null) {
			asakusaFwVersion = c.getCurrentVersion(project);
		}
		return asakusaFwVersion;
	}

	/**
	 * 該当プロジェクトを受け付けられるかどうか.
	 * 
	 * @param project
	 *            プロジェクト
	 * @return true：受け付けられる
	 */
	public boolean acceptable(IProject project) {
		String version = getCurrentVersion(project);
		if (version == null) {
			return false;
		}
		return containsVersion(version, getVersionMin(), getVersionMax());
	}

	/**
	 * バージョン確認.
	 * 
	 * @param version
	 *            バージョン
	 * @param min
	 *            範囲：最低バージョン
	 * @param max
	 *            範囲：最高バージョン
	 * @return true：バージョンが範囲に含まれる場合
	 * @since 2013.11.22
	 */
	public static boolean containsVersion(String version, String min, String max) {
		return compareVersion(min, version) <= 0 && compareVersion(version, max) <= 0;
	}

	/**
	 * バージョン比較.
	 * <ul>
	 * <li>ピリオドおよびハイフンでバージョン文字列を分割し、各部分を数値として比較する。</li>
	 * <li>分割された個数が少ない方は、足りない部分を0として扱う。</li>
	 * <li>分割された部分が数値でない場合は最大整数として扱う。</li>
	 * </ul>
	 * 
	 * @param ver1
	 *            バージョン1
	 * @param ver2
	 *            バージョン2
	 * @return 等しいとき0、ver1&lt;ver2のとき負の数、ver1&gt;ver2のとき正の数
	 * @since 2013.11.22
	 */
	public static int compareVersion(String ver1, String ver2) {
		if (ver1 == null) {
			ver1 = "ANY";
		}
		if (ver2 == null) {
			ver2 = "ANY";
		}
		String[] ss1 = ver1.split("\\.|\\-");
		String[] ss2 = ver2.split("\\.|\\-");
		int n = Math.max(ss1.length, ss2.length);
		for (int i = 0; i < n; i++) {
			String s1 = (i < ss1.length) ? ss1[i] : "0";
			String s2 = (i < ss2.length) ? ss2[i] : "0";
			int n1;
			try {
				n1 = Integer.parseInt(s1);
			} catch (Exception e) {
				n1 = Integer.MAX_VALUE;
			}
			int n2;
			try {
				n2 = Integer.parseInt(s2);
			} catch (Exception e) {
				n2 = Integer.MAX_VALUE;
			}
			int c = n1 - n2;
			if (c != 0) {
				return c;
			}
		}
		return 0;
	}

	/**
	 * build.propertiesファイルのデフォルトの場所.
	 * 
	 * @return ファイルのパス
	 */
	public abstract String getDefaultBuildPropertiesPath();

	/**
	 * DMDLのコンパイルに必要なjarファイル.
	 * 
	 * @param project
	 *            プロジェクト
	 * @return ライブラリー一覧
	 */
	public abstract List<Library> getDefaultLibraries(IProject project);

	/**
	 * ライブラリー情報.
	 */
	public static class Library {
		/** ライブラリーファイルのパス */
		public String path;
		/** デフォルトの選択状態 */
		public boolean selected;

		/**
		 * コンストラクター.
		 * 
		 * @param path
		 *            ライブラリーファイルのパス
		 * @param defaultSelected
		 *            デフォルトの選択状態
		 */
		public Library(String path, boolean defaultSelected) {
			this.path = path;
			this.selected = defaultSelected;
		}

		/**
		 * @see #valudOf(String)
		 */
		@Override
		public String toString() {
			return selected + "\0" + path;
		}

		/**
		 * インスタンス生成.
		 * 
		 * @param s
		 *            String
		 * @return Library
		 * @see #toString()
		 */
		public static Library valudOf(String s) {
			String[] ss = s.split("\0");
			return new Library(ss[1], Boolean.valueOf(ss[0]));
		}
	}

	/**
	 * Asakusa Frameworkプロパティー取得.
	 * 
	 * @param project
	 *            プロジェクト
	 * @param propertyFilePath
	 *            設定で指定されているbuild.propertiesファイルのパス
	 * @return Asakusa Frameworkプロパティー
	 * @throws IOException
	 *             build.propertiesの読み込みに関する何らかの例外
	 */
	public abstract AsakusafwProperties getAsakusafwProperties(IProject project, String propertyFilePath)
			throws IOException;
}
