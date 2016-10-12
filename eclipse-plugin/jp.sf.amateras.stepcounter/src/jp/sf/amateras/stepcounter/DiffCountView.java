package jp.sf.amateras.stepcounter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.sf.amateras.stepcounter.diffcount.DiffCounter;
import jp.sf.amateras.stepcounter.diffcount.DiffCounterUtil;
import jp.sf.amateras.stepcounter.diffcount.object.DiffFileResult;
import jp.sf.amateras.stepcounter.diffcount.object.DiffFolderResult;
import jp.sf.amateras.stepcounter.diffcount.object.DiffStatus;
import jp.sf.amateras.stepcounter.diffcount.renderer.ExcelRenderer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

/**
 * �����J�E���g���ʂ�\�����邽�߂�ViewPart�B
 *
 * @author takanori
 */
public class DiffCountView extends ViewPart implements IPropertyChangeListener {

	private static final String	FILE		= StepCounterPlugin.getResourceString("DiffCountView.columnName");		//$NON-NLS-1$
	private static final String	TYPE		= StepCounterPlugin.getResourceString("DiffCountView.columnType");		//$NON-NLS-1$
	private static final String	STATUS		= StepCounterPlugin.getResourceString("DiffCountView.columnStatus");	//$NON-NLS-1$
	private static final String	CATEGORY	= StepCounterPlugin.getResourceString("DiffCountView.columnCategory");	//$NON-NLS-1$
	private static final String	DIFF_ADD	= StepCounterPlugin.getResourceString("DiffCountView.columnDiffAdd");	//$NON-NLS-1$
	private static final String	DIFF_DEL	= StepCounterPlugin.getResourceString("DiffCountView.columnDiffDel");	//$NON-NLS-1$
	private static final String	TOTAL		= StepCounterPlugin.getResourceString("DiffCountView.total");			//$NON-NLS-1$

	private TabFolder			tabFolder;
	private Table				fileTable;
	private Table				categoryTable;
	private Clipboard			clipboard;
	private Menu				fileMenu;
	private Menu				categoryMenu;
	private MenuItem			openMenuItem;
	private MenuItem			copyMenuItem1;
	private MenuItem			copyMenuItem2;
	private MenuItem			selectAllMenuItem1;
	private MenuItem			selectAllMenuItem2;
	private MenuItem			clearMenuItem1;
	private MenuItem			clearMenuItem2;
	private MenuItem			saveExcelMenuItem1;
	private MenuItem			saveExcelMenuItem2;
	private DiffFolderResult	results		= null;

	/**
	 * �f�t�H���g�R���X�g���N�^�B
	 */
	public DiffCountView() {
		super();
		StepCounterPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
		// �^�u���쐬
		this.tabFolder = new TabFolder(parent, SWT.NULL);

		// �N���b�v�{�[�h�̏���
		this.clipboard = new Clipboard(parent.getDisplay());

		createFileTable(this.tabFolder);
	}

