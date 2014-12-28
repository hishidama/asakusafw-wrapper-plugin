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
	 * DMDLファイルのエンコーディング.
	 * 
	 * @return エンコーディング（null可）
	 */
	public abstract String getDmdlEncoding();

	/**
	 * テストデータExcelシートの生成先ディレクトリー.
	 * 
	 * @return ディレクトリーのパス
	 * @since 2014.05.04
	 */
	public abstract String getTestDataSheetOutput();

	/**
	 * コンパイルされたソースディレクトリー.
	 * 
	 * @return ディレクトリーのパス
	 * @since 2014.12.28
	 */
	public abstract String getCompiledSourceDirectory();

	/**
	 * コンパイルされたソースのパッケージ名.
	 * 
	 * @return パッケージ名
	 * @since 2014.12.28
	 */
	public abstract String getCompiledSourcePackage();

	/**
	 * Hadoopワークディレクトリー.
	 * 
	 * @return ディレクトリーのパス
	 * @since 2014.12.28
	 */
	public abstract String getHadoopWorkDirectory();

	/**
	 * コンパイラーワークディレクトリー.
	 * 
	 * @return ディレクトリーのパス
	 * @since 2014.12.28
	 */
	public abstract String getCompilerWorkDirectory();
}
