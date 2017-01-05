
package general;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.math3.analysis.function.Floor;
import org.omg.CORBA.Environment;


public class MainGame {
	
	ParamConfiguration conf;
	//protected int generation;
	private final String outdir;
	FileWriter writer1; 
	private PrintWriter out1;
	FileWriter writerOfEnvAndAssociatedTools; // writes the environment per generation + the number of associated tools. 
	FileWriter writerOfEnvAndAssociatedTools_labels; 
	FileWriter writerOfCarryingCapacityChange;
	FileWriter writerOfLossRateChange;
	private PrintWriter outEnvData;
	private PrintWriter outEnvData_labels;
	private PrintWriter outCarCapChanges;
	private PrintWriter outLossRateChanges;
	protected MetaPopulation metapop;
	
	public MainGame(ParamConfiguration conf) throws Exception
	{
		this.conf = conf;
		this.metapop = conf.metaPop;
		this.outdir = conf.outdir;
		writer1 = new FileWriter(outdir + "mainout.txt");
		out1 = new PrintWriter(writer1);
		writerOfCarryingCapacityChange = new FileWriter(outdir + "CarryingCapacityChanges.txt");
		outCarCapChanges = new PrintWriter(writerOfCarryingCapacityChange);
		writerOfLossRateChange = new FileWriter(outdir + "LossRateChanges.txt");
		outLossRateChanges = new PrintWriter(writerOfLossRateChange);
		writerOfEnvAndAssociatedTools = new FileWriter(outdir + "EnvironmentData.txt");
		outEnvData = new PrintWriter(writerOfEnvAndAssociatedTools);
		writerOfEnvAndAssociatedTools_labels = new FileWriter(conf.mainoutdir + "EnvironmentData_labels.txt");
		outEnvData_labels = new PrintWriter(writerOfEnvAndAssociatedTools_labels);
		outEnvData_labels.println("generation, environment,<pop1>,<pop2>,<pop3>,...");
		outEnvData_labels.println("for each population: "); 
		outEnvData_labels.println("NumOfOverallTools, NumOfUsefulTraitsInEnv1, NumOfUsefulTraitsInEnv2,...");
		outEnvData_labels.close();
	}

	public void run() throws IOException
	{
		while (!this.isGameOver())
		{
			this.playRound();			
			this.collectstats();
			this.advanceGeneration();
			this.resetVariables();
			System.out.println("================Just finished generation "+(conf.generation-1)+" ==================");
		}
		System.out.println("Game Over!");
		writer1.close(); outEnvData.close();writerOfEnvAndAssociatedTools.close();writerOfEnvAndAssociatedTools_labels.close();
		conf.statCollector.CloseOutputFilesAtEndOfSimulation(); outCarCapChanges.close(); writerOfCarryingCapacityChange.close();
		outLossRateChanges.close();writerOfLossRateChange.close();
	}
	
	protected void playRound() throws IOException {
		// This is the main loop, that calls the processes that run each generation.
		DetermineEnvironment();
		outEnvData.print(conf.generation+","+conf.currentEnvironment);
		for (int i=0;i<conf.metaPop.getNumOfPopulations();i++) {// for each population, do this:
			//System.out.println("now taking care of pop " + i + " in main loop");
			// outputting the number of traits useful in each environment:
			Population currpop = conf.metaPop.poplist.get(i);
			outEnvData.print(","+currpop.getOverAllNumberOfTraits());
			for (int q=0;q<currpop.listOfPerEnvUselessTraits.size();q++) {// runs within the useless tools lists over the various environments
				int notusefulInThisEnv = 0;
				for (int w=0;w<currpop.listOfPerEnvUselessTraits.get(q).size();w++) { // in this pop, in this env, run over different tool distribution types
					notusefulInThisEnv = notusefulInThisEnv + currpop.listOfPerEnvUselessTraits.get(q).get(w).size();
				}
				int usefulinEnv_q = currpop.getOverAllNumberOfTraits() - notusefulInThisEnv;
				outEnvData.print(","+usefulinEnv_q);
			}
			
			// The actual round of the game:
			ExecuteLuckyLeapDraw(i); // Checks whether a far-reaching innovation (main axis) is uncovered this generation
			if (conf.toolKitModeOn) {
				ExecuteToolKitDraw(i);
			}
			//System.out.println("checked for lucky leap in main loop");
			if (conf.combinationModeOn) {
				ExecuteCombinationAttemptDraw(i);
			}
			conf.metaPop.poplist.get(i).CalculateNumOfIndsThatKnowEAchTrait();	// this is used by the two following functions.
			CheckWhetherTraitsAreLostBecauseOfFailedTransmission(i);
			CheckWhetherEnvironmentalLossOccurs(i);
			if (conf.probOfLossRateReductionToBeReversed>0) {
				CheckWhetherLossRateReductionIsReversed();
			}
		}
		outEnvData.println();
	}
	
	private void advanceGeneration() {
		conf.generation++; 
	}

	private void DetermineEnvironment() {
		// choose whether to switch:
		double x1 = conf.randomizer.nextDouble();
		if (x1<conf.environmentSwitchProbability) { //decided to switch: (note that the same env might be re-chosen randomly)
			double r1 = conf.randomizer.nextDouble();
			int count = 1; boolean found = false;
			while (!found) {
				if (r1<=(count*(1/(double)conf.numOfPossibleEnvironments))) {
					found = true;
					conf.currentEnvironment = count-1; //the minus 1 is because envs are from 0 to X-1, if there are X possible envs.
				}
				count++;
				//System.out.println("internal Env Determination loop: " + count + " " + r1 + " " + temp);
			}
		}
	}
	
