package tools;

public class ToolLocationTriplet {
	// a data structure that contains <mainAxisLocation,WhichMinorAxis,WhereOnMinorAxis>
	// Where: (some examples are easier than explaining)
	// <5,'MainAxis','-1'> == this is the main axis trait itself, at location 5 on it.
	// <6,'combinations','143_873'> == the combination trait whose name is 143_873, on the vector going
	//								 out of main axis trait 6.
	// <10,'toolkit','4'> == tool at location 4 in toolkit vector associated with main trait 10
	
	//public int mainAxisLocation;
	public int MainAxisFatherID; // could be my own, if I'm a main axis trait
	public String WhichMinorAxis;
	public String WhereOnMinorAxis; // This field actually holds the name of the tool.
	
	public ToolLocationTriplet(int MainAxisFatherID, String whichMinorAxis, String whereOnMinorAxis) {
		super();
		//this.mainAxisLocation = mainAxisLocation;
		this.MainAxisFatherID = MainAxisFatherID;
		WhichMinorAxis = whichMinorAxis;
		WhereOnMinorAxis = whereOnMinorAxis;
	}

	//public int getMainAxisLocation() {
	//	return mainAxisLocation;
	//}

	public int getMainAxisFatherID() {
		return MainAxisFatherID;
	}
	
	public String getWhichMinorAxis() {
		return WhichMinorAxis;
	}

	public String getWhereOnMinorAxis() {
		return WhereOnMinorAxis;
	}
	
	
	

}
