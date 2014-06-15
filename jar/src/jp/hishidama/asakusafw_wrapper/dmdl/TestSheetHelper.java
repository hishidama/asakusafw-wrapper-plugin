package jp.hishidama.asakusafw_wrapper.dmdl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.asakusafw.testdriver.excel.NullityConditionKind;
import com.asakusafw.testdriver.excel.RuleSheetFormat;
import com.asakusafw.testdriver.excel.TotalConditionKind;
import com.asakusafw.testdriver.excel.ValueConditionKind;

public class TestSheetHelper {

	public Map<String, Object[]> getRuleItem() {
		Map<String, Object[]> map = new LinkedHashMap<String, Object[]>();

		// 全体の比較
		put(map, "TotalConditionKind", TotalConditionKind.getOptions(), RuleSheetFormat.TOTAL_CONDITION);
		// 値の比較
		put(map, "ValueConditionKind", ValueConditionKind.getOptions(), RuleSheetFormat.VALUE_CONDITION);
		// NULLの比較
		put(map, "NullityConditionKind", NullityConditionKind.getOptions(), RuleSheetFormat.NULLITY_CONDITION);

		return map;
	}

	private void put(Map<String, Object[]> map, String key, String[] options, RuleSheetFormat rule) {
		Object[] object = { options, rule.getRowIndex(), rule.getColumnIndex() };
		map.put(key, object);
	}
}
