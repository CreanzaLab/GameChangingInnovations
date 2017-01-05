package MainPackage;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import general.MainGame;
import general.ParamConfiguration;
import general.Population;
import general.Tool;

public class Main {
	
	public static int iterator;
	public static String nameForThisRunsOutputRoot;
	
	public static void main (String[] args) throws Exception {

		nameForThisRunsOutputRoot = produceNameForThisSimulationDirectory();

		for (iterator=0;iterator<20;iterator++) { // for multiple runs of the simulation, that may differ
			System.out.println(iterator);		 // in some of their parameters.	
			mmain(args);
		}
	}

	
	public static void mmain(String[] args) throws Exception {
		
		ParamConfiguration conf = new ParamConfiguration(nameForThisRunsOutputRoot);
		Tool.toolcounter = 0;
		Population.populationIDCount = 0;
		conf.externalLoopIterator = iterator;
		if (iterator<1) {
			//conf.probOfCombinationAttempt = 0.1;
			//conf.probOfCombinationSpurringAMainAxisTool = 0;
		} else {
			//conf.probOfCombinationAttempt = 0.5;
			//conf.probOfCombinationSpurringAMainAxisTool = 0.1;
		}
		
//		if (iterator<2) {
//			conf.associateEachCombinationWithBothItsFathers = false;
//		} else {
//			conf.associateEachCombinationWithBothItsFathers = true;
//		}
//		if (iterator<10) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.2;
//			conf.environmentSwitchProbability = 0.00001;
//		} else if (iterator<20) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.4;
//			conf.environmentSwitchProbability = 0.00001;
//		} else if (iterator<30) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.6;
//			conf.environmentSwitchProbability = 0.00001;	
//		} else if (iterator<40) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.8;
//			conf.environmentSwitchProbability = 0.00001;	
//		} else if (iterator<50) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.2;
//			conf.environmentSwitchProbability = 0.0001;	
//		} else if (iterator<60) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.4;
//			conf.environmentSwitchProbability = 0.0001;
//		} else if (iterator<70) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.6;
//			conf.environmentSwitchProbability = 0.0001;		
//		} else if (iterator<80) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.8;
//			conf.environmentSwitchProbability = 0.0001;	
//		} else if (iterator<90) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.2;
//			conf.environmentSwitchProbability = 0.001;		
//		} else if (iterator<100) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.4;
//			conf.environmentSwitchProbability = 0.001;
//		} else if (iterator<110) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.6;
//			conf.environmentSwitchProbability = 0.001;
//		} else if (iterator<120) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.8;
//			conf.environmentSwitchProbability = 0.001;
//		} else if (iterator<130) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.2;
//			conf.environmentSwitchProbability = 0.01;
//		} else if (iterator<140) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.4;
//			conf.environmentSwitchProbability = 0.01;
//		} else if (iterator<150) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.6;
//			conf.environmentSwitchProbability = 0.01;
//		} else if (iterator<160) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.8;
//			conf.environmentSwitchProbability = 0.01;
//		} else if (iterator<170) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.0009;
//			conf.environmentSwitchProbability = 0.00001;
//		} else if (iterator<180) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.009;
//			conf.environmentSwitchProbability = 0.00001;
//		} else if (iterator<190) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.09;
//			conf.environmentSwitchProbability = 0.00001;	
//		} else if (iterator<200) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.9;
//			conf.environmentSwitchProbability = 0.00001;	
//		} else if (iterator<210) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.0009;
//			conf.environmentSwitchProbability = 0.0001;
//		} else if (iterator<220) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.009;
//			conf.environmentSwitchProbability = 0.0001;
//		} else if (iterator<230) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.09;
//			conf.environmentSwitchProbability = 0.0001;	
//		} else if (iterator<240) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.9;
//			conf.environmentSwitchProbability = 0.0001;
//		} else if (iterator<250) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.0009;
//			conf.environmentSwitchProbability = 0.001;
//		} else if (iterator<260) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.009;
//			conf.environmentSwitchProbability = 0.001;
//		} else if (iterator<270) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.09;
//			conf.environmentSwitchProbability = 0.001;	
//		} else if (iterator<280) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.9;
//			conf.environmentSwitchProbability = 0.001;
//		} else if (iterator<290) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.0009;
//			conf.environmentSwitchProbability = 0.01;
//		} else if (iterator<300) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.009;
//			conf.environmentSwitchProbability = 0.01;
//		} else if (iterator<310) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.09;
//			conf.environmentSwitchProbability = 0.01;	
//		} else if (iterator<320) {
//			conf.probOfToolBeingUsefulInEnvironment = 0.9;
//			conf.environmentSwitchProbability = 0.01;
//		}
		//		if (iterator<20) {
//		conf.popsubdivision2[0] = 0.6; //{0.0,1.0,0.0,0.0,0.0};
//		conf.popsubdivision2[1] = 0.4; //{0.0,1.0,0.0,0.0,0.0};
//	} else if (iterator<40) {
//		conf.popsizes = new int[] {80};
//		conf.popsubdivision2[0] = 0.7; //{0.0,1.0,0.0,0.0,0.0};
//		conf.popsubdivision2[1] = 0.3; //{0.0,1.0,0.0,0.0,0.0};
//	} else if (iterator<60) {
//		conf.popsizes = new int[] {100};
//		conf.popsubdivision2[0] = 0.8; //{0.0,1.0,0.0,0.0,0.0};
//		conf.popsubdivision2[1] = 0.2; //{0.0,1.0,0.0,0.0,0.0};
//	} else if (iterator<80) {
//		conf.popsizes = new int[] {80};
//		conf.popsubdivision2[0] = 0.9; //{0.0,1.0,0.0,0.0,0.0};
//		conf.popsubdivision2[1] = 0.1; //{0.0,1.0,0.0,0.0,0.0};
//	} else if (iterator<100) {
//		conf.popsizes = new int[] {100};
//		conf.popsubdivision2[0] = 1.0; //{0.0,1.0,0.0,0.0,0.0};
//		conf.popsubdivision2[1] = 0.0; //{0.0,1.0,0.0,0.0,0.0};
//	}
//		if (iterator<1) {
//			conf.popsizes[0] = 5;
//			conf.probOfLossOfToolThatIsntUsefulInCurrentEnv = 0.05;
//		} else if (iterator<2) {
//			conf.popsizes[0] = 1000;
//			conf.probOfLossOfToolThatIsntUsefulInCurrentEnv = 0.05;
//		} else if (iterator<12) {
//			conf.popsizes[0] = 1000;
//			conf.probOfLossOfToolThatIsntUsefulInCurrentEnv = 0.05;
//		} else if (iterator<16) {
//			conf.popsizes[0] = 1000;
//			conf.probOfLossOfToolThatIsntUsefulInCurrentEnv = 0.5;
//		}
//		if (iterator<20) {
//			conf.popsubdivision2[0] = 0.6; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.4; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<40) {
//			conf.popsizes = new int[] {80};
//			conf.popsubdivision2[0] = 0.7; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.3; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<60) {
//			conf.popsizes = new int[] {100};
//			conf.popsubdivision2[0] = 0.8; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.2; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<40) {
//			conf.popsizes = new int[] {80};
//			conf.popsubdivision2[0] = 0.9; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.1; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<60) {
//			conf.popsizes = new int[] {100};
//			conf.popsubdivision2[0] = 1.0; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.0; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<140) {
//			conf.popsizes = new int[] {100};
//			conf.popsubdivision2[0] = 0.2; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.8; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<160) {
//			conf.popsizes = new int[] {100};
//			conf.popsubdivision2[0] = 0.4; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.6; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<180) {
//			conf.popsizes = new int[] {100};
//			conf.popsubdivision2[0] = 0.6; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.4; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<200) {
//			conf.popsizes = new int[] {100};
//			conf.popsubdivision2[0] = 0.8; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.2; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<220) {
//			conf.popsizes = new int[] {120};
//			conf.popsubdivision2[0] = 0.0; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 1.0; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<240) {
//			conf.popsizes = new int[] {120};
//			conf.popsubdivision2[0] = 0.2; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.8; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<260) {
//			conf.popsizes = new int[] {120};
//			conf.popsubdivision2[0] = 0.4; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.6; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<280) {
//			conf.popsizes = new int[] {120};
//			conf.popsubdivision2[0] = 0.6; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.4; //{0.0,1.0,0.0,0.0,0.0};
//		} else if (iterator<300) {
//			conf.popsizes = new int[] {120};
//			conf.popsubdivision2[0] = 0.8; //{0.0,1.0,0.0,0.0,0.0};
//			conf.popsubdivision2[1] = 0.2; //{0.0,1.0,0.0,0.0,0.0};		
//		}
		conf.copy_all_data_regarding_this_run();
		conf.OpenFilesForOuputPrinting();
		MainGame world_realization = new MainGame(conf);
		world_realization.run();
		
		// debug:
		System.out.println("num of combination tools created:" + conf.debugCombCounter);
		System.out.println("and accepted: " + conf. debugCombCounter_chosenOnes);
		//System.out.println("which is (fraction): " + conf.debugCombCounter_chosenOnes/conf.debugCombCounter);
		
		//double factorofToolsThatWErentLostBecauseOfTechnicalIssue = conf.lostToolsThatWereLostBeforeHavingAChanceToBeLost/conf.lostToolsCounter;
		System.out.println("lost traits: " + conf.lostToolsCounter);
		System.out.println("Traits meant to be lost that were not: " + conf.lostToolsThatWereLostBeforeHavingAChanceToBeLost);
		//System.out.println("factor of tools that should have been lost and werent: " + factorofToolsThatWErentLostBecauseOfTechnicalIssue);
	
		// close output file/s :
		conf.outLossRateReverseChange.close(); conf.writerOfLossRateReverseChange.close();
	}
	
	public static String produceNameForThisSimulationDirectory() {
		// I'd like to have all the output of a certain run in one directory, that won't be overrun by the
		// next run, so:
		Date date = Calendar.getInstance().getTime();
		String mytime = date.toString();
		System.out.println(mytime);
		mytime = mytime.replaceAll("^.+? ",""); mytime=mytime.replaceAll("GMT+.+","");
		mytime = mytime.replaceAll(" ","");mytime = mytime.replaceAll(":","");
		//mytime = mytime.substring(0,mytime.length()-2); //this throws away the seconds.
		mytime = mytime.split("PDT", 2)[0];
		// to here - preparing name of run
		return mytime;
	}
}

//Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
