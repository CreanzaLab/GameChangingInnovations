package general;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import tools.ToolLocationTriplet;

public class MainAxisTool {

	Tool mainTool;
	private ArrayList<Tool> toolList;
	HashMap<String, Tool> combList; // list of tools that are combinations of this mainAxis tool with previous tools.
	HashMap<Integer, Integer> secondaryCombList; // id of secondarily-associated combination tool, and location of its father on main axis 
	     // (this list contains the tools in which this main axis tool participates, but that aren't associated with it) 
	public ParamConfiguration conf;
	ArrayList<Integer> alreadyRemovedIDs; // for debug purposes.
	int myPopNumber; // this was added, stupidly, only very late - for the multiple-fatherhood-of-tools scheme, so is barely used.
	
	public MainAxisTool(Tool mainTool) {
		super();
		this.mainTool = mainTool;
		this.toolList = new ArrayList<Tool>();
		this.combList = new HashMap<String,Tool>(); // the string representing each tool is the concatenation of its parents IDs
		this.secondaryCombList = new HashMap<Integer, Integer>();
		for (int i=0;i<mainTool.getAssociatedToolKitSize();i++) {
			toolList.add(null);
		}
		conf = mainTool.conf;
		alreadyRemovedIDs = conf.alreadyRemovedIDs; // for debug purposes.
	}
	
	// Note that for historical/structural reasons, adding and removing a main axis trait is done in Population,
	// so there is similar code there. Changes to one of the following should probably be done there, too!!!
	
	public void addAToolToSecondaryAssociationList(int combToolID, int combToolsFatherID) {
		secondaryCombList.put(combToolID, combToolsFatherID);
	}
	
	public void addANewToolToToolKitAxis (int locationOnToolKitAxis, Tool t, int popnum) throws IOException {
		Population p = conf.metaPop.poplist.get(popnum);
		//int myMainAxisLocation = p.getWholeMainAxisArrayList().indexOf(this);
		ToolLocationTriplet loc = new ToolLocationTriplet(this.getMainAxisToolID(), "toolkit", Integer.toString(locationOnToolKitAxis));
		AddToolToCensusAndLocLists (p, t, loc);
		toolList.set(locationOnToolKitAxis, t);
		// debug:
		FileWriter outcheck1 = new FileWriter(conf.outdir + "WhichToolsAreInAndOutOfMinorAxes.txt", true); 
		PrintWriter outc1 = new PrintWriter(outcheck1);
		outc1.println("MainAxisTool "+this.getMainAxisToolID()+" thinks that he has the following tools in his kit:");
		for (int f=0;f<this.toolList.size();f++) {
			if (!(this.toolList.get(f)==null)) {
				outc1.println(" "+this.toolList.get(f).id);
			}
		}
		outc1.close(); // => debug
	}
	
	public void RemoveAToolFromToolKitAxis (int locationOnToolKitAxis, int popnum) throws IOException {
		
		
		// take care of the case it is null	
		Tool temp;
		/*
		try { // There's some redundancy here, but I'm leaving it as it behaved funny.
			//System.out.println("removing a tool from toolkit.");
		    //System.out.println("location on toolkit axis is: " + locationOnToolKitAxis);
			temp = toolList.get(locationOnToolKitAxis);
		    System.out.println(temp);
		} catch (NullPointerException e) {
			//System.out.println("reached this catch");
			return;
		}
		*/

		if (toolList.get(locationOnToolKitAxis)==null) {
			//System.out.println("reached this condition");
			return;
		}

		
		// If it is an actual tool: 
		// Take care of clearing the tool from the census list:
		Population p = conf.metaPop.poplist.get(popnum);			
		Tool t = toolList.get(locationOnToolKitAxis);
		
		FileWriter outcheck1 = new FileWriter(conf.outdir + "WhichToolsAreInAndOutOfMinorAxes.txt", true); 
		PrintWriter outc1 = new PrintWriter(outcheck1);
		outc1.println("removing tool with ID " + t.getId() + " from toolkit axis");
		outc1.close();
		
		//System.out.println(t.toString());
		/*// debug:
		if (!(p.knowledgeDist0CensusListByID.contains(t.getId()) || p.knowledgeDist1CensusListByID.contains(t.getId()))) {
			System.out.println("trying to remove a tool that doesnt apear in the lists! MainAxisTool");
			System.out.println(t.getNameOfTool() + " " + t.getId());
			System.out.println("pop num = " + popnum);
			System.out.println("here are the id lists of population this population, population " +popnum + " :");
			conf.printToScreenAnArrayList(conf.metaPop.poplist.get(popnum).knowledgeDist0CensusListByID);
			System.out.println();
			conf.printToScreenAnArrayList(conf.metaPop.poplist.get(popnum).knowledgeDist1CensusListByID);
			Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		} // debug / sanity check : to here */
				
		RemoveToolFromCensusAndLocLists(p,t);
		// actual removal:
		//toolList.remove(locationOnToolKitAxis); // I don't actually remove, because then the size starts fluctuating and
		// it makes a mess, instead, I turn it to null:
		toolList.set(locationOnToolKitAxis, null);
	}
	
