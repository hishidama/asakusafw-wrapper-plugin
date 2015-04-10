package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class OpenDeclaredOperatorHyperlinkHandler extends AbstractHandler {
	private OpenDeclaredOperatorHyperlinkDetector detector = new OpenDeclaredOperatorHyperlinkDetector();

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ITextEditor editor = (ITextEditor) HandlerUtil.getActiveEditor(event);
		if (editor == null) {
			return null;
		}
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		int offset = selection.getOffset();

		IHyperlink[] links = detector.detectHyperlinks(editor, offset);
		if (links != null && links.length >= 1) {
			IHyperlink link = links[0];
			link.open();
		}

		return null;
	}
}
