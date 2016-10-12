package jp.sf.amateras.stepcounter.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import jp.sf.amateras.stepcounter.StepCounterPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = StepCounterPlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_IGNORE_GENERATED_FILE, false);
		store.setDefault(PreferenceConstants.P_EXTENSION_PAIRS, "");
		store.setDefault(PreferenceConstants.P_IGNORE_FILENAME_PATTERNS, false);
		store.setDefault(PreferenceConstants.P_FILENAME_PATTERNS, "");
	}

}
