package jp.sf.amateras.stepcounter.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import jp.sf.amateras.stepcounter.StepCounterPlugin;

public class EclipseLogger {
	public static void info(String message) {
		log(IStatus.INFO, StepCounterPlugin.PLUGIN_ID, message);
	}
	
	public static void warn(String message) {
		log(IStatus.WARNING, StepCounterPlugin.PLUGIN_ID, message);
	}
	
	public static void error(String message) {
		log(IStatus.ERROR, StepCounterPlugin.PLUGIN_ID, message);
	}
	
	public static void log(int severity, String pluginId, String message) {
		StepCounterPlugin.getDefault().getLog().log(new Status(severity, pluginId, message));
	}
}
