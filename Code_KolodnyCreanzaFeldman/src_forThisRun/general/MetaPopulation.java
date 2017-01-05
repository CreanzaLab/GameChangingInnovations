package general;

import java.io.IOException;
import java.util.ArrayList;

public class MetaPopulation {

// simply the whole world: a list of all the populations (in phase A of the project this will be a list of length 1)
		public ArrayList<Population> poplist;
		int numOfPopulations;
		ParamConfiguration conf;
		
		
		public MetaPopulation(ParamConfiguration conf) throws IOException {
			super();
			this.conf = conf;
			this.numOfPopulations = conf.numOfPopulations;
			this.poplist = new ArrayList<Population>();
			CreateGenerationZeroMetaPop();
		}

		public void CreateGenerationZeroMetaPop () throws IOException {
			for (int i = 0; i<numOfPopulations; i++) {
				Population p1 = new Population(conf.popsizes[i], conf.poptypes[i], conf, i);
				System.out.println("popsize:" + conf.popsizes[i]);
				this.poplist.add(p1);
			}
			
		}

		public int getNumOfPopulations() {
			return numOfPopulations;
		}

		public void setNumOfPopulations(int numOfPopulations) { // this can be used if populations are annihilated or diverge
			this.numOfPopulations = numOfPopulations;
		}
		
}
