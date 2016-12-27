package jp.hishidama.eclipse_plugin.asakusafw_wrapper.property;

import java.util.ArrayList;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension.AsakusafwConfiguration.Library;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.LogUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.task.DMDLErrorCheckTask;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.internal.util.ParserClassUtil;
import jp.hishidama.eclipse_plugin.asakusafw_wrapper.util.BuildPropertiesUtil;
import jp.hishidama.eclipse_plugin.dialog.NewVariableEntryDialog;
import jp.hishidama.eclipse_plugin.dialog.ProjectFileSelectionDialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class AsakusafwDmdlParserPropertyPage extends PropertyPage {

	private Text buildProperties;
	private CheckboxTableViewer classpathViewer;
	private Button removeButton;

	public AsakusafwDmdlParserPropertyPage() {
		setDescription("Asakusa FrameworkのDmdlParserに関する設定を指定してください。");
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		{
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout layout = new GridLayout();
			layout.numColumns = 3; // 列数
			composite.setLayout(layout);
		}

		IProject project = getProject();
		createBuildPropertiesField(composite, project);
		createClasspathTable(composite, project);

		return composite;
	}

	private void createBuildPropertiesField(Composite composite, final IProject project) {
		Label label = new Label(composite, SWT.NONE);
		label.setText("build.properties path:");

		buildProperties = new Text(composite, SWT.SINGLE | SWT.BORDER);
		buildProperties.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String value = BuildPropertiesUtil.getBuildPropertiesFileName(project);
		if (value != null) {
			buildProperties.setText(value);
		}

		final Button button = new Button(composite, SWT.NONE);
		button.setText("Browse");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ProjectFileSelectionDialog dialog = new ProjectFileSelectionDialog(getShell(), project);
				dialog.setTitle("build.properties Selection");
				dialog.setMessage("Select the build.properties for Asakusa Framework:");
				dialog.setAllowMultiple(false);
				dialog.setHelpAvailable(false);
				dialog.setInitialSelection(buildProperties.getText());
				dialog.open();
				String[] r = dialog.getResult();
				if (r != null && r.length >= 1) {
					buildProperties.setText(r[0]);
				}

				e.doit = true;
			}
		});
	}

	private void createClasspathTable(Composite composite, final IProject project) {
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		label.setText("jar files (classpath):");

		{
			Composite rows = new Composite(composite, SWT.NONE);
			{
				rows.setLayoutData(new GridData(GridData.FILL_BOTH));
				GridLayout layout = new GridLayout(1, false);
				layout.marginWidth = 0;
				layout.marginHeight = 0;
				layout.horizontalSpacing = 0;
				layout.verticalSpacing = 0;
				rows.setLayout(layout);
			}
			{
				classpathViewer = CheckboxTableViewer.newCheckList(rows, SWT.BORDER | SWT.MULTI);
				Table table = classpathViewer.getTable();
				GridData tableGrid = new GridData(GridData.FILL_BOTH);
				tableGrid.widthHint = 380;
				tableGrid.minimumHeight = 20 * 9;
				table.setLayoutData(tableGrid);
				table.setLinesVisible(true);
				initTable(classpathViewer, project);
				classpathViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						if (removeButton != null) {
							ISelection selection = event.getSelection();
							removeButton.setEnabled(!selection.isEmpty());
						}
					}
				});

				Composite cols = new Composite(rows, SWT.NONE);
				{
					GridLayout layout = new GridLayout(2, false);
					layout.marginWidth = 0;
					cols.setLayout(layout);
				}
				Button button1 = new Button(cols, SWT.NONE);
				button1.setText("check all");
				button1.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						classpathViewer.setAllChecked(true);
						e.doit = true;
					}
				});

				Button button2 = new Button(cols, SWT.NONE);
				button2.setText("uncheck all");
				button2.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						classpathViewer.setAllChecked(false);
						e.doit = true;
					}
				});
			}
			{
				new Label(rows, SWT.NONE).setText("※DmdlParserを実行する際には、上記のライブラリーの他にプロジェクトのビルドパスも追加されます。");
			}
		}

		{
			Composite rows = new Composite(composite, SWT.NONE);
			GridLayout layout = new GridLayout(1, false);
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 5;
			rows.setLayout(layout);
			rows.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			Button button1 = new Button(rows, SWT.NONE);
			button1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
			button1.setText("Add JARs...");
			button1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ProjectFileSelectionDialog dialog = new ProjectFileSelectionDialog(getShell(), project);
					dialog.setTitle("JAR Selection");
					dialog.setMessage("Choose the archives to be added to the classpath of DmdlCompiler:");
					dialog.setAllowMultiple(true);
					dialog.setHelpAvailable(false);
					dialog.setInitialSelection(buildProperties.getText());
					dialog.addFileterExtension("jar", "zip");
					dialog.open();
					String[] r = dialog.getResult();
					if (r != null) {
						for (String file : r) {
							classpathViewer.add(file);
						}
					}

					e.doit = true;
				}
			});

			Button button2 = new Button(rows, SWT.NONE);
			button2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
			button2.setText("Add External JARs...");
			button2.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
					dialog.setText("JAR Selection");
					dialog.setFilterExtensions(new String[] { "*.jar; *.zip", "*.*" });
					String file = dialog.open();
					if (file != null) {
						classpathViewer.add(file);
					}

					e.doit = true;
				}
			});

			Button button3 = new Button(rows, SWT.NONE);
			button3.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
			button3.setText("Add Variable...");
			button3.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					NewVariableEntryDialog dialog = new NewVariableEntryDialog(getShell());
					dialog.setTitle("JAR Selection");
					dialog.open();
					IPath[] r = dialog.getResult();
					if (r != null) {
						for (IPath path : r) {
							String file = path.toPortableString();
							classpathViewer.add(file);
						}
					}

					e.doit = true;
				}
			});

			Button button4 = new Button(rows, SWT.NONE);
			button4.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
			button4.setText("Remove");
			button4.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) classpathViewer.getSelection();
					classpathViewer.remove(selection.toArray());
					e.doit = true;
				}
			});
			button4.setEnabled(false);
			removeButton = button4;
		}
	}

	@Override
	protected void performDefaults() {
		IProject project = getProject();

		String path = BuildPropertiesUtil.getDefaultBuildPropertiesFileName(project);
		buildProperties.setText(path);

		initTableDefault(classpathViewer, project);

		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		IProject project = getProject();

		BuildPropertiesUtil.saveBuildPropertiesFileName(project, buildProperties.getText());
		save(classpathViewer, project);

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

	public static void initTable(CheckboxTableViewer viewer, IProject project) {
		List<Library> libs = ParserClassUtil.getLibraries(project);
		setTable(viewer, libs);
	}

	public static void initTableDefault(CheckboxTableViewer viewer, IProject project) {
		List<Library> libs = ParserClassUtil.getDefaultLibraries(project);
		setTable(viewer, libs);
	}

	public static void initTableDefault(CheckboxTableViewer viewer, IProject project, AsakusafwConfiguration c) {
		List<Library> libs = ParserClassUtil.getDefaultLibraries(project, c);
		setTable(viewer, libs);
	}

	private static void setTable(CheckboxTableViewer viewer, List<Library> libs) {
		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				return (String[]) inputElement;
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}
		});

		List<String> path = new ArrayList<String>();
		for (Library lib : libs) {
			path.add(lib.path);
		}
		viewer.setInput(path.toArray(new String[path.size()]));

		TableItem[] items = viewer.getTable().getItems();
		for (int i = 0; i < libs.size(); i++) {
			boolean state = libs.get(i).selected;
			items[i].setChecked(state);
		}
	}

	public static void save(CheckboxTableViewer viewer, IProject project) {
		List<Library> list = new ArrayList<Library>();

		TableItem[] items = viewer.getTable().getItems();
		for (TableItem item : items) {
			String path = (String) item.getData();
			boolean check = item.getChecked();
			Library lib = new Library(path, check);
			list.add(lib);
		}
		ParserClassUtil.setLibraries(project, list);

		try {
			project.setSessionProperty(DMDLErrorCheckTask.PASER_KEY, null);
		} catch (CoreException e) {
			LogUtil.log(e.getStatus());
		}
	}
}
