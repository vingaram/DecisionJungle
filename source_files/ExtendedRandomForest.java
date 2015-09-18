/**
 * 
 */
package weka.classifiers.trees;

import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.meta.ExtendedBagging;
import weka.core.Instances;
import weka.core.Utils;

/**
 * @author Vignesh Ram
 * Extension of the Bagging implementation in WEKA to include
the total number of nodes as an additional evaluation measure for use in WEKA's Ex-
perimenter. Obtains the total number of nodes from the ExtendedBagging object.
 */
public class ExtendedRandomForest extends RandomForest {

	/* (non-Javadoc)
	 * @see weka.classifiers.trees.RandomForest#buildClassifier(weka.core.Instances)
	 */
	@Override
	public void buildClassifier(Instances data) throws Exception {

	    // can classifier handle the data?
	    getCapabilities().testWithFail(data);

	    // remove instances with missing class
	    data = new Instances(data);
	    data.deleteWithMissingClass();
	    
	    m_bagger = new ExtendedBagging();
	    ExtendedRandomTree model = new ExtendedRandomTree();

	    // set up the random tree options
	    m_KValue = m_numFeatures;
	    if (m_KValue < 1) m_KValue = (int) Utils.log2(data.numAttributes())+1;
	    model.setKValue(m_KValue);
	    model.setMaxDepth(getMaxDepth());

	    // set up the bagger and build the forest
	    m_bagger.setClassifier(model);
	    m_bagger.setSeed(m_randomSeed);
	    m_bagger.setNumIterations(m_numTrees);
	    m_bagger.setCalcOutOfBag(true);
	    m_bagger.setNumExecutionSlots(m_numExecutionSlots);
	    m_bagger.buildClassifier(data);
	  }

	/* (non-Javadoc)
	 * @see weka.classifiers.trees.RandomForest#enumerateMeasures()
	 */
	@Override
	public Enumeration<String> enumerateMeasures() {
	    
	    Vector<String> newVector = new Vector<String>(2);
	    newVector.addElement("measureOutOfBagError");
	    newVector.addElement("measureTotal_Numer_Of_Nodes");
	    return newVector.elements();
	  }

	/* (non-Javadoc)
	 * @see weka.classifiers.trees.RandomForest#getMeasure(java.lang.String)
	 */
	@Override
	public double getMeasure(String additionalMeasureName) {
	    
	    if (additionalMeasureName.equalsIgnoreCase("measureOutOfBagError")) {
	      return measureOutOfBagError();
	    }
	    else if(additionalMeasureName.equalsIgnoreCase("measureTotal_Numer_Of_Nodes")) {
	        return ((ExtendedBagging) m_bagger).getTreeSize();
	      }
	    else {throw new IllegalArgumentException(additionalMeasureName 
						     + " not supported (ExtendedRandomForest)");
	    }
	  }

	/**
	 * 
	 */
	private static final long serialVersionUID = -7779728631155872134L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
