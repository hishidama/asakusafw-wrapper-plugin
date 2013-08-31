package jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension;

import java.io.IOException;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.config.AsakusafwProperties;

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
	 * 該当プロジェクトを受け付けられるかどうか.
	 * 
	 * @param project
	 *            プロジェクト
	 * @return true：受け付けられる
	 */
	public abstract boolean acceptable(IProject project);

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
