package jp.sf.amateras.stepcounter.preferences;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

public abstract class TableFieldEditor extends FieldEditor {

	protected Table table;
	private Composite buttonBox;
	private Button addButton;
	private Button editButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
	private SelectionListener selectionListener;
	private final String[] columnNames;
	private final int[] columnWidths;
	
	public TableFieldEditor() {
		columnNames = new String[0];
		columnWidths = new int[0];
	}

	public TableFieldEditor(String name, String labelText,
			String[] columnNames, int[] columnWidths, Composite parent) {
		init(name, labelText);
		this.columnNames = columnNames;
		this.columnWidths = columnWidths;
		createControl(parent);
	}
	
	protected abstract String createList(String[][] items);
	protected abstract String[][] parseString(String string);
	protected abstract String[] getNewInputObject();
	protected abstract String[] getChangedInputObject(TableItem tableItem);

	private void createButtons(Composite box) {
		addButton = createPushButton(box, "New");
		editButton = createPushButton(box, "Edit");
		removeButton = createPushButton(box, "Remove");
		upButton = createPushButton(box, "Up");
		downButton = createPushButton(box, "Down");
	}

	protected Button getAddButton() {
		return addButton;
	}

	protected Button getEditButton() {
		return editButton;
	}

	protected Button getRemoveButton() {
		return removeButton;
	}

	protected Button getUpButton() {
		return upButton;
	}

	protected Button getDownButton() {
		return downButton;
	}

	private Button createPushButton(Composite parent, String key) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(key);
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button,
				IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
				SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}
	
	@Override
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) table.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	public void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == addButton) {
					addPressed();
				} else if (widget == editButton) {
          editPressed();
				} else if (widget == removeButton) {
					removePressed();
				} else if (widget == upButton) {
          upPressed();
			  } else if (widget == downButton) {
			  	downPressed();
				} else if (widget == table) {
					selectionChanged();
				}
			}
		};
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 550;
		gridData.heightHint = 200;
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(2, false));

		table = getTableControl(composite);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		table.setLayoutData(gd);

		buttonBox = getButtonBoxControl(composite);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		buttonBox.setLayoutData(gd);
	}

	protected int getNumberOfItems() {
		int numberOfItems = 0;
		if(table != null) {
			numberOfItems = table.getItems().length;
		}
		return numberOfItems;
	}
	
	protected TableItem[] getItems() {
		TableItem[] items = null;
		if(table != null) {
			items = table.getItems();
		}
		return items;
	}
	
	protected void removeTableItems() {
		if(table != null) {
			table.removeAll();
		}
	}

	@Override
	protected void doLoad() {
		if (table != null) {
			String value = getPreferenceStore().getString(getPreferenceName());
			initTable(value);
		}
	}

	@Override
	protected void doLoadDefault() {
		if (table != null) {
			String value = getPreferenceStore().getDefaultString(getPreferenceName());
			initTable(value);
		}
	}

	private void initTable(String value) {
		table.removeAll();
		String[][] items = parseString(value);
		for(String[] columns : items) {
			TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(columns);
		}
	}

	@Override
	protected void doStore() {
		String[][] items = new String[getNumberOfItems()][];
		TableItem[] tableItems = getItems();
		for(int i=0; i<getNumberOfItems(); i++) {
			String[] columns = new String[columnNames.length];
			for(int j=0; j<table.getColumnCount(); j++) {
				columns[j] = tableItems[i].getText(j);
			}
			items[i] = columns;
		}
		getPreferenceStore().setValue(getPreferenceName(), createList(items));
	}

	public Composite getButtonBoxControl(Composite parent) {
		if (buttonBox == null) {
			buttonBox = new Composite(parent, SWT.NULL);
//			buttonBox.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			buttonBox.setLayout(layout);
			createButtons(buttonBox);
			buttonBox.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					addButton = null;
					editButton = null;
					removeButton = null;
					upButton = null;
					downButton = null;
					buttonBox = null;
				}
			});
		} else {
			checkParent(buttonBox, parent);
		}

		selectionChanged();
		return buttonBox;
	}

	public Table getTableControl(Composite parent) {
		if (table == null) {
			table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL
					| SWT.H_SCROLL | SWT.FULL_SELECTION);
			table.setFont(parent.getFont());
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			table.addSelectionListener(getSelectionListener());
			table.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					table = null;
				}
			});
			for (String columnName : columnNames) {
				TableColumn tableColumn = new TableColumn(table, SWT.LEAD);
				tableColumn.setText(columnName);
				tableColumn.setWidth(100);
			}
			if (columnNames.length > 0) {
				TableLayout layout = new TableLayout();
				if (columnNames.length > 1) {
					for (int i = 0; i < (columnNames.length - 1); i++) {
						layout.addColumnData(new ColumnWeightData(0,
								columnWidths[i], false));

					}
				}
				layout.addColumnData(new ColumnWeightData(100,
						columnWidths[columnNames.length - 1], true));
				table.setLayout(layout);
			}
			final TableEditor editor = new TableEditor(table);
			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
		
		} else {
			checkParent(table, parent);
		}
		return table;
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	private SelectionListener getSelectionListener() {
		if (selectionListener == null) {
			createSelectionListener();
		}
		return selectionListener;
	}

	protected Shell getShell() {
		if (addButton == null) {
			return null;
		}
		return addButton.getShell();
	}

	private void addPressed() {
		setPresentsDefaultValue(false);
		String[] newInputObject = getNewInputObject();
		if (newInputObject != null) {
			TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(newInputObject);
			selectionChanged();
		}
	}

	private void editPressed() {
		setPresentsDefaultValue(false);
		int index = table.getSelectionIndex();
		TableItem tableItem = table.getItem(index);
		String[] changedInputObject = getChangedInputObject(tableItem);
		if (changedInputObject != null) {
			tableItem.setText(changedInputObject);
			selectionChanged();
		}
	}

	private void removePressed() {
		setPresentsDefaultValue(false);
		int index = table.getSelectionIndex();
		if (index >= 0) {
			table.remove(index);
			selectionChanged();
		}
	}

	protected void upPressed() {
		swap(true);
	}

	protected void downPressed() {
		swap(false);
	}

	protected void selectionChanged() {
		int index = table.getSelectionIndex();
		int size = table.getItemCount();

		editButton.setEnabled(index >= 0);
		removeButton.setEnabled(index >= 0);
		upButton.setEnabled(size > 1 && index > 0);
		downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
	}

	public void setFocus() {
		if (table != null) {
			table.setFocus();
		}
	}

	private void swap(boolean up) {
		setPresentsDefaultValue(false);
		int index = table.getSelectionIndex();
		int target = up ? index - 1 : index + 1;

		if (index >= 0) {
			TableItem[] selection = table.getSelection();
			if (selection.length == 1) {
				String[] values = new String[columnNames.length];
				for (int j = 0; j < columnNames.length; j++) {
					values[j] = selection[0].getText(j);
				}
				table.remove(index);
				TableItem tableItem = new TableItem(table, SWT.NONE, target);
				tableItem.setText(values);
				table.setSelection(target);
			}
		}
		selectionChanged();
	}

	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getTableControl(parent).setEnabled(enabled);
		addButton.setEnabled(enabled);
		editButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
		upButton.setEnabled(enabled);
		downButton.setEnabled(enabled);
	}

	public void setVisible(boolean visible) {
		table.setVisible(visible);
		addButton.setVisible(visible);
		editButton.setVisible(visible);
		removeButton.setVisible(visible);
		upButton.setVisible(visible);
		downButton.setVisible(visible);

	}
}
