package general;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import tools.ToolLocationTriplet;
//import tools.ToolLocationTriplet;

public class Population {
	// A population has a Type, which defines what fraction of the population is in each subclass
	// and how many subclasses are there in the population (e.g. Shamans 0.1, Hunters 0.2, Gatherers 0.6)
	// A population is simply the data structure that holds the info about the culture in it: it is a 
	// vector that represents the main axis tools in the population (the tools along the secondary dimensions
	// are within fields of their respective main axis tool). 
	private ArrayList<MainAxisTool> popMainAxis;
	int popsize;
	int popType;
	ParamConfiguration conf;
	public static int populationIDCount = 1;
	int popID;
	public HashMap<Integer, ToolLocationTriplet> locOfEachToolByID = new HashMap<Integer, ToolLocationTriplet>();
	public ArrayList<Integer> knowledgeDist0CensusListByID = new ArrayList<Integer>(); 
	public ArrayList<Integer> knowledgeDist1CensusListByID = new ArrayList<Integer>(); 
	public ArrayList<ArrayList<Integer>> listOfPerDistTypeCensuses = new ArrayList<ArrayList<Integer>>();
	public ArrayList<ArrayList<ArrayList<Integer>>> listOfPerEnvUselessTraits = new ArrayList<ArrayList<ArrayList<Integer>>>(); // each list 
	// here is the set of IDs of traits that are not useful in the environment whose number is that of the list index (list0 -- Env 0).
	// For each such environment there are X lists, each representing traits with a different distribution in the population, and the 
	// content of the internal-most lists are the actual IDs of the traits.
	public HashMap<Integer, MainAxisTool> IDtoToolHash = new HashMap<Integer, MainAxisTool>(); // this is a shortcut: a pointer that links each ID of a main axis tool to the mainaxis tool itself, whereever it is.
	
	public ArrayList<Integer> numOfIndividualsThatKnowTraitOfEachDistType;
	private int popnum;
	
	public Population(int popsize, int popType, ParamConfiguration conf, int popnum) throws IOException {
		super();
		this.conf = conf;
		this.popsize = popsize;	
		this.popType = popType;
		this.popMainAxis = new ArrayList<MainAxisTool>();
		this.popID = populationIDCount;
		this.popnum = popnum;
		populationIDCount = populationIDCount + 1;
		listOfPerDistTypeCensuses.add(knowledgeDist0CensusListByID);
		listOfPerDistTypeCensuses.add(knowledgeDist1CensusListByID);
		for (int i=0;i<conf.numOfPossibleEnvironments;i++) {
			ArrayList<ArrayList<Integer>> aaii = new ArrayList<ArrayList<Integer>>();
			listOfPerEnvUselessTraits.add(aaii);
			for (int g=0;g<conf.numOfKnowledgeDistTypes;g++) {
				ArrayList<Integer> ai = new ArrayList<Integer>();
				listOfPerEnvUselessTraits.get(i).add(ai);
			}
		}
	}

	public int getNumberOfMainAxisTraits() {
		return (popMainAxis.size());
	}
	
	public int getNumberOfMainAxisTraitsThatOriginatedFromCombination() {
		// added Sep 2015, following the added scheme in which a comb spurs a main axis trait at some probability
		int MainComb = 0;
		for (int i=0;i<popMainAxis.size();i++) {
			String nameOfMainTrait = popMainAxis.get(i).mainTool.getNameOfTool();
			if ((nameOfMainTrait!=null)&&(nameOfMainTrait.contains("Clone"))) {
				MainComb = MainComb + 1;
			}
		}
		return MainComb;
	}
	
	public int getNumberOfToolKitTraits() {
		int toolkitcount = 0;
		for (int i=0;i<popMainAxis.size();i++) {
			toolkitcount = toolkitcount + popMainAxis.get(i).getRealizedAssociatedToolKitSizeOfMainAxisTool();
		}
		return toolkitcount;
	}
	
	public double getPercentageOfSaturationOfExistingToolKits() { // of the potential tool kit traits that can be uncovered, 
		// given the known main axis traits, how many have been realized?
		int potentialTKTRaits = 0;
		for (int i=0;i<popMainAxis.size();i++) {
			potentialTKTRaits = potentialTKTRaits + popMainAxis.get(i).getToolKitMaxLengthOfThisMainAxisTool();
		}
		double result = (double)getNumberOfToolKitTraits() / (double) potentialTKTRaits;
		//System.out.println("toolkitSaturation: " + result + " = " + getNumberOfToolKitTraits() + " / " + potentialTKTRaits);
		return result*100;
	}
	
