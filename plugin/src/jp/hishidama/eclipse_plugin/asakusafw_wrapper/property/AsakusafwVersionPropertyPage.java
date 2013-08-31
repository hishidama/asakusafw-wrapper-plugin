package jp.hishidama.eclipse_plugin.asakusafw_wrapper.property;

import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util.ParserClassUtil;
import jp.hishidama.eclipse_plugin.util.SwtCheckedTableUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PropertyPage;

public class AsakusafwVersionPropertyPage extends PropertyPage {

	private Table versionTable;

	public AsakusafwVersionPropertyPage() {
		setDescription("Asakusa Frameworkのバージョンを指定してください。");
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

		createVersionTable(composite);

		return composite;
	}

	private void createVersionTable(Composite composite) {
		{
			Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			label.setText("version name:");
		}

		versionTable = new Table(composite, SWT.BORDER | SWT.CHECK);
		GridData grid = GridDataFactory.swtDefaults().minSize(256, 20 * 4).hint(256, 20 * 4).create();
		versionTable.setLayoutData(grid);
		versionTable.setHeaderVisible(false);
		versionTable.setLinesVisible(true);
		SwtCheckedTableUtil.setSingleCheckedTable(versionTable);

		TableColumn col = new TableColumn(versionTable, SWT.NONE);
		col.setWidth(256);
		col.setText("version name");

		IProject project = getProject();
		String defaultName = ParserClassUtil.getConfigurationName(project);

		List<AsakusafwConfiguration> list = Activator.getExtensionLoader().getConfigurations();
		for (AsakusafwConfiguration c : list) {
			String name = null;
			try {
				name = c.getConfigurationName();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (name == null) {
				continue;
			}
			TableItem item = new TableItem(versionTable, SWT.NONE);
			item.setText(0, name);
			item.setData(c);
			if (name.equals(defaultName)) {
				item.setChecked(true);
			}
		}
	}

	@Override
	protected void performDefaults() {
		IProject project = getProject();

		for (TableItem item : versionTable.getItems()) {
			item.setChecked(false);
		}
		AsakusafwConfiguration dc = ParserClassUtil.getDefaultConfiguration(project);
		if (dc != null) {
			String dname = dc.getConfigurationName();
			for (TableItem item : versionTable.getItems()) {
				AsakusafwConfiguration c = (AsakusafwConfiguration) item.getData();
				if (dname.equals(c.getConfigurationName())) {
					item.setChecked(true);
					break;
				}
			}
		}

		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		IProject project = getProject();

		{
			String versionName = null;
			for (TableItem item : versionTable.getItems()) {
				if (item.getChecked()) {
					AsakusafwConfiguration c = (AsakusafwConfiguration) item.getData();
					versionName = c.getConfigurationName();
					break;
				}
			}

			if (versionName == null) {
				versionName = "";
			}
			ParserClassUtil.setConfigurationName(project, versionName);
		}

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
}
