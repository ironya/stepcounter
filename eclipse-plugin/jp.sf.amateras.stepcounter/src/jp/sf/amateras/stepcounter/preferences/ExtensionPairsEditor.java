package jp.sf.amateras.stepcounter.preferences;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import jp.sf.amateras.stepcounter.StepCounterPlugin;

public class ExtensionPairsEditor extends TableFieldEditor {
	
	public ExtensionPairsEditor(String name, String labelText, Composite parent) {
		super(name, labelText, new String[] { "from", "to" }, new int[] { 100, 100 }, parent);
	}
	
	@Override
	protected String createList(String[][] items) {
		StringBuilder listString = new StringBuilder();
		for(String[] columns : items) {
			if (listString.length() > 0) {
				listString.append('|');
			}
			for(int i=0; i<columns.length; i++) {
				StepCounterPlugin.getDefault().getLog().log(new Status(IStatus.INFO, StepCounterPlugin.PLUGIN_ID, "columns[" + i + "]=" + columns[i]));
				listString.append(columns[i]);
				if (i+1<columns.length) {
					listString.append(',');
				}
			}
			StepCounterPlugin.getDefault().getLog().log(new Status(IStatus.INFO, StepCounterPlugin.PLUGIN_ID, "listString=" + listString));
		}
		return listString.toString();
	}

	@Override
	protected String[][] parseString(String string) {
		if (string == null || string.length() == 0) {
			return new String[0][0];
		}
		String[] rows = string.split("\\|");
		String[][] items = new String[rows.length][];
		for(int i=0; i<rows.length; i++) {
			items[i] = rows[i].split(",", 2);
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
			return new String[]{fromExtension, toExtension};
		}
		return null;
	}

	@Override
	protected String[] getChangedInputObject(TableItem tableItem) {
		String fromExtension = tableItem.getText(0);
		String toExtension = tableItem.getText(1);
		PairDialog dialog = new PairDialog(fromExtension, toExtension, getShell());
		int ret = dialog.open();
		if (ret == IDialogConstants.OK_ID) {
			fromExtension = dialog.getFromExtension();
			toExtension = dialog.getToExtension();
			return new String[]{fromExtension, toExtension};
		}
		return null;
	}

	private class PairDialog extends Dialog {
		private String fromExtension;
		private String toExtension;
		
		protected PairDialog(Shell parentShell) {
			this("", "", parentShell);
		}
		
		protected PairDialog(String fromExtension, String toExtension, Shell parentShell) {
			super(parentShell);
			this.fromExtension = fromExtension;
			this.toExtension = toExtension;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			Label label = new Label(composite, SWT.NONE);
			label.setText("Input extension pair.");
			Text fromText = new Text(composite, SWT.SINGLE | SWT.BORDER);
			fromText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fromText.setText("");
			fromText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					Text source = (Text) e.getSource();
					fromExtension = source.getText();
				}
			});
			Text toText = new Text(composite, SWT.SINGLE | SWT.BORDER);
			toText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			toText.setText("");
			toText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					Text source = (Text) e.getSource();
					toExtension = source.getText();
				}
			});
			return composite;
		}

		@Override
		protected Point getInitialSize() {
			return new Point(400, 150);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Extension pair");
		}
		
		public String getFromExtension() {
			return fromExtension;
		}

		public String getToExtension() {
			return toExtension;
		}
	}
}
