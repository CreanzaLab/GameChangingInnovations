package general;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;




public class ParamConfiguration {

	public int maxgeneration = 400000;
	public int numOfPopulations = 1; // This and the next two lines should be in accord!!!
	public int[] popsizes = new int[] {80};//,50000};
	public int poptypes[] = new int[] {0};//,0}; // a poptype defines its class/caste subdivision. definition of the meaning of each is found below.
	public boolean toolKitModeOn = true;
	public boolean combinationModeOn = false;
	public boolean associateEachCombinationWithBothItsFathers = true; // added in response to Kendal's suggestion. implemented only as a pointer list that each main tool holds, saying for which combinations it acts as a secondary father.
	public boolean fillMinorAxesAutomaticallyRegardlessOfToolkitOrCombinationRates = false; // for producing the analytical expectations, both ensuring saturation of the minor axes and that there's no unaccounted-for re-invention of lost traits.
	public int numOfPossibleEnvironments = 1; // note that they will later be referred to in many contexts as in a vector, i.e. if there are 4 environments, they will be viewed as {0,1,2,3}
	public double lamdaOfSelectionCoefficientExpDistribution = 10;
	public double environmentSwitchProbability = 0;//.01;
	public double luckyInnovationProb = 0.08; // per individual, i.e. this is multiplied by popsize
	public double probOfCombinationSpurringAMainAxisTool = 0; //0.05; // Added Sep2015, rep to Kendal. The probability that a combination, upon invention, will lead to the creation of a main axis tool. (simplification of its becoming a main axis tool itself) 
	public double probOfCombinationAttempt = 0.2; // per individual => multiplied by popsize
	public double probOfToolKitAttempt = 0.2; // per individual => multiplied by popsize
	public double probOfNewCombinationTurningOutUseful = 0.2; // per new combination; can currently be in 
				// increments of 0.1 only (0.15 is realized as though it's 0.1)
	public double probOfToolBeingUsefulInEnvironment = 0.1;//.1;//.25;//0.2;//it is always useful at least in the environment in which it was invented.
	public double probOfSpontaneousLoss = 0.08; //per existing tool; it is then divided by the num of individuals that know it
	// i.e. this is the prob of tool loss if known by a single person, and prob of a tool known by 10 people is a tenth of this.
	public double probOfLossOfToolThatIsntUsefulInCurrentEnv = 0;//.1;//.1;//0.0004; // per existing tool 
	public boolean envLossDependentOnNumOfIndividualsWhoKnowIt = true;
	public double probOfToolKnownByFirstSubPopOnly = 0.5; //all other tools are known by the whole population, currently
	public int toolCombinationScheme = 0; // 0 = only mainaxis traits combine. 
							// 1 = a mainaxis trait can combine with previous combinations and main-axis traits. 
							// (attempts are made only on the most recent main axis trait, with previous main-axis-traits or combinations)
							// 2 = any two traits can combine.
	public double probOfAMainAxisToolChangingPopSize = 0.002;//0.005;//0; //0.0075;//0.005; //7Dec2015, see ExecuteToolChangingCarryingCapacity in MainGame.
	public double probOfMainAxisToolChangingLossRate = 0;//0.00002; //0.000007; // 0.00001;//1;//0.000025;
	public boolean undoLossRateReductionUponInitiatingToolsLoss = false; // This reverses the rate reduction after regular spontanous
	// loss of the accountable tool. I'm also implementing an indepedent reversal probability.
	public double probOfLossRateReductionToBeReversed = 0; //0.000004; // prob of reversal of each of the reductions that occured, per generation
	
	public int maxToolKitSize = 10; // actually this'll lead to some cases of toolkits of size max+1.
	public int maxNumOfToolsForGameOver = 20000; // stops the game if the number of tools in population 0 goes above this.
	// if this parameter's value is 0, it is ignored and the only game-over condition is the number of generations that had passed.
	
	
	public int debugCombCounter = 0;
	public int debugCombCounter_chosenOnes = 0;
	public int lostToolsCounter = 0;
	public int lostToolsThatWereLostBeforeHavingAChanceToBeLost = 0;
	
	// globals, in effect:
	public final String workingdir = "";
	public int currentEnvironment;
	public MetaPopulation metaPop;
	public String outdir; public String mainoutdir; // the latter is the root of the former.
	public int externalLoopIterator;

	public ArrayList<Double[]> PopSubDivisionDefinitionTable = new ArrayList<Double[]>(); // the length of 
	  // the vectors entered here (see below) set the number of subpops.
	// The subdivision of the population and the distribution-type vectors below must be of the same length!
	public Double[] popsubdivision1 = {1.0,0.0,0.0,0.0,0.0}; // Type 1 = no subdivision in the population 
	public Double[] popsubdivision2 = {0.1,0.9,0.0,0.0,0.0}; //{0.1,0.9,0.0,0.0,0.0}; // Type 2 = 10% elite group, and all the others
	
