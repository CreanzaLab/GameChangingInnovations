package general;

import java.util.ArrayList;
import java.util.Scanner;

public class Tool {
	public static int toolcounter = 0;
	private double s;
	private boolean[] whichSubPopsKnowIt;
	private int knowledgeDistributionType; // there's overlap between this and the above. I'll see what turns out to be more useful.
	private ArrayList<Integer> envsInWhichItIsUseful;
	private int associatedToolKitSize;
	public final int id;
	public String NameOfTool; // composed of a concatenation of the ParentsIDs.
	public ParamConfiguration conf;
	public double createdACarryingCapacityChangeByAFactorOf = 1; // A factor of 1 is the default, and 
	 		// means that the tool doesn't change the carrying capacity. 
	public double reducedLossRateByAFactorOf = 1; // 1 is default, means nothing is changed.
	
	public Tool(ParamConfiguration conf, double s, int distributionTypeOfToolKnowledge,
			ArrayList<Integer> envsInWhichItIsUseful, int associatedToolKitSize) {
		super();
		if (associatedToolKitSize==0) {
			System.out.println("someone tried to define a tool with a max size toolkit of zero tools!");
			Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		}
		this.s = s;
		this.conf = conf;
		//this.whichSubPopsKnowIt = whichSubPopsKnowIt;
		this.envsInWhichItIsUseful = envsInWhichItIsUseful;
		this.associatedToolKitSize = associatedToolKitSize;
		this.id = toolcounter;
		toolcounter = toolcounter+1;
		System.out.println("just created tool " + id);
		this.knowledgeDistributionType = distributionTypeOfToolKnowledge;
		if (distributionTypeOfToolKnowledge==0) {this.whichSubPopsKnowIt = conf.KnowledgeDistType0;}
		else if (distributionTypeOfToolKnowledge==1) {this.whichSubPopsKnowIt = conf.KnowledgeDistType1;}
		else {System.out.println("Tool ran into an unknown type of knowledge distribution: " + distributionTypeOfToolKnowledge);
			  Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
			}
	}


	public double getS() {
		return s;
	}


	public boolean[] getWhichSubPopsKnowIt() {
		return whichSubPopsKnowIt;
	}


	public ArrayList<Integer> getEnvsInWhichItIsUseful() {
		return envsInWhichItIsUseful;
	}


	public int getAssociatedToolKitSize() {
		return associatedToolKitSize;
	}


	public int getId() {
		return id;
	}


	public String getNameOfTool() {
		return NameOfTool;
	}


	public void setNameOfTool(String nameOfTool) {
		NameOfTool = nameOfTool;
	}


	public int getKnowledgeDistributionType() {
		return knowledgeDistributionType;
	}

	public void setValueOfCreatedACarryingCapacityChangeByAFactorOf(double PopSizeMultiplicationFactor) {
		this.createdACarryingCapacityChangeByAFactorOf = createdACarryingCapacityChangeByAFactorOf;
	}


	public void setReducedLossRateByAFactorOf(double reducedLossRateByAFactorOf) {
		this.reducedLossRateByAFactorOf = reducedLossRateByAFactorOf;
	}
	
	
	
	
}
