package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.hishidama.eclipse_plugin.jdt.util.AnnotationUtil;
import jp.hishidama.eclipse_plugin.jdt.util.TypeUtil;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * JobFlow/FlowPartユーティリティー.
 * 
 * @since 2014.05.04
 */
public class FlowUtil {
	public static final String JOBFLOW_NAME = "com.asakusafw.vocabulary.flow.JobFlow";
	public static final String FLOWPART_NAME = "com.asakusafw.vocabulary.flow.FlowPart";
	public static final String FLOW_DESCRIPTION_NAME = "com.asakusafw.vocabulary.flow.FlowDescription";
	public static final String IMPORT_NAME = "com.asakusafw.vocabulary.flow.Import";
	public static final String EXPORT_NAME = "com.asakusafw.vocabulary.flow.Export";
	public static final String IN_NAME = "com.asakusafw.vocabulary.flow.In";
	public static final String OUT_NAME = "com.asakusafw.vocabulary.flow.Out";

	public static boolean isFlow(IType type) {
		Set<String> set = new HashSet<String>(2);
		set.add(JOBFLOW_NAME);
		set.add(FLOWPART_NAME);
		return AnnotationUtil.getAnnotation(type, type, set) != null && TypeUtil.isExtends(type, FLOW_DESCRIPTION_NAME);
	}

	public static boolean isJobFlow(IType type) {
		return AnnotationUtil.getAnnotation(type, JOBFLOW_NAME) != null
				&& TypeUtil.isExtends(type, FLOW_DESCRIPTION_NAME);
	}

	public static boolean isFlowPart(IType type) {
		return AnnotationUtil.getAnnotation(type, FLOWPART_NAME) != null
				&& TypeUtil.isExtends(type, FLOW_DESCRIPTION_NAME);
	}

	public static String getJobFlowName(IType type) {
		return AnnotationUtil.getAnnotationValue(type, JOBFLOW_NAME, "name");
	}

	public static List<FlowParameter> getFlowParameters(IType type) {
		IMethod constructor = getConstructor(type);
		if (constructor == null) {
			return Collections.emptyList();
		}

		try {
			ILocalVariable[] parameterList = constructor.getParameters();
			List<FlowParameter> list = new ArrayList<FlowParameter>(parameterList.length);
			for (ILocalVariable parameter : parameterList) {
				String name = parameter.getElementName();
				String typeName = TypeUtil.getVariableTypeName(parameter);
				IAnnotation porter = AnnotationUtil.getAnnotation(type, parameter, IMPORT_NAME);
				if (porter == null) {
					porter = AnnotationUtil.getAnnotation(type, parameter, EXPORT_NAME);
				}

				FlowParameter element = new FlowParameter();
				element.setName(name);
				element.setType(typeName);
				element.setPorterName(AnnotationUtil.<String> getAnnotationValue(porter, "name"));
				element.setPorterClassName(TypeUtil.resolveTypeName(
						AnnotationUtil.<String> getAnnotationValue(porter, "description"), type));

				list.add(element);
			}
			return list;
		} catch (JavaModelException e) {
			return Collections.emptyList();
		}
	}

	private static IMethod getConstructor(IType type) {
		try {
			for (IMethod method : type.getMethods()) {
				if (method.isConstructor()) {
					return method;
				}
			}
		} catch (JavaModelException e) {
			return null;
		}
		return null;
	}
}
