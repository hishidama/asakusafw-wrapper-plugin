package jp.hishidama.eclipse_plugin.asakusafw_wrapper.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.Activator;
import jp.hishidama.eclipse_plugin.dialog.EditDialog;
import jp.hishidama.eclipse_plugin.jface.ModifiableTable;
import jp.hishidama.eclipse_plugin.util.StringUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class AsakusafwCompileBatchappsPropertyPage extends PropertyPage {

	public static final String COMMAND_LINE_OPTION_KEY = "AsakusafwCompileBatchappsPropertyPage.COMMAND_LINE_OPTIONS";
	public static final String COMPILER_PROPERTIES_KEY = "AsakusafwCompileBatchappsPropertyPage.COMPILER_PROPERTIES";

	private CommandLineOptionsTable optionTable;

	private CompilerPropertiesTable propertyTable;

	public AsakusafwCompileBatchappsPropertyPage() {
		setDescription("xxxCompileBatchappsに渡す引数を指定してください。");
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		{
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout layout = new GridLayout();
			layout.numColumns = 1; // 列数
			composite.setLayout(layout);
		}

		createCommandLineOptionField(composite);
		createCompilerPropertyField(composite);
		load(getProject());

		return composite;
	}

	private void createCommandLineOptionField(Composite composite) {
		Label label = new Label(composite, SWT.NONE);
		label.setText("command line options:");

		optionTable = new CommandLineOptionsTable(composite);
		optionTable.addColumn("compile type", 96 * 2, SWT.NONE);
		optionTable.addColumn("option name", 128, SWT.NONE);
		optionTable.addColumn("value", 128, SWT.NONE);

		Composite field = new Composite(composite, SWT.NONE);
		// field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		field.setLayout(new FillLayout(SWT.HORIZONTAL));
		optionTable.createButtonArea(field);
	}

	public static class CommandLineOptionRow implements Cloneable {
		public String compileType;
		public String optionName;
		public String optionValue;

		@Override
		protected CommandLineOptionRow clone() {
			try {
				return (CommandLineOptionRow) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new InternalError();
			}
		}
	}

	private class CommandLineOptionsTable extends ModifiableTable<CommandLineOptionRow> {

		public CommandLineOptionsTable(Composite parent) {
			super(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
			setVisibleDupButton(true);
		}

		@Override
		protected String getText(CommandLineOptionRow element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return element.compileType;
			case 1:
				return element.optionName;
			default:
				return element.optionValue;
			}
		}

		@Override
		protected void doAdd() {
			CommandLineOptionRow row = createElement();
			if (doEditDialog(row)) {
				super.doAdd(row);
			}
		}

		@Override
		protected CommandLineOptionRow createElement() {
			return new CommandLineOptionRow();
		}

		@Override
		protected void editElement(CommandLineOptionRow element) {
			doEditDialog(element);
		}

		private boolean doEditDialog(CommandLineOptionRow row) {
			EditCommandLineOptionDialog dialog = new EditCommandLineOptionDialog(getShell(), row);
			return dialog.open() == Window.OK;
		}

		public class EditCommandLineOptionDialog extends EditDialog {
			private final CommandLineOptionRow row;

			private Text typeText;
			private Text nameText;
			private Text valueText;

			public EditCommandLineOptionDialog(Shell parentShell, CommandLineOptionRow row) {
				super(parentShell, "コマンドラインオプション編集");
				this.row = row;
			}

			@Override
			protected void createFields(Composite composite) {
				typeText = createTextField(composite, "compile type:");
				nameText = createTextField(composite, "option name:");
				valueText = createTextField(composite, "value:");

				typeText.setText(StringUtil.nonNull(row.compileType));
				nameText.setText(StringUtil.nonNull(row.optionName));
				valueText.setText(StringUtil.nonNull(row.optionValue));
			}

			@Override
			protected void refresh() {
				// do nothing
			}

			@Override
			protected boolean validate() {
				return true;
			}

			@Override
			protected void okPressed() {
				row.compileType = typeText.getText().trim();
				row.optionName = nameText.getText().trim();
				row.optionValue = valueText.getText();

				super.okPressed();
			}
		}

		@Override
		protected CommandLineOptionRow dupElement(CommandLineOptionRow element) {
			return element.clone();
		}
	}

	private void createCompilerPropertyField(Composite composite) {
		Label label = new Label(composite, SWT.NONE);
		label.setText("--compiler-properties option:");

		propertyTable = new CompilerPropertiesTable(composite);
		propertyTable.addColumn("compile type", 96 * 2, SWT.NONE);
		propertyTable.addColumn("property name", 128, SWT.NONE);
		propertyTable.addColumn("value", 128, SWT.NONE);

		Composite field = new Composite(composite, SWT.NONE);
		// field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		field.setLayout(new FillLayout(SWT.HORIZONTAL));
		propertyTable.createButtonArea(field);
	}

	private static class CompilerPropertyRow implements Cloneable {
		public String compileType;
		public String propertyName;
		public String optionValue;

		@Override
		protected CompilerPropertyRow clone() {
			try {
				return (CompilerPropertyRow) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new InternalError();
			}
		}
	}

	private class CompilerPropertiesTable extends ModifiableTable<CompilerPropertyRow> {

		public CompilerPropertiesTable(Composite parent) {
			super(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
			setVisibleDupButton(true);
		}

		@Override
		protected String getText(CompilerPropertyRow element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return element.compileType;
			case 1:
				return element.propertyName;
			default:
				return element.optionValue;
			}
		}

		@Override
		protected void doAdd() {
			CompilerPropertyRow row = createElement();
			if (doEditDialog(row)) {
				super.doAdd(row);
			}
		}

		@Override
		protected CompilerPropertyRow createElement() {
			return new CompilerPropertyRow();
		}

		@Override
		protected void editElement(CompilerPropertyRow element) {
			doEditDialog(element);
		}

		private boolean doEditDialog(CompilerPropertyRow row) {
			EditCompilerPropertyDialog dialog = new EditCompilerPropertyDialog(getShell(), row);
			return dialog.open() == Window.OK;
		}

		public class EditCompilerPropertyDialog extends EditDialog {
			private final CompilerPropertyRow row;

			private Text typeText;
			private Text nameText;
			private Text valueText;

			public EditCompilerPropertyDialog(Shell parentShell, CompilerPropertyRow row) {
				super(parentShell, "--compiler-properties編集");
				this.row = row;
			}

			@Override
			protected void createFields(Composite composite) {
				typeText = createTextField(composite, "compile type:");
				nameText = createTextField(composite, "property name:");
				valueText = createTextField(composite, "value:");

				typeText.setText(StringUtil.nonNull(row.compileType));
				nameText.setText(StringUtil.nonNull(row.propertyName));
				valueText.setText(StringUtil.nonNull(row.optionValue));
			}

			@Override
			protected void refresh() {
				// do nothing
			}

			@Override
			protected boolean validate() {
				return true;
			}

			@Override
			protected void okPressed() {
				row.compileType = typeText.getText().trim();
				row.propertyName = nameText.getText().trim();
				row.optionValue = valueText.getText();

				super.okPressed();
			}
		}

		@Override
		protected CompilerPropertyRow dupElement(CompilerPropertyRow element) {
			return element.clone();
		}
	}

	@Override
	protected void performDefaults() {
		optionTable.removeAll();
		List<CommandLineOptionRow> list = getDefaultCommandLineOption();
		for (CommandLineOptionRow row : list) {
			optionTable.addItem(row);
		}
		optionTable.refresh();

		propertyTable.removeAll();
		propertyTable.refresh();

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
		{
			List<CommandLineOptionRow> list = loadCommandLineOption(project, null);
			for (CommandLineOptionRow row : list) {
				optionTable.addItem(row);
			}
			optionTable.refresh();
		}
		{
			List<CompilerPropertyRow> list = loadCompilerProperty(project, null);
			for (CompilerPropertyRow row : list) {
				propertyTable.addItem(row);
			}
			propertyTable.refresh();
		}
	}

	private static List<CommandLineOptionRow> loadCommandLineOption(IProject project, String compileType) {
		int count = getIntValue(project, COMMAND_LINE_OPTION_KEY);
		if (count < 0) {
			List<CommandLineOptionRow> list = getDefaultCommandLineOption();
			List<CommandLineOptionRow> result = new ArrayList<CommandLineOptionRow>(list.size());
			for (CommandLineOptionRow row : list) {
				if (compileType == null || compileType.equals(row.compileType)) {
					result.add(row);
				}
			}
			return result;
		}
		List<CommandLineOptionRow> list = new ArrayList<CommandLineOptionRow>(count);
		for (int i = 0; i < count; i++) {
			String type = getValue(project, COMMAND_LINE_OPTION_KEY, i, "type");
			if (compileType == null || compileType.equals(type)) {
				CommandLineOptionRow row = new CommandLineOptionRow();
				row.compileType = type;
				row.optionName = getValue(project, COMMAND_LINE_OPTION_KEY, i, "name");
				row.optionValue = getValue(project, COMMAND_LINE_OPTION_KEY, i, "value");
				list.add(row);
			}
		}
		return list;
	}

	private static List<CommandLineOptionRow> getDefaultCommandLineOption() {
		List<CommandLineOptionRow> list = new ArrayList<CommandLineOptionRow>(2);
		{
			CommandLineOptionRow row = new CommandLineOptionRow();
			row.compileType = "sparkCompileBatchapps";
			row.optionName = "--fail-on-error";
			row.optionValue = "true";
			list.add(row);
		}
		{
			CommandLineOptionRow row = new CommandLineOptionRow();
			row.compileType = "m3bpCompileBatchapps";
			row.optionName = "--fail-on-error";
			row.optionValue = "true";
			list.add(row);
		}
		return list;
	}

	private static List<CompilerPropertyRow> loadCompilerProperty(IProject project, String compileType) {
		int count = getIntValue(project, COMPILER_PROPERTIES_KEY);
		if (count <= 0) {
			return Collections.emptyList();
		}
		List<CompilerPropertyRow> list = new ArrayList<CompilerPropertyRow>(count);
		for (int i = 0; i < count; i++) {
			String type = getValue(project, COMPILER_PROPERTIES_KEY, i, "type");
			if (compileType == null || compileType.equals(type)) {
				CompilerPropertyRow row = new CompilerPropertyRow();
				row.compileType = type;
				row.propertyName = getValue(project, COMPILER_PROPERTIES_KEY, i, "name");
				row.optionValue = getValue(project, COMPILER_PROPERTIES_KEY, i, "value");
				list.add(row);
			}
		}
		return list;
	}

	protected void save(IProject project) {
		{
			List<CommandLineOptionRow> list = optionTable.getElementList();
			setValue(project, COMMAND_LINE_OPTION_KEY, list.size());
			int i = 0;
			for (CommandLineOptionRow row : list) {
				setValue(project, COMMAND_LINE_OPTION_KEY, i, "type", row.compileType);
				setValue(project, COMMAND_LINE_OPTION_KEY, i, "name", row.optionName);
				setValue(project, COMMAND_LINE_OPTION_KEY, i, "value", row.optionValue);
				i++;
			}
		}
		{
			List<CompilerPropertyRow> list = propertyTable.getElementList();
			setValue(project, COMPILER_PROPERTIES_KEY, list.size());
			int i = 0;
			for (CompilerPropertyRow row : list) {
				setValue(project, COMPILER_PROPERTIES_KEY, i, "type", row.compileType);
				setValue(project, COMPILER_PROPERTIES_KEY, i, "name", row.propertyName);
				setValue(project, COMPILER_PROPERTIES_KEY, i, "value", row.optionValue);
				i++;
			}
		}
	}

	public static List<CommandLineOptionRow> getComandLineOptions(IProject project, String compileType) {
		List<CommandLineOptionRow> result = loadCommandLineOption(project, compileType);

		List<CompilerPropertyRow> list = loadCompilerProperty(project, compileType);
		if (!list.isEmpty()) {
			StringBuilder sb = new StringBuilder(32 * list.size());
			for (CompilerPropertyRow row : list) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(row.propertyName);
				sb.append('=');
				sb.append(row.optionValue);
			}
			if (sb.length() > 0) {
				CommandLineOptionRow row = new CommandLineOptionRow();
				row.compileType = compileType;
				row.optionName = "--compiler-properties";
				row.optionValue = sb.toString();
				result.add(row);
			}
		}

		return result;
	}

	private static int getIntValue(IProject project, String key) {
		String s = getValue(project, key);
		if (s == null) {
			return -1;
		}
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 0;
		}
	}

	private static String getValue(IProject project, String group, int n, String name) {
		String key = String.format("%s.%d.%s", group, n, name);
		return getValue(project, key);
	}

	private static String getValue(IProject project, String key) {
		try {
			return project.getPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, key));
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
			return null;
		}
	}

	private static void setValue(IProject project, String key, int value) {
		setValue(project, key, Integer.toString(value));
	}

	private static void setValue(IProject project, String group, int n, String name, String value) {
		String key = String.format("%s.%d.%s", group, n, name);
		setValue(project, key, StringUtil.nonNull(value));
	}

	private static void setValue(IProject project, String key, String value) {
		try {
			project.setPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, key), value);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}
}
