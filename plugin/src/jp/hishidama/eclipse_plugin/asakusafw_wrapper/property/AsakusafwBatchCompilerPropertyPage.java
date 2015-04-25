package jp.hishidama.eclipse_plugin.asakusafw_wrapper.property;

import java.io.File;
import java.text.MessageFormat;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;
import jp.hishidama.eclipse_plugin.util.StringUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class AsakusafwBatchCompilerPropertyPage extends PropertyPage {

	public static final String LINKING_RESOURCES_KEY = "AsakusafwBatchCompilerPropertyPage.LINKING_RESOURCES";
	public static final String PLUGIN_LOCATIONS_KEY = "AsakusafwBatchCompilerPropertyPage.PLUGIN_LOCATIONS";
	public static final String COMPILE_JAVA_KEY = "AsakusafwBatchCompilerPropertyPage.COMPILE_JAVA";

	private Text linkingResourcesText;
	private Text pluginLocationsText;
	private Button compileJavaButton;

	public AsakusafwBatchCompilerPropertyPage() {
		setDescription(MessageFormat.format("Asakusa FrameworkのBatchCompilerに渡す引数を指定してください。（パス区切り文字は「{0}」）",
				File.pathSeparator));
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		{
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout layout = new GridLayout();
			layout.numColumns = 2; // 列数
			composite.setLayout(layout);
		}

		createLinkingResourcesField(composite);
		createPluginLocationsField(composite);
		createCompileJavaField(composite);
		load(getProject());

		return composite;
	}

	private void createLinkingResourcesField(Composite composite) {
		Label label = new Label(composite, SWT.NONE);
		label.setText("linking resources:");

		linkingResourcesText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		linkingResourcesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createPluginLocationsField(Composite composite) {
		Label label = new Label(composite, SWT.NONE);
		label.setText("plugin locations:");

		pluginLocationsText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		pluginLocationsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createCompileJavaField(Composite composite) {
		new Label(composite, SWT.NONE).setText("compileJava:");
		compileJavaButton = new Button(composite, SWT.CHECK);
		compileJavaButton.setText("バッチコンパイル前にJavaソースのコンパイルを行う（Shafuがインストールされている場合のみ）");
	}

	@Override
	protected void performDefaults() {
		IProject project = getProject();
		linkingResourcesText.setText(StringUtil.nonNull(getDefaultLinkingResources(project)));
		pluginLocationsText.setText("");
		compileJavaButton.setSelection(true);

		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		IProject project = getProject();
		save(project);

		return true;
	}

	private IProject getProject() {
		IAdaptable element = getElement();
		if (element instanceof IResource) {
			return ((IResource) getElement()).getProject();
		}
		if (element instanceof IJavaElement) {
			return ((IJavaElement) element).getJavaProject().getProject();
		}
		IProject project = (IProject) element.getAdapter(IProject.class);
		if (project != null) {
			return project;
		}
		throw new UnsupportedOperationException("未対応:" + element.getClass());
	}

	protected void load(IProject project) {
		linkingResourcesText.setText(StringUtil.nonNull(getLinkingResources(project)));
		pluginLocationsText.setText(StringUtil.nonNull(getPluginLocations(project)));
		compileJavaButton.setSelection(getCompileJava(project));
	}

	public static String getLinkingResources(IProject project) {
		String value = getValue(project, LINKING_RESOURCES_KEY);
		if (value != null) {
			return value;
		}
		return getDefaultLinkingResources(project);
	}

	private static String getDefaultLinkingResources(IProject project) {
		return "build/classes/main";
	}

	public static String getPluginLocations(IProject project) {
		return getValue(project, PLUGIN_LOCATIONS_KEY);
	}

	public static boolean getCompileJava(IProject project) {
		String s = getValue(project, COMPILE_JAVA_KEY);
		if (s == null) {
			return true;
		}
		return Boolean.parseBoolean(s);
	}

	protected void save(IProject project) {
		setValue(project, LINKING_RESOURCES_KEY, linkingResourcesText.getText());
		setValue(project, PLUGIN_LOCATIONS_KEY, pluginLocationsText.getText());
		setValue(project, COMPILE_JAVA_KEY, Boolean.toString(compileJavaButton.getSelection()));
	}

	private static String getValue(IProject project, String key) {
		try {
			return project.getPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, key));
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
			return null;
		}
	}

	private static void setValue(IProject project, String key, String value) {
		try {
			project.setPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, key), value);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}
}
