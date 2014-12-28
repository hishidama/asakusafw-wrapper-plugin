package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import jp.hishidama.eclipse_plugin.jdt.util.AnnotationUtil;
import jp.hishidama.eclipse_plugin.jdt.util.TypeUtil;

import org.eclipse.jdt.core.IType;

/**
 * Batchユーティリティー.
 * 
 * @since 2014.05.17
 */
public class BatchUtil {
	public static final String BATCH_NAME = "com.asakusafw.vocabulary.batch.Batch";
	public static final String BATCH_DESCRIPTION_NAME = "com.asakusafw.vocabulary.batch.BatchDescription";
	public static final String WORK_NAME = "com.asakusafw.vocabulary.batch.Work";

	public static boolean isBatch(IType type) {
		return AnnotationUtil.getAnnotation(type, BATCH_NAME) != null
				&& TypeUtil.isExtends(type, BATCH_DESCRIPTION_NAME);
	}
}