	public void addANewToolToCombinationAxis (Tool t, int popnum) throws IOException {
		Population p = conf.metaPop.poplist.get(popnum);
		int myMainAxisLocation = p.getWholeMainAxisArrayList().indexOf(this);
		ToolLocationTriplet loc = new ToolLocationTriplet(this.getMainAxisToolID(), "combinations", t.NameOfTool);
		AddToolToCensusAndLocLists (p, t, loc);
		combList.put(t.getNameOfTool(), t);
	}
	
	public void RemoveAToolFromCombinationAxis (String nameOfToolToRemove, int popnum) throws IOException {
		Population p = conf.metaPop.poplist.get(popnum);
		Tool t = combList.get(nameOfToolToRemove);
		
		FileWriter outcheck1 = new FileWriter(conf.outdir + "WhichToolsAreInAndOutOfMinorAxes.txt", true); 
		PrintWriter outc1 = new PrintWriter(outcheck1);
		outc1.println("now removing tool with ID " + t.getId() + " from combination axis");
		outc1.close();
		
		// debug from here:
		if (!(p.knowledgeDist0CensusListByID.contains(t.getId()) || p.knowledgeDist1CensusListByID.contains(t.getId()))) {
			System.out.println("trying to remove a tool that doesnt apear in the lists! MainAxisTool");
			System.out.println(t.getNameOfTool() + " " + t.getId());
			System.out.println("pop num = " + popnum);
			System.out.println("here are the id lists of population this population, population " +popnum + " :");
			conf.printToScreenAnArrayList(conf.metaPop.poplist.get(popnum).knowledgeDist0CensusListByID);
			System.out.println();
			conf.printToScreenAnArrayList(conf.metaPop.poplist.get(popnum).knowledgeDist1CensusListByID);
			Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		} // debug / sanity check : to here
		
		RemoveToolFromCensusAndLocLists(p,t);
		combList.remove(nameOfToolToRemove);
	}
	
	public void AddToolToCensusAndLocLists (Population p, Tool t, ToolLocationTriplet loc) throws IOException {
		
		FileWriter outcheck1 = new FileWriter(conf.outdir + "WhichToolsAreInAndOutOfMinorAxes.txt", true); 
		PrintWriter outc1 = new PrintWriter(outcheck1);
		outc1.println("Added tool with ID " + t.getId() + " to census lists, whose axis is " + loc.WhichMinorAxis + " and dather is " + loc.MainAxisFatherID);
		outc1.close();
		
		p.locOfEachToolByID.put(t.getId(), loc);
		if (t.getKnowledgeDistributionType()==0) {
			p.knowledgeDist0CensusListByID.add(t.getId());
		} else if (t.getKnowledgeDistributionType()==1) {
			p.knowledgeDist1CensusListByID.add(t.getId());
		} else {
			System.out.println("ran into unknown knowledge distribution type! See addition of tool in MainAxisTool");
			Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		}
		for (int g=0;g<conf.numOfPossibleEnvironments;g++) { // add the tool of this ID to the lists of not-useful-tools for each environment.
			if (!(t.getEnvsInWhichItIsUseful().contains(g))) {
				p.listOfPerEnvUselessTraits.get(g).get(t.getKnowledgeDistributionType()).add(t.getId());
			}
		}
	}
	
