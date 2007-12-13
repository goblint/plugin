package ee.ut.goblin;

import java.io.File;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

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


public class GoblinPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	public static final String P_GOBLIN_EXEC = "goblinExecPreference";
	public static final String P_EXTRA_PARAMETERS = "goblinExtraParametersPreference";


	public GoblinPreferencePage() {
		super(GRID);
		setPreferenceStore(GoblinPlugin.getDefault().getPreferenceStore());
		//setDescription("Settings for the Goblin.");
		initializeDefaults();
	}
/**
 * Sets the default values of the preferences.
 */
	private void initializeDefaults() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(P_GOBLIN_EXEC, "");
		store.setDefault(P_EXTRA_PARAMETERS, "");
	}
	
/**
 * Creates the field editors. Field editors are abstractions of
 * the common GUI blocks needed to manipulate various types
 * of preferences. Each field editor knows how to save and
 * restore itself.
 */

	public void createFieldEditors() {
		addField(new GoblinFileFieldEditor(getFieldEditorParent()));

		addField(new ExtraParametersFieldEditor(getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench) {
	}
	
}

class ExtraParametersFieldEditor extends StringFieldEditor {
	public static String label = "Additional &parameters:";
	
	public ExtraParametersFieldEditor(Composite parent){
		super(GoblinPreferencePage.P_EXTRA_PARAMETERS, label, parent);
	}
}

class GoblinFileFieldEditor extends FileFieldEditor {
    public GoblinFileFieldEditor(Composite parent) {
        super(GoblinPreferencePage.P_GOBLIN_EXEC,"&Goblin executable:", true, parent);
    }

    protected boolean checkState() {
        boolean ok = super.checkState();
        if (ok) {
        	String msg = null;
        	String path = getTextControl().getText();
        	if (path != null) {
        		path = path.trim();
        		File exectest = new File(path);
                if (! exectest.exists()) 
                    msg = "No such file found!";
        	}
        	if (msg != null) { // error
        		showErrorMessage(msg);
        		return false;
        	}
        	clearErrorMessage();
        	return true;
        } else
            return false;
    }
}