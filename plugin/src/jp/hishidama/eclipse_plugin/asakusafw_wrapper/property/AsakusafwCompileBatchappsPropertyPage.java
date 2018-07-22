package jp.hishidama.eclipse_plugin.asakusafw_wrapper.property;

import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class AsakusafwCompileBatchappsPropertyPage extends PropertyPage {

	public static final String USE_GRALDE_URL_KEY = "AsakusafwCompileBatchappsPropertyPage.USE_GRADLE_URL";
	public static final String COMMAND_LINE_OPTION_KEY = "AsakusafwCompileBatchappsPropertyPage.COMMAND_LINE_OPTIONS";
	public static final String COMPILER_PROPERTIES_KEY = "AsakusafwCompileBatchappsPropertyPage.COMPILER_PROPERTIES";

	private Button useGradleUrlButton;
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

		createUseGradleUrlField(composite);
		createCommandLineOptionField(composite);
		createCompilerPropertyField(composite);
		load(getProject());

		return composite;
	}

	private void createUseGradleUrlField(Composite composite) {
		useGradleUrlButton = new Button(composite, SWT.CHECK);
		useGradleUrlButton.setText("GradleバージョンにPROJECT/.buildtools/gradlew.propertiesを使用する");
	}

	private void createCommandLineOptionField(Composite composite) {
		Label label = new Label(composite, SWT.NONE);
		label.setText("command line options:");

		optionTable = new CommandLineOptionsTable(composite);
		optionTable.addColumn("compile type", 96 * 2, SWT.NONE);
		optionTable.addColumn("option name", 128 + 64, SWT.NONE);
		optionTable.addColumn("value", 128 + 128, SWT.NONE);

		Composite field = new Composite(composite, SWT.NONE);
		// field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		field.setLayout(new FillLayout(SWT.HORIZONTAL));
		optionTable.createButtonArea(field);
	}

	public static class CommandLineOptionRow implements Cloneable {
		public String compileType;
		public String optionName;
		public String optionValue;

		public CommandLineOptionRow(String compileType, String optionName, String optionValue) {
			this.compileType = compileType;
			this.optionName = optionName;
			this.optionValue = optionValue;
		}

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
			return new CommandLineOptionRow(null, null, null);
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
		propertyTable.addColumn("property name", 128 + 128, SWT.NONE);
		propertyTable.addColumn("value", 128 + 64, SWT.NONE);

		Composite field = new Composite(composite, SWT.NONE);
		// field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		field.setLayout(new FillLayout(SWT.HORIZONTAL));
		propertyTable.createButtonArea(field);
	}

	private static class CompilerPropertyRow implements Cloneable {
		public String compileType;
		public String propertyName;
		public String optionValue;

		public CompilerPropertyRow(String compileType, String propertyName, String optionValue) {
			this.compileType = compileType;
			this.propertyName = propertyName;
			this.optionValue = optionValue;
		}

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
			return new CompilerPropertyRow(null, null, null);
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
		useGradleUrlButton.setSelection(true);
		{
			optionTable.removeAll();
			List<CommandLineOptionRow> list = getDefaultCommandLineOption(null);
			for (CommandLineOptionRow row : list) {
				optionTable.addItem(row);
			}
			optionTable.refresh();
		}
		{
			propertyTable.removeAll();
			List<CompilerPropertyRow> list = getDefaultCompilerProperty(null);
			for (CompilerPropertyRow row : list) {
				propertyTable.addItem(row);
			}
			propertyTable.refresh();
		}
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
		useGradleUrlButton.setSelection(getUseGradleUrl(project));
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

	public static boolean getUseGradleUrl(IProject project) {
		String s = getValue(project, USE_GRALDE_URL_KEY);
		if (s == null) {
			return true;
		}
		return Boolean.parseBoolean(s);
	}

	private static List<CommandLineOptionRow> loadCommandLineOption(IProject project, String compileType) {
		int count = getIntValue(project, COMMAND_LINE_OPTION_KEY);
		if (count < 0) {
			return getDefaultCommandLineOption(compileType);
		}
		List<CommandLineOptionRow> list = new ArrayList<CommandLineOptionRow>(count);
		for (int i = 0; i < count; i++) {
			String type = getValue(project, COMMAND_LINE_OPTION_KEY, i, "type");
			if (compileType == null || compileType.equals(type)) {
				list.add(new CommandLineOptionRow(type, getValue(project, COMMAND_LINE_OPTION_KEY, i, "name"), getValue(project, COMMAND_LINE_OPTION_KEY, i, "value")));
			}
		}
		return list;
	}

	private static List<CommandLineOptionRow> getDefaultCommandLineOption(String compileType) {
		List<CommandLineOptionRow> list = new ArrayList<CommandLineOptionRow>(2);
		if (compileType == null || "sparkCompileBatchapps".equals(compileType)) {
			list.add(new CommandLineOptionRow("sparkCompileBatchapps", "--fail-on-error", "true"));
		}
		if (compileType == null || "m3bpCompileBatchapps".equals(compileType)) {
			list.add(new CommandLineOptionRow("m3bpCompileBatchapps", "--fail-on-error", "true"));
		}
		return list;
	}

	private static List<CompilerPropertyRow> loadCompilerProperty(IProject project, String compileType) {
		int count = getIntValue(project, COMPILER_PROPERTIES_KEY);
		if (count < 0) {
			return getDefaultCompilerProperty(compileType);
		}
		List<CompilerPropertyRow> list = new ArrayList<CompilerPropertyRow>(count);
		for (int i = 0; i < count; i++) {
			String type = getValue(project, COMPILER_PROPERTIES_KEY, i, "type");
			if (compileType == null || compileType.equals(type)) {
				list.add(new CompilerPropertyRow(type, getValue(project, COMPILER_PROPERTIES_KEY, i, "name"), getValue(project, COMPILER_PROPERTIES_KEY, i, "value")));
			}
		}
		return list;
	}

	private static List<CompilerPropertyRow> getDefaultCompilerProperty(String compileType) {
		List<CompilerPropertyRow> list = new ArrayList<CompilerPropertyRow>(6);
		if (compileType == null || "m3bpCompileBatchapps".equals(compileType)) {
			String type = "m3bpCompileBatchapps";
			String os = System.getProperty("os.name");
			if (os != null && os.contains("Windows")) {
				list.add(new CompilerPropertyRow(type, "m3bp.native.cmake", "cmake.exe"));
				list.add(new CompilerPropertyRow(type, "m3bp.native.make", "make.exe"));
				list.add(new CompilerPropertyRow(type, "m3bp.native.cmake.CMAKE_SYSTEM_NAME", "Linux"));
				list.add(new CompilerPropertyRow(type, "m3bp.native.cmake.CMAKE_GENERATOR", "Unix Makefiles"));
				list.add(new CompilerPropertyRow(type, "m3bp.native.cmake.CMAKE_C_COMPILER", "x86_64-pc-linux-gnu-gcc.exe"));
				list.add(new CompilerPropertyRow(type, "m3bp.native.cmake.CMAKE_CXX_COMPILER", "x86_64-pc-linux-gnu-g++.exe"));
			} else {
				list.add(new CompilerPropertyRow(type, "m3bp.native.cmake", "cmake"));
				list.add(new CompilerPropertyRow(type, "m3bp.native.make", "make"));
				list.add(new CompilerPropertyRow(type, "m3bp.native.cmake.CMAKE_SYSTEM_NAME", "Linux"));
				list.add(new CompilerPropertyRow(type, "m3bp.native.cmake.CMAKE_GENERATOR", "Unix Makefiles"));
				list.add(new CompilerPropertyRow(type, "m3bp.native.cmake.CMAKE_C_COMPILER", "gcc"));
				list.add(new CompilerPropertyRow(type, "m3bp.native.cmake.CMAKE_CXX_COMPILER", "g++"));
			}
		}
		return list;
	}

	protected void save(IProject project) {
		setValue(project, USE_GRALDE_URL_KEY, Boolean.toString(useGradleUrlButton.getSelection()));
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
				result.add(new CommandLineOptionRow(compileType, "--compiler-properties", sb.toString()));
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