	@Override
	public void dispose() {
		super.dispose();
		StepCounterPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

	/**
	 *
	 * @param tabFolder
	 */
	private void createFileTable(TabFolder tabFolder) {
		TabItem fileTabItem = new TabItem(tabFolder, SWT.NULL);
		fileTabItem.setText(StepCounterPlugin.getResourceString("DiffCountView.tabFile"));

		Composite fileComposite = new Composite(tabFolder, SWT.NULL);
		fileComposite.setLayout(new FillLayout());
		fileTabItem.setControl(fileComposite);

		// �t�@�C���ʂ̃e�[�u�����쐬
		this.fileTable = new Table(fileComposite, SWT.FULL_SELECTION
				| SWT.MULTI);
		this.fileTable.setHeaderVisible(true);
		this.fileTable.setLinesVisible(true);
		this.fileTable.addMouseListener(new FileTableMouseListener());

		String[] fileCols = { FILE, TYPE, STATUS, CATEGORY, DIFF_ADD, DIFF_DEL };
		for (int index = 0; index < fileCols.length; index++) {
			TableColumn col = null;
			if (index == 0 || index == 1 || index == 2 || index == 3) {
				col = new TableColumn(this.fileTable, SWT.LEFT);
			} else {
				col = new TableColumn(this.fileTable, SWT.RIGHT);
			}
			col.setText(fileCols[index]);
			if (index == 0) {
				col.setWidth(250);
			} else {
				col.setWidth(80);
			}

			col.addSelectionListener(new FileTableHeaderListener());
		}

		// �J�e�S���ʂ̃^�u���쐬
		TabItem tabItem2 = new TabItem(tabFolder, SWT.NULL);
		tabItem2.setText(StepCounterPlugin.getResourceString("StepCountView.tabCategory"));

		Composite composite2 = new Composite(tabFolder, SWT.NULL);
		composite2.setLayout(new FillLayout());
		tabItem2.setControl(composite2);

		categoryTable = new Table(composite2, SWT.FULL_SELECTION | SWT.MULTI);
		categoryTable.setHeaderVisible(true);
		categoryTable.setLinesVisible(true);
		categoryTable.addMouseListener(new CategoryTableMouseListener());

		String[] cols2 = { CATEGORY, DIFF_ADD, DIFF_DEL };
		for (int i = 0; i < cols2.length; i++) {
			TableColumn col = null;
			if (i == 0) {
				col = new TableColumn(categoryTable, SWT.LEFT);
			} else {
				col = new TableColumn(categoryTable, SWT.RIGHT);
			}
			col.setText(cols2[i]);
			if (i == 0) {
				col.setWidth(250);
			} else {
				col.setWidth(80);
			}
			col.addSelectionListener(new CategoryTableHeaderListener());
		}

		// �t�@�C���ʃe�[�u���Ƀ|�b�v�A�b�v���j���[��ǉ�
		this.fileMenu = new Menu(this.fileTable.getShell(), SWT.POP_UP);

		this.openMenuItem = new MenuItem(this.fileMenu, SWT.PUSH);
		this.openMenuItem.setText(StepCounterPlugin.getResourceString("DiffCountView.menuOpen")); //$NON-NLS-1$
		this.openMenuItem.addSelectionListener(new TableOpenListener());

		this.copyMenuItem1 = new MenuItem(this.fileMenu, SWT.PUSH);
		this.copyMenuItem1.setText(StepCounterPlugin.getResourceString("DiffCountView.menuCopy")); //$NON-NLS-1$
		this.copyMenuItem1.addSelectionListener(new TableCopyListener(
				this.fileTable, this.clipboard));

		saveExcelMenuItem1 = new MenuItem(this.fileMenu, SWT.PUSH);
		saveExcelMenuItem1.setText(StepCounterPlugin.getResourceString("StepCountView.menuExcel")); //$NON-NLS-1$
		saveExcelMenuItem1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				saveToExcel();
			}
		});

		this.selectAllMenuItem1 = new MenuItem(this.fileMenu, SWT.PUSH);
		this.selectAllMenuItem1.setText(StepCounterPlugin.getResourceString("DiffCountView.menuSelectAll")); //$NON-NLS-1$
		this.selectAllMenuItem1.addSelectionListener(new TableSelectAllListener(
				this.fileTable));

		new MenuItem(this.fileMenu, SWT.SEPARATOR);

		this.clearMenuItem1 = new MenuItem(this.fileMenu, SWT.PUSH);
		this.clearMenuItem1.setText(StepCounterPlugin.getResourceString("DiffCountView.menuClear")); //$NON-NLS-1$
		this.clearMenuItem1.addSelectionListener(new TableClearListener());

		// �J�e�S���ʃe�[�u���Ƀ|�b�v�A�b�v���j���[��ǉ�
		this.categoryMenu = new Menu(this.categoryTable.getShell(), SWT.POP_UP);

		this.copyMenuItem2 = new MenuItem(this.categoryMenu, SWT.PUSH);
		this.copyMenuItem2.setText(StepCounterPlugin.getResourceString("DiffCountView.menuCopy")); //$NON-NLS-1$
		this.copyMenuItem2.addSelectionListener(new TableCopyListener(
				this.categoryTable, this.clipboard));

		saveExcelMenuItem2 = new MenuItem(this.categoryMenu, SWT.PUSH);
		saveExcelMenuItem2.setText(StepCounterPlugin.getResourceString("StepCountView.menuExcel")); //$NON-NLS-1$
		saveExcelMenuItem2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				saveToExcel();
			}
		});

		this.selectAllMenuItem2 = new MenuItem(this.categoryMenu, SWT.PUSH);
		this.selectAllMenuItem2.setText(StepCounterPlugin.getResourceString("DiffCountView.menuSelectAll")); //$NON-NLS-1$
		this.selectAllMenuItem2.addSelectionListener(new TableSelectAllListener(
				this.categoryTable));

		new MenuItem(this.categoryMenu, SWT.SEPARATOR);

		this.clearMenuItem2 = new MenuItem(this.categoryMenu, SWT.PUSH);
		this.clearMenuItem2.setText(StepCounterPlugin.getResourceString("DiffCountView.menuClear")); //$NON-NLS-1$
		this.clearMenuItem2.addSelectionListener(new TableClearListener());
	}

	private void saveToExcel() {
		// �G�N�X�|�[�g��̃t�@�C�����w��
		FileDialog dialog = new FileDialog(
				Display.getDefault().getActiveShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] { "*.xls" });
		String path = dialog.open();
		if (path != null) {
			ExcelRenderer renderer = new ExcelRenderer();
			byte[] data = renderer.render(results);

			FileOutputStream out = null;
			try {
				out = new FileOutputStream(path);
				out.write(data);
			} catch (Exception ex) {
				// TODO RuntimeException�ł����̂��H
				throw new RuntimeException(ex);
			} finally {
				Util.close(out);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		this.fileTable.setFocus();
	}

	/**
	 * �����̃J�E���g���s���܂��B
	 *
	 * @param selection �I��v�f
	 * @param comparePath �����̔�r�Ώۂ̃��[�g�p�X
	 */
	public void count(ISelection selection, String comparePath) {
		this.fileTable.removeAll();
		this.categoryTable.removeAll();

		IStructuredSelection sel;
		if (selection != null && selection instanceof IStructuredSelection) {
			sel = (IStructuredSelection)selection;
		} else {
			return;
		}

		if (comparePath == null || comparePath.length() <= 0) {
			return;
		}

		Object obj = sel.getFirstElement();
		if (obj == null) {
			return;

		} else if (obj instanceof IResource) {
			IResource resource = (IResource)obj;

			// ��r�Ώۂ�ۑ����Ă���
			try {
				resource.setPersistentProperty(new QualifiedName(
						StepCounterPlugin.PLUGIN_ID, "comparePath"),
						comparePath);

			} catch (CoreException ex) {
				ex.printStackTrace();
			}

			String targetPath = resource.getLocation().toOSString();
			this.results = count(targetPath, comparePath);

			// �t�@�C���ʃe�[�u���̍X�V
			List<DiffFileResult> diffFileResults = DiffCounterUtil.convertToList(this.results);
			viewFileTable((IResource)obj, diffFileResults);

			// �J�e�S���ʃe�[�u���̍X�V
			List<CategoryDiffDto> categoryList = new ArrayList<CategoryDiffDto>();
			for (DiffFileResult fileResult : diffFileResults) {
				CategoryDiffDto category = CategoryDiffDto.getDto(
						categoryList,
						fileResult.getCategory() != null ? fileResult.getCategory() : "");
				category.setAddCount(category.getAddCount()
						+ fileResult.getAddCount());
				category.setDelCount(category.getDelCount()
						+ fileResult.getDelCount());
			}

			CategoryDto.sort(categoryList);
			viewCategoryTable(categoryList);
		}
	}

	/**
	 * �t�@�C���ʃe�[�u���̕\�����e���X�V���܂��B
	 *
	 * @param diffElemList �����J�E���g���ʂ̃��X�g
	 */
	private void viewFileTable(IResource resource,
			List<DiffFileResult> diffElemList) {

		String rootPath = resource.getParent().getFullPath().toString();

		if (rootPath.equals("/")) {
			rootPath = "";
		}

		int totalAddCount = 0;
		int totalDelCount = 0;

		for (DiffFileResult fileObj : diffElemList) {
			String path = rootPath + "/" + fileObj.getPath();
			String fileType = (fileObj.getFileType() != null) ? fileObj.getFileType().toString() : "";
			String status = (fileObj.getStatus() != null) ? fileObj.getStatus().toString() : "";
			String category = (fileObj.getCategory() != null) ? fileObj.getCategory() : "";

			String[] data = { path, fileType, status, category,
					String.valueOf(fileObj.getAddCount()),
					String.valueOf(fileObj.getDelCount()) };

			TableItem item = new TableItem(this.fileTable, SWT.NULL);
			item.setText(data);

			totalAddCount += fileObj.getAddCount();
			totalDelCount += fileObj.getDelCount();
		}

		// ���v�s��\��
		{
			String[] totalData = { TOTAL, "", "", "",
					String.valueOf(totalAddCount),
					String.valueOf(totalDelCount) };
			TableItem item = new TableItem(this.fileTable, SWT.NULL);
			item.setText(totalData);
		}
	}

	/**
	 * �J�e�S���ʃe�[�u���̕\�����e���X�V���܂��B
	 *
	 * @param categoryList �J�e�S���ʍ����J�E���g���ʂ̃��X�g
	 */
	private void viewCategoryTable(List<CategoryDiffDto> categoryList) {

		int totalAdd = 0;
		int totalDel = 0;

		for (CategoryDiffDto categoryDto : categoryList) {
			String[] categoryData = { categoryDto.getCategory(),
					String.valueOf(categoryDto.getAddCount()),
					String.valueOf(categoryDto.getDelCount()) };

			TableItem categoryItem = new TableItem(categoryTable, SWT.NULL);
			categoryItem.setText(categoryData);

			totalAdd += categoryDto.getAddCount();
			totalDel += categoryDto.getDelCount();
		}
		{
			// �J�e�S���P�ʂ̍��v�s��\��
			String[] data = { TOTAL, String.valueOf(totalAdd),
					String.valueOf(totalDel) };
			TableItem item = new TableItem(categoryTable, SWT.NULL);
			item.setText(data);
		}
	}

	/**
	 * �w�肳�ꂽ���\�[�X�̍������J�E���g���܂��B
	 *
	 * @param targetPath �����̃J�E���g�Ώۂ̃��[�g�p�X
	 * @param comparePath �����̔�r�Ώۂ̃��[�g�p�X
	 * @return �����J�E���g�̌���
	 */
	private DiffFolderResult count(String targetPath, String comparePath) {
		File oldRoot = new File(comparePath);
		File newRoot = new File(targetPath);

		return DiffCounter.count(oldRoot, newRoot);
	}

	/**
	 * �e�[�u���őI����ԂɂȂ��Ă���t�@�C�����G�f�B�^�ŊJ���܂��B
	 */
	private void openEditor() {
		TableItem[] items = this.fileTable.getSelection();
		for (TableItem tableItem : items) {
			String filePath = tableItem.getText(0);

			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			IFile file = wsRoot.getFile(new Path(filePath));
			if (file != null && file.exists()) {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				try {
					IDE.openEditor(window.getActivePage(), file, true);
				} catch (PartInitException ex) {
					// TODO
				}
			}
		}
	}

	/**
	 * �t�@�C���ʃe�[�u���̃|�b�v�A�b�v���j���[�̏�Ԃ��X�V���܂��B
	 */
	private void updateFileMenu() {
		// ���ڂ�1�ȏ゠��΁u�S�đI���v�u�N���A�v��������
		TableItem[] items = this.fileTable.getItems();
		if (items.length == 0) {
			this.selectAllMenuItem1.setEnabled(false);
			this.clearMenuItem1.setEnabled(false);
			this.saveExcelMenuItem1.setEnabled(false);
		} else {
			this.selectAllMenuItem1.setEnabled(true);
			this.clearMenuItem1.setEnabled(true);
			this.saveExcelMenuItem1.setEnabled(true);
		}

		// ���ڂ�1�ł��I������Ă���΁u�R�s�[�v��������
		TableItem[] selection = this.fileTable.getSelection();
		if (selection.length == 0) {
			this.copyMenuItem1.setEnabled(false);
		} else {
			this.copyMenuItem1.setEnabled(true);
		}

		// �t�@�C�����P�ł��I������Ă���΁u�J���v��������
		openMenuItem.setEnabled(false);
		for (TableItem item : selection) {
			if (item.getText(0).equals(TOTAL)) {
				continue;
			}
			if (item.getText(2).equals(DiffStatus.REMOVED.toString())) {
				continue;
			}
			openMenuItem.setEnabled(true);
			break;
		}
	}

	private void updateCategoryMenu() {
		// ���ڂ�1�ȏ゠��΁u�S�đI���v�u�N���A�v��������
		TableItem[] items = this.categoryTable.getItems();
		if (items.length == 0) {
			this.selectAllMenuItem2.setEnabled(false);
			this.clearMenuItem2.setEnabled(false);
			this.saveExcelMenuItem2.setEnabled(false);
		} else {
			this.selectAllMenuItem2.setEnabled(true);
			this.clearMenuItem2.setEnabled(true);
			this.saveExcelMenuItem2.setEnabled(true);
		}

		// ���ڂ�1�ł��I������Ă���΁u�R�s�[�v��������
		TableItem[] selection = this.categoryTable.getSelection();
		if (selection.length == 0) {
			this.copyMenuItem2.setEnabled(false);
		} else {
			this.copyMenuItem2.setEnabled(true);
		}
	}

	/**
	 * �t�@�C���ʃe�[�u���̃|�b�v�A�b�v���j���[��\�����邽�߂̃}�E�X���X�i�B
	 */
	private class FileTableMouseListener extends MouseAdapter {
		public void mouseUp(MouseEvent e) {
			if (e.button == 3) {
				updateFileMenu();
				DiffCountView.this.fileMenu.setVisible(true);
			}
		}

		public void mouseDoubleClick(MouseEvent e) {
			openEditor();
		}
	}

	/**
	 * �t�@�C���ʃe�[�u���̃|�b�v�A�b�v���j���[��\�����邽�߂̃}�E�X���X�i�B
	 */
	private class CategoryTableMouseListener extends MouseAdapter {
		public void mouseUp(MouseEvent e) {
			if (e.button == 3) {
				updateCategoryMenu();
				DiffCountView.this.categoryMenu.setVisible(true);
			}
		}
	}

	/**
	 * �t�@�C���P�ʂ̃e�[�u���̃w�b�_���N���b�N���ꂽ�ۂɃ\�[�g���s�����X�i
	 */
	private class FileTableHeaderListener extends SelectionAdapter {

		private int	sortColumn	= 0;

		private int	sortOrder	= TableComparator.ASC;

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				// �\�[�g����J����������
				TableColumn column = (TableColumn)e.getSource();
				String name = column.getText();
				int selectColumn = 0;
				if (name.equals(FILE)) {
					selectColumn = 0;
				} else if (name.equals(TYPE)) {
					selectColumn = 1;
				} else if (name.equals(STATUS)) {
					selectColumn = 2;
				} else if (name.equals(CATEGORY)) {
					selectColumn = 3;
				} else if (name.equals(DIFF_ADD)) {
					selectColumn = 4;
				} else if (name.equals(DIFF_DEL)) {
					selectColumn = 5;
				}

				if (this.sortColumn != selectColumn) {
					this.sortOrder = TableComparator.ASC;
				}
				this.sortColumn = selectColumn;

				// �f�[�^����������ArrayList�Ɋi�[
				TableItem[] items = DiffCountView.this.fileTable.getItems();
				ArrayList<String[]> list = new ArrayList<String[]>();
				for (int i = 0; i < items.length; i++) {
					list.add(new String[] { items[i].getText(0),
							items[i].getText(1), items[i].getText(2),
							items[i].getText(3), items[i].getText(4) });
				}

				// �\�[�g����
				String[][] datas = list.toArray(new String[list.size()][]);
				Arrays.sort(datas, new TableComparator(this.sortColumn, 4, this.sortOrder));
				this.sortOrder = this.sortOrder * -1;

				// �f�[�^���ĕ\��
				DiffCountView.this.fileTable.removeAll();
				for (int i = 0; i < datas.length; i++) {
					TableItem item = new TableItem(DiffCountView.this.fileTable, SWT.NULL);
					item.setText(datas[i]);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * �J�e�S���P�ʂ̃e�[�u���̃w�b�_���N���b�N���ꂽ�ۂɃ\�[�g���s�����X�i
	 */
	private class CategoryTableHeaderListener extends SelectionAdapter {

		private int	sortColumn	= 0;

		private int	sortOrder	= TableComparator.ASC;

		public void widgetSelected(SelectionEvent e) {
			try {
				// �\�[�g����J����������
				TableColumn column = (TableColumn)e.getSource();
				int selectColumn = 0;
				String name = column.getText();
				if (name.equals(CATEGORY)) {
					selectColumn = 0;
				} else if (name.equals(DIFF_ADD)) {
					selectColumn = 1;
				} else if (name.equals(DIFF_DEL)) {
					selectColumn = 2;
				}

				if (this.sortColumn != selectColumn) {
					this.sortOrder = TableComparator.ASC;
				}
				this.sortColumn = selectColumn;

				// �f�[�^����������ArrayList�Ɋi�[
				TableItem[] items = categoryTable.getItems();
				ArrayList<String[]> list = new ArrayList<String[]>();
				for (int i = 0; i < items.length; i++) {
					list.add(new String[] {
							items[i].getText(0),
							items[i].getText(1),
							items[i].getText(2)});
				}

				// �\�[�g����
				String[][] datas = list.toArray(new String[list.size()][]);
				Arrays.sort(datas, new TableComparator(this.sortColumn, 1, this.sortOrder));
				this.sortOrder = this.sortOrder * -1;

				// �f�[�^���ĕ\��
				categoryTable.removeAll();
				for (int i = 0; i < datas.length; i++) {
					TableItem item = new TableItem(categoryTable, SWT.NULL);
					item.setText(datas[i]);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * �e�[�u���őI�����ꂽ�t�@�C�����J�����߂̃��X�i�B
	 */
	private class TableOpenListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			openEditor();
		}
	}

	/**
	 * �e�[�u���̕\�����e���N���A���邽�߂̃��X�i�B
	 */
	private class TableClearListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			DiffCountView.this.fileTable.removeAll();
			DiffCountView.this.results = null;
			DiffCountView.this.categoryTable.removeAll();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// TODO implement this method
		
	}

}
