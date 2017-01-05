package general;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class StatCollector {
	
	ParamConfiguration conf;
	FileWriter writer2; 
	private PrintWriter out2;
	FileWriter writerLabels1; 
	private PrintWriter outlab1;

	public StatCollector(ParamConfiguration conf) throws IOException {
		super();
		this.conf = conf;
		writer2 = new FileWriter(conf.outdir + "NumOfToolsPerGeneration.txt", true);
		out2 = new PrintWriter(writer2);
		writerLabels1 = new FileWriter(conf.mainoutdir + "NumOfToolsPerGeneration_labels.txt", true);
		outlab1 = new PrintWriter(writerLabels1);
	}
	
	
	public void PrintNumOfToolsPerPopAndSubPopInCurrentGeneration() {
		// meant to be called on each generation to update what's going on.
		if ((conf.externalLoopIterator==1)) {
			if (conf.generation==1) {
			outlab1.println("generation,<data for population 1>,<data for population 2>,<data for population 3>,...");
			outlab1.print("<data for populatio X> == OverAllNumberOfTraits , MainAxisTraits , ToolKitTraits , Combination Traits , saturationOfExistingToolKits , saturationOfPossibleUSefulCombinations");
		}}
		
		out2.print(conf.generation+",");
		for (int i=0;i<conf.metaPop.getNumOfPopulations();i++) {
			out2.print(conf.metaPop.poplist.get(i).getOverAllNumberOfTraits()+",");	
			out2.print(conf.metaPop.poplist.get(i).getNumberOfMainAxisTraits()+",");
			out2.print(conf.metaPop.poplist.get(i).getNumberOfToolKitTraits()+",");
			out2.print(conf.metaPop.poplist.get(i).getNumberOfCombinationTraits()+",");
			out2.print(conf.metaPop.poplist.get(i).getNumberOfMainAxisTraitsThatOriginatedFromCombination()+",");
			out2.print(conf.metaPop.poplist.get(i).getPercentageOfSaturationOfExistingToolKits()+",");
			out2.print(conf.metaPop.poplist.get(i).getPercentageOfSaturationOfCombinationTraits()+",");
			}
		out2.println("");
	//out2.close();
	}
	
	public void PrintTheFullToolDataStructureOfAPopulation(Population pop) throws IOException {
		int popID = pop.getPopID();
		FileWriter writer3 = new FileWriter(conf.outdir + "pop" + popID + "gen" + conf.generation + "_toolKnowledge.txt"); 
		PrintWriter out3 = new PrintWriter(writer3);
		FileWriter writer4 = new FileWriter(conf.mainoutdir + "pop" + popID + "gen" + conf.generation + "_toolKnowledgeLabels.txt");
		PrintWriter out4 = new PrintWriter(writer4);
		for (int i = 0;i<pop.getNumberOfMainAxisTraits();i++) {
			MainAxisTool ttool = pop.getMainAxisToolFromLocation(i);
			//out3.print(ttool.getMainAxisToolID()+":");
			//out3.print("{");
			int tcount = 0;
			for (int j=0;j<ttool.getToolKitMaxLengthOfThisMainAxisTool();j++) {
				if (!(ttool.getAToolFromToolKitByIndex(j)==null)) {
					//out3.print(ttool.toolList.get(j).getId()+",");
					tcount++;
				}				
			}
			//out3.print("}");
			//out3.println();
			//+ttool.toolList.size()+","+ttool.combList.size());
			out3.println(ttool.getMainAxisToolID()+":"+tcount+","+ttool.combList.keySet().size());
		}
		out4.println("IDofMainAxisTrait:NumOfToolKitTraits,NumOfCombinationTraits");
		
		out3.close(); out4.close();
	}
	
	public void CloseOutputFilesAtEndOfSimulation() {
		out2.close(); outlab1.close();
	}
}
