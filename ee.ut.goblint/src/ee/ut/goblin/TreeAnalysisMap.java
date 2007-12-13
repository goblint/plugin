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
 * Keep analysis data in a form we can query quickly and easely
 * 
 * @author kalmera
 */
public class TreeAnalysisMap {
	private static final long serialVersionUID = 1L;
	
	protected Map 			map = new HashMap(); /*Map<filename,Map<line,TreeLoc>>*/      
	
	/**
	 * Constructor. Convert analysis data to a more suitable form.
	 * @param data analysis result tree
	 */
	public TreeAnalysisMap(TreeAnalysis data){
		// get all data locations
		TreeLeaf[] nodes = data.getChildren();

		// split data locations by files
		Map fileMap; /*<filename,LikedList<TreeLoc>*/
		fileMap = splitByFilename(nodes);
		
		// split file data locations by lines 
		Set files = fileMap.keySet();
		Iterator it = files.iterator();
		while(it.hasNext()){
			String key = (String) it.next();
			// create logical map: line -> TreeLoc  NB! may create addidional line info 
			map.put(key, splitByLogicalLine((LinkedList)fileMap.get(key)));
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
		Map lns = (Map)map.get(file);

		if (lns==null){
			return null;
		}

		// get data for line in our file
		Integer key = new Integer(line);
		if (lns.containsKey(key)){
			return (TreeLoc)lns.get(key);
		}else {
			return null;
		}
	}
	/*
	 * produce mapping: line nr -> relevant data (TreeLoc) 
	 */
	private static Map splitByLogicalLine(LinkedList nodes){
		HashMap lineMap = new HashMap(); /*<Integer,TreeLoc>*/
		
		// Sort input by line number. We need earlier (< line nr)
		// state first.
		Collections.sort(nodes, compareLocByLine());

		int lastln = -1;
		String cur_fun = "";
		Iterator it = nodes.iterator();
		while (it.hasNext()){
			TreeLoc tl = (TreeLoc)it.next();
			
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
	public static Comparator compareLocByLine(){
		return new Comparator() {
			public int compare(Object o1, Object i1){
				if (!(o1 instanceof TreeLoc && i1 instanceof TreeLoc)){
					return 0;
				}
				TreeLoc i = (TreeLoc)i1;
				TreeLoc o = (TreeLoc)o1;
				
				return o.getLine() - i.getLine();		
			}
		};
	}
	
	/*
	 * prioduce mapping: file name ->  data
	 */
	private static Map splitByFilename(TreeLeaf[] nodes){
		HashMap fileMap = new HashMap(); /*<filename,LikedList<TreeLoc>*/
		
		for (int i = 0; i < nodes.length; ++i){
			if (nodes[i] instanceof TreeLoc ){
				TreeLoc tl = (TreeLoc) nodes[i];
				String key = tl.getFilename();
				
				LinkedList l;
				if  (fileMap.containsKey(key)) {
					l = (LinkedList) fileMap.get(key);
					l.add(tl);
				} else {
					l = new LinkedList();
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

