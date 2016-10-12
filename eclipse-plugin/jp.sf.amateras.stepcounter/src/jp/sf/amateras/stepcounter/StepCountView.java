package jp.sf.amateras.stepcounter;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import jp.sf.amateras.stepcounter.format.ExcelFormatter;
import jp.sf.amateras.stepcounter.preferences.PreferenceConstants;

/**
 * �J�E���g���ʂ�\�����邽�߂�ViewPart
 *
 * @see ViewPart
 */
public class StepCountView extends ViewPart {

	private TabFolder				tabFolder;
	private Table					fileTable;
	private Table					categoryTable;
	private Menu					filePopup;
	private Menu					categoryPopup;
	private MenuItem				copy1;
	private MenuItem				copy2;
	private MenuItem				saveExcel1;
	private MenuItem				saveExcel2;
	private MenuItem				selectAll1;
	private MenuItem				selectAll2;
	private MenuItem				clear1;
	private MenuItem				clear2;
	private MenuItem				open;
	private Clipboard				clipboard;

	private static final String		FILE		= StepCounterPlugin.getResourceString("StepCountView.columnName");		//$NON-NLS-1$
	private static final String		TYPE		= StepCounterPlugin.getResourceString("StepCountView.columnType");		//$NON-NLS-1$
	private static final String		CATEGORY	= StepCounterPlugin.getResourceString("StepCountView.columnCategory");	//$NON-NLS-1$
	private static final String		STEP		= StepCounterPlugin.getResourceString("StepCountView.columnStep");		//$NON-NLS-1$
	private static final String		NONE		= StepCounterPlugin.getResourceString("StepCountView.columnNone");		//$NON-NLS-1$
	private static final String		COMMENT		= StepCounterPlugin.getResourceString("StepCountView.columnComment");	//$NON-NLS-1$
	private static final String		TOTAL		= StepCounterPlugin.getResourceString("StepCountView.columnTotal");	//$NON-NLS-1$

	private HashMap<String, IFile>	files		= new HashMap<String, IFile>();

	private List<CountResult>		results		= new ArrayList<CountResult>();

	/**
	 * �R���X�g���N�^
	 */
	public StepCountView() {
		super();
	}

