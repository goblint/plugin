package ee.ut.goblin;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class GoblinPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static GoblinPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	public static QualifiedName RESULT_NAME = new QualifiedName("ee.ut.ee", "result");
	
	/**
	 * The constructor.
	 */
	public GoblinPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("ee.ut.goblin.GoblinPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		//System.out.println("We is being started!");
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
	    //System.out.println("Oh no! We is being stopped!");
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static GoblinPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = GoblinPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
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