	public int getNumberOfCombinationTraits() {
		int combcount = 0;
		for (int i=0;i<popMainAxis.size();i++) {
			combcount = combcount + popMainAxis.get(i).combList.keySet().size();
		}
		return combcount;
	}
	
	public double getPercentageOfSaturationOfCombinationTraits() {
		double result=0;
		if (conf.toolCombinationScheme==0) { // the num of possible combinations is the series 1+2+3+...+(n-1) = n * (1+(n-1))/2 = 0.5*n^2
			int numOfMains = popMainAxis.size();
			double potentials = 0.5 * (double)numOfMains * ((double)numOfMains-1);
			result = (double)getNumberOfCombinationTraits() / (conf.probOfNewCombinationTurningOutUseful * potentials);
		} else if (conf.toolCombinationScheme==1) { // the num of possible combinations is the series above, plus combinations
			// of main axis trait with another combination, which is the series: (starting from the third main trait, because
			// the first has no combinations and the second has a single comb with the 1st main trait; each component is 
			// composed of combination-with-combination-of-main-traits plus combination-with-combination-that-includes-a-combination)
			// 1 + (1+(2+1)) + (1+(2+1)+(3+4)) + ((1+(2+1)+(3+4))+15) + (((1+(2+1)+(3+4))+15)+31) + ((((1+(2+1)+(3+4))+15)+31)+(57+6)) ==
			// == 0,1,4,11,26,57,120 == sum(2^n-n-1)
			// n in our case goes from 0 to the number of traits on the main axis minus 2.
			// to that we add the series that is the number of combinations that are maintrait-maintrait combinations, which is
			// n*((n-1)/2)
			int numOfMains = popMainAxis.size();
			int sumOfComplexCombs = 0;			
			for (int q=0;q<numOfMains-1;q++) {
				sumOfComplexCombs += (Math.pow(2, q) - q - 1);
			}
			double sumOfSums = (double)sumOfComplexCombs + (0.5 * (double)numOfMains * ((double)numOfMains-1));
			result = (double)getNumberOfCombinationTraits() / (conf.probOfNewCombinationTurningOutUseful * sumOfSums);
			//System.out.println("combtraits: " + getNumberOfCombinationTraits() + " / "+ sumOfSums);
		} 
		return result*100;
	}
	
	public int getOverAllNumberOfTraits() {
		int sum = 0;
		sum = sum + getNumberOfCombinationTraits() + getNumberOfMainAxisTraits() + getNumberOfToolKitTraits();
		return sum;
	}
	
	public int getPopsize() {
		return popsize;
	}
	
	public void setPopsize(int popsize) {
		this.popsize = popsize;
	}

	public int getPopID() {
		return popID;
	}
	
	public MainAxisTool getMainAxisToolFromLocation(int loc) {
		return popMainAxis.get(loc);
	}
	
	public void CalculateNumOfIndsThatKnowEAchTrait() {
		// We calculate the number of individuals that know each type of trait:
		// (this is called on every generation because we may want to look at non-constant population size)			
		//Population pop = conf.metaPop.poplist.get(popnum);
		numOfIndividualsThatKnowTraitOfEachDistType = new ArrayList<Integer>();
		// for this population (given its size and subdivision), find num of individuals that know each kind of trait:
		System.out.println("in Population of subdivision of type: " + popType);
		Double[] subDivVect = conf.PopSubDivisionDefinitionTable.get(popType);
		for (int j=0;j<conf.numOfKnowledgeDistTypes;j++) {// find #inds that know about each trait of this type:
			boolean[] whoKnows = conf.KnowledgeDistributionDefinitionTable.get(j);
			double counter = 0;
			for (int k=0;k<whoKnows.length;k++) {
				if (whoKnows[k]) {
					counter = counter + ((double)popsize * subDivVect[k]);
				}
			}
			numOfIndividualsThatKnowTraitOfEachDistType.add(j, (int)counter);
			// Thus this arraylist's value at location 1 is the number of individuals that know a trait that
			// has a distribution of type 1 among the subpopulations.
		}
	}
	
