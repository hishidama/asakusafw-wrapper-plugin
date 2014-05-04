package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink;

import java.text.MessageFormat;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.util.FileUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class FileHyperlink implements IHyperlink {
	private IFile file;
	private IRegion region;

	public FileHyperlink(IFile file, IRegion region) {
		this.file = file;
		this.region = region;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return region;
	}

	@Override
	public String getTypeLabel() {
		return "Open File";
	}

	@Override
	public String getHyperlinkText() {
		return "Open File";
	}

	@Override
	public void open() {
		try {
			FileUtil.openEditor(file);
		} catch (Exception e) {
			LogUtil.logWarn(MessageFormat.format("{0} openEditor error.", getClass().getSimpleName()), e);
		}
	}
}
