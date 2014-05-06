package jp.hishidama.eclipse_plugin.asakusafw_wrapper.operator;

public enum OperatorType {
	// フロー制御演算子
	/** 分岐演算子 */
	BRANCH("分岐演算子", "Branch"),
	/** 合流演算子 */
	CONFLUENT("合流演算子", "confluent"),

	// データ操作演算子
	/** 更新演算子 */
	UPDATE("更新演算子", "Update"),
	/** 変換演算子 */
	CONVERT("変換演算子", "Convert"),
	/** 拡張演算子 */
	EXTEND("拡張演算子", "extend"),
	/** 射影演算子 */
	PROJECT("射影演算子", "project"),
	/** 再構築演算子 */
	RESTRUCTURE("再構築演算子", "restructure"),
	/** 抽出演算子 */
	EXTRACT("抽出演算子", "Extract"),

	// 結合演算子
	/** マスター確認演算子 */
	MASTER_CHECK("マスター確認演算子", "MasterCheck"),
	/** マスター結合演算子 */
	MASTER_JOIN("マスター結合演算子", "MasterJoin"),
	/** マスター分岐演算子 */
	MASTER_BRANCH("マスター分岐演算子", "MasterBranch"),
	/** マスターつき更新演算子 */
	MASTER_JOIN_UPDATE("マスターつき更新演算子", "MasterJoinUpdate"),
	/** グループ結合演算子 */
	CO_GROUP("グループ結合演算子", "CoGroup"),
	/** 分割演算子 */
	SPLIT("分割演算子", "Split"),

	// 集計演算子
	/** 単純集計演算子 */
	SUMMARIZE("単純集計演算子", "Summarize"),
	/** 畳み込み演算子 */
	FOLD("畳み込み演算子", "Fold"),
	/** グループ整列演算子 */
	GROUP_SORT("グループ整列演算子", "GroupSort"),

	// 特殊演算子
	/** フロー演算子 */
	FLOW_PART("フロー演算子", "FlowPart"),
	/** チェックポイント演算子 */
	CHECKPOINT("チェックポイント演算子", "checkpoint"),
	/** ロギング演算子 */
	LOGGING("ロギング演算子", "Logging"),
	/** 空演算子 */
	EMPTY("空演算子", "empty"),
	/** 停止演算子 */
	STOP("停止演算子", "stop"),

	;

	private String name;
	private String type;
	private boolean user;
	private boolean core;

	OperatorType(String name, String type) {
		this.name = name;
		this.type = type;
		this.user = Character.isUpperCase(type.charAt(0)) && !type.equals("FlowPart");
		this.core = Character.isLowerCase(type.charAt(0));
	}

	public String getName() {
		return name;
	}

	public String getShortTypeName() {
		if (!core) {
			return "@" + type;
		} else {
			return type;
		}
	}

	public String getTypeName() {
		if (user) {
			return "com.asakusafw.vocabulary.operator." + type;
		} else {
			return type;
		}
	}

	public boolean isUser() {
		return user;
	}

	public boolean isCore() {
		return core;
	}
}
