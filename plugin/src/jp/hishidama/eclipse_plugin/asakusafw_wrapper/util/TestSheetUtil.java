package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task.TestSheetHelperTask;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class TestSheetUtil {

	public static class RuleItem {
		private String[] options;
		private int rowIndex;
		private int columnIndex;

		public String[] getOptions() {
			return options;
		}

		public int getRowIndex() {
			return rowIndex;
		}

		public int getColumnIndex() {
			return columnIndex;
		}
	}

	public static Map<String, RuleItem> getRuleItem(IProject project, IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		TestSheetHelperTask task = new TestSheetHelperTask(project);
		task.run(monitor);
		Map<String, Object[]> map = task.getRuleItem();
		if (map == null) {
			throw new IllegalStateException(); // task.run()で例外が発生しているはず
		}

		Map<String, RuleItem> result = new HashMap<String, RuleItem>(map.size());
		for (Entry<String, Object[]> entry : map.entrySet()) {
			String key = entry.getKey();
			Object[] value = entry.getValue();

			RuleItem item = new RuleItem();
			item.options = (String[]) value[0];
			item.rowIndex = (Integer) value[1];
			item.columnIndex = (Integer) value[2];
			result.put(key, item);
		}
		return result;
	}
}
