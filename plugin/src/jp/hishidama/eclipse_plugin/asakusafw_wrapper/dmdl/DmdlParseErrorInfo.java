package jp.hishidama.eclipse_plugin.asakusafw_wrapper.dmdl;

import java.net.URI;

/**
 * DMDLコンパイルエラー情報.
 * 
 * @since 2013.08.26
 */
public class DmdlParseErrorInfo {

	public URI file;
	public int level;
	public String message;
	public int beginLine;
	public int beginColumn;
	public int endLine;
	public int endColumn;
}