	private void ExecuteLuckyLeapDraw(int popnum) throws IOException { // the probability is per individual per generation, so 
		// I do a binomial draw, decide how many leaps will occur in a single generation, and then carry them out.
		// Normally there should be maximum one leap per generation!! I'm leaving this to the variables to define, but they should
		// be set that way, and so I leave a warning when this is not the case.
		//double r2 = conf.randomizer.nextDouble();
		int n = conf.metaPop.poplist.get(popnum).getPopsize();
		double p = conf.luckyInnovationProb;		
		int numberOfLeapsInThisGeneration = DrawBinomialNumberOfOccsInNTrialsWithProbP(n, p);
		if (numberOfLeapsInThisGeneration>1) {
			System.out.println("Note that there was more than one lucky leap in a single generation!!!");
			//Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		}
		//if (r2<TheProbabilityOfASuccessInNAttemptsWithProbOfP(n, p)) {			
		for (int leapnum=0;leapnum<numberOfLeapsInThisGeneration;leapnum++) {
			MainAxisTool newMainAxisTool = new MainAxisTool(CreateANewTool());
			newMainAxisTool.myPopNumber = popnum;
			double randomDriftDraw = conf.randomizer.nextDouble();
			System.out.println("here");
			if (randomDriftDraw<newMainAxisTool.getSofAMainAxisTool()) {
				conf.metaPop.poplist.get(popnum).AddAToolToMainAxis(newMainAxisTool);
				if (conf.fillMinorAxesAutomaticallyRegardlessOfToolkitOrCombinationRates) {
					this.FillArtificially_the_AssociatedToolAxesForANewMainAxisTool(popnum, conf.metaPop.poplist.get(popnum).getNumberOfMainAxisTraits()-1);
				}
				//System.out.println("s = " + newMainAxisTool.getSofAMainAxisTool());
			}
			// adding (7Dec2015) the option that a main axis tool changes carrying capacity => popsize:
			if (conf.probOfAMainAxisToolChangingPopSize>0) { // see explanation in ExecuteToolChangingCarryingCapacity
				double mainIsACarryingCapacityChangerRand = conf.randomizer.nextDouble();
				if (mainIsACarryingCapacityChangerRand<(conf.probOfAMainAxisToolChangingPopSize/conf.metaPop.poplist.get(popnum).popsize)) {
					ExecuteToolChangingCarryingCapacity(popnum, newMainAxisTool); //see below
				}
			}
			if (conf.probOfMainAxisToolChangingLossRate>0) { // see explanation in ExecuteToolChangingLossRate
				double mainIsALossRateChangerRand = conf.randomizer.nextDouble();
				if (mainIsALossRateChangerRand<(conf.probOfMainAxisToolChangingLossRate)) {
					ExecuteToolChangingLossRate(popnum, newMainAxisTool); //see below
				}
			}
		}
	}
	
	private void ExecuteToolChangingCarryingCapacity(int popnum, MainAxisTool popChangingMainAxisTool) {
		// The idea is that at a certain probability, the invention of a main axis tool, that can be thought of as a new technology,
		// could be such that it changes the carrying capacity, and leads to the (in the model, immediate) growth of the population 
		// size. This scenario is explored after the PNAS publication, intended as a separate short communication.
		// Note that the probability of a pop size change is inversely proportional to the popsize, which sort of makes sense:
		// the larger the pop is, the more likely it is that a groundbreaking technology will be found, but it is also less likely
		// that the carrying capacity can be significantly increased.

		// Note that the loss of the tool that led to the pop size increase does not lead to a decrease. We wave this away by
		// claiming that a technology that actively supports the livelihood of a significant part of the population is extremely
		// unlikely to be lost. This is not the case for tech that reduces loss probability - see there.
		
		double deltaChangeFactor = 0.4*(conf.randomizer.nextDouble());
		double popsizeChangeFactor = 1.2 + deltaChangeFactor; 
		popChangingMainAxisTool.mainTool.setValueOfCreatedACarryingCapacityChangeByAFactorOf(popsizeChangeFactor);
		outCarCapChanges.println(conf.generation + " " + popsizeChangeFactor);
		
		System.out.println("new popsize: ");		
		conf.metaPop.poplist.get(popnum).popsize = (int)Math.round((double)conf.metaPop.poplist.get(popnum).popsize * popsizeChangeFactor);
		System.out.println(conf.metaPop.poplist.get(popnum).popsize);
		//Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"

	}
	
	private void ExecuteToolChangingLossRate (int popnum, MainAxisTool lossChangingMainAxisTool) {
		// The idea (added 8Dec2015) is that at some low prob a technology can aid the retention of tools, by improving
		// methods of transmission or sharing and storing of knowledge (writing, drawing, papirus, etc.). 
		// This may be lost, because the loss does not immediately affect the tool repertoire - that would take another generation
		// or two.
		// Note that this has an effect on all populations, not only the one in which it arose, so is not good for runs in which
		// multiple populations are studied!!!
		
		// Within object Population, under RemoveAToolFromMainAxis, this is undone in case of the initiating tool's loss.		

		double deltaChangeFactor = (conf.randomizer.nextDouble()*0.4)+0.1; // can end up between 0.1 and 0.5
		//deltaChangeFactor = 0.001;
		double lossRateChangeFactor = 1 - deltaChangeFactor;
		lossChangingMainAxisTool.mainTool.setReducedLossRateByAFactorOf(lossRateChangeFactor);
		outLossRateChanges.println(conf.generation + " " + lossRateChangeFactor);
		System.out.println("new loss rate: ");		
		System.out.println(conf.probOfSpontaneousLoss);
		conf.RateReductionsSoFar.add(lossRateChangeFactor);
		conf.probOfSpontaneousLoss = conf.probOfSpontaneousLoss * lossRateChangeFactor;
		//Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
	}
	
	private void CheckWhetherLossRateReductionIsReversed() {
		// The idea is that the technology acountable for the reduction of loss probability may itself be lost, thus the 
		// change is reversible.
		// For simplicity (for us) I do this not through loss of the tool that specifically was accountable for it (because
		// then I can't control it - it is coupled with other aspects of the overall dynamics), but here: I count the number of
		// rate reductions that have occurred and their sizes, and the prob of reversal is, per generation, 
		// the reversalProbXNumOfREversalsSoFAr.

		double lossOfLossR = conf.randomizer.nextDouble();
		if (lossOfLossR<(conf.probOfLossRateReductionToBeReversed*conf.RateReductionsSoFar.size())) {
			int randloc = conf.randomizer.nextInt(conf.RateReductionsSoFar.size());
			double factor = conf.RateReductionsSoFar.get(randloc);
			conf.RateReductionsSoFar.remove(randloc);
			conf.probOfSpontaneousLoss = conf.probOfSpontaneousLoss / factor;
			conf.outLossRateReverseChange.println(conf.generation + " " + factor + " " + conf.probOfSpontaneousLoss);
			System.out.println("increasing the rate of loss");
		}
	}
	
	private void CreateMainAxisTraitAdHoc(int popnum, String originCombinationTool) throws IOException {
		// Added 30Sep2015: each combination trait may, at a certain low probability, spur the immediate invention of a main axis tool.
		// (say, a generalized version of that combination, that is ground-breaking). If this probability is realized, it is done via
		// a call to this function. The name of the tool records this unique invention-pathway.
		MainAxisTool newMainAxisTool = new MainAxisTool(CreateANewTool());
		newMainAxisTool.myPopNumber = popnum;
		newMainAxisTool.mainTool.NameOfTool = originCombinationTool;
		conf.metaPop.poplist.get(popnum).AddAToolToMainAxis(newMainAxisTool);
		if (conf.fillMinorAxesAutomaticallyRegardlessOfToolkitOrCombinationRates) {
			this.FillArtificially_the_AssociatedToolAxesForANewMainAxisTool(popnum, conf.metaPop.poplist.get(popnum).getNumberOfMainAxisTraits()-1);
		}
	}
	
	private void ExecuteToolKitDraw(int popnum) throws IOException { // the probOfToolKitAttempt is per individual per generation,
		// so in order to allow multiple attempts to occur in a single population, I first run a binomial draw that chooses how many
		// attempts will be made in this round, and then I execute all of them.
		//double r6 = conf.randomizer.nextDouble();
		int n = conf.metaPop.poplist.get(popnum).getPopsize();
		double p = conf.probOfToolKitAttempt;		
		if (metapop.poplist.get(popnum).getWholeMainAxisArrayList().isEmpty()) {return;}
		int numOfAttemptsInThisGeneration = DrawBinomialNumberOfOccsInNTrialsWithProbP(n, p);
		
		//if (r6<TheProbabilityOfASuccessInNAttemptsWithProbOfP(n, p)) {
		for (int attemptnum=0;attemptnum<numOfAttemptsInThisGeneration;attemptnum++) {
			// choosing a location in the datastruct that the tool would be in: (to check if the tool is novel)
			Population currpop = metapop.poplist.get(popnum);
			int locationAlongMainAxis = conf.randomizer.nextInt(currpop.getNumberOfMainAxisTraits());
			int toolKitSizeForThisMainTrait = currpop.getMainAxisToolFromLocation(locationAlongMainAxis).getToolKitMaxLengthOfThisMainAxisTool();
			//System.out.println("this main trait's ID is: " + currpop.getMainAxisToolFromLocation(locationAlongMainAxis).getMainAxisToolID());
			//System.out.println(currpop.getMainAxisToolFromLocation(locationAlongMainAxis));
			//System.out.println("this main trait's toolkit size: " + toolKitSizeForThisMainTrait);
			int locationAlongMinorAxis = conf.randomizer.nextInt(toolKitSizeForThisMainTrait);
			boolean alreadyTaken = true;
			if (currpop.getMainAxisToolFromLocation(locationAlongMainAxis).getAToolFromToolKitByIndex(locationAlongMinorAxis)==null) {
				alreadyTaken = false;
			} else {
				alreadyTaken = true;
				}
			if (!alreadyTaken) {
				Tool newToolKitTool = CreateANewTool(); 
				// currpop.getMainAxisToolFromLocation(locationAlongMainAxis);
				//System.out.println("reached here: " + locationAlongMinorAxis + " allowed, allegedly: " + toolKitSizeForThisMainTrait );
				//System.out.println("here  " + currpop.popMainAxis.get(locationAlongMainAxis).toolList.get(locationAlongMinorAxis));
				//System.out.println("calling removal of a null tool, MainGame");
				//currpop.getMainAxisToolFromLocation(locationAlongMainAxis).RemoveAToolFromToolKitAxis(locationAlongMinorAxis, popnum); // removes the initial 'null'
				// the removal in the line above was eliminated because in addANew... I now replace the content of this location (null) with the new tool.
				currpop.getMainAxisToolFromLocation(locationAlongMainAxis).addANewToolToToolKitAxis(locationAlongMinorAxis, newToolKitTool, popnum);
				//if (currpop.getMainAxisToolFromLocation(locationAlongMainAxis).getRealizedAssociatedToolKitSizeOfMainAxisTool()>21) {
				//	System.out.println("what?!?? line 132 on maingame  " + currpop.getMainAxisToolFromLocation(locationAlongMainAxis).getRealizedAssociatedToolKitSizeOfMainAxisTool());
				//	Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
				//}
			}
		}
	}
	
	private void ExecuteCombinationAttemptDraw(int popnum) throws IOException { // the probOfCombinationAttempt is per individual per generation,
		// so in order to allow multiple attempts to occur in a single population, I first run a binomial draw that chooses how many
		// attempts will be made in this round, and then I execute all of them.
		
		//double r7 = conf.randomizer.nextDouble();
		int n = conf.metaPop.poplist.get(popnum).getPopsize();
		double p = conf.probOfCombinationAttempt;		
		if (metapop.poplist.get(popnum).getWholeMainAxisArrayList().isEmpty()) {return;}
		if (metapop.poplist.get(popnum).getWholeMainAxisArrayList().size()==1) {return;} // noone to combine with
		//if (r7<TheProbabilityOfASuccessInNAttemptsWithProbOfP(n, p)) {
		int numOfcombinationAttemptsInThisGeneration = DrawBinomialNumberOfOccsInNTrialsWithProbP(n, p);
		
		// for illustration of 'clean' exponential:
		//numOfcombinationAttemptsInThisGeneration = 10000;
		
		for (int attnum=0;attnum<numOfcombinationAttemptsInThisGeneration;attnum++) {
			Population currpop = metapop.poplist.get(popnum);
			// Three schemes are now available: allow combination of main-axis traits only ; allow a mainaxis 
			// trait to combine with any other trait; or allow any trait to combine with any other.
			// Somewhat arbitrarily, I'll add any combination trait to the combination-trait-arrayList that
			// belongs to the trait among the two that has a higher ID.
			Integer id1=0; Integer id2=0;
			int locationOfTrait1AlongMainAxis=-1; int locationOfTrait2AlongMainAxis=-1;
			int locationOfParticipatingMainAxisTrait = -1;
			if (conf.toolCombinationScheme==0) {//only mainaxis traits can combine with one another
				locationOfTrait1AlongMainAxis = conf.randomizer.nextInt(currpop.getNumberOfMainAxisTraits());
				locationOfTrait2AlongMainAxis = conf.randomizer.nextInt(currpop.getNumberOfMainAxisTraits());
				id1 = currpop.getMainAxisToolFromLocation(locationOfTrait1AlongMainAxis).getMainAxisToolID();
				id2 = currpop.getMainAxisToolFromLocation(locationOfTrait2AlongMainAxis).getMainAxisToolID();
			} else if (conf.toolCombinationScheme==1) { // a main trait axis can combine with any other trait that
				// is a main-axis/combination that had preceded it or that could have preceded it, i.e a main axis 
				// trait at location X along the main axis can combine with traits belonging to locations X-1 and down.
				// This is to prevent infinite loops of things recombining again and again, and also makes more sense.
				// to realize this, a combination trait is always part of the vector of traits that is associated
				// with the main trait that participates in it.
				// The attempt is always combining the most recently discovered main axis trait, with some other trait. (This 
				// is because else we got too many sampling attempts allocated to the early parts of the main axis, in which 
				// saturation is much faster. This also makes sense: attempting to combine the new thing with older things.
				
				// Allowing combination of any main axis trait: (Apr 7th, commented out) =+=+=+=+=
				//int templocationOfParticipatingMainAxisTrait = conf.randomizer.nextInt(currpop.getNumberOfMainAxisTraits()-1);
				//locationOfParticipatingMainAxisTrait = templocationOfParticipatingMainAxisTrait + 1; // the main participating
				// trait may be only the second trait and up, else it has noone to combine with.
				// =+=+=+=+==+=+=+=+==+=+=+=+==+=+=+=+==+=+=+=+==+=+=+=+==+=+=+=+==+=+=+=+==+=+=+=
				
				locationOfParticipatingMainAxisTrait = currpop.getNumberOfMainAxisTraits()-1;
				id1 = currpop.getMainAxisToolFromLocation(locationOfParticipatingMainAxisTrait).getMainAxisToolID();

				locationOfTrait2AlongMainAxis = conf.randomizer.nextInt(locationOfParticipatingMainAxisTrait);				
				int numofcombtraits = currpop.getMainAxisToolFromLocation(locationOfTrait2AlongMainAxis).getLengthOfCombinationVector();
				int trait2 = conf.randomizer.nextInt(numofcombtraits+1);
				if (trait2==numofcombtraits) { // combining with the main axis trait itself.
					id2 = currpop.getMainAxisToolFromLocation(locationOfTrait2AlongMainAxis).getMainAxisToolID();
				} else {
					id2 = currpop.getMainAxisToolFromLocation(locationOfTrait2AlongMainAxis).getIDOfCombTraitByItsLocationInHashMapOfCombTraits(trait2);
				}				
			} else if (conf.toolCombinationScheme==2) {
				
			} else {System.out.println("somethings wrong in combinationAttempt");}
			// now I check whether this combination was destined to be useful:
			String surrogate = id1.toString() + id2.toString(); int sum=0;
			for (int i=0;i<surrogate.length();i++)  {
				sum = sum + (Character.getNumericValue(surrogate.charAt(i)));
			}
			//conf.debugCombCounter++;
			int rightmostDigit = sum % 10;
			if (rightmostDigit<=((conf.probOfNewCombinationTurningOutUseful*10)-1)) {// creating the combined tool and 
													// adding it to the combined-tools vector that the higher id 
													// is associated with.
				//conf.debugCombCounter_chosenOnes++;
				// checking if this combination is known already: (first, establishing where to look for it)
				int locationOfCombinationAlongMainAxis;
				if (conf.toolCombinationScheme==0) {
					locationOfCombinationAlongMainAxis = locationOfTrait1AlongMainAxis; 
					if (id1<id2) {locationOfCombinationAlongMainAxis = locationOfTrait2AlongMainAxis;}				
				} else if (conf.toolCombinationScheme==1) {
					locationOfCombinationAlongMainAxis = locationOfParticipatingMainAxisTrait;
				} else {
					System.out.println("in MainGame, executing combination attampt draw, undefined scheme");
					locationOfCombinationAlongMainAxis = -1;
				}
				// search for it and add if unknown:
				String newToolName = id1.toString() + "_" + id2.toString();				
				if (!(currpop.getMainAxisToolFromLocation(locationOfCombinationAlongMainAxis).combList.containsKey(newToolName))) { // if it isn't known already:
					Tool newcom = CreateANewTool();
					newcom.setNameOfTool(newToolName);
					currpop.getMainAxisToolFromLocation(locationOfCombinationAlongMainAxis).addANewToolToCombinationAxis(newcom, popnum);
					if (conf.associateEachCombinationWithBothItsFathers) { // works only in combination scheme 0 !!!! Sep 2015
						int idOfSecFather = id1; int idOfRealFather = id2;
						if (id1>id2) {idOfSecFather=id2; idOfRealFather = id1;}
						currpop.IDtoToolHash.get(idOfSecFather).addAToolToSecondaryAssociationList(newcom.getId(), idOfRealFather);
					}
					if (conf.probOfCombinationSpurringAMainAxisTool>0) { // see explanation in CreateMainAxisTraitAdHoc
						double combIsALargeLeapRand = conf.randomizer.nextDouble();
						if (combIsALargeLeapRand<conf.probOfCombinationSpurringAMainAxisTool) {
							CreateMainAxisTraitAdHoc(popnum, "CloneOf_" + newToolName);
						}
					}
				}
			} else {
				//System.out.println("not accepting a combination as useful");
				//Scanner sct = new Scanner(System.in);while(!sct.nextLine().equals("")); //wait for "enter"
			}
			
		}
	}
	
	public void FillArtificially_the_AssociatedToolAxesForANewMainAxisTool(int popnum, int locationAlongMainAxis) throws IOException {
		// this is for runs that will reproduce the simple analytical derivation: in these runs I set the prob of attempting
		// a combination or toolkit invention to zero, and instead whenever a main axis tool is created, its associated vector
		// fill up deterministically.
		// This prevents re-invention of tools that were lost, which is a process hard to account for analytically.
		Population currpop = metapop.poplist.get(popnum);
		if (conf.toolKitModeOn) {
			int toolKitSizeForThisMainTrait = currpop.getMainAxisToolFromLocation(locationAlongMainAxis).getToolKitMaxLengthOfThisMainAxisTool();
			for (int asd=0;asd<toolKitSizeForThisMainTrait;asd++) {
				Tool newToolKitTool = CreateANewTool(); 
				currpop.getMainAxisToolFromLocation(locationAlongMainAxis).addANewToolToToolKitAxis(asd, newToolKitTool, popnum);
			}
		}
		if (conf.combinationModeOn) {
			if (metapop.poplist.get(popnum).getWholeMainAxisArrayList().isEmpty()) {return;}
			if (metapop.poplist.get(popnum).getWholeMainAxisArrayList().size()==1) {return;} // noone to combine with
			int locationOfTrait1AlongMainAxis=-1; int locationOfTrait2AlongMainAxis=-1;
			int locationOfParticipatingMainAxisTrait = -1; Integer id1=0; Integer id2=0;
			
			locationOfTrait1AlongMainAxis = locationAlongMainAxis;
			id1 = currpop.getMainAxisToolFromLocation(locationOfTrait1AlongMainAxis).getMainAxisToolID();
			if (conf.toolCombinationScheme==0) {//only mainaxis traits can combine with one another
				for (int qwe=0;qwe<locationAlongMainAxis;qwe++) {				
					locationOfTrait2AlongMainAxis = qwe;
					id2 = currpop.getMainAxisToolFromLocation(locationOfTrait2AlongMainAxis).getMainAxisToolID();
					outsourcingOfCommonPartOfFillArtificiallyProcedure(id1,id2,locationAlongMainAxis,currpop, popnum);
				}
			} else if (conf.toolCombinationScheme==1) {
				for (int wer=0;wer<locationAlongMainAxis;wer++) {
					locationOfTrait2AlongMainAxis = wer;
					int numofcombtraits = currpop.getMainAxisToolFromLocation(locationOfTrait2AlongMainAxis).getLengthOfCombinationVector();
					for (int ert=0;ert<numofcombtraits+1;ert++) {
						int trait2=ert;
						if (trait2==numofcombtraits) { // combining with the main axis trait itself.
							id2 = currpop.getMainAxisToolFromLocation(locationOfTrait2AlongMainAxis).getMainAxisToolID();
						} else {
							id2 = currpop.getMainAxisToolFromLocation(locationOfTrait2AlongMainAxis).getIDOfCombTraitByItsLocationInHashMapOfCombTraits(trait2);
						}
						outsourcingOfCommonPartOfFillArtificiallyProcedure(id1,id2,locationAlongMainAxis, currpop, popnum);
					}
				}				
			} // end of (conf.toolCombinationScheme==1)
		}		
	}
	
	private void outsourcingOfCommonPartOfFillArtificiallyProcedure(Integer id1,Integer id2, int locAlongMainAxis, Population currpop, int popnum) throws IOException {
		// this is the part that the combination section of the artificial filling of associated vectors does, and is separate
		// from it in order to be called by the different for loops as is necessary based on the combination scheme that's turned on.
		
		// first I check whether this combination was destined to be useful:
		String surrogate = id1.toString() + id2.toString(); int sum=0;
		for (int i=0;i<surrogate.length();i++)  {
			sum = sum + (Character.getNumericValue(surrogate.charAt(i)));
		}
		int rightmostDigit = sum % 10;
		if (rightmostDigit<=((conf.probOfNewCombinationTurningOutUseful*10)-1)) {// creating the combined tool and 
												// adding it to the combined-tools vector that the higher id is associated with.
			// checking if this combination is known already: (first, establishing where to look for it)
			int locationOfCombinationAlongMainAxis = locAlongMainAxis;
			// search for it and add if unknown:
			String newToolName = id1.toString() + "_" + id2.toString();				
			if (!(currpop.getMainAxisToolFromLocation(locationOfCombinationAlongMainAxis).combList.containsKey(newToolName))) { // if it isn't known already:
				Tool newcom = CreateANewTool();
				newcom.setNameOfTool(newToolName);
				currpop.getMainAxisToolFromLocation(locationOfCombinationAlongMainAxis).addANewToolToCombinationAxis(newcom, popnum);
			}
		}
	}
	
	private void CheckWhetherTraitsAreLostBecauseOfFailedTransmission(int popnum) throws IOException {
		// for each population, for each type of knowledge distribution in the population (knowledge dist
		// implies num of people that know the trait), we draw a number of lost tools in this generation
		// which is a binomial draw with p=(Ploss/NumOfIndividualsWithKnowledge) and n=number of traits with
		// this distribution. We then draw which traits those would be.
		//I added records in the population of the census (ID list) of tools in each type of knowledge distribution, and 
		//a hash linking the ID to its actual location, to simplify implementation.
		
			
			// now to calculating how many traits of each type were lost:
			Population pop = conf.metaPop.poplist.get(popnum);
			ArrayList<Integer> lossesInThisGenerationPerDistType = new ArrayList<Integer>();
			for (int q=0;q<pop.numOfIndividualsThatKnowTraitOfEachDistType.size();q++) { // q is the distribution type index
				double lossprob = conf.probOfSpontaneousLoss / pop.numOfIndividualsThatKnowTraitOfEachDistType.get(q);				
				int numOfTraitsOfCurrType = pop.listOfPerDistTypeCensuses.get(q).size();
				int lostTraitsOfCurrType = DrawBinomialNumberOfOccsInNTrialsWithProbP(numOfTraitsOfCurrType,lossprob);
				lossesInThisGenerationPerDistType.add(lostTraitsOfCurrType);
				//System.out.println("to be lost (non-env reason): " + lossesInThisGenerationPerDistType);
				//System.out.println("pop num of main axis tools: " + pop.getNumberOfMainAxisTraits());
			}
			
		// Now Choosing which to loose and loosing them:
			for (int w=0;w<lossesInThisGenerationPerDistType.size();w++) {
				//System.out.println("no reason loss. w is: " + w);
				for (int e=0;e<lossesInThisGenerationPerDistType.get(w);e++) {
					//System.out.println("lossesInThisGenerationPerDistType.get(w): " + lossesInThisGenerationPerDistType.get(w));
					//System.out.println("e is " + e);
					//System.out.println("size of list of this dist type tools: " + pop.listOfPerDistTypeCensuses.get(w).size());
					//System.out.println("list: " + pop.listOfPerDistTypeCensuses.get(w));
					/*// debug:
					System.out.println("Main-axis trait ID of the traits in this dist type:");
					 for (int p=0;p<pop.listOfPerDistTypeCensuses.get(w).size();p++) {
						 System.out.println("loc (including ID) is: " + pop.locOfEachToolByID.get(pop.listOfPerDistTypeCensuses.get(w).get(p)).MainAxisFatherID);
					 }
					 */// debug to here
					conf.lostToolsCounter++; //debug
					if (pop.listOfPerDistTypeCensuses.get(w).size()>0) { // added because there are cases where,e.g., 2 traits were chosen to be lost, 
						// but the first lost was a main axis trait and all existent traits were associated with it and lost as well.
						int lostind = conf.randomizer.nextInt(pop.listOfPerDistTypeCensuses.get(w).size());
						int idOfLost = pop.listOfPerDistTypeCensuses.get(w).get(lostind);
						//System.out.println("deleting a trait for failed transmission");
						DeleteATraitChosenToBeLost(pop, idOfLost, popnum);
					} else {
						conf.lostToolsThatWereLostBeforeHavingAChanceToBeLost++;
					}
				}
			}
		
	}
	
	
	private void DeleteATraitChosenToBeLost(Population pop, int idOfLost, int popNumber) throws IOException {
		//System.out.println("I was requested to delete a trait!");
		//Scanner sct = new Scanner(System.in);while(!sct.nextLine().equals("")); //wait for "enter"
		
		// sending both the pop and pop number is redundant, but remains for historical reasons.
		String CategoryOfLost = pop.locOfEachToolByID.get(idOfLost).getWhichMinorAxis();
		int fatherID = pop.locOfEachToolByID.get(idOfLost).getMainAxisFatherID();
		int locOfFatherAlongMainAxis = pop.getWholeMainAxisArrayList().indexOf(pop.IDtoToolHash.get(fatherID));	
		/*if (locOfFatherAlongMainAxis==(-1)) { // debug
			System.out.println("father ID is "+ fatherID + "  and I fail to find him along the main axis");
			System.out.println("the actual tool I'm looking for may have been found, though: " + pop.IDtoToolHash.get(fatherID));
			System.out.println("the main axis tool Im looking for is : " + pop.IDtoToolHash.get(fatherID));
			Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		}*/
		
		if (CategoryOfLost.equals("MainAxis")) {
			pop.RemoveAToolFromMainAxis(locOfFatherAlongMainAxis);
		} else if (CategoryOfLost.equals("combinations")) {
			// I need to ask his father (main axis tool) to remove him:
			MainAxisTool t2 = pop.getMainAxisToolFromLocation(locOfFatherAlongMainAxis);
			t2.RemoveAToolFromCombinationAxis(pop.locOfEachToolByID.get(idOfLost).getWhereOnMinorAxis(), popNumber);
		} else if (CategoryOfLost.equals("toolkit")) {
			//System.out.println("father location is: " + locOfFatherAlongMainAxis);
			//System.out.println("id of trait being lost is: " + idOfLost);
			MainAxisTool t3 = pop.getMainAxisToolFromLocation(locOfFatherAlongMainAxis);
			t3.RemoveAToolFromToolKitAxis(Integer.parseInt(pop.locOfEachToolByID.get(idOfLost).getWhereOnMinorAxis()), popNumber);
		} else {
			System.out.println("Trying to loose a trait of non-existent category! In MainGame");
			Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		}
	}
	
	private void CheckWhetherEnvironmentalLossOccurs(int popnum) throws IOException {
		// Population holds a list of lists of which traits are not useful in each environment, subdvided to their distribution types among
		// subgroups.
		// For each of these, on each round, I'll choose from a binomial distribution a number of traits that'll be lost, and then choose
		// which those would be.
		// This is actually the same as regular loss, with a different probability, with regard to only those traits that are
		// momentarily useless, and it can be with or without dependence on the number of individuals who know the trait.
		// The draws are binomial draws with p=Ploss/NumOfIndividualsThatKnowTheTrait or P=Ploss, and with n = the number of traits
		// with a certain distribution and that are useless in the current env.
		
			// Calculating how many traits of each distribution type were lost:
			Population pop = conf.metaPop.poplist.get(popnum);
			ArrayList<Integer> lossesInThisGenerationPerDistTypeForEnvReason = new ArrayList<Integer>();
			for (int q=0;q<conf.numOfKnowledgeDistTypes;q++) { // q is the distribution type index
				double lossprob;
				if (conf.envLossDependentOnNumOfIndividualsWhoKnowIt) {
					lossprob = conf.probOfLossOfToolThatIsntUsefulInCurrentEnv / pop.numOfIndividualsThatKnowTraitOfEachDistType.get(q);				
				} else {
					lossprob = conf.probOfLossOfToolThatIsntUsefulInCurrentEnv;
				}
				//System.out.println("env number:  " + conf.currentEnvironment);
				//System.out.println("q: " + q);
				//System.out.println("num of main traits: " + pop.getNumberOfMainAxisTraits());
				//System.out.println("curr env useless traits: "+ pop.listOfPerEnvUselessTraits.get(conf.currentEnvironment).get(q).size());
					int numOfTraitsOfCurrDistType = pop.listOfPerEnvUselessTraits.get(conf.currentEnvironment).get(q).size();
					int lostTraitsOfCurrDistType = DrawBinomialNumberOfOccsInNTrialsWithProbP(numOfTraitsOfCurrDistType,lossprob);
					lossesInThisGenerationPerDistTypeForEnvReason.add(lostTraitsOfCurrDistType);
					//System.out.println("to be lost: " + lossesInThisGenerationPerDistTypeForEnvReason);
			}
			
		// Now Choosing which to loose and loosing them:
			for (int w=0;w<conf.numOfKnowledgeDistTypes;w++) {
				//System.out.println("env loss. dist type is: " + w);
				for (int e=0;e<lossesInThisGenerationPerDistTypeForEnvReason.get(w);e++) {
					//System.out.println("lossesInThisGenerationPerDistTypeForEnvReason.get(w): " + lossesInThisGenerationPerDistTypeForEnvReason.get(w));
					//System.out.println("e is " + e);
					//System.out.println("chossing from among the follwing number of existent traits: " + pop.listOfPerEnvUselessTraits.get(conf.currentEnvironment).get(w).size());
					if (pop.listOfPerEnvUselessTraits.get(conf.currentEnvironment).get(w).size()>0) { // added because there are cases where,e.g., 2 traits were chosen to be lost, 
						// but the first lost was a main axis trait and all existent traits were associated with it and lost as well.	
						int lostind = conf.randomizer.nextInt(pop.listOfPerEnvUselessTraits.get(conf.currentEnvironment).get(w).size());
						int idOfLost = pop.listOfPerEnvUselessTraits.get(conf.currentEnvironment).get(w).get(lostind);
						//System.out.println("deleting a trait for environmental reasons");
						DeleteATraitChosenToBeLost(pop, idOfLost, popnum);
					}
				}
			}
		
	}
	
	private Tool CreateANewTool() { // a tool, upon its discovery, is always useful in the current environment
		// and could additionally be useful in others as well. 
		
		double s = DrawASelectionCoefficientValue();
		
		// In which environments its useful:
		ArrayList<Integer> envInWhichItsUseful = new ArrayList<Integer>();		
		for (int i=0;i<conf.numOfPossibleEnvironments;i++) {
			if (i==conf.currentEnvironment) {
				envInWhichItsUseful.add(i);
			} else {
				double r3 = conf.randomizer.nextDouble();
				if (r3<conf.probOfToolBeingUsefulInEnvironment) {
					envInWhichItsUseful.add(i);
				}
			}
		}
		
		// Associated toolkit size:
		int drawOfToolKitSize = conf.randomizer.nextInt(conf.maxToolKitSize) + 1; // so there're no tools with kit of size zero.
		int knowldgeDistOfThisTool;
		
		// which parts of the pop know it:
		//================================
		// scheme in which some of the tools are known by all, and the rest by the first subpop only:
		double r5 = conf.randomizer.nextDouble();
		if (r5<conf.probOfToolKnownByFirstSubPopOnly) {
			knowldgeDistOfThisTool = 1; // Only subgroup1 knows
		} else {
			knowldgeDistOfThisTool = 0; // everybody knows
		}
		
		Tool newtool = new Tool(conf, s, knowldgeDistOfThisTool, envInWhichItsUseful, drawOfToolKitSize);
		return newtool;
	}
	
	private double TheProbabilityOfASuccessInNAttemptsWithProbOfP(int n, double p) {
		// returns the value of f(1;n,p) of the probability mass function of a binomial distribution
		// which means we neglect the probability of multiple successes within a generation.
		//long nChooseP = org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficient(n,p);
		//double ttt = Math.pow((1-p),(double)(n-1));
		return (n*p); // make sure this captures what we want to a reasonable approximation 
	}
	
	private double DrawASelectionCoefficientValue() {
		double u;double s=1.1;
		// realizing an exponential distribution:
		while (s>1) { // see matlab script in the root of the src directory that plots this distribution
			u = conf.randomizer.nextDouble();
			s = Math.log(1-u)/(-conf.lamdaOfSelectionCoefficientExpDistribution);
			//System.out.println("in search for proper s. s = " + s );
		}
		if (s<0) { // should never happen
			System.out.println("Impossible (negative) selection coefficient chosen - MainGame");
			Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		}
		return s;
	}
	
	private void resetVariables() {
		
	}
	
	protected boolean isGameOver() throws IOException {
		// checking whether the game is over because of running its full generation length:
		boolean gameisover = (conf.generation > conf.maxgeneration);
		if (conf.maxNumOfToolsForGameOver>0) { // checks only the first population (this is mainly a debugging tool, allowing runs to end nicely even if I chose params that cause them to run endlessly and slowly)
			if (conf.metaPop.poplist.get(0).getOverAllNumberOfTraits()>conf.maxNumOfToolsForGameOver) {
				gameisover = true;
			}
		}
		if (gameisover) GameIsOverStatCollection();	
		//System.out.println("is game over?" + gameisover + "  <<in isGameOver function>>");
		return (gameisover);
	}
	

	public String toString() {
		return "generation number: " + conf.generation;
	}
	
	public void collectstats() throws IOException {
		out1.println("stats along the way");
		conf.statCollector.PrintNumOfToolsPerPopAndSubPopInCurrentGeneration();
		if (conf.generation == (conf.maxgeneration-1)) {
		}
	
	}
	
	public void GameIsOverStatCollection() throws IOException {
		for (int i=0;i<conf.metaPop.numOfPopulations;i++) {
			conf.statCollector.PrintTheFullToolDataStructureOfAPopulation(conf.metaPop.poplist.get(i));
		}
		
	}
	
	public int DrawBinomialNumberOfOccsInNTrialsWithProbP(int n, double p) {
		// This will need to be done more efficiently, because I do this a lot.
		  int x = 0;
		  for(int i = 0; i < n; i++) {
		    if (conf.randomizer.nextDouble() < p)
		      x++;
		  }
		  return x;
		}
}






