package jp.sf.amateras.stepcounter.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FileNamePatternEditor extends ListEditor {

	public FileNamePatternEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	protected String createList(String[] items) {
		StringBuilder path = new StringBuilder();

		for (String item : items) {
			if (path.length() > 0) {
				path.append("|");
			}
			path.append(item);
		}
		return path.toString();
	}

	@Override
	protected String getNewInputObject() {
		PatternDialog dialog = new PatternDialog(getShell());
		int ret = dialog.open();
		if (ret == IDialogConstants.OK_ID) {
			String text = dialog.getText();
			return text;
		}
		return null;
	}

	@Override
	protected String[] parseString(String stringList) {
		StringTokenizer st = new StringTokenizer(stringList, "|");
		List<String> values = new ArrayList<String>();
		while(st.hasMoreTokens()) {
			values.add(st.nextToken());
		}
		return values.toArray(new String[values.size()]);
	}

	private class PatternDialog extends Dialog {
		private String text;
		
		protected PatternDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			Label label = new Label(composite, SWT.NONE);
			label.setText("Input file name pattern.");
			Text textBox = new Text(composite, SWT.SINGLE | SWT.BORDER);
			textBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			textBox.setText("");
			textBox.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					Text source = (Text) e.getSource();
					text = source.getText();
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
			newShell.setText("File name regular expression pattern");
		}
		
		public String getText() {
			return text;
		}
	}
}
