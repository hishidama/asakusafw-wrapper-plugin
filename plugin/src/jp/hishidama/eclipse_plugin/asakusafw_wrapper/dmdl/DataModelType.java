package jp.hishidama.eclipse_plugin.asakusafw_wrapper.dmdl;

public enum DataModelType {

	RECORD, SUMMARIZED, JOINED, PROJECTIVE;

	public String displayName() {
		return name().toLowerCase();
	}

	@Override
	public String toString() {
		return displayName();
	}
}
