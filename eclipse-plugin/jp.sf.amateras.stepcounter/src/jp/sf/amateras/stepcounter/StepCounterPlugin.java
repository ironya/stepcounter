package jp.sf.amateras.stepcounter;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class StepCounterPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "jp.sf.amateras.stepcounter";

	//The shared instance.
	private static StepCounterPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public StepCounterPlugin(){
	    super();
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("jp.sf.amateras.stepcounter.StepCounterPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		Util.setFileEncodingDetector(new EclipseFileEncodingDetector());
	}

	/**
	 * Returns the shared instance.
	 */
	public static StepCounterPlugin getDefault() {
		return plugin;
	}

//	/**
//	 * Returns the workspace instance.
//	 */
//	public static IWorkspace getWorkspace() {
//		return ResourcesPlugin.getWorkspace();
//	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= StepCounterPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
