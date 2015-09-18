/**
 * 
 */
package weka.classifiers.meta;

import weka.core.Instances;
import weka.classifiers.*;
import weka.classifiers.trees.*;
/**
 * @author Vignesh Ram
 * Extension of the Bagging implementation to include the
total number of nodes for use in WEKA's Experimenter. Calculates the total number of
nodes by summing the number of nodes from each classifier in the ensemble after building
the ensemble.
 */
public class ExtendedBagging extends Bagging {
	
	int treeSize;

	/**
	 * @return the treeSize
	 */
	public int getTreeSize() {
		return treeSize;
	}

	/**
	 * @param treeSize the treeSize to set
	 */
	public void setTreeSize(int treeSize) {
		this.treeSize = treeSize;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8051540514061814207L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see weka.classifiers.meta.Bagging#buildClassifier(weka.core.Instances)
	 */
	@Override
	public void buildClassifier(Instances arg0) throws Exception {
		// TODO Auto-generated method stub
		super.buildClassifier(arg0);
		if(m_Classifier instanceof DecisionDAG)
		for(Classifier classifier:this.m_Classifiers)
		{
				treeSize+=((DecisionDAG) classifier).getNoOfNodes();
		}
		else if(m_Classifier instanceof ExtendedRandomTree)
			for(Classifier classifier:this.m_Classifiers)
			{
					treeSize+=((ExtendedRandomTree) classifier).getNumNodes();
			}	
	}

}