	public void AddAToolToMainAxis(MainAxisTool t) throws IOException {
		popMainAxis.add(t);
		this.IDtoToolHash.put(t.getMainAxisToolID(), t);
		if (t.mainTool.getKnowledgeDistributionType()==0) {
			knowledgeDist0CensusListByID.add(t.getMainAxisToolID());
		} else if (t.mainTool.getKnowledgeDistributionType()==1) {
			knowledgeDist1CensusListByID.add(t.getMainAxisToolID());
		}
		ToolLocationTriplet tlt = new ToolLocationTriplet(t.getMainAxisToolID(), "MainAxis", "-1");
		locOfEachToolByID.put(t.getMainAxisToolID(), tlt);
		
		for (int g=0;g<conf.numOfPossibleEnvironments;g++) { // add the tool of this ID to the proper lists of not-useful-tools-per-environment.
			if (!(t.mainTool.getEnvsInWhichItIsUseful().contains(g))) {
				this.listOfPerEnvUselessTraits.get(g).get(t.mainTool.getKnowledgeDistributionType()).add(t.mainTool.getId());
			}
		}
	
		FileWriter outcheck1 = new FileWriter(conf.outdir + "WhichToolsAreInAndOutOfMainAxis.txt", true); 
		PrintWriter outc1 = new PrintWriter(outcheck1);
		outc1.println("added tool with ID " + t.getMainAxisToolID() + " to main axis");
		outc1.close();

		
	}
	
	public void RemoveAToolFromMainAxis(int location) throws IOException {
		
		// remove this main trait's associated traits: toolkit and combination list
		popMainAxis.get(location).deleteAllAssociatedToolsOfMainTool(this.popnum);
		
		// remove from Census List:
		Tool t = popMainAxis.get(location).mainTool;
		if (conf.alreadyRemovedIDs.contains(t.getId())) {
			System.out.println("trying to remove a tool that is not in the lists! Population");
			Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		}
		if (t.getKnowledgeDistributionType()==0) {
			//System.out.println("in Population, and theres a tool here to be removed: " + t.toString() + ", with id: " + t.getId());
			int locationInList = knowledgeDist0CensusListByID.indexOf(t.getId());
			this.knowledgeDist0CensusListByID.remove(locationInList);
		} else if (t.getKnowledgeDistributionType()==1) {
			int locationInList = knowledgeDist1CensusListByID.indexOf(t.getId());
			this.knowledgeDist1CensusListByID.remove(locationInList);
		} else {
			System.out.println("ran into unknown knowledge distribution type! See removal of main axis tool in Population");
			Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		}
		// remove from location-by-ID list:
		locOfEachToolByID.remove(t.getId());
		IDtoToolHash.remove(t.getId());
		
		// Remove from non-useful-tools-per-environment lists:
		for (int m=0;m<listOfPerEnvUselessTraits.size();m++) {
			for (int q=0;q<conf.numOfKnowledgeDistTypes;q++) {
				if (listOfPerEnvUselessTraits.get(m).get(q).contains(t.getId())) {
					int loc = listOfPerEnvUselessTraits.get(m).get(q).indexOf(t.getId());	
					listOfPerEnvUselessTraits.get(m).get(q).remove(loc);
				}
			}
		}
		
		// If this main axis tool was accountable for a reduction of the loss rate, un-do that reduction:
		if (conf.undoLossRateReductionUponInitiatingToolsLoss) {
		if (!(t.reducedLossRateByAFactorOf==1)) {
			conf.probOfSpontaneousLoss = conf.probOfSpontaneousLoss / t.reducedLossRateByAFactorOf;
			conf.outLossRateReverseChange.println(conf.generation + " " + t.reducedLossRateByAFactorOf + " " + conf.probOfSpontaneousLoss);
			System.out.println("increasing the rate of loss back to what it was");
			//Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		}}
		
		// remove from main axis:
		popMainAxis.remove(location);	
		conf.alreadyRemovedIDs.add(t.getId());
				
		FileWriter outcheck1 = new FileWriter(conf.outdir + "WhichToolsAreInAndOutOfMainAxis.txt", true); 
		PrintWriter outc1 = new PrintWriter(outcheck1);
		outc1.println("removed tool with ID " + t.getId() + " from main axis");
		outc1.close();
	}
	
	public ArrayList<MainAxisTool> getWholeMainAxisArrayList() {
		return popMainAxis;
	}
	
}
