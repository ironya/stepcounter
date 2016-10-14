package jp.sf.amateras.stepcounter.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import jp.sf.amateras.stepcounter.StepCounterPlugin;
import jp.sf.amateras.stepcounter.Util;

public class ExtensionPairsEditor extends TableFieldEditor {
	
	private static String[] columnNames = new String[] { StepCounterPlugin.getResourceString("ExtensionPairsEditor.columnNameFrom"), StepCounterPlugin.getResourceString("ExtensionPairsEditor.columnNameTo"), StepCounterPlugin.getResourceString("ExtensionPairsEditor.columnNameComment") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static int[] columnWidths = new int[] { 100, 100, 150 };
	
	public ExtensionPairsEditor(String name, String labelText, Composite parent) {
		super(name, labelText, columnNames, columnWidths, parent);
	}
	
	@Override
	protected String createList(String[][] items) {
		StringBuilder listString = new StringBuilder();
		for(String[] columns : items) {
			if (listString.length() > 0) {
				listString.append(Util.PAIR_SEPARATOR);
			}
			for(int i=0; i<columns.length; i++) {
				listString.append(columns[i]);
				if (i+1<columns.length) {
					listString.append(Util.EXTENSION_SEPARATOR);
				}
			}
		}
		return listString.toString();
	}

	@Override
	protected String[][] parseString(String string) {
		if (string == null || string.length() == 0) {
			return new String[0][0];
		}
		String[] rows = string.split(Util.PAIR_SEPARATOR_REGEX); //$NON-NLS-1$
		String[][] items = new String[rows.length][];
		for(int i=0; i<rows.length; i++) {
			items[i] = rows[i].split(",", columnNames.length); //$NON-NLS-1$
		}
		return items;
	}

	@Override
	protected String[] getNewInputObject() {
		PairDialog dialog = new PairDialog(getShell());
		int ret = dialog.open();
		if (ret == IDialogConstants.OK_ID) {
			String fromExtension = dialog.getFromExtension();
			String toExtension = dialog.getToExtension();
			String comment = dialog.getComment();
			return new String[]{fromExtension, toExtension, comment};
		}
		return null;
	}

	@Override
	protected String[] getChangedInputObject(TableItem tableItem) {
		String fromExtension = tableItem.getText(0);
		String toExtension = tableItem.getText(1);
		String comment = tableItem.getText(2);
		PairDialog dialog = new PairDialog(fromExtension, toExtension, comment, getShell());
		int ret = dialog.open();
		if (ret == IDialogConstants.OK_ID) {
			fromExtension = dialog.getFromExtension();
			toExtension = dialog.getToExtension();
			comment = dialog.getComment();
			return new String[]{fromExtension, toExtension, comment};
		}
		return null;
	}

	private class PairDialog extends Dialog {
		private String fromExtension;
		private String toExtension;
		private String comment;
		
		protected PairDialog(Shell parentShell) {
			this("", "", "", parentShell); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		protected PairDialog(String fromExtension, String toExtension, String comment, Shell parentShell) {
			super(parentShell);
			this.fromExtension = fromExtension;
			this.toExtension = toExtension;
			this.comment = comment;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			Label label = new Label(composite, SWT.NONE);
			label.setText(StepCounterPlugin.getResourceString("ExtensionPairsEditor.labelInputExtensionPair")); //$NON-NLS-1$
			GridLayout layout = new GridLayout(2, false);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			Group group = new Group(composite, SWT.NONE);
			group.setLayoutData(data);
			group.setLayout(layout);;

			Label fromTextLabel = new Label(group, SWT.NONE);
			fromTextLabel.setText(StepCounterPlugin.getResourceString("ExtensionPairsEditor.labelFromText")); //$NON-NLS-1$
			Text fromText = new Text(group, SWT.SINGLE | SWT.BORDER);
			fromText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fromText.setText(fromExtension);
			fromText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					Text source = (Text) e.getSource();
					fromExtension = source.getText();
					if (!fromExtension.startsWith(".")) { //$NON-NLS-1$
						fromExtension = "." + fromExtension; //$NON-NLS-1$
					}
				}
			});
			
			Label toTextLabel = new Label(group, SWT.NONE);
			toTextLabel.setText(StepCounterPlugin.getResourceString("ExtensionPairsEditor.labelToText")); //$NON-NLS-1$
			Text toText = new Text(group, SWT.SINGLE | SWT.BORDER);
			toText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			toText.setText(toExtension);
			toText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					Text source = (Text) e.getSource();
					toExtension = source.getText();
					if (!toExtension.startsWith(".")) { //$NON-NLS-1$
						toExtension = "." + toExtension; //$NON-NLS-1$
					}
				}
			});
			Label commentTextLabel = new Label(group, SWT.NONE);
			commentTextLabel.setText(StepCounterPlugin.getResourceString("ExtensionPairsEditor.labelCommentText")); //$NON-NLS-1$
			Text commentText = new Text(group, SWT.SINGLE | SWT.BORDER);
			commentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			commentText.setText(comment); //$NON-NLS-1$
			commentText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					Text source = (Text) e.getSource();
					comment = source.getText();
				}
			});
			return composite;
		}

		@Override
		protected Point getInitialSize() {
			return new Point(250, 230);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(StepCounterPlugin.getResourceString("ExtensionPairsEditor.widgetName")); //$NON-NLS-1$
		}
		
		public String getFromExtension() {
			return fromExtension;
		}

		public String getToExtension() {
			return toExtension;
		}
		
		public String getComment() {
			return comment;
		}
	}
}
