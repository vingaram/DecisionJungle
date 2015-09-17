/**
 * 
 */
package weka.classifiers.trees;

/**
 * @author Vignesh Ram
 * Extension of the RandomTree implementation in WEKA to in-
clude the number of nodes in the tree to be used in ExtendedBagging for calculating the
total number of nodes in the ensemble.
 */
public class ExtendedRandomTree extends RandomTree {

	/**
	 * @return the numNodes
	 */
	public int getNumNodes() {
		return m_Tree.numNodes();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2605187515764634969L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
