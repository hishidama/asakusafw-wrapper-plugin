package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

public class FlowParameter {

	private String name;
	private String type;
	private String modelClassName;

	private String porterName;
	private String porterClassName;

	public boolean isPort() {
		return isIn() || isOut();
	}

	public boolean isIn() {
		return type.startsWith(FlowUtil.IN_NAME);
	}

	public boolean isOut() {
		return type.startsWith(FlowUtil.OUT_NAME);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getModelClassName() {
		if (modelClassName == null) {
			int n = type.indexOf('<');
			if (n < 0) {
				modelClassName = "";
			} else {
				modelClassName = AfwStringUtil.extractModelClassName(type);
			}
		}
		return modelClassName;
	}

	public String getPorterName() {
		return porterName;
	}

	public void setPorterName(String porterName) {
		this.porterName = porterName;
	}

	public String getPorterClassName() {
		return porterClassName;
	}

	public void setPorterClassName(String porterClassName) {
		this.porterClassName = porterClassName;
	}
}
