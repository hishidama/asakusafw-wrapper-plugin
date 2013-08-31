package jp.hishidama.eclipse_plugin.asakusafw_wrapper.config;

/**
 * Asakusa Frameworkのプロパティー.
 * 
 * @since 2013.08.26
 */
public abstract class AsakusafwProperties {

	/**
	 * デフォルトのパッケージ名.
	 * 
	 * @return パッケージ名
	 */
	public abstract String getPackageDefault();

	/**
	 * DMDLのソースディレクトリー.
	 * 
	 * @return ディレクトリーのパス
	 */
	public abstract String getDmdlDir();

	/**
	 * データモデルクラスの生成先ディレクトリー.
	 * 
	 * @return ディレクトリーのパス
	 */
	public abstract String getModelgenOutput();

	/**
	 * データモデルクラスのパッケージ名.
	 * 
	 * @return パッケージ名
	 */
	public abstract String getModelgenPackage();

	/**
	 * DMDLファイルのエンコーディング
	 * 
	 * @return エンコーディング（null可）
	 */
	public abstract String getDmdlEncoding();
}
