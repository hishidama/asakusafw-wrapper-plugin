package jp.hishidama.asakusafw_wrapper.dmdl.excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;

public abstract class TestSheetGenerator {

	private DmdlSemantics dmdlSemantics;

	public void execute(DmdlSemantics dmdlSemantics, List<Map<String, String>> names) throws IOException {
		this.dmdlSemantics = dmdlSemantics;

		StringBuilder sb = new StringBuilder(128);

		Map<String, List<SheetInfo>> map = new LinkedHashMap<String, List<SheetInfo>>();
		for (Map<String, String> name : names) {
			SheetInfo info = new SheetInfo(name);

			String srcModelName = info.getSrcModelName();
			if (findModelDeclaration(srcModelName) == null) {
				if (sb.length() == 0) {
					sb.append("モデル定義が見つかりませんでした\n");
				} else {
					sb.append("\n");
				}
				sb.append(srcModelName);
			}

			String key = info.getDstBookName();
			List<SheetInfo> list = map.get(key);
			if (list == null) {
				list = new ArrayList<SheetInfo>();
				map.put(key, list);
			}
			list.add(info);
		}

		if (sb.length() != 0) {
			throw new IOException(sb.toString());
		}
		generate(map);
	}

	public static class SheetInfo {
		private Map<String, String> map;

		public SheetInfo(Map<String, String> map) {
			this.map = map;
		}

		public String getDstBookName() {
			return map.get("dstBookName");
		}

		public String getIndexSheetName() {
			return map.get("indexSheetName");
		}

		public String getFlowClassName() {
			return map.get("flowClassName");
		}

		public String getSrcModelName() {
			return map.get("srcModelName");
		}

		public String getSrcModelDescription() {
			return map.get("srcModelDescription");
		}

		public String getSrcSheetName() {
			return map.get("srcSheetName");
		}

		public String getDstSheetName() {
			return map.get("dstSheetName");
		}

		public String getDstSheetDescription() {
			return map.get("dstSheetDescription");
		}
	}

	protected final ModelDeclaration findModelDeclaration(String modelName) {
		return dmdlSemantics.findModelDeclaration(modelName);
	}

	protected abstract void generate(Map<String, List<SheetInfo>> map) throws IOException;
}
