package jp.sf.amateras.stepcounter;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import jp.sf.amateras.stepcounter.preferences.PreferenceConstants;

/**
 * 「ステップ数をカウント」メニュー
 */
public class StepCountAction implements IObjectActionDelegate {

//	private IWorkbenchPart targetPart;
	private ISelection selection;

	/* (非 Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
//		this.targetPart = targetPart;
	}

	/* (非 Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			// POJO の core 系クラスからの参照用
			System.setProperty(PreferenceConstants.P_SHOW_DIRECTORY, Boolean.toString(StepCounterPlugin
					.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DIRECTORY)));
			System.setProperty(PreferenceConstants.P_IGNORE_GENERATED_FILE, Boolean.toString(StepCounterPlugin
					.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_IGNORE_GENERATED_FILE)));
			System.setProperty(Util.EXTENSION_PAIRS, StepCounterPlugin
					.getDefault().getPreferenceStore().getString(PreferenceConstants.P_EXTENSION_PAIRS));
			System.setProperty(Util.IGNORE_FILENAME_PATTERNS, Boolean.toString(StepCounterPlugin
					.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_IGNORE_FILENAME_PATTERNS)));
			System.setProperty(Util.FILENAME_PATTERNS,
					StepCounterPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_FILENAME_PATTERNS));
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			window.getActivePage().showView("jp.sf.amateras.stepcounter.StepCountView");
			IViewReference[] views = window.getActivePage().getViewReferences();
			for(int i=0;i<views.length;i++){
				IViewPart view = views[i].getView(false);
				if(view instanceof StepCountView){
					((StepCountView)view).count(selection);
				}
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
