package jp.hishidama.eclipse_plugin.asakusafw_wrapper.operator;

public enum OperatorType {
	// フロー制御演算子
	/** 分岐演算子 */
	BRANCH("分岐演算子", "Branch", true, false),
	/** 合流演算子 */
	CONFLUENT("合流演算子", "confluent", false, true),

	// データ操作演算子
	/** 更新演算子 */
	UPDATE("更新演算子", "Update", true, false),
	/** 変換演算子 */
	CONVERT("変換演算子", "Convert", true, false),
	/** 拡張演算子 */
	EXTEND("拡張演算子", "extend", false, true),
	/** 射影演算子 */
	PROJECT("射影演算子", "project", false, true),
	/** 再構築演算子 */
	RESTRUCTURE("再構築演算子", "restructure", false, true),
	/** 抽出演算子 */
	EXTRACT("抽出演算子", "Extract", true, false),

	// 結合演算子
	/** マスター確認演算子 */
	MASTER_CHECK("マスター確認演算子", "MasterCheck", true, false),
	/** マスター結合演算子 */
	MASTER_JOIN("マスター結合演算子", "MasterJoin", true, false),
	/** マスター分岐演算子 */
	MASTER_BRANCH("マスター分岐演算子", "MasterBranch", true, false),
	/** マスターつき更新演算子 */
	MASTER_JOIN_UPDATE("マスターつき更新演算子", "MasterJoinUpdate", true, false),
	/** マスター選択演算子 */
	MASTER_SELECTION("マスター選択演算子", "MasterSelection", false, false),
	/** グループ結合演算子 */
	CO_GROUP("グループ結合演算子", "CoGroup", true, false),
	/** 分割演算子 */
	SPLIT("分割演算子", "Split", true, false),

	// 集計演算子
	/** 単純集計演算子 */
	SUMMARIZE("単純集計演算子", "Summarize", true, false),
	/** 畳み込み演算子 */
	FOLD("畳み込み演算子", "Fold", true, false),
	/** グループ整列演算子 */
	GROUP_SORT("グループ整列演算子", "GroupSort", true, false),

	// 特殊演算子
	/** フロー演算子 */
	FLOW_PART("フロー演算子", "FlowPart", false, false),
	/** チェックポイント演算子 */
	CHECKPOINT("チェックポイント演算子", "checkpoint", false, true),
	/** ロギング演算子 */
	LOGGING("ロギング演算子", "Logging", true, false),
	/** 空演算子 */
	EMPTY("空演算子", "empty", false, true),
	/** 停止演算子 */
	STOP("停止演算子", "stop", false, true),

	;

	private String name;
	private String type;
	private boolean user;
	private boolean core;

	OperatorType(String name, String type, boolean user, boolean core) {
		this.name = name;
		this.type = type;
		this.user = user;
		this.core = core;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getShortTypeName() {
		if (!core) {
			return "@" + type;
		} else {
			return type;
		}
	}

	public String getTypeName() {
		if (!core) {
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
