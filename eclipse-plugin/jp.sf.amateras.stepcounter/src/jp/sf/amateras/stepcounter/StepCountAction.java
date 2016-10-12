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
 * �u�X�e�b�v�����J�E���g�v���j���[
 */
public class StepCountAction implements IObjectActionDelegate {

//	private IWorkbenchPart targetPart;
	private ISelection selection;

	/* (�� Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
//		this.targetPart = targetPart;
	}

	/* (�� Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			// POJO �� core �n�N���X����̎Q�Ɨp
			System.setProperty(PreferenceConstants.P_IGNORE_GENERATED_FILE, Boolean.toString(StepCounterPlugin
					.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_IGNORE_GENERATED_FILE)));
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

	/* (�� Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
