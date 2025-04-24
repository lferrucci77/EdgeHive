package PaperScenario;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel.Unit;

import com.mechalikh.pureedgesim.simulationcore.SimulationManager;
import com.mechalikh.pureedgesim.datacentersmanager.DataCenter;
import com.mechalikh.pureedgesim.locationmanager.Location;
import com.mechalikh.pureedgesim.locationmanager.MobilityModel;
import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters;
import com.mechalikh.pureedgesim.tasksgenerator.Task;
import com.mechalikh.pureedgesim.tasksgenerator.TasksGenerator;

public class PaperTaskGenerator extends TasksGenerator {

	static Random randTime = null;
	double execTime = SimulationParameters.INITIALIZATION_TIME;
	
	public PaperTaskGenerator(SimulationManager simulationManager) {
		super(simulationManager);
		randTime = simulationManager.getRandomGenerator();
	}

	private double CalculateMaxLinkLatency(DataCenter device1, DataCenter device2, long size) {
		long size_in_kbit = size *8;
		double latency = 0;
		if(!device1.equals(device2))	
			latency = PaperSettings.MIN_LATENCY_DELAY + (PaperSettings.LATENCY_DELAY * device1.getMobilityManager().distanceTo(device2));
		return latency + PaperSettings.LINK_LATENCY + (PaperSettings.LINK_LATENCY*((double)size_in_kbit/SimulationParameters.BANDWIDTH_WLAN)); 	
	}
	
	public List<Task> generate() {
		
		int NumNoEdgeDevices = SimulationParameters.NUM_OF_CLOUD_DATACENTERS + SimulationParameters.NUM_OF_EDGE_DATACENTERS;
		
		// I 
		for (int dev = NumNoEdgeDevices; dev < datacentersList.size(); dev++) 
		{ 							
			int DeviceID = dev - NumNoEdgeDevices;
			int DatacenterDeviceID;
			
			
			// The Edge id is equal to the DeviceID % the number of Edges ( if distribution != RANDOM )
			if (DeviceID<SimulationParameters.NUM_OF_EDGE_DATACENTERS* SimulationParameters.APPLICATIONS_LIST.size())
				DatacenterDeviceID = (DeviceID % SimulationParameters.NUM_OF_EDGE_DATACENTERS);
			
			else
				if(!SimulationParameters.DEVICES_DISTRIBUTION.equals("RANDOM"))
					DatacenterDeviceID = (DeviceID % SimulationParameters.NUM_OF_EDGE_DATACENTERS);
				else
					DatacenterDeviceID = randTime.nextInt(SimulationParameters.NUM_OF_EDGE_DATACENTERS);
				
			int NumApp = 0;		
			// The App id is equal to the DeviceID  divides the number of Edges, % the number of apps ( if distribution = UNIFORM ) 
				
			if (DeviceID<SimulationParameters.NUM_OF_EDGE_DATACENTERS* SimulationParameters.APPLICATIONS_LIST.size())
				NumApp =  (DeviceID / SimulationParameters.NUM_OF_EDGE_DATACENTERS) % SimulationParameters.APPLICATIONS_LIST.size();
			else 
				if(SimulationParameters.DEVICES_DISTRIBUTION.equals("UNIFORM"))
					NumApp =  (DeviceID / SimulationParameters.NUM_OF_EDGE_DATACENTERS) % SimulationParameters.APPLICATIONS_LIST.size();
				else
					NumApp = randTime.nextInt(SimulationParameters.APPLICATIONS_LIST.size());

			//((PaperEdgeDevice)datacentersList.get(dev)).setEdgeDatacenterOrchestratorChosen(((PaperEdgeDevice)datacentersList.get(DatacenterDeviceID)).getOrchestrator());	
				
			PaperEdgeDevice Datacenter = (PaperEdgeDevice) datacentersList.get(DatacenterDeviceID + SimulationParameters.NUM_OF_CLOUD_DATACENTERS);
			
			int EdgeDatacenterPosX, EdgeDatacenterPosY,choicePosX,choicePosY;
			do 
			{
				do
				{		
					EdgeDatacenterPosX = (int) (Datacenter.getMobilityManager().getCurrentLocation().getXPos());
					EdgeDatacenterPosY = (int)(Datacenter.getMobilityManager().getCurrentLocation().getYPos());
	
					choicePosX = randTime.nextInt(Datacenter.getRange()*2) + EdgeDatacenterPosX - Datacenter.getRange();
					choicePosY = randTime.nextInt(Datacenter.getRange()*2) + EdgeDatacenterPosY - Datacenter.getRange();								
				}
				while ((((choicePosX-EdgeDatacenterPosX) * (choicePosX-EdgeDatacenterPosX)) + ((choicePosY-EdgeDatacenterPosY) * (choicePosY-EdgeDatacenterPosY))) >= (Datacenter.getRange()*Datacenter.getRange()));				
				
				if(choicePosX <= 0)
					choicePosX = 0;
				if(choicePosX >= (SimulationParameters.AREA_WIDTH))
					choicePosX = SimulationParameters.AREA_WIDTH;	
				
				if(choicePosY <= 0)
					choicePosY = 0;
				if(choicePosY >= SimulationParameters.AREA_LENGTH)
					choicePosY = SimulationParameters.AREA_LENGTH;
					
				Location DevLocation =  new Location(choicePosX,choicePosY);
	
				MobilityModel oldMobilityModel = datacentersList.get(dev).getMobilityManager();
				
				Class<? extends MobilityModel> oldmobilityManagerClass = oldMobilityModel.getClass();
	
				Constructor<?> oldmobilityManagerConstructor;
				try
				{
					oldmobilityManagerConstructor = oldmobilityManagerClass.getConstructor(Location.class, boolean.class, double.class,
						double.class, double.class, double.class, double.class);
	
				
					datacentersList.get(dev).setMobilityManager(oldmobilityManagerConstructor.newInstance(DevLocation, oldMobilityModel.isMobile(), oldMobilityModel.getSpeed(),
						oldMobilityModel.getMinPauseDuration(), oldMobilityModel.getMaxPauseDuration(), oldMobilityModel.getMinMobilityDuration(), oldMobilityModel.getMaxMobilityDuration()));
				} 
				catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) 
				{
				}
			}
			while((CalculateMaxLinkLatency(datacentersList.get(dev), Datacenter, Math.max(Math.max(SimulationParameters.APPLICATIONS_LIST.get(NumApp).getContainerSize(),
						SimulationParameters.APPLICATIONS_LIST.get(NumApp).getRequestSize()),
						SimulationParameters.APPLICATIONS_LIST.get(NumApp).getResultsSize())) > SimulationParameters.APPLICATIONS_LIST.get(NumApp).getMaxLinkLatency()));
			
			// associate to each device an application which is the DeviceID divides for the number of EdgeDatacenter, truncated to the lower integer number 
			//int app = randTime.nextInt(SimulationParameters.APPLICATIONS_LIST.size()); 
			datacentersList.get(dev).setApplicationType(NumApp); // assign this application to that device

			// Generate only a task for each device, at the beginning of the simulation

			// start after generating all the resources
			insert(NumApp, dev, (PaperEdgeDevice)datacentersList.get(DatacenterDeviceID + SimulationParameters.NUM_OF_CLOUD_DATACENTERS));

			//}
		}
		
