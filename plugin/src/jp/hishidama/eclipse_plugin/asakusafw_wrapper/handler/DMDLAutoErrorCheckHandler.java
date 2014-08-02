package jp.hishidama.eclipse_plugin.asakusafw_wrapper.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.operation.IRunnableContext;

public class DMDLAutoErrorCheckHandler extends DMDLErrorCheckHandler {

	public static final String COMMAND_ID = "jp.hishidama.asakusafwWrapper.command.dmdlAutoErrorCheck";

	@Override
	protected boolean prepare(ExecutionEvent event) {
		return true;
	}

	@Override
	protected IRunnableContext createRunnableContext(ExecutionEvent event) {
		return null;
	}
}
