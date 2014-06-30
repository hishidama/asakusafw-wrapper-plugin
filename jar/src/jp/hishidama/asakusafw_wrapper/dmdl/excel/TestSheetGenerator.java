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

	public void execute(DmdlSemantics dmdlSemantics, List<String[]> names) throws IOException {
		this.dmdlSemantics = dmdlSemantics;

		StringBuilder sb = new StringBuilder(128);

		Map<String, List<SheetInfo>> map = new LinkedHashMap<String, List<SheetInfo>>();
		for (String[] name : names) {
			SheetInfo info = new SheetInfo();
			info.srcModelName = name[0];
			info.srcSheetName = name[1];
			info.dstBookName = name[2];
			info.dstSheetName = name[3];

			if (findModelDeclaration(info.srcModelName) == null) {
				if (sb.length() == 0) {
					sb.append("モデル定義が見つかりませんでした\n");
				} else {
					sb.append("\n");
				}
				sb.append(info.srcModelName);
			}

			String key = info.dstBookName;
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
		public String srcModelName;
		public String srcSheetName;
		public String dstBookName;
		public String dstSheetName;
	}

	protected final ModelDeclaration findModelDeclaration(String modelName) {
		return dmdlSemantics.findModelDeclaration(modelName);
	}

	protected abstract void generate(Map<String, List<SheetInfo>> map) throws IOException;
}
