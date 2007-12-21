package ee.ut.goblin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;


/**
 * Property page for the goblin plug-in for eclipse. It features
 * an analyses selector, option for analysing all functions and
 * makes it possible to add your own command line options.
 * 
 * 
 * @author Kalmer
 *
 */
public final class GoblinPropertyPage extends PropertyPage {
	
	/* keys for IResource.get/setPersistentProperty*/
	public static final String P_ANALYSIS = "goblinAnalysisProperty";
	public static final String P_ANALYSE_ALLFUNS = "goblinAnalyseAllFunsProperty";
	public static final String P_EXTRA_PARAMETERS = "goblinExtraParametersProperty";
	
	private static final String[][] AVALIABLE_ANALYSES = { {"Mutex","mutex"}, {"No path","no_path"}, {"Base","base"} };
	private static final int DEFAULT_ANALYSIS = 0;
	
	private Combo analysis_combo;
	private Button analyse_allfuns;
	private Text additional_parameters;

	public GoblinPropertyPage() {
		super();
	}

	private void addAnalysisSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);
		IResource res = ((IResource) getElement().getAdapter(IResource.class));

		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText("Analysis:");

		analysis_combo = new Combo(composite, SWT.READ_ONLY |  SWT.DROP_DOWN);
		analysis_combo.setToolTipText("Specify default analysis for this project.");
		for (int i = 0; i < AVALIABLE_ANALYSES.length; ++i)
			analysis_combo.add(AVALIABLE_ANALYSES[i][0]);

		
		  
		 try {
			String analysis = res.getPersistentProperty(new QualifiedName("", P_ANALYSIS));
			analysis_combo.select(DEFAULT_ANALYSIS);
			for(int i = 0; i < AVALIABLE_ANALYSES.length; ++i){
				if (AVALIABLE_ANALYSES[i][1].equals(analysis)){
					analysis_combo.select(i);
					break;
				}
			}
		} catch (CoreException e) {
			analysis_combo.select(DEFAULT_ANALYSIS);
		}
		
		
		analyse_allfuns = new Button(composite, SWT.CHECK);
		analyse_allfuns.setText("Analyse all functions");
		analyse_allfuns.setToolTipText("Analyses all functions, not just main.");
		analyse_allfuns.setSelection(getAllFuns(res));
	}
	
	
	/**
	 * Get name of default analysis.
	 * 
	 * @param res resource (for example project)
	 * @return name of analysis
	 */
	public static String getAnalysis(IResource res){
		String ret = AVALIABLE_ANALYSES[DEFAULT_ANALYSIS][1];
		String analysis;

		try {
			analysis = res.getPersistentProperty(new QualifiedName("", P_ANALYSIS));
		} catch (CoreException e) {
			return ret;
		}
		
		for(int i = 0; i < AVALIABLE_ANALYSES.length; ++i){
			if (AVALIABLE_ANALYSES[i][1].equals(analysis)){
				ret = AVALIABLE_ANALYSES[i][1];
				break;
			}
		}
		
		return ret;
	}
	
	/**
	 * Analyse all functions?
	 * 
	 * @param res resource (for example project)
	 * @return true if goblin is to analyse all fuinctions.
	 */
	public static boolean getAllFuns(IResource res){
		try {
			String  allfuns = res.getPersistentProperty(new QualifiedName("", P_ANALYSE_ALLFUNS));
			return allfuns==null?false:allfuns.equals("true");
		} catch (Throwable e) {
			return false;
		}
	}	
	
	/**
	 * Additional parameters hopefully seperated by spaces.
	 * 
	 * @param res resource (for example project)
	 * @return string of additional parameters 
	 */
	public static String getAdditionalParameters(IResource res){
		try {
			String s = res.getPersistentProperty(new QualifiedName("", P_EXTRA_PARAMETERS));
			if (s==null)
				return "";
			else 
				return s;
		} catch (Throwable e) {
			return "";
		}
	}	

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	private void addExtraParametersSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);
		IResource res = ((IResource) getElement().getAdapter(IResource.class));

		Label param = new Label(composite, SWT.NONE);
		param.setText("Additional parameters:");
		
		additional_parameters = new Text(composite, SWT.SINGLE | SWT.BORDER );
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		additional_parameters.setLayoutData(gd);
		additional_parameters.setText(getAdditionalParameters(res));
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addAnalysisSection(composite);
		addSeparator(composite);
		addExtraParametersSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {
		// Populate the owner text field with the default value
		analyse_allfuns.setSelection(false);
		analysis_combo.select(DEFAULT_ANALYSIS);
		additional_parameters.setText("");
	}
	
	public boolean performOk() {
		// store values
		
		try {
			String analysis = "";
			for(int i = 0; i < AVALIABLE_ANALYSES.length; ++i){
				if (AVALIABLE_ANALYSES[i][0].equals(analysis_combo.getText())){
					analysis = AVALIABLE_ANALYSES[i][1];
					break;
				}
			}
			
			IResource res = ((IResource) getElement().getAdapter(IResource.class));
			res.setPersistentProperty(
				new QualifiedName("", P_ANALYSIS),
				analysis);
			
			res.setPersistentProperty(
					new QualifiedName("", P_ANALYSE_ALLFUNS),
					analyse_allfuns.getSelection()?"true":"false");

			res.setPersistentProperty(
					new QualifiedName("", P_EXTRA_PARAMETERS),
					additional_parameters.getText());

		} catch (CoreException e) {
			return false;
		}
		return true;
	}

}
