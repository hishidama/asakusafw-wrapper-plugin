package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink;

import java.text.MessageFormat;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class DeclaredOperatorHyperlink implements IHyperlink {
	private IJavaElement element;
	private IRegion region;

	public DeclaredOperatorHyperlink(IJavaElement element, IRegion region) {
		this.element = element;
		this.region = region;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return region;
	}

	@Override
	public String getTypeLabel() {
		return "Open Operator";
	}

	@Override
	public String getHyperlinkText() {
		return "Open Operator";
	}

	@Override
	public void open() {
		try {
			JavaUI.openInEditor(element);
		} catch (Exception e) {
			LogUtil.logWarn(MessageFormat.format("{0} openEditor error.", getClass().getSimpleName()), e);
		}
	}
}
