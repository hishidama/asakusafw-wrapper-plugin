package jp.hishidama.asakusafw_wrapper.dmdl.excel;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;

public abstract class TestSheetGenerator {

	private DmdlSemantics dmdlSemantics;

	public void execute(DmdlSemantics dmdlSemantics, List<Map<String, String>> names) throws IOException {
		this.dmdlSemantics = dmdlSemantics;

		Set<String> errorSet = new LinkedHashSet<String>();
		Map<String, List<SheetInfo>> map = new LinkedHashMap<String, List<SheetInfo>>();
		for (Map<String, String> name : names) {
			SheetInfo info = new SheetInfo(name);

			String srcModelNames = info.getSrcModelName();
			for (String s : srcModelNames.split(Pattern.quote("+"))) {
				String srcModelName = s.trim();
				if (findModelDeclaration(srcModelName) == null) {
					errorSet.add(srcModelName);
				}
			}

			String key = info.getDstBookName();
			List<SheetInfo> list = map.get(key);
			if (list == null) {
				list = new ArrayList<SheetInfo>();
				map.put(key, list);
			}
			list.add(info);
		}

		if (!errorSet.isEmpty()) {
			throw new IOException(MessageFormat.format("モデル定義が見つかりませんでした\nmodel={0}", errorSet));
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
		if (modelName == null) {
			return null;
		}
		if (modelName.contains("+")) {
			CompositeModelDeclaration result = new CompositeModelDeclaration(dmdlSemantics, modelName);
			String[] ss = modelName.split(Pattern.quote("+"));
			for (String s : ss) {
				ModelDeclaration model = dmdlSemantics.findModelDeclaration(s.trim());
				if (model != null) {
					result.addProperties(model);
				}
			}
			return result;
		}

		return dmdlSemantics.findModelDeclaration(modelName);
	}

	static class CompositeModelDeclaration extends ModelDeclaration {
		private final Set<String> nameSet = new HashSet<String>();
		private final List<PropertyDeclaration> propertyList = new ArrayList<PropertyDeclaration>();

		public CompositeModelDeclaration(DmdlSemantics owner, String name) {
			super(owner, null, new AstSimpleName(null, name), null, Collections.<AstAttribute> emptyList());
		}

		public void addProperties(ModelDeclaration model) {
			for (PropertyDeclaration p : model.getDeclaredProperties()) {
				String name = p.getName().identifier;
				if (nameSet.add(name)) {
					propertyList.add(p);
				}
			}
		}

		@Override
		public List<PropertyDeclaration> getDeclaredProperties() {
			return propertyList;
		}
	}

	protected abstract void generate(Map<String, List<SheetInfo>> map) throws IOException;
}
