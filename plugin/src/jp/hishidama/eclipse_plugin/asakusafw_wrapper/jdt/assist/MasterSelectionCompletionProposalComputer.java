package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink.MasterSelectionFinder;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

public class MasterSelectionCompletionProposalComputer implements IJavaCompletionProposalComputer {

	// @Override
	public void sessionStarted() {
	}

	// @Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		if (!isAssist(context)) {
			return Collections.emptyList();
		}

		if (!(context instanceof JavaContentAssistInvocationContext)) {
			return Collections.emptyList();
		}
		JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;

		ICompilationUnit cu = javaContext.getCompilationUnit();
		int offset = context.getInvocationOffset();
		MasterSelectionFinder finder = new MasterSelectionFinder(cu, offset);
		if (finder.getMemberName() == null) {
			return Collections.emptyList();
		}

		List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
		for (String name : MasterSelectionStringCompletionProposalComputer.getMasterSelectionMethod(cu)) {
			String s = '"' + name + '"';
			CompletionProposal proposal = new CompletionProposal(s, offset, 0, s.length());
			list.add(proposal);
		}
		return list;
	}

	private boolean isAssist(ContentAssistInvocationContext context) {
		IDocument document = context.getDocument();
		for (int i = context.getInvocationOffset() - 1; i >= 0; i--) {
			char c;
			try {
				c = document.getChar(i);
			} catch (BadLocationException e) {
				return false;
			}
			switch (c) {
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				continue;
			case '=':
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	// @Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		return null;
	}

	// @Override
	public String getErrorMessage() {
		return null;
	}

	// @Override
	public void sessionEnded() {
	}
}
