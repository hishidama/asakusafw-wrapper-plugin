package jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

public class ExtensionLoader {
	private static final String CONFIGURATION_POINT_ID = Activator.PLUGIN_ID + ".asakusafwConfiguration";

	private List<AsakusafwConfiguration> configList;

	public List<AsakusafwConfiguration> getConfigurations() {
		if (configList != null) {
			return configList;
		}

		IExtensionPoint point = getExtensionPoint(CONFIGURATION_POINT_ID);

		configList = new ArrayList<AsakusafwConfiguration>();
		for (IExtension extension : point.getExtensions()) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				try {
					Object obj = element.createExecutableExtension("class");
					if (obj instanceof AsakusafwConfiguration) {
						configList.add((AsakusafwConfiguration) obj);
					}
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(e.getStatus());
				}
			}
		}
		Collections.sort(configList, new Comparator<AsakusafwConfiguration>() {
			@Override
			public int compare(AsakusafwConfiguration c0, AsakusafwConfiguration c1) {
				return c0.getConfigurationName().compareTo(c1.getConfigurationName());
			}
		});
		return configList;
	}

	private IExtensionPoint getExtensionPoint(String id) {
		IExtensionRegistry registory = Platform.getExtensionRegistry();
		IExtensionPoint point = registory.getExtensionPoint(id);
		if (point == null) {
			throw new IllegalStateException(id);
		}
		return point;
	}
}