		return this.getTaskList();
	}

	private void insert(int app, int dev, DataCenter orchestrator) {	

		// Generate next exec time for the task 
		// execTime += SimulationParameters.UPDATE_INTERVAL*(randTime.nextInt(20)+20);
		
		double maxLatency = SimulationParameters.APPLICATIONS_LIST.get(app).getLatency(); // Load length from
																							// application file
		long length = (long) SimulationParameters.APPLICATIONS_LIST.get(app).getTaskLength(); // Load length from
																								// application file
		long requestSize = SimulationParameters.APPLICATIONS_LIST.get(app).getRequestSize();
		long outputSize = SimulationParameters.APPLICATIONS_LIST.get(app).getResultsSize();
		int pesNumber = SimulationParameters.APPLICATIONS_LIST.get(app).getNumberOfCores();
		long containerSize = SimulationParameters.APPLICATIONS_LIST.get(app).getContainerSize(); // the size of the
																									// container
		Task[] task = new Task[SimulationParameters.APPLICATIONS_LIST.get(app).getRate()];
		int id;

		// generate tasks for every edge device
		for (int i = 0; i < SimulationParameters.APPLICATIONS_LIST.get(app).getRate(); i++) {
			id = taskList.size();
			UtilizationModel utilizationModeldynamicRam = new UtilizationModelDynamic(Unit.ABSOLUTE,SimulationParameters.APPLICATIONS_LIST.get(app).getRequiredRam());
			UtilizationModel utilizationModeldynamicBW = new UtilizationModelDynamic(Unit.ABSOLUTE,SimulationParameters.APPLICATIONS_LIST.get(app).getRequiredBW());
			// if length is negative, the task execute forever
			task[i] = new Task(id, length, pesNumber);
			task[i].setFileSize(requestSize).setOutputSize(outputSize).setUtilizationModelBw(utilizationModeldynamicBW)
					.setUtilizationModelRam(utilizationModeldynamicRam).setUtilizationModelCpu(new UtilizationModelFull());
			task[i].setTime(execTime);
			task[i].setContainerSize(containerSize);
			task[i].setMaxLatency(maxLatency);
			task[i].setApplicationID(app);
			task[i].setEdgeDevice(datacentersList.get(dev)); // the device that generate this task (the origin)
			task[i].setRegistry(null); // set the registry as nothing, ignored
			task[i].setOrchestrator(orchestrator);
			taskList.add(task[i]);
			getSimulationManager().getSimulationLogger()
					.deepLog("PaperTasksGenerator, Task " + id + " of application type "+  app +" at simulation execution time " + execTime + " (s) generated.");
		}
	}

}
