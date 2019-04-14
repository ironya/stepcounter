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

public class FileNamePatternEditor extends TableFieldEditor {

	private static String[] columnNames = new String[] { StepCounterPlugin.getResourceString("FileNamePatternEditor.columnNamePattern"), StepCounterPlugin.getResourceString("FileNamePatternEditor.columnNameComment") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static int[] columnWidths = new int[] { 200, 150 };

	public FileNamePatternEditor(String name, String labelText, Composite parent) {
		super(name, labelText, columnNames, columnWidths, parent);
	}

	@Override
	protected String createList(String[][] items) {
		StringBuilder listString = new StringBuilder();
		for(String[] columns : items) {
			if (listString.length() > 0) {
				listString.append(Util.FILENAME_PATTERN_SEPARATOR);
			}
			for(int i=0; i<columns.length; i++) {
				if (i != 0) {
					if (i+1 != columns.length || (columns[i] != null && !columns[i].isEmpty())) {
						listString.append(Util.FILENAME_ITEM_SEPARATOR);
					}
				}
				listString.append(columns[i]);
			}
		}
		return listString.toString();
	}

	@Override
	protected String[][] parseString(String string) {
		if (string == null || string.length() == 0) {
			return new String[0][0];
		}
		String[] rows = string.split(Util.FILENAME_PATTERN_SEPARATOR_REGEX); //$NON-NLS-1$
		String[][] items = new String[rows.length][];
		for(int i=0; i<rows.length; i++) {
			items[i] = rows[i].split(Util.FILENAME_ITEM_SEPARATOR, columnNames.length); //$NON-NLS-1$
		}
		return items;
	}

	@Override
	protected String[] getNewInputObject() {
		FileNamePatternDialog dialog = new FileNamePatternDialog(getShell());
		int ret = dialog.open();
		if (ret == IDialogConstants.OK_ID) {
			String fileNamePattern = dialog.getFileNamePattern();
			String comment = dialog.getComment();
			return new String[]{fileNamePattern, comment};
		}
		return null;
	}

	@Override
	protected String[] getChangedInputObject(TableItem tableItem) {
		String fileNamePattern = tableItem.getText(0);
		String comment = tableItem.getText(1);
		FileNamePatternDialog dialog = new FileNamePatternDialog(fileNamePattern, comment, getShell());
		int ret = dialog.open();
		if (ret == IDialogConstants.OK_ID) {
			fileNamePattern = dialog.getFileNamePattern();
			comment = dialog.getComment();
			return new String[]{fileNamePattern, comment};
		}
		return null;
	}

	private class FileNamePatternDialog extends Dialog {
		private String fileNamePattern;
		private String comment;

		protected FileNamePatternDialog(Shell parentShell) {
			this("", "", parentShell); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		protected FileNamePatternDialog(String fromExtension, String comment, Shell parentShell) {
			super(parentShell);
			this.fileNamePattern = fromExtension;
			this.comment = comment;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			Label label = new Label(composite, SWT.NONE);
			label.setText(StepCounterPlugin.getResourceString("FileNamePatternEditor.labelInputFileNamePattern")); //$NON-NLS-1$
			GridLayout layout = new GridLayout(2, false);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			Group group = new Group(composite, SWT.NONE);
			group.setLayoutData(data);
			group.setLayout(layout);;

			Label fromTextLabel = new Label(group, SWT.NONE);
			fromTextLabel.setText(StepCounterPlugin.getResourceString("FileNamePatternEditor.labelPatternText")); //$NON-NLS-1$
			Text fromText = new Text(group, SWT.SINGLE | SWT.BORDER);
			fromText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fromText.setText(fileNamePattern);
			fromText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					Text source = (Text) e.getSource();
					fileNamePattern = source.getText();
				}
			});

			Label commentTextLabel = new Label(group, SWT.NONE);
			commentTextLabel.setText(StepCounterPlugin.getResourceString("FileNamePatternEditor.labelCommentText")); //$NON-NLS-1$
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
			return new Point(400, 180);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(StepCounterPlugin.getResourceString("FileNamePatternEditor.widgetName")); //$NON-NLS-1$
		}

		public String getFileNamePattern() {
			return fileNamePattern;
		}

		public String getComment() {
			return comment;
		}
	}
}