	public void RemoveToolFromCensusAndLocLists(Population p, Tool t) {
		if (alreadyRemovedIDs.contains(t.getId())) {
			System.out.println("trying to remove a tool that has already been removed! MainAxisTool");
			Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		}
		if (t.getKnowledgeDistributionType()==0) {
			//System.out.println("in MainAxisTool, knowledgedist 0, and theres a tool here: " + t.toString() + ", with id: " + t.getId());
			/*for (int h=0;h<p.knowledgeDist0CensusListByID.size();h++) {
				System.out.println(" " + p.knowledgeDist0CensusListByID.get(h));
			} */
			int locationInList = p.knowledgeDist0CensusListByID.indexOf(t.getId());
			p.knowledgeDist0CensusListByID.remove(locationInList);
		} else if (t.getKnowledgeDistributionType()==1) {
			//System.out.println("in MainAxisTool, knowledgeDist1, and theres a tool here: " + t.toString() + ", with id: " + t.getId());
			int locationInList = p.knowledgeDist1CensusListByID.indexOf(t.getId());
			/*for (int h=0;h<p.knowledgeDist1CensusListByID.size();h++) { // for debug
				System.out.println(" " + p.knowledgeDist1CensusListByID.get(h)); // for debug
			} // for debug */
			//System.out.println("MainAxisTool, removing someone from the census lists"); // for debug
			p.knowledgeDist1CensusListByID.remove(locationInList);
			//Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"

		} else {
			System.out.println("ran into unknown knowledge distribution type! See removal of tool in MainAxisTool");
			Scanner sc = new Scanner(System.in);while(!sc.nextLine().equals("")); //wait for "enter"
		}
		// Take care of clearing the tool from the location list:
		p.locOfEachToolByID.remove(t.getId());
		
		// Remove from non-useful-tools-per-environment lists:
		for (int m=0;m<p.listOfPerEnvUselessTraits.size();m++) {
			for (int w=0;w<conf.numOfKnowledgeDistTypes;w++) {	
				if (p.listOfPerEnvUselessTraits.get(m).get(w).contains(t.getId())) {
					int loc = p.listOfPerEnvUselessTraits.get(m).get(w).indexOf(t.getId());	
					p.listOfPerEnvUselessTraits.get(m).get(w).remove(loc);
				}
			}
		}
		alreadyRemovedIDs.add(t.getId());
	}
		
	
	public void deleteAllAssociatedToolsOfMainTool(int popnum) throws IOException {
		
		// debug:
		FileWriter outcheck2 = new FileWriter(conf.outdir + "WhichToolsAreInAndOutOfMinorAxes.txt", true); 
		PrintWriter outc2 = new PrintWriter(outcheck2);
		outc2.println("MainAxisTool, "+this.getMainAxisToolID()+" , just before all his stuff is deleted, thinks that he has the following tools in his kit:");
		for (int f=0;f<this.toolList.size();f++) {
			if (!(this.toolList.get(f)==null)) {
				outc2.println(" "+this.toolList.get(f).id);
			}
		}
		outc2.close(); // => debug
		
		FileWriter outcheck1 = new FileWriter(conf.outdir + "WhichToolsAreInAndOutOfMainAxis.txt", true); 
		PrintWriter outc1 = new PrintWriter(outcheck1);
		outc1.println("deleting those associated with tool with ID " + this.getMainAxisToolID());
		outc1.println("removing minor axes traits:");
		
		for (int l=0;l<toolList.size();l++) {
			if (!(this.getAToolFromToolKitByIndex(l)==null))
				{outc1.println("removing toolkit trait: " + this.getAToolFromToolKitByIndex(l).id);}
			RemoveAToolFromToolKitAxis(l, popnum); // po kavur hakelev
		}
		
		outc1.println("combination axis size is: " + combList.size());
		
		ArrayList<String> names = new ArrayList<String>();
		for (String name : combList.keySet()) {
			names.add(name);
		}
		
		for (String name : names) {
			outc1.println("comb: " + this.combList.get(name));
			RemoveAToolFromCombinationAxis(name, popnum);
		}
		
		if (conf.associateEachCombinationWithBothItsFathers) {
			for (int secToRemove : secondaryCombList.keySet()) { // removing the combination tools that this main axis tool
				// is secondarily associated with.
				Population CP = conf.metaPop.poplist.get(myPopNumber); //current population
				int realFatherID = secondaryCombList.get(secToRemove);
				System.out.println(CP.IDtoToolHash.containsKey(realFatherID));
				if (CP.IDtoToolHash.containsKey(realFatherID)) {
					MainAxisTool realFather = CP.IDtoToolHash.get(realFatherID);
					if (CP.locOfEachToolByID.get(secToRemove) != null) {
						realFather.RemoveAToolFromCombinationAxis(CP.locOfEachToolByID.get(secToRemove).getWhereOnMinorAxis(), myPopNumber);
					}
				}
			}
		}
		
		outc1.close();
	}
	
	public int getRealizedAssociatedToolKitSizeOfMainAxisTool() {
		int c=0;
		for (int q=0;q<toolList.size();q++) {
			if (!(toolList.get(q)==null)) {
				c++;
			}
		}
		return (c);
	}
	
	public int getMainAxisToolID() {
		return this.mainTool.getId();
	}
	
	public int getToolKitMaxLengthOfThisMainAxisTool() {
		//System.out.println("I was requested to send back the size of this tool's (tool " + getMainAxisToolID() + ") toolKitMax Size");
		//System.out.println("here is the full list as it is so far realized:");
		/*for (int q=0;q<toolList.size();q++) {
			System.out.println(toolList.get(q));
		}*/
		return mainTool.getAssociatedToolKitSize(); // the number of POSSIBLE tools associated to it in the toolkit.
	}
	
	public Tool getAToolFromToolKitByIndex (int ind) {
		//System.out.println("In MainAxisTool, required to send back a tool at index " + ind + " but maxlength of toolkit is " + getToolKitMaxLengthOfThisMainAxisTool());
		return toolList.get(ind);
	}
	
	public int getLengthOfCombinationVector() {
		return this.combList.size();
	}
	
	public int getIDOfCombTraitByItsLocationInHashMapOfCombTraits(int index) {
		ArrayList<String> keysOfHash = new ArrayList<String>();
		keysOfHash.addAll(combList.keySet());
		return combList.get(keysOfHash.get(index)).getId();
	}
	
	public double getSofAMainAxisTool() {
		return mainTool.getS();
	}

	
	
}
