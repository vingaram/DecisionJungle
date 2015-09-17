package weka.classifiers.trees;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

/**
 * @author Vignesh Ram
 * Cllas that describes the DAG model to be built. The DAG is built one level at a time and
 * each split is determined from randomly chosen splits.   
 */
public class DecisionDAG extends AbstractClassifier implements OptionHandler,AdditionalMeasureProducer{

	private static final long serialVersionUID = -8904119725204748047L;
	public class AttributeParameter implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -696708523563859850L;
		int attribute;
		double value;
		
		public boolean computeBranch(Instance instance)
		{
			if(data.attribute(attribute).isNominal())
				if(instance.value(attribute)==(value))
					return false;
				else
					return true;
			else
			{
				if(instance.value(attribute)<=value)
					return false;
				else
					return true;
			}	
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "AttributeParameter [attribute=" + attribute + ", value="
					+ value + "]";
		}
		/**
		 * @return the attribute
		 */
		public int getAttribute() {
			return attribute;
		}
		/**
		 * 
		 */
		public AttributeParameter() {
			// TODO Auto-generated constructor stub
			attribute=-10;
		}
		/**
		 * @param attribute the attribute to set
		 */
		public void setAttribute(int attribute) {
			this.attribute = attribute;
		}
		public void setParameter(int attr,double value)
		{
			attribute=attr;
			this.value=value;
		}
		public void generateRandomValue(Random rand)
		{
			Attribute att=data.attribute(attribute);
			if(att.isNominal())
				value=(rand).nextInt((att.numValues()));
			else
					value=numericValues.get(att.index()).get(rand.nextInt(numericValues.get(att.index()).size()));
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return (((AttributeParameter)obj).attribute==attribute)&&(((AttributeParameter)obj).value==(value));
		}
	}
	public class Node implements Serializable {

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Node [type=" + type + ", split_parameter="
					+ split_parameter + ", instances=" + instances.size()
					+ ", distribution=" + Utils.sum(distribution) + "]";
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = 8618309508859405297L;
		Node left;
		Node right;
		int level;
		int[] parameterList;
		/**
		 * 
		 */
		public Node(Random rand) {
			// TODO Auto-generated constructor stub
			instances=new ArrayList<Integer>();
			distribution=new double[noClasses];
			parameterList=new int[numFeatures];
		}
		public void randomFeatureSelection(Random rand)
		{
			for(int i=0;i<numFeatures;i++)
			{
				do
				{
					parameterList[i]=data.attribute(rand.nextInt(data.numAttributes())).index();
				}while(data.attribute(parameterList[i]).equals(data.classAttribute())); 
			}
		}
		char type;
		AttributeParameter split_parameter;
		ArrayList<Integer> instances;
		double[] distribution;
	}
	
	public class Level implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -6727658712643062646L;
		int level;
		Node[] nodeList;
		/**
		 * @param level
		 * @param nodeList
		 */
		public Level(int level, Node[] nodeList) {
			this.level = level;
			this.nodeList = nodeList;
		}
		/**
		 * 
		 */
		public Level() {
			// TODO Auto-generated constructor stub
		}
		Level next;
	}
	/**
	 * 
	 */
	Level zero;
	double multiplier=2.0, b;
	/**
	 * @return the noOfNodes
	 */
	public int getNoOfNodes() {
		return noOfNodes;
	}


	int maxNodes=128;
	int leafPC=95;
	int leafSize=1;
	int noClasses;
	int seed=1;
	int maxDepth=100;
	int threshold=100;
	int noOfNodes;
	int numFeatures=0;
	LinkedHashMap<Integer,LinkedList<Double>> numericValues=new LinkedHashMap<Integer, LinkedList<Double>>();
	Instances data;
	@Override
	public void buildClassifier(Instances arg0) throws Exception {
		// TODO Auto-generated method stub
		data=arg0;
		b=1;
		if(numFeatures==0)
			numFeatures= (int) Utils.log2(data.numAttributes()) + 1;
		else if(numFeatures>data.numAttributes())
			numFeatures=data.numAttributes();
		Random rand=new Random(seed);
		noClasses = arg0.numClasses();
		zero = new Level();
		zero.level = 0;
		zero.nodeList = new Node[1];
		zero.nodeList[0] = new Node(rand);
		int attIndex=0;
		for(Instance instance:arg0)
		{
			attIndex=0;
			while(attIndex<arg0.numAttributes())
			{
				Attribute att=arg0.attribute(attIndex++);
				if(att.equals(data.classAttribute()))
					continue;
				if(att.isNumeric())
				{
					if(numericValues.containsKey(att.index()))
					{
						if(!numericValues.get(att.index()).contains(instance.value(att)))
						{
							numericValues.get(att.index()).add(instance.value(att));
						}
					}
					else
					{
						LinkedList<Double> list=new LinkedList<Double>();
						list.add(instance.value(att));
						numericValues.put(att.index(), list);
					}
					
				}
			}
		}
		for(Integer att:numericValues.keySet())
		{
			List<Double> list=numericValues.get(att);
			int size=list.size();
			if(size>threshold&&threshold>0)
				list.sort(null);
			while(size>threshold&&threshold>0)
			{
				for(int i=1;i<size;i+=size/threshold)
				{
					list.remove(i);
					size--;
				}
			}	
		}
		

		
		for (int i = 0; i < arg0.numInstances(); i++)
		{
			if(zero.nodeList[0].instances.add(i))
			zero.nodeList[0].distribution[(int)data.get(i).classValue()]++;
		}
		zero.nodeList[0].type = 'r';
		zero.nodeList[0].level=0;
		noOfNodes=1;
		make(zero, Double.MAX_VALUE,rand);
		Level l=zero.next;
		while(l!=null)
		{
			noOfNodes+=l.nodeList.length;
			l=l.next;
		}
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DecisionDAG [multiplier=" + multiplier + ", b=" + b + ", maxNodes=" + maxNodes + ", leafPC="
				+ leafPC + ", leafSize=" + leafSize + ", noClasses="
				+ noClasses + ", seed=" + seed + ", maxDepth=" + maxDepth
				+ ", noOfNodes=" + noOfNodes + ", numFeatures=" + numFeatures
				+ "]";
	}

	public void make(Level level, double obj,Random rand) {

		// TODO Auto-generated method stub
		int no_nodes;
		no_nodes=(int) (multiplier*b);
		if(level.level<5)
			no_nodes=(int) Math.pow(2, level.level+1);
		if(no_nodes>maxNodes)
			no_nodes=(int) maxNodes;
		b=no_nodes;
		Level newLevel = new Level();
		newLevel.nodeList = new Node[no_nodes];
		newLevel.level = level.level + 1;
		for (int i = 0; i < no_nodes; i++) {
			newLevel.nodeList[i] = new Node(rand);
			newLevel.nodeList[i].level=newLevel.level;
		}
		level.next = newLevel;
		for (Node n : level.nodeList) {
			if (n.instances == null || n.instances.size() == 0 || n.type == 'l')
				continue;
			n.split_parameter=new AttributeParameter();
			n.randomFeatureSelection(rand);
			n.split_parameter.setAttribute(n.parameterList[rand.nextInt(numFeatures)]);
			n.split_parameter.generateRandomValue(rand);
			n.left = newLevel.nodeList[(Math.abs(rand.nextInt()) % no_nodes)];
			do {
				n.right = newLevel.nodeList[(Math.abs(rand.nextInt()) % no_nodes)];
			} while (n.right == n.left);
			for (Integer instance : n.instances) {
				if (n.split_parameter.computeBranch(data.get(instance)) == false) {
					if(n.left.instances.add(instance))
					n.left.distribution[(int) data.get(instance).classValue()]++;
				} else {
					if(n.right.instances.add(instance))
					n.right.distribution[(int) data.get(instance).classValue()]++;
				}
			}
		}
		lsearch(level,rand);
		
		

		for (int i=0;i<newLevel.nodeList.length;i++) {
			if (newLevel.nodeList[i].instances == null || newLevel.nodeList[i].instances.size() == 0)
			{
				newLevel.nodeList[i]=null;
				continue;
			}
			newLevel.nodeList[i].type='i';
			for (int j = 0; j < newLevel.nodeList[i].distribution.length; j++) {
				if (newLevel.nodeList[i].distribution[j]/newLevel.nodeList[i].instances.size() >= leafPC/100.0 || newLevel.nodeList[i].instances.size() <= leafSize) {
					newLevel.nodeList[i].type = 'l';
				}
			}
		}
		for(Node node:level.nodeList)
		{
			if(node.type=='i')
			if(node.right.instances.size()==0||node.left.instances.size()==0)
			{
				node.type='l';
				node.split_parameter=null;
				node.left=null;
				node.right=null;
			}
		}
		newLevel.nodeList=clean(newLevel.nodeList);
		
		if (obj > objectiveFunction(level)&&newLevel.level<maxDepth)
			make(newLevel, objectiveFunction(level),rand);
		else
		{
			for(Node node:newLevel.nodeList)
			{
					node.type='l';
					node.split_parameter=null;
					node.left=null;
					node.right=null;
			}
		}

	}

	 public static Node[] clean(final Node[] v) {
		    List<Node> list = new ArrayList<Node>(Arrays.asList(v));
		    list.removeAll(Collections.singleton(null));
		    return list.toArray(new Node[list.size()]);
		}
	
	private void lsearch(Level level,Random rand) {
		// TODO Auto-generated method stub
		boolean modified = false;
		do
		{
			modified=false;
			double[] energetic=new double[level.nodeList.length];
			for(int i=0;i<energetic.length;i++)
			{
				energetic[i]=computeEntropy(level.nodeList[i])*level.nodeList[i].instances.size();
			}
			int[] order=new int[energetic.length];
			for(int i=0;i<order.length;i++)
			{
				order[i]=Utils.maxIndex(energetic);
				energetic[order[i]]=Double.MIN_VALUE;
			}
			for(int orderNo:order) {
			Node n=level.nodeList[orderNo];
			if (n.type == 'l' || n.instances == null || n.instances.size() == 0)
			{
				continue;
			}
			modified = bestAttributeParameter(level, n,rand) || modified;
			modified = bestLeft(level, n) || modified;
			modified = bestRight(level, n) || modified;
		}
		}while(modified);
	}

	private boolean bestRight(Level level, Node n) {
		// TODO Auto-generated method stub
		Node temp = n.right;
		double min = objectiveFunction(level);
		double current;
		Node minNode = n.right;
		for (Node right : level.next.nodeList) {
			if (right == temp)
				continue;
			for (Integer instance : n.instances) {
				if (n.split_parameter.computeBranch(data.get(instance))) {
					
					if(n.right.instances.remove(instance))
						n.right.distribution[(int) data.get(instance).classValue()]--;
					if(right.instances.add(instance))
						right.distribution[(int) data.get(instance).classValue()]++;
				}
			}
			n.right = right;
			current = objectiveFunction(level);
			if (current < min) {
				min = current;
				minNode = n.right;
			}
		}
		for (Integer instance : n.instances) {
			if (n.split_parameter.computeBranch(data.get(instance))) {
				if(n.right.instances.remove(instance))
				n.right.distribution[(int) data.get(instance).classValue()]--;
				if(minNode.instances.add(instance))
				minNode.distribution[(int) data.get(instance).classValue()]++;
			}
		}
		n.right = minNode;
		return !minNode.equals(temp);
	}

	private boolean bestLeft(Level level, Node n) {
		// TODO Auto-generated method stub
		Node temp = n.left;
		double min = objectiveFunction(level);
		double current;
		Node minNode = n.left;
		for (Node left : level.next.nodeList) {
			if (left == temp)
				continue;
			for (Integer instance : n.instances) {
				if (n.split_parameter.computeBranch(data.get(instance)) == false) {
					if(n.left.instances.remove(instance))
					n.left.distribution[(int) data.get(instance).classValue()]--;
					if(left.instances.add(instance))
					left.distribution[(int) data.get(instance).classValue()]++;
				}
			}
			n.left = left;
			current = objectiveFunction(level);
			if (current < min) {
				min = current;
				minNode = n.left;
			}
		}
		for (Integer instance : n.instances) {
			if (n.split_parameter.computeBranch(data.get(instance)) == false) {
				if(n.left.instances.remove(instance))
				n.left.distribution[(int) data.get(instance).classValue()]--;
				if(minNode.instances.add(instance))
				minNode.distribution[(int) data.get(instance).classValue()]++;
			}
		}
		n.left = minNode;
		return !minNode.equals(temp);
	}

	private boolean bestAttributeParameter(Level level, Node n,Random rand) {
		// TODO Auto-generated method stub
		AttributeParameter temp = n.split_parameter;
		Node left=n.left;
		Node right=n.right;
		double min = objectiveFunction(level);
		double current;
		AttributeParameter minNode = n.split_parameter;
		n.randomFeatureSelection(rand);
		for (int att:n.parameterList) {
			Attribute attr = data.attribute(att);
			if (attr.isNominal()) {
				@SuppressWarnings("unchecked")
				Enumeration<String> valueList = attr.enumerateValues();
				while (valueList.hasMoreElements()) {
					String str = valueList.nextElement();
					double value = attr.indexOfValue(str);
					AttributeParameter parameter = new AttributeParameter();
					parameter.setParameter(att, value);
					if (parameter.equals(temp))
						continue;
					for (Integer instance : n.instances) {
						if (n.split_parameter.computeBranch(data.get(instance)) != parameter
								.computeBranch(data.get(instance))) {
							if (!parameter.computeBranch(data.get(instance))) {
								if(right.instances.remove(instance))
								right.distribution[(int) data.get(instance).classValue()]--;
								if(left.instances.add(instance))
								left.distribution[(int) data.get(instance).classValue()]++;
							} else {
								if(left.instances.remove(instance))
								left.distribution[(int) data.get(instance).classValue()]--;
								if(right.instances.add(instance))
								right.distribution[(int) data.get(instance).classValue()]++;
							}
						}
					}
					n.split_parameter = parameter;
					current = objectiveFunction(level);
					if (current < min) {
						min = current;
						minNode = n.split_parameter;
					}
				}

			} else {
				for (Double value:numericValues.get(att)) {
					AttributeParameter parameter = new AttributeParameter();
					parameter.setParameter(att, value);
					if (parameter.equals(temp))
						continue;
					for (Integer instance : n.instances) {
						if (n.split_parameter.computeBranch(data.get(instance)) != parameter
								.computeBranch(data.get(instance))) {
							if (!parameter.computeBranch(data.get(instance))) {
								if(right.instances.remove(instance))
								right.distribution[(int) data.get(instance).classValue()]--;
								if(left.instances.add(instance))
								left.distribution[(int) data.get(instance).classValue()]++;
							} else {
								if(left.instances.remove(instance))
								left.distribution[(int) data.get(instance).classValue()]--;
								if(right.instances.add(instance))
								right.distribution[(int) data.get(instance).classValue()]++;
							}
						}
					}
					n.split_parameter = parameter;
					current = objectiveFunction(level);
					if (current < min) {
						min = current;
						minNode = n.split_parameter;
					}
				}
			}
		}
		for (Integer instance : n.instances) {
			if (n.split_parameter.computeBranch(data.get(instance)) != minNode
					.computeBranch(data.get(instance))) {
				if (!minNode.computeBranch(data.get(instance))) {
					if(right.instances.remove(instance))
					right.distribution[(int) data.get(instance).classValue()]--;
					if(left.instances.add(instance))
					left.distribution[(int) data.get(instance).classValue()]++;
				} else {
					if(left.instances.remove(instance))
					left.distribution[(int) data.get(instance).classValue()]--;
					if(right.instances.add(instance))
					right.distribution[(int) data.get(instance).classValue()]++;
				}
			}
		}
		n.split_parameter = minNode;
		return !minNode.equals(temp);
	}

	/**
	 * Computes the entropy of a dataset.
	 * 
	 * @param instances
	 *            the data for which entropy is to be computed
	 * @return the entropy of the data's class distribution
	 * @throws Exception
	 *             if computation fails
	 */
	private double computeEntropy(Node n) {

		double[] classCounts = n.distribution;
		double entropy = 0;
		for (int j = 0; j < noClasses; j++) {
			if (classCounts[j] > 0) {
				entropy -= classCounts[j] * Utils.log2(classCounts[j]);
			}
		}
		entropy /= (double) n.instances.size();
		return entropy + Utils.log2(n.instances.size());
	}

	private double objectiveFunction(Level level) {
		double objective = 0;
		Level next=level.next;
		for (Node n : next.nodeList) {
			ArrayList<Integer> instances=n.instances;
			if (instances != null && instances.size() != 0)
				objective += instances.size() * computeEntropy(n);
		}

		return objective;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * weka.classifiers.AbstractClassifier#distributionForInstance(weka.core
	 * .Instance)
	 */
	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		// TODO Auto-generated method stub
		double[] distribution = new double[instance.numClasses()];
		calc(distribution, zero.nodeList[0], instance);
		Utils.normalize(distribution);

		return distribution;
	}


	private void calc(double[] distribution, Node node, Instance instance) {
		// TODO Auto-generated method stub

		Node temp=null;;
		if (node.type == 'l' || node.split_parameter == null
				|| node.split_parameter.attribute == -10) {
			for (int i = 0; i < distribution.length; i++)
				distribution[i] = node.distribution[i];
			return;
		}
		else {
			temp = !node.split_parameter.computeBranch(instance) ? node.left
					: node.right;
			if (temp == null) {

				for (int i = 0; i < distribution.length; i++) {
					distribution[i] = node.distribution[i];
				}
				return;
			}
			calc(distribution, temp, instance);
			if(Utils.sum(distribution)==0)
			{
				for (int i = 0; i < distribution.length; i++) {
					distribution[i] = node.distribution[i];
				}
			}
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.classifiers.AbstractClassifier#setOptions(java.lang.String[])
	 */
	@Override
	public void setOptions(String[] options) throws Exception {

	    String tmpStr;

	    tmpStr = Utils.getOption('K', options);
	    if (tmpStr.length() != 0) {
	      setNumFeatures(Integer.parseInt(tmpStr));
	    } else {
	      setNumFeatures(0);
	    }

	    tmpStr = Utils.getOption("LS", options);
	    if (tmpStr.length() != 0) {
	       setLeafSize(Integer.parseInt(tmpStr));
	    } else {
	    	setLeafSize(1);
	    }

	    tmpStr = Utils.getOption('S', options);
	    if (tmpStr.length() != 0) {
	      setSeed(Integer.parseInt(tmpStr));
	    } else {
	      setSeed(1);
	    }

	    tmpStr = Utils.getOption("depth", options);
	    if (tmpStr.length() != 0) {
	      setMaxDepth(Integer.parseInt(tmpStr));
	    } else {
	      setMaxDepth(100);
	    }

	    tmpStr = Utils.getOption("LP", options);
	    if (tmpStr.length() != 0) {
	      setLeafPC(Integer.parseInt(tmpStr));
	    } else {
	    	setLeafPC(100);
	    }

	    tmpStr = Utils.getOption('A', options);
	    if (tmpStr.length() != 0) {
	      setMultiplier(Double.parseDouble(tmpStr));
	    } else {
	    	setMultiplier(2.0);
	    }
	    
	    tmpStr = Utils.getOption('C', options);
	    if (tmpStr.length() != 0) {
	      setMaxNodes(Integer.parseInt(tmpStr));
	    } else {
	    	setMaxNodes(128);
	    }
	    
	    tmpStr = Utils.getOption("TL", options);
	    if (tmpStr.length() != 0) {
	    	setThreshold(Integer.parseInt(tmpStr));
	    } else {
	    	setThreshold(128);
	    }
	    
	    super.setOptions(options);

	    Utils.checkForRemainingOptions(options);
	  
	}

	/**
	 * @return the threshold
	 */
	public int getThreshold() {
		return threshold;
	}


	/**
	 * @param threshold the threshold to set
	 */
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}


	/**
	 * @return the multiplier
	 */
	public double getMultiplier() {
		return multiplier;
	}


	/**
	 * @param multiplier the multiplier to set
	 */
	public void setMultiplier(double a) {
		this.multiplier = a;
	}


	/**
	 * @return the maxNodes
	 */
	public int getMaxNodes() {
		return maxNodes;
	}


	/* (non-Javadoc)
	 * @see weka.classifiers.AbstractClassifier#getOptions()
	 */
	@Override
	public String[] getOptions() {
	    Vector<String> result;
	    String[] options;
	    int i;

	    result = new Vector<String>();

	    result.add("-K");
	    result.add("" + getNumFeatures());

	    result.add("-LS");
	    result.add("" + getLeafSize());

	    result.add("-S");
	    result.add("" + getSeed());

	    result.add("-D");
	    result.add("" + getMaxDepth());
	    
	    result.add("-LP");
	    result.add(""+getLeafPC());
	    
	    result.add("-A");
	    result.add(""+getMultiplier());
	    
	    result.add("-C");
	    result.add(""+getMaxNodes());
	    
	    result.add("-TL");
	    result.add(""+getThreshold());
	    
	    options = super.getOptions();
	    for (i = 0; i < options.length; i++)
	      result.add(options[i]);

	    return (String[]) result.toArray(new String[result.size()]);
	  }


	/* (non-Javadoc)
	 * @see weka.classifiers.AbstractClassifier#listOptions()
	 */
	@Override
	public Enumeration<Option> listOptions() {

	    Vector<Option> newVector = new Vector<Option>();

	    newVector.addElement(new Option(
	        "\tNumber of attributes to randomly investigate\n"
	            + "\t(<0 = int(log_2(#attributes)+1)).", "K", 1,
	        "-K <number of attributes>"));

	    newVector.addElement(new Option(
	        "\tSMinimum number of instances per leaf.", "LS", 1,
	        "-LS <minimum number of instances>"));

	    newVector.addElement(new Option("\tSeed for random number generator.\n"
	        + "\t(default 1)", "S", 1, "-S <num>"));

	    newVector
	        .addElement(new Option(
	            "\tThe maximum depth of the tree.\n"
	                + "\t(default 100)", "depth", 1, "-depth <num>"));

	    newVector
        .addElement(new Option(
            "\tPercentage of class to turn into leaf.\n"
                + "\t(default 100)", "LP", 1, "-LP <num>"));

	    newVector
        .addElement(new Option(
            "\tThe multiplier for number of available nodes at a level.\n"
                + "\treal value(default 2.0)", "A", 1, "-A <num>"));

	    newVector
        .addElement(new Option(
            "\tThe maximum number of nodes for any level.\n"
                + "\t(default 128)", "C", 1, "-C <num>"));

	    newVector
	    .addElement(new Option(
	    		"\tThe thresold limit for values of any attribute.\n"
	    				+ "\t(default 100)", "TL", 1, "-TL <num>"));
	    
	    @SuppressWarnings("unchecked")
		Enumeration<Option> enu = super.listOptions();
	    while (enu.hasMoreElements()) {
	      newVector.addElement(enu.nextElement());
	    }

	    return newVector.elements();
	  }


	/**
	 * @param maxNodes the maxNodes to set
	 */
	public void setMaxNodes(int maxNodes) {
		this.maxNodes = maxNodes;
	}


	/**
	 * @return the leafPC
	 */
	public int getLeafPC() {
		return leafPC;
	}


	/**
	 * @param leafPC the leafPC to set
	 */
	public void setLeafPC(int leafPC) {
		this.leafPC = leafPC;
	}


	/**
	 * @return the leafSize
	 */
	public int getLeafSize() {
		return leafSize;
	}


	/**
	 * @param leafSize the leafSize to set
	 */
	public void setLeafSize(int leafSize) {
		this.leafSize = leafSize;
	}


	/**
	 * @return the seed
	 */
	public int getSeed() {
		return seed;
	}


	/**
	 * @param seed the seed to set
	 */
	public void setSeed(int seed) {
		this.seed = seed;
	}


	/**
	 * @return the maxDepth
	 */
	public int getMaxDepth() {
		return maxDepth;
	}


	/**
	 * @param maxDepth the maxDepth to set
	 */
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}


	/**
	 * @return the numFeatures
	 */
	public int getNumFeatures() {
		return numFeatures;
	}


	/**
	 * @param numFeatures the numFeatures to set
	 */
	public void setNumFeatures(int numFeatures) {
		this.numFeatures = numFeatures;
	}


	public static void main(String[] args) throws Exception {
		DecisionDAG tm = new DecisionDAG();
		runClassifier(tm, args);
	}


	@Override
	public Enumeration<String> enumerateMeasures() {
	    Vector<String> newVector = new Vector<String>(1);
	    newVector.addElement("measureTreeSize");
	    return newVector.elements();
	  }


	@Override
	public double getMeasure(String additionalMeasureName) {
	      if (additionalMeasureName.compareToIgnoreCase("measureTreeSize") == 0) {
	        return this.noOfNodes;
	      }
	      return 0;
	    }
}
