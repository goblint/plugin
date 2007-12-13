/*
 * Created on Apr 1, 2005
 *
 */
package ee.ut.goblin.views;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

/**
 * @author Vesal Vojdani
 */
public class TreeAnalysis extends TreeNode {
	
	private Map<TreeLeaf, TreeLeaf> map;
	
	public TreeAnalysis() {
		super("Analysis");
		map = new HashMap<TreeLeaf, TreeLeaf>();
	}
	
	public TreeLeaf[] getChildren() {
		Collection<TreeLeaf> v = map.values();
		return v.toArray(new TreeLeaf[v.size()]);
	}
	
	public boolean hasChildren() {
		return !map.isEmpty();
	}
	
	public void addChild(TreeLeaf child) {
	    map.put(child,child);
	    child.setParent(this);
	}
	
	public String toShortString() {
		return "Mapping";
	}
	
	public String toString() {
		String out = "Mapping: ";
		int len = out.length();
		TreeLeaf[] children = getChildren();
		
		for (int i = 0; i < children.length; i++) {
			if (len > 80) {
				out += ", ... ,";
				break;
			}
			out += children[i].toShortString() + ", ";
			len += children[i].toShortString().length() + 2;
		}
		return out;
	}
	
	public void put(TreeLeaf key, TreeLeaf value) {
		map.put(key, value);
	}
	
	public TreeLeaf get(TreeLeaf key) {
		return map.get(key);
	}
}
