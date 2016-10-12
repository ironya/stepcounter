package jp.sf.amateras.stepcounter.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import jp.sf.amateras.stepcounter.StepCounterPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class StepCounterPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public StepCounterPreferencePage() {
		super(GRID);
		setPreferenceStore(StepCounterPlugin.getDefault().getPreferenceStore());
		setDescription("Preference of StepCounter");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceConstants.P_IGNORE_GENERATED_FILE,
				"&Enable to ignore generated file by some translators.", getFieldEditorParent()));
		addField(new ExtensionPairsEditor(PreferenceConstants.P_EXTENSION_PAIRS, "Extension pairs for ignoring generated file",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_IGNORE_FILENAME_PATTERNS,
				"E&nable to ignore filename patterns.", getFieldEditorParent()));
		addField(new FileNamePatternEditor(PreferenceConstants.P_FILENAME_PATTERNS, "File name patterns to ignore.",
				getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}