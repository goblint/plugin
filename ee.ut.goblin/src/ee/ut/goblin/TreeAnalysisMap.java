package ee.ut.goblin;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import ee.ut.goblin.views.TreeAnalysis;
import ee.ut.goblin.views.TreeLeaf;
import ee.ut.goblin.views.TreeLoc;

/**
 * Keep analysis data in a form we can query quickly and easily
 * 
 * @author kalmera
 */
public class TreeAnalysisMap {
	private static final long serialVersionUID = 1L;
	
	protected Map<String, Map<Integer, TreeLoc>> map = new HashMap<String, Map<Integer, TreeLoc>>(); /*Map<filename,Map<line,TreeLoc>>*/      
	
	/**
	 * Constructor. Convert analysis data to a more suitable form.
	 * @param data analysis result tree
	 */
	public TreeAnalysisMap(TreeAnalysis data){
		// get all data locations
		TreeLeaf[] nodes = data.getChildren();

		// split data locations by files
		Map<String, LinkedList<TreeLoc>> fileMap; /*<filename,LikedList<TreeLoc>*/
		fileMap = splitByFilename(nodes);
		
		// split file data locations by lines 
		Set<String> files = fileMap.keySet();
		Iterator<String> it = files.iterator();
		while(it.hasNext()){
			String key = it.next();
			// create logical map: line -> TreeLoc  NB! may create additional line info 
			map.put(key, splitByLogicalLine(fileMap.get(key)));
		}
	}
	
	/**
	 * Get relevant data tree for each file and line number.
	 *  
	 * @param file file name
	 * @param line line number
	 * @return data tree (TreeLoc) or null
	 */
	public TreeLoc getAnalysis(String file, int line){
		// try to get data for file
		Map<Integer,TreeLoc> lns = map.get(file);

		if (lns==null){
			return null;
		}

		// get data for line in our file
		Integer key = new Integer(line);
		if (lns.containsKey(key)){
			return lns.get(key);
		}else {
			return null;
		}
	}
	/*
	 * produce mapping: line nr -> relevant data (TreeLoc) 
	 */
	private static Map<Integer, TreeLoc> splitByLogicalLine(LinkedList<TreeLoc> nodes){
		HashMap<Integer, TreeLoc> lineMap = new HashMap<Integer, TreeLoc>(); /*<Integer,TreeLoc>*/
		
		// Sort input by line number. We need earlier (< line nr)
		// state first.
		Collections.sort(nodes, compareLocByLine());

		int lastln = -1;
		String cur_fun = "";
		Iterator<TreeLoc> it = nodes.iterator();
		while (it.hasNext()){
			TreeLoc tl = it.next();
			
			if (cur_fun.equals(tl.getFunction())){
				// same data applies within one function 
				// between this location and last location 
				for (int i = lastln+1; i < tl.getLine(); ++i)
					lineMap.put(new Integer(i), tl);
			} else {
				// don't fill the gap between functions
				cur_fun = tl.getFunction();
			}
			
			lineMap.put(new Integer(tl.getLine()), tl);
			lastln = tl.getLine();
		}
		
		return lineMap;
	}
	
	/*
	 * Smaller line number is less than bigger line number ...
	 */
	public static Comparator<TreeLoc> compareLocByLine(){
		return new Comparator<TreeLoc>() {
			public int compare(TreeLoc o, TreeLoc i){
				return o.getLine() - i.getLine();		
			}
		};
	}
	
	/*
	 * produce mapping: file name ->  data
	 */
	private static Map<String, LinkedList<TreeLoc>> splitByFilename(TreeLeaf[] nodes){
		HashMap<String, LinkedList<TreeLoc>> fileMap = new HashMap<String, LinkedList<TreeLoc>>(); /*<filename,LikedList<TreeLoc>*/
		
		for (int i = 0; i < nodes.length; ++i){
			if (nodes[i] instanceof TreeLoc ){
				TreeLoc tl = (TreeLoc) nodes[i];
				String key = tl.getFilename();
				
				LinkedList<TreeLoc> l;
				if  (fileMap.containsKey(key)) {
					l = fileMap.get(key);
					l.add(tl);
				} else {
					l = new LinkedList<TreeLoc>();
					l.add(tl);
					fileMap.put(key, l);
				}
			} else {
				throw new Error("Bad analysis structure");
			}
		}
		return fileMap;
	}
	
}