	/**
	 * ViewPart�̒��g���쐬�B
	 *
	 * @see ViewPart#createPartControl
	 */
	public void createPartControl(Composite parent) {
		// �^�u���쐬
		tabFolder = new TabFolder(parent, SWT.NULL);

		TabItem tabItem1 = new TabItem(tabFolder, SWT.NULL);
		tabItem1.setText(StepCounterPlugin.getResourceString("StepCountView.tabFile"));

		Composite composite1 = new Composite(tabFolder, SWT.NULL);
		composite1.setLayout(new FillLayout());
		tabItem1.setControl(composite1);

		// �t�@�C���ʂ̃e�[�u�����쐬
		fileTable = new Table(composite1, SWT.FULL_SELECTION | SWT.MULTI);
		fileTable.setHeaderVisible(true);
		fileTable.setLinesVisible(true);
		String[] cols1 = { FILE, TYPE, CATEGORY, STEP, NONE, COMMENT, TOTAL };
		for (int i = 0; i < cols1.length; i++) {
			TableColumn col = null;
			if (i == 0 || i == 1 || i == 2) {
				col = new TableColumn(fileTable, SWT.LEFT);
			} else {
				col = new TableColumn(fileTable, SWT.RIGHT);
			}
			col.setText(cols1[i]);
			if (i == 0) {
				col.setWidth(250);
			} else {
				col.setWidth(80);
			}
			col.addSelectionListener(new FileTableHeaderListener());
		}

		// �J�e�S���ʂ̃e�[�u�����쐬
		TabItem tabItem2 = new TabItem(tabFolder, SWT.NULL);
		tabItem2.setText(StepCounterPlugin.getResourceString("StepCountView.tabCategory"));

		Composite composite2 = new Composite(tabFolder, SWT.NULL);
		composite2.setLayout(new FillLayout());
		tabItem2.setControl(composite2);

		categoryTable = new Table(composite2, SWT.FULL_SELECTION | SWT.MULTI);
		categoryTable.setHeaderVisible(true);
		categoryTable.setLinesVisible(true);
		String[] cols2 = { CATEGORY, STEP, NONE, COMMENT, TOTAL };
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

		// �N���b�v�{�[�h�̏���
		clipboard = new Clipboard(parent.getDisplay());

		// �e�[�u���Ƀ|�b�v�A�b�v���j���[��ǉ�
		{
			filePopup = new Menu(fileTable.getShell(), SWT.POP_UP);

			open = new MenuItem(filePopup, SWT.PUSH);
			open.setText(StepCounterPlugin.getResourceString("StepCountView.menuOpen")); //$NON-NLS-1$
			open.addSelectionListener(new TableOpenListener());

			copy1 = new MenuItem(filePopup, SWT.PUSH);
			copy1.setText(StepCounterPlugin.getResourceString("StepCountView.menuCopy")); //$NON-NLS-1$
			copy1.addSelectionListener(new TableCopyListener(fileTable,
					clipboard));

			saveExcel1 = new MenuItem(filePopup, SWT.PUSH);
			saveExcel1.setText(StepCounterPlugin.getResourceString("StepCountView.menuExcel")); //$NON-NLS-1$
			saveExcel1.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					saveToExcel();
				}
			});

			selectAll1 = new MenuItem(filePopup, SWT.PUSH);
			selectAll1.setText(StepCounterPlugin.getResourceString("StepCountView.menuSelectAll")); //$NON-NLS-1$
			selectAll1.addSelectionListener(new TableSelectAllListener(
					fileTable));

			new MenuItem(filePopup, SWT.SEPARATOR);

			clear1 = new MenuItem(filePopup, SWT.PUSH);
			clear1.setText(StepCounterPlugin.getResourceString("StepCountView.menuClear")); //$NON-NLS-1$
			clear1.addSelectionListener(new TableClearListener());

			fileTable.addMouseListener(new TableMouseListener1());
		}
		{
			categoryPopup = new Menu(categoryTable.getShell(), SWT.POP_UP);

			copy2 = new MenuItem(categoryPopup, SWT.PUSH);
			copy2.setText(StepCounterPlugin.getResourceString("StepCountView.menuCopy")); //$NON-NLS-1$
			copy2.addSelectionListener(new TableCopyListener(categoryTable,
					clipboard));

			saveExcel2 = new MenuItem(categoryPopup, SWT.PUSH);
			saveExcel2.setText(StepCounterPlugin.getResourceString("StepCountView.menuExcel")); //$NON-NLS-1$
			saveExcel2.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					saveToExcel();
				}
			});

			selectAll2 = new MenuItem(categoryPopup, SWT.PUSH);
			selectAll2.setText(StepCounterPlugin.getResourceString("StepCountView.menuSelectAll")); //$NON-NLS-1$
			selectAll2.addSelectionListener(new TableSelectAllListener(
					categoryTable));

			new MenuItem(categoryPopup, SWT.SEPARATOR);

			clear2 = new MenuItem(categoryPopup, SWT.PUSH);
			clear2.setText(StepCounterPlugin.getResourceString("StepCountView.menuClear")); //$NON-NLS-1$
			clear2.addSelectionListener(new TableClearListener());

			categoryTable.addMouseListener(new TableMouseListener2());
		}
	}

	private void saveToExcel() {
		// �G�N�X�|�[�g��̃t�@�C�����w��
		FileDialog dialog = new FileDialog(
				Display.getDefault().getActiveShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] { "*.xls" });
		String path = dialog.open();
		if (path != null) {
			ExcelFormatter formatter = new ExcelFormatter();
			byte[] data = formatter.format(results.toArray(new CountResult[results.size()]));
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
	 * �J�E���g�����s
	 *
	 * @param selection ISelection
	 */
	public void count(ISelection selection) {
		// ���ׂč폜
		fileTable.removeAll();
		files.clear();
		results.clear();
		categoryTable.removeAll();

		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection iSel = (IStructuredSelection)selection;

			@SuppressWarnings("unchecked")
			Iterator<Object> ite = iSel.iterator();

			long totalStep = 0;
			long totalComment = 0;
			long totalNone = 0;
			List<CategoryStepDto> categoryResult = new ArrayList<CategoryStepDto>();

			while (ite.hasNext()) {
				Object obj = ite.next();
				CountResult result = null;
				if (obj instanceof ICompilationUnit) {
					// Java�\�[�X�t�@�C���iJDT�j
					ICompilationUnit file = (ICompilationUnit)obj;
					result = countFile((IFile)file.getResource(), categoryResult);
				} else if (obj instanceof IPackageFragment) {
					// Java�p�b�P�[�W�iJDT�j
					IPackageFragment pkg = (IPackageFragment)obj;
					result = countPackage(pkg, categoryResult);
				} else if (obj instanceof IFile) {
					// �t�@�C��
					result = countFile((IFile)obj, categoryResult);
				} else if (obj instanceof IContainer) {
					// �f�B���N�g��
					result = countFolder((IContainer)obj, categoryResult);
				}
				if (result != null) {
					totalStep += result.getStep();
					totalNone += result.getNon();
					totalComment += result.getComment();
				}
			}
			{
				// �t�@�C���P�ʂ̍��v�s��\��
				String[] data = {
						TOTAL,
						"", //$NON-NLS-1$
						"", //$NON-NLS-1$
						String.valueOf(totalStep), String.valueOf(totalNone),
						String.valueOf(totalComment),
						String.valueOf(totalStep + totalNone + totalComment) };
				TableItem item = new TableItem(fileTable, SWT.NULL);
				item.setText(data);
			}

			// �J�e�S���ʂ̏W�v�l��\��
			totalStep = 0;
			totalNone = 0;
			totalComment = 0;

			// �J�e�S�����\�[�g
			CategoryDto.sort(categoryResult);

			for (CategoryStepDto categoryDto : categoryResult) {
				String[] categoryData = {
						categoryDto.getCategory(),
						String.valueOf(categoryDto.getStep()),
						String.valueOf(categoryDto.getNone()),
						String.valueOf(categoryDto.getComment()),
						String.valueOf(categoryDto.getStep()
								+ categoryDto.getNone()
								+ categoryDto.getComment()), };

				TableItem categoryItem = new TableItem(categoryTable, SWT.NULL);
				categoryItem.setText(categoryData);

				totalStep += categoryDto.getStep();
				totalNone += categoryDto.getNone();
				totalComment += categoryDto.getComment();
			}
			{
				// �J�e�S���P�ʂ̍��v�s��\��
				String[] data = { TOTAL, String.valueOf(totalStep),
						String.valueOf(totalNone),
						String.valueOf(totalComment),
						String.valueOf(totalStep + totalNone + totalComment) };
				TableItem item = new TableItem(categoryTable, SWT.NULL);
				item.setText(data);
			}
		}
	}

	/**
	 * �P�t�@�C�����J�E���g
	 *
	 * @param file �t�@�C��
	 * @return ���̃t�@�C���̃J�E���g����
	 */
	private CountResult countFile(IFile file, List<CategoryStepDto> categoryResult) {
		if(files.containsValue(file)){
			return null;
		}
		try {
			StepCounter counter = StepCounterFactory.getCounter(file.getName());
			if (counter != null) {
				// �Ή�����J�E���^�����݂���ꍇ
				CountResult result = counter.count(
						file.getLocation().makeAbsolute().toFile(),
						file.getCharset());
				if (result == null) {
					// �J�E���g�ΏۊO
					return null;
				}

				results.add(result);

				String type = result.getFileType();
				String category = result.getCategory();
				long comment = result.getComment();
				long none = result.getNon();
				long step = result.getStep();

				CategoryStepDto categoryDto = CategoryStepDto.getDto(categoryResult, result.getCategory());
				categoryDto.setStep(categoryDto.getStep() + result.getStep());
				categoryDto.setNone(categoryDto.getNone() + result.getNon());
				categoryDto.setComment(categoryDto.getComment() + result.getComment());

				String[] data = {
						file.getFullPath().toString(),
						//FILE.getName(),
						type, category, String.valueOf(step),
						String.valueOf(none), String.valueOf(comment),
						String.valueOf(step + none + comment) };
				TableItem item = new TableItem(fileTable, SWT.NULL);
				item.setText(data);
				files.put(file.getFullPath().toString(), file);

				return result;

			} else {
				// �Ή�����J�E���^�����݂��Ȃ��ꍇ
				String[] data = {
						file.getFullPath().toString(),
						//FILE.getName(),
						StepCounterPlugin.getResourceString("StepCountView.notSupported"), //$NON-NLS-1$
						"", "", //$NON-NLS-1$
						"", //$NON-NLS-1$
						"", //$NON-NLS-1$
						"" //$NON-NLS-1$
				};
				TableItem item = new TableItem(fileTable, SWT.NULL);
				item.setText(data);
				files.put(file.getFullPath().toString(), file);
				return null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(file.getName() + "�ŃG���[���������܂����I");
			return null;
		}
	}

	/**
	 * �t�H���_���J�E���g
	 *
	 * @param container �t�H���_
	 * @return �t�H���_���̃J�E���g���v
	 */
	private CountResult countFolder(IContainer container,
			List<CategoryStepDto> categoryResult) {
		CountResult result = new CountResult();
		try {
			IResource[] children = exceptGeneratedResource(Util.PRIOR_EXTENSION_PAIRS, container.members());
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof IFile) {
					CountResult count = countFile((IFile)children[i],
							categoryResult);
					if (count != null) {
						result.setStep(result.getStep() + count.getStep());
						result.setNon(result.getNon() + count.getNon());
						result.setComment(result.getComment()
								+ count.getComment());
					}
				} else if (children[i] instanceof IContainer) {
					CountResult count = countFolder((IContainer)children[i],
							categoryResult);
					result.setStep(result.getStep() + count.getStep());
					result.setNon(result.getNon() + count.getNon());
					result.setComment(result.getComment() + count.getComment());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	private IResource[] exceptGeneratedResource(Map<String, String> extensionPairs, IResource[] members) {
		if (members == null || members.length == 0)
			return members;
		if (!StepCounterPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.P_IGNORE_GENERATED_FILE))
			return members;
		
		List<IResource> excepted = new ArrayList<IResource>(Arrays.asList(members));
		List<String> priors = gatherPriorExtensionFiles(extensionPairs, members);
		for (Iterator<IResource> itr = excepted.iterator(); itr.hasNext();) {
			IResource member = itr.next();
			if (member instanceof IFile) {
				IFile file = (IFile) member;
				String fileExtension = file.getFileExtension() == null ? null : "." + file.getFileExtension();
				if (extensionPairs.containsValue(fileExtension)) {
					String fileName = file.getFullPath().toString();
					String fileNameWithoutExtension = fileName.substring(0, fileName.length() - fileExtension.length());
					for (Entry<String, String> extensionPair : extensionPairs.entrySet()) {
						if (!fileExtension.equals(extensionPair.getValue())) {
							continue;
						}
						if (priors.contains(fileNameWithoutExtension + extensionPair.getKey())) {
							itr.remove();
							break;
						}
					}
				}
			}
		}
		
		return excepted.toArray(new IResource[excepted.size()]);
	}

	private List<String> gatherPriorExtensionFiles(Map<String, String> extensionPairs, IResource[] members) {
		List<String> priors = new ArrayList<String>(members.length);
		for (IResource member : members) {
			if (member instanceof IFile) {
				String extension = member.getFileExtension() == null ? null : "." + member.getFileExtension();
				if (extension != null) {
					if (extensionPairs.containsKey(extension)) {
						priors.add(member.getFullPath().toString());
					}
				}
			}
		}
		return priors;
	}

	/**
	 * �p�b�P�[�W���J�E���g
	 *
	 * @param pkg �p�b�P�[�W
	 * @return �p�b�P�[�W���̃J�E���g���v
	 */
	private CountResult countPackage(IPackageFragment pkg, List<CategoryStepDto> categoryResult) {
		CountResult result = new CountResult();
		try {
			IFile[] merged = merge(pkg.getCompilationUnits(), pkg.getNonJavaResources());
			for (IFile file : merged) {
				if (!this.files.containsValue(file)) {
					count(file, result, categoryResult);
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	private IFile[] merge (ICompilationUnit[] javaFiles, Object[] nonJavaFiles) {
		int javaFilesLength = javaFiles == null ? 0 : javaFiles.length;
		int nonJavaFilesLength = nonJavaFiles == null ? 0 : nonJavaFiles.length;
		List<IFile> merged = new ArrayList<IFile>(javaFiles.length + nonJavaFiles.length);
		for (int i=0; i<javaFilesLength; i++) {
			merged.add((IFile) javaFiles[i].getResource());
		}
		for (int i=0; i<nonJavaFilesLength; i++) {
			merged.add((IFile) nonJavaFiles[i]);
		}
		return merged.toArray(new IFile[merged.size()]);
	}
	
	private void count(IFile file, CountResult result, List<CategoryStepDto> categoryResult) {
		CountResult count = countFile(file, categoryResult);
		if (count != null) {
			result.setStep(result.getStep() + count.getStep());
			result.setNon(result.getNon() + count.getNon());
			result.setComment(result.getComment() + count.getComment());
		}	}

	/**
	 * @see ViewPart#setFocus
	 */
	public void setFocus() {
		fileTable.setFocus();
	}

	/**
	 * �t�@�C���ʃe�[�u���̃|�b�v�A�b�v���j���[�̏�Ԃ��X�V���܂��B
	 */
	private void updatePopupMenu1() {
		// ���ڂ��P�ȏ゠��΁u�S�đI���v�u�N���A�v�uExcel�t�@�C���ɕۑ��v��������
		TableItem[] items = fileTable.getItems();
		if (items.length == 0) {
			selectAll1.setEnabled(false);
			clear1.setEnabled(false);
			saveExcel1.setEnabled(false);
		} else {
			selectAll1.setEnabled(true);
			clear1.setEnabled(true);
			saveExcel1.setEnabled(true);
		}
		// ���ڂ��P�ł��I������Ă���΁u�R�s�[�v��������
		TableItem[] selection = fileTable.getSelection();
		if (selection.length == 0) {
			copy1.setEnabled(false);
		} else {
			copy1.setEnabled(true);
		}
		// �t�@�C�����P�ł��I������Ă���΁u�J���v��������
		open.setEnabled(false);
		for (int i = 0; i < selection.length; i++) {
			String filePath = selection[i].getText(0);
			if (files.get(filePath) != null) {
				open.setEnabled(true);
				break;
			}
		}
	}

	/**
	 * �J�e�S���ʃe�[�u���̃|�b�v�A�b�v���j���[�̏�Ԃ��X�V���܂��B
	 */
	private void updatePopupMenu2() {
		// ���ڂ��P�ȏ゠��΁u�S�đI���v�u�N���A�v�uExcel�t�@�C���ɕۑ��v��������
		TableItem[] items = categoryTable.getItems();
		if (items.length == 0) {
			selectAll2.setEnabled(false);
			clear2.setEnabled(false);
			saveExcel2.setEnabled(false);
		} else {
			selectAll2.setEnabled(true);
			clear2.setEnabled(true);
			saveExcel2.setEnabled(true);
		}
		// ���ڂ��P�ł��I������Ă���΁u�R�s�[�v��������
		TableItem[] selection = categoryTable.getSelection();
		if (selection.length == 0) {
			copy2.setEnabled(false);
		} else {
			copy2.setEnabled(true);
		}
	}

	/**
	 * �e�[�u���őI����ԂɂȂ��Ă���t�@�C�����G�f�B�^�ŊJ���܂��B
	 */
	private void openEditor() {
		TableItem[] items = fileTable.getSelection();
		for (int i = 0; i < items.length; i++) {
			try {
				String filePath = items[i].getText(0);
				if (files.get(filePath) != null) {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					// Eclipse 3.0�Ή�
					IDE.openEditor(window.getActivePage(),
							(IFile)files.get(filePath), true);
				}
			} catch (Exception ex) {
				// TODO ��O�͈���Ԃ��Ă����܂��c
			}
		}
	}

	/**
	 * �t�@�C���P�ʂ̃e�[�u���̃w�b�_���N���b�N���ꂽ�ۂɃ\�[�g���s�����X�i
	 */
	private class FileTableHeaderListener extends SelectionAdapter {

		private int	sortColumn	= 0;

		private int	sortOrder	= TableComparator.ASC;

		public void widgetSelected(SelectionEvent e) {
			try {
				// �\�[�g����J����������
				TableColumn column = (TableColumn)e.getSource();
				int selectColumn = 0;
				String name = column.getText();
				if (name.equals(FILE)) {
					selectColumn = 0;
				} else if (name.equals(TYPE)) {
					selectColumn = 1;
				} else if (name.equals(CATEGORY)) {
					selectColumn = 2;
				} else if (name.equals(STEP)) {
					selectColumn = 3;
				} else if (name.equals(NONE)) {
					selectColumn = 4;
				} else if (name.equals(COMMENT)) {
					selectColumn = 5;
				} else if (name.equals(TOTAL)) {
					selectColumn = 6;
				}

				if (this.sortColumn != selectColumn) {
					this.sortOrder = TableComparator.ASC;
				}
				this.sortColumn = selectColumn;

				// �f�[�^����������ArrayList�Ɋi�[
				TableItem[] items = fileTable.getItems();
				ArrayList<String[]> list = new ArrayList<String[]>();
				for (int i = 0; i < items.length; i++) {
					list.add(new String[] { items[i].getText(0),
							items[i].getText(1), items[i].getText(2),
							items[i].getText(3), items[i].getText(4),
							items[i].getText(5), items[i].getText(6) });
				}

				// �\�[�g����
				String[][] datas = list.toArray(new String[list.size()][]);
				Arrays.sort(datas, new TableComparator(sortColumn, 3,
						TableComparator.ASC));
				this.sortOrder = this.sortOrder * -1;

				// �f�[�^���ĕ\��
				fileTable.removeAll();
				for (int i = 0; i < datas.length; i++) {
					TableItem item = new TableItem(fileTable, SWT.NULL);
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
				} else if (name.equals(STEP)) {
					selectColumn = 1;
				} else if (name.equals(NONE)) {
					selectColumn = 2;
				} else if (name.equals(COMMENT)) {
					selectColumn = 3;
				} else if (name.equals(TOTAL)) {
					selectColumn = 4;
				}

				if (this.sortColumn != selectColumn) {
					this.sortOrder = TableComparator.ASC;
				}
				this.sortColumn = selectColumn;

				// �f�[�^����������ArrayList�Ɋi�[
				TableItem[] items = categoryTable.getItems();
				ArrayList<String[]> list = new ArrayList<String[]>();
				for (int i = 0; i < items.length; i++) {
					list.add(new String[] { items[i].getText(0),
							items[i].getText(1), items[i].getText(2),
							items[i].getText(3), items[i].getText(4) });
				}

				// �\�[�g����
				String[][] datas = list.toArray(new String[list.size()][]);
				Arrays.sort(datas, new TableComparator(sortColumn, 1,
						TableComparator.ASC));
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

	/** �t�@�C���ʃe�[�u���̃|�b�v�A�b�v���j���[��\�����邽�߂̃}�E�X���X�i */
	private class TableMouseListener1 extends MouseAdapter {
		public void mouseUp(MouseEvent e) {
			if (e.button == 3) {
				updatePopupMenu1();
				filePopup.setVisible(true);
			}
		}

		public void mouseDoubleClick(MouseEvent e) {
			openEditor();
		}
	}

	/** �J�e�S���ʃe�[�u���̃|�b�v�A�b�v���j���[��\�����邽�߂̃}�E�X���X�i */
	private class TableMouseListener2 extends MouseAdapter {
		public void mouseUp(MouseEvent e) {
			if (e.button == 3) {
				updatePopupMenu2();
				categoryPopup.setVisible(true);
			}
		}
	}

	/** �e�[�u���őI�����ꂽ�t�@�C�����J�����߂̃��X�i */
	private class TableOpenListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			openEditor();
		}
	}

	/** �e�[�u���̕\�����e���N���A���邽�߂̃��X�i */
	private class TableClearListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			fileTable.removeAll();
			categoryTable.removeAll();
			files.clear();
			results.clear();
		}
	}
}