	// IF more knowledge distribution types are added, add census lists in population accordingly!!!
	public int numOfKnowledgeDistTypes = 2;
	public boolean[] KnowledgeDistType0 = {true, true, true, true, true};// all subpops know the tool
	public boolean[] KnowledgeDistType1 = {true, false, false, false, false}; // only the Elite knows
	public ArrayList<boolean[]> KnowledgeDistributionDefinitionTable = new ArrayList<boolean[]>();
	
	public Random randomizer = new Random(); // chooses random numbers from 0 from 1, uniformly
	public StatCollector statCollector;
	public int generation;
	
	ArrayList<Integer> alreadyRemovedIDs = new ArrayList<Integer>();

	public FileWriter writerOfLossRateReverseChange;
	public PrintWriter outLossRateReverseChange;
	public ArrayList<Double> RateReductionsSoFar = new ArrayList<Double>();
	
	public ParamConfiguration(String mainoutdirectoryTitle) throws IOException {
		super();
		this.outdir = this.workingdir + "output"+File.separator;
		this.mainoutdir = this.outdir+mainoutdirectoryTitle+File.separator;
		this.metaPop = new MetaPopulation(this);
		PopSubDivisionDefinitionTable.add(popsubdivision1);
		PopSubDivisionDefinitionTable.add(popsubdivision2);
		KnowledgeDistributionDefinitionTable.add(KnowledgeDistType0);
		KnowledgeDistributionDefinitionTable.add(KnowledgeDistType1);
		currentEnvironment = 0;
		this.generation = 1;
	}
	
	public void copy_all_data_regarding_this_run() throws IOException {
		// This creates an output directory, and makes sure that the src files, paramconfig, etc.
		// are copied into it, to allow us to know what run params created each result.
				
		File T_outdirectory = new File(this.outdir);
		if (!T_outdirectory.exists()) {
			new File(this.outdir).mkdir();
		}
		
		// normal directory:
		String popNumStr = Integer.toString(this.numOfPopulations);
		String popSizeStr = Integer.toString(this.popsizes[0]);
		String popTypeStr = Integer.toString(this.poptypes[0]);
		String iter = Integer.toString(externalLoopIterator);
		String rundataString = "run"+ iter;
		//String rundataString = "run"+ iter + "_" + popNumStr+"popsOfSize"+popSizeStr+"Type"+popTypeStr;
		boolean success1 = (new File(this.mainoutdir)).mkdirs();		
		boolean success2 = (new File(this.mainoutdir+File.separator+rundataString)).mkdirs();		
		//boolean success1 = (new File(this.outdir+mytime)).mkdirs();
		//boolean success2 = (new File(this.outdir+mytime+File.separator+rundataString)).mkdirs();		
		this.outdir = this.mainoutdir+File.separator+rundataString+File.separator;
		
		// for multiple runs into a single output directory: (taken from BEAGLE, not used here yet)
		//boolean success1 = (new File(this.outdirectory+"ahud")).mkdirs();
		//this.outdirectory = this.outdirectory+"ahud"+File.separator;
		
		this.statCollector = new StatCollector(this); // defined here so that the output would be in the
		// out directory that was just defined. This happens at the beginning of each simulation, via a call
		// from the mmain loop.
		
		// copying the parmaconfig file, so that I'll know exactly the params of each run:
		String sourcecodedirectory = outdir.replaceAll("output.+", "");
		String sourcecodedirectory1 = sourcecodedirectory + "src"+File.separator+"general"+File.separator;
		String fullsourcecodeddirectory = sourcecodedirectory + "src";
		System.out.println(sourcecodedirectory1);
		boolean paramconfigJavaFileexists = (new File(sourcecodedirectory1+"ParamConfiguration.java")).exists();
		if (paramconfigJavaFileexists) {
			FileUtils.copyFile(new File(sourcecodedirectory1+"ParamConfiguration.java"), new File(mainoutdir+"ParamConfigCopyOfthisRun.txt"));
		} else {
			FileUtils.copyFile(new File(this.workingdir+"ParamConfig.java"), new File(mainoutdir+"ParamConfigCopyOfthisRun.txt"));
		}
		boolean fullsourceexists = (new File(fullsourcecodeddirectory)).exists();
		if (fullsourceexists) {
			FileUtils.copyDirectory(new File(fullsourcecodeddirectory), new File(mainoutdir+"src_forThisRun"));
		}
	}

	public void OpenFilesForOuputPrinting() throws IOException {
		writerOfLossRateReverseChange = new FileWriter(outdir + "LossRateReverseChange.txt");
		outLossRateReverseChange = new PrintWriter(writerOfLossRateReverseChange);
	}
	
	public void printToScreenAnArrayList(ArrayList<Integer> ar1) {
		for (int i=0;i<ar1.size();i++) {
			System.out.println(ar1.get(i));
		}
	}
}
