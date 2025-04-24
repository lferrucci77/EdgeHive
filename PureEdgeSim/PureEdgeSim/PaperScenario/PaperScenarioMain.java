package PaperScenario;

import com.mechalikh.pureedgesim.simulationcore.Simulation;

public class PaperScenarioMain {

	// Below is the path for the settings folder of this example
	//private static String settingsPath; // "PureEdgeSim/PaperScenario_Settings/";

	// The custom output folder is
	//private static String outputPath; // "PureEdgeSim/PaperScenario_Settings/";
	
	// args[1] = folder dei parametri e risultati del test
	
	public static void main(String[] args) {

		// Create a PureEdgeSim simulation
		Simulation sim = new Simulation();
		
		// changing the default output folder
		sim.setCustomOutputFolder(args[0]+"/");

		/** if we want to change the path of all configuration files at once : */

		// changing the simulation settings folder
		sim.setCustomSettingsFolder(args[0]+"/");
		
		sim.setCustomNetworkModel(PaperNetworkModel.class);
		
		// To change the mobility model
		//setCustomMobilityModel(CustomMobilityManager.class);

		// To change the tasks orchestrator
		sim.setCustomEdgeOrchestrator(PaperEdgeOrchestrator.class);

		// To change the tasks generator
		sim.setCustomTasksGenerator(PaperTaskGenerator.class);

		// To use a custom edge device/datacenters class
		sim.setCustomEdgeDataCenters(PaperEdgeDevice.class);

		// To use a custom energy model
		//setCustomEnergyModel(CustomEnergyModel.class);

		/* to use the default one you can simply delete or comment those lines */

		// Finally,you can launch the simulation
		sim.launchSimulation();
	}
}