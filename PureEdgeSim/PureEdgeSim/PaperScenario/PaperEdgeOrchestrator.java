/**
 *     PureEdgeSim:  A Simulation Framework for Performance Evaluation of Cloud, Edge and Mist Computing Environments 
 *
 *     This file is part of PureEdgeSim Project.
 *
 *     PureEdgeSim is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     PureEdgeSim is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with PureEdgeSim. If not, see <http://www.gnu.org/licenses/>.
 *     
 *     @author Mechalikh
 **/
package PaperScenario;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.cloudbus.cloudsim.cloudlets.Cloudlet.Status;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel.Unit;
import org.cloudbus.cloudsim.cloudlets.CloudletExecution;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.listeners.EventInfo;

import com.mechalikh.pureedgesim.simulationcore.SimLog;
import com.mechalikh.pureedgesim.simulationcore.SimulationManager;
import com.mechalikh.pureedgesim.datacentersmanager.DataCenter;
import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters;
import com.mechalikh.pureedgesim.tasksgenerator.Application;
import com.mechalikh.pureedgesim.tasksgenerator.Task;
import com.mechalikh.pureedgesim.tasksorchestration.Orchestrator;

public class PaperEdgeOrchestrator extends Orchestrator {
	
	private static enum Result { OK, NOT_ENOUGH_RESOURCES, LATENCY_VIOLATED };
	
	private PaperLog log;
	
	long taskcount = 0;

	public PaperEdgeOrchestrator(SimulationManager simulationManager) {
		super(simulationManager);
		simulationManager.getSimulation().addOnClockTickListener(this::onClockTickListener);
		log = new PaperLog();
		
		String ReachableDatacenterAndAssignedApp_Header = "";
		for(int i = 0;i <= simulationManager.getDataCentersManager().getOrchestratorsList().size()+1;i++)
		{
			if (i==0)
				ReachableDatacenterAndAssignedApp_Header = "APPLICATION";
			else
			{
				if(i==1)
					ReachableDatacenterAndAssignedApp_Header += "DATACENTER";
				else
					ReachableDatacenterAndAssignedApp_Header += ", DATACENTER" + (i-2);
			}
		}
			
		log.addNewLog("ReachableDatacenterAndAssignedApp",ReachableDatacenterAndAssignedApp_Header);
		log.addNewLog("Resources", "ITERATION,VCPUS,RAM,BANDWIDTH");
		log.addNewLog("Tasks", "ITERATION,TASKS");
		log.addNewLog("Latency", "ITERATION,MEDIUMLATENCY,MAXMEDIUMLATENCY");
		log.addNewLog("ResourcesLatencyXApp", "ITERATION,APPID,APPCORES,PERCAPPCORES,APPRAM,PERCAPPRAM,APPMEANLATENCY,PERCAPPMEANLATENCY");

	}
	
	protected int findVM(String[] architecture, Task task) {
		if ("LOCAL_FIRST".equals(algorithm)) {
			return localFirst(architecture, task);
		} else {
			SimLog.println("");
			SimLog.println("Paper Orchestrator- Unknown orchestration algorithm '" + algorithm
					+ "', please check the 'settings/simulation_parameters.properties' file you are using");
			// Cancel the simulation
			SimulationParameters.STOP = true;
			simulationManager.getSimulation().terminate();
		}
		return -1;
	}

	/**
	 * @param architecture
	 * @param task
	 * @return
	 */
	protected int localFirst(String[] architecture, Task task) {
		int vm = -1;
		
		taskcount++;
		
		if(vmList != Vm.NULL)
			for (int i = 0; i < orchestrationHistory.size(); i++) 
			{			
				Vm VM = vmList.get(i);
				
				//Search the VM local to the orchestrator, first
				if((VM.getHost().getDatacenter()).equals(task.getOrchestrator()))
				{		
					boolean found = false;
					
					List<PaperEdgeDevice> Users = new ArrayList<PaperEdgeDevice>(1);
					
					ListIterator<Task> LT = simulationManager.getTasksList().listIterator();

					while(LT.hasNext())
					{
						Task t = LT.next();		
						if(!t.equals(task))
							if(((List<Integer>)orchestrationHistory.get(i)).contains((int)t.getId()))
							{
								
								// check if the task t is finished yet
								boolean finished = false;
								ListIterator<CloudletExecution> CLE_list = t.getVm().getCloudletScheduler().getCloudletFinishedList().listIterator();
								while(CLE_list.hasNext())
								{
									Task t_finished = (Task) CLE_list.next().getCloudlet();
									if(t_finished.equals(t))
										finished=true;
								}
								
								if(!finished)
									// check if the new task and the selected task had the same application ID
									if (t.getApplicationID() == task.getApplicationID())
										{
											found = true;
											Users.add((PaperEdgeDevice)task.getEdgeDevice());
											if(offloadingIsPossible(t, VM, Users)==Result.OK)
											{
												updateResources(t, VM, Users.size());
												System.out.println("Task " + task.getId() + " di tipo applicazione " + task.getApplicationID() + " ricevuto al tempo " + simulationManager.getSimulation().clock() + " schedulato dal Datacenter " +  VM.getHost().getDatacenter().getId() + " : aggiunto un nuovo utente al task " +t.getId() +"!");

												t.addUserDevices((PaperEdgeDevice)task.getEdgeDevice());
											}
											else
												System.out.println("Task " + task.getId() + " di tipo applicazione " + task.getApplicationID() + " ricevuto al tempo " + simulationManager.getSimulation().clock() + " non schedulato dal Datacenter " +  VM.getHost().getDatacenter().getId() + " : risorse insufficienti per un nuovo utente al task"+ t.getId() +" !");
											break;
										}								
							}
					}
						
					if(!found)
					{
						//check if local resources are enough to execute the new task
						if(offloadingIsPossible(task, VM, Users)==Result.OK)						
						{			
						
							// Update the VM resources, for tasks deployed at the same simulation time
							updateResources(task, VM, 0);
							task.addUserDevices((PaperEdgeDevice)task.getEdgeDevice());
							((PaperEdgeDevice)task.getOrchestrator()).addApplicationIDsInExecution(task.getApplicationID());
							System.out.println("Task " + task.getId() + " di tipo applicazione " + task.getApplicationID() + " ricevuto al tempo " + this.simulationManager.getSimulation().clock() + " schedulato dal Datacenter " +  VM.getHost().getDatacenter().getId() + " sulla Vm " + i);
							vm =  i;
							break;
						}
						System.out.println("Task " + task.getId() + " di tipo applicazione " + task.getApplicationID() + " ricevuto al tempo " + this.simulationManager.getSimulation().clock() + " non schedulato dal Datacenter " +  VM.getHost().getDatacenter().getId() + ": risorse insufficienti!");
					}
				}
			}
		
		if (taskcount == simulationManager.getTasksList().size())
		{			
			
			System.out.println("Last task processed by the orchestrator at time " + simulationManager.getSimulation().clock() + "! ");
			ListIterator<DataCenter> ListOrchIter =  simulationManager.getDataCentersManager().getOrchestratorsList().listIterator();
			boolean first = true;
			while(ListOrchIter.hasNext())
			{
				DataCenter Orchestrator = ListOrchIter.next();
				if(first)
				{
					PrintLog();
					PrintAndSaveLogOnce();
					Orchestrator.schedule(PaperSettings.ORCHESTRATION_ALGORITHM_INTERVAL, PaperEdgeDevice.PRINT_LOG);
					first = false;
				}

				Orchestrator.schedule(SimulationParameters.UPDATE_INTERVAL, PaperEdgeDevice.OPTIMIZATION_REQUEST_INIT);
				
				Vm VM = Orchestrator.getResources().getVmList().get(0);
				
				System.out.println("---------------------------------------------------------------------------------------------------------");
				System.out.println("-----------------------SITUAZIONE ALL'INIZIO DELLA SIMULAZIONE AL TEMPO "+ simulationManager.getSimulation().clock() +" -------------------------------");

				System.out.printf("Edge Datacenter " + Orchestrator.getId() + " | Vm %d CPU Usage: %d vCPUs on %d vCPUs | Running Tasks: #%d | RAM usage: %d MB on %d MB \n",
						VM.getId(), VM.getNumberOfPes()-VM.getFreePesNumber(), VM.getNumberOfPes(),
						VM.getCloudletScheduler().getCloudletExecList().size(),
						VM.getRam().getAllocatedResource(), VM.getRam().getCapacity());


				ListIterator<CloudletExecution> CLE_list =	Orchestrator.getResources().getVmList().get(0).getCloudletScheduler().getCloudletExecList().listIterator();
				while(CLE_list.hasNext())
				{
					Task t = (Task) CLE_list.next().getCloudlet();		
					System.out.printf("---- Task %3d: " + Orchestrator + "| App ID: %3d |  CPU usage: %3d vCPUs | RAM usage: %5.2f MB | BW Usage: %5.2f | Num Users: %d \n",
							t.getId(), t.getApplicationID(),t.getNumberOfPes(), t.getUtilizationOfRam(),
							t.getUtilizationOfBw(), t.getUsersDevices().size());
				}
			}
			SaveResourcesStats("Initial");
		}
		
		// assign the tasks to the vm found
		return vm;
	}
	
    public void SaveResourcesStats(String phase) {
        // Nome del file di destinazione
        String nomeFileRam = "DatacentersRam" + phase + "Stats";
        String ext = ".txt";

        String nomeFileCpu = "DatacentersCpu" + phase + "Stats";
        String nomeFileBW = "DatacentersBW" + phase + "Stats";

       
        
        // Uso di BufferedWriter per scrivere nel file
        try {
        	BufferedWriter writerRam = new BufferedWriter(new FileWriter(PaperLog.getFileName(nomeFileRam,ext)));
        	BufferedWriter writerCpu = new BufferedWriter(new FileWriter(PaperLog.getFileName(nomeFileCpu,ext)));
        	BufferedWriter writerBW = new BufferedWriter(new FileWriter(PaperLog.getFileName(nomeFileBW,ext)));

        	List<? extends DataCenter> dclist = simulationManager.getDataCentersManager().getDatacenterList();
        	
            // Scrivo i numeri da 0 a 125 nel file, uno per riga
    		for (int dev = SimulationParameters.NUM_OF_CLOUD_DATACENTERS; dev <= SimulationParameters.NUM_OF_EDGE_DATACENTERS; dev++)  {
    			PaperEdgeDevice Datacenter = (PaperEdgeDevice) dclist.get(dev);
				int EdgeDatacenterPosX = (int) (Datacenter.getMobilityManager().getCurrentLocation().getXPos());
				int EdgeDatacenterPosY = (int)(Datacenter.getMobilityManager().getCurrentLocation().getYPos());
				Vm VM = Datacenter.getResources().getVmList().get(0);
				
				double CpuOccupancy =  (VM.getNumberOfPes()-VM.getFreePesNumber())/(double) VM.getNumberOfPes();
				double RamOccupancy = VM.getRam().getAllocatedResource()/(double)VM.getRam().getCapacity();
				double BWOccupancy = VM.getBw().getAllocatedResource()/(double)VM.getBw().getCapacity();
				
                writerRam.write(Integer.toString(dev-1) + ',' + EdgeDatacenterPosX + ',' + EdgeDatacenterPosY + ',' + RamOccupancy + ',' + Datacenter.getCoefficient("MCU_RAM_linear") + ',' + Datacenter.getCoefficient("MCU_RAM_exponential"));  // Scrivo i dati della CPU di un edge                          
                writerCpu.write(Integer.toString(dev-1) + ',' + EdgeDatacenterPosX + ',' + EdgeDatacenterPosY + ',' + CpuOccupancy + ',' + Datacenter.getCoefficient("MCU_CPU_linear") + ',' + Datacenter.getCoefficient("MCU_bandwidth_exponential"));  // Scrivo i dati della CPU di un edge                
                writerBW.write(Integer.toString(dev-1) + ',' + EdgeDatacenterPosX + ',' + EdgeDatacenterPosY + ',' + BWOccupancy + ',' + Datacenter.getCoefficient("MCU_bandwidth_linear") + ',' + Datacenter.getCoefficient("MCU_CPU_exponential"));  // Scrivo i dati della BW di un edge
                
                if(dev!=SimulationParameters.NUM_OF_EDGE_DATACENTERS) {
                	writerRam.newLine();  // Aggiungo una nuova riga
                	writerCpu.newLine();  // Aggiungo una nuova riga
                	writerBW.newLine();  // Aggiungo una nuova riga
    			}                
            }	
    		writerRam.close();
    		writerCpu.close();
    		writerBW.close();
        } catch (IOException e) {
            // Gestione dell'errore se il file non può essere scritto
            System.err.println("Errore durante la scrittura del file: " + e.getMessage());
        }
    }
	
	public void PrintAndSaveLogOnce()
	{

		ListIterator<Task> LT = simulationManager.getTasksList().listIterator();
		int NumDataCenters = simulationManager.getDataCentersManager().getOrchestratorsList().size();

		long[][] lines = new long[NumDataCenters*SimulationParameters.APPLICATIONS_LIST.size()][NumDataCenters+2];
		for (int i=0; i<lines.length;i++)			
			for (int j=0; j<lines[i].length;j++)
			{
				if(j==0 || j==1)					
					lines[i][j] = -1;
				else lines[i][j] = 1;
			}
		
		while(LT.hasNext())
		{
			Task t = LT.next();	
			for (PaperEdgeDevice user: t.getUsersDevices())
			{				
				for(DataCenter datacenter: simulationManager.getDataCentersManager().getOrchestratorsList())
				{			
					if (sameLocation(datacenter,user,datacenter.getRange())
							&& (sameLocation(datacenter, user,SimulationParameters.EDGE_DEVICES_RANGE))
							&& (CalculateMaxLinkLatency(user,datacenter,Math.max(Math.max(t.getContainerSize(),t.getFileSize()),t.getOutputSize())) <=
							SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getMaxLinkLatency()))
					{
						lines[(t.getApplicationID()*NumDataCenters)+((int)t.getOrchestrator().getId()-4)][0] = t.getApplicationID();  
						lines[(t.getApplicationID()*NumDataCenters)+((int)t.getOrchestrator().getId()-4)][1] = t.getOrchestrator().getId()-4;	
					}
					else
						lines[(t.getApplicationID()*NumDataCenters)+((int)t.getOrchestrator().getId()-4)][(int)datacenter.getId()-2] = 0;										
				}
			}
		}

		for (long[] line:lines)
		{
			
			String Line = String.valueOf(line[0]);
			
			Line += ","+String.valueOf(line[1]);
			
			for (int i=2; i<line.length;i++)	
				Line += ","+ String.valueOf(line[i]);																	
			log.print("ReachableDatacenterAndAssignedApp", Line);	
		}
		
		log.saveLog("ReachableDatacenterAndAssignedApp");
	}

	
	public void SaveAllLogs()
	{
		log.saveLog("Resources");
		log.saveLog("Tasks");
		log.saveLog("Latency");
		//log.saveLog("ResourcesLatencyPerDatacenter");
		log.saveLog("ResourcesLatencyXApp");
		SaveResourcesStats("Final");
	}
	
	public void PrintLog () 
	{		
		double edCpuUsage = 0;
		double edCpuTotal = 0;
		double edRamTotal = 0;
		double edRamUsage = 0;
		//double edBWUsage = 0;
		//double edBWTotal = 0;
		double edTasksTotal = 0;
		double edTotalActualLatency = 0;
		double edTotalMaxLatency = 0;
		
		HashMap<Integer,double[]> AppInfo = null;
		
		if(vmList != Vm.NULL)
			for (Vm VM:vmList) 
			{				
				double edDatacenterCpuUsage = 0;
				double edDatacenterRamUsage = 0;

				edTasksTotal +=	VM.getCloudletScheduler().getCloudletExecList().size();

				edCpuTotal += VM.getNumberOfPes();
				edRamTotal += VM.getRam().getCapacity();
				//edBWTotal += VM.getBw().getCapacity();		
				
				//Datacenter VmDatacenter = VM.getHost().getDatacenter();		
				
				ListIterator<CloudletExecution> CLE_list =	VM.getCloudletScheduler().getCloudletExecList().listIterator();
				while(CLE_list.hasNext())
				{
					Task t = (Task) CLE_list.next().getCloudlet();	
					if (AppInfo == null)
					{
						AppInfo = new HashMap<Integer,double[]>();
						double[] el = {t.getNumberOfPes(),t.getUtilizationOfRam(),0, 0, t.getUsersDevices().size(), t.getApplicationID()};
						for(PaperEdgeDevice UserDevice:t.getUsersDevices())
						{ 
							edTotalActualLatency += CalculateMaxLinkLatency(UserDevice, t.getOrchestrator(),Math.max(Math.max(t.getContainerSize(),t.getFileSize()),t.getOutputSize()));
							edTotalMaxLatency += SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getMaxLinkLatency();
							el[2] += CalculateMaxLinkLatency(UserDevice, t.getOrchestrator(),Math.max(Math.max(t.getContainerSize(),t.getFileSize()),t.getOutputSize()));
							el[3] += SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getMaxLinkLatency();
						}	
											
						AppInfo.put(t.getApplicationID(),el);
					}
					else
					{
						double[] el = AppInfo.get(t.getApplicationID());
					
						if (el==null)
						{	
							el = new double[6];
							el[0] = el[1] = el[2] = el[3] = el[4] = el[5] = 0;							
						}
						
						el[0] += t.getNumberOfPes();
						el[1] += t.getUtilizationOfRam();
						
						for(PaperEdgeDevice UserDevice:t.getUsersDevices())
						{ 
							edTotalActualLatency += CalculateMaxLinkLatency(UserDevice, t.getOrchestrator(),Math.max(Math.max(t.getContainerSize(),t.getFileSize()),t.getOutputSize()));
							edTotalMaxLatency += SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getMaxLinkLatency();
							el[2] += CalculateMaxLinkLatency(UserDevice, t.getOrchestrator(),Math.max(Math.max(t.getContainerSize(),t.getFileSize()),t.getOutputSize()));
							el[3] += SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getMaxLinkLatency();
							
						}	
						
						el[4] += t.getUsersDevices().size();
						el[5] = t.getApplicationID();
						AppInfo.put(t.getApplicationID(),el);

					}															
										
					edDatacenterCpuUsage += t.getNumberOfPes();					
					edDatacenterRamUsage += t.getUtilizationOfRam();
					//edBWUsage += t.getUtilizationOfBw();

				}
				edRamUsage += edDatacenterRamUsage;
				edCpuUsage += edDatacenterCpuUsage;
				
				CLE_list =	VM.getCloudletScheduler().getCloudletExecList().listIterator();
	
				// Repeat loop to add new infos
				/*
				while(CLE_list.hasNext())
				{
					Task t = (Task) CLE_list.next().getCloudlet();	
					
					for(PaperEdgeDevice UserDevice:t.getUsersDevices())
					{ 
						// Iteration, DataCenterID, AppID, Cores ( x app ), % Cores ( respect to Max Cores of a Datacenter ), RAM ( x app ), % RAM ( respect to Max RAM of a Datacenter ), Latency in seconds ( singolo user ), % Latency ( on Max Latency per App )    
						log.print("ResourcesLatencyPerDatacenter", ((PaperEdgeDevice) VmDatacenter).getOptIteration() + "," + VmDatacenter.getId()+"," + t.getApplicationID() + "," + t.getNumberOfPes()+ "," + (((double) t.getNumberOfPes()*100)/VM.getNumberOfPes())
								  + "," + t.getUtilizationOfRam() + "," + (((double)t.getUtilizationOfRam() * 100) / VM.getRam().getCapacity()) + ","+ CalculateMaxLinkLatency(UserDevice, t.getOrchestrator(),Math.max(Math.max(t.getContainerSize(),t.getFileSize()),t.getOutputSize()))
								  + "," + (CalculateMaxLinkLatency(UserDevice, t.getOrchestrator(),Math.max(Math.max(t.getContainerSize(),t.getFileSize()),t.getOutputSize()))*100/SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getMaxLinkLatency()));
					}
				}
				*/
			}
		
		for(double[] el:AppInfo.values())
		{
			// Iteration, AppID, Cores totali ( x app ), %Cores totali ( x app, su tutti i DC ), Ram totale ( x app ), %Ram totale ( x app, su tutti ), latenza ( media, x app), %latenza (media, x app, su latenza max )
			log.print("ResourcesLatencyXApp", ((PaperEdgeDevice)vmList.get(0).getHost().getDatacenter()).getOptIteration() + "," 
					+ el[5] + "," + el[0] + "," + (el[0]*100/edCpuTotal) + "," + el[1] + "," + (el[1]*100/edRamTotal) + "," + (el[2]/el[4]) +","+ (el[2]*100/el[3])); 
		}
		
		log.print("Resources", ((PaperEdgeDevice)vmList.get(0).getHost().getDatacenter()).getOptIteration() + "," 
				+ edCpuUsage*100/edCpuTotal + "," + edRamUsage*100/edRamTotal);// + "," + edBWUsage*100/edBWTotal);			
		
		log.print("Tasks", ((PaperEdgeDevice)vmList.get(0).getHost().getDatacenter()).getOptIteration() + "," + edTasksTotal);
		
		log.print("Latency", ((PaperEdgeDevice)vmList.get(0).getHost().getDatacenter()).getOptIteration() + "," + edTotalActualLatency*100/edTotalMaxLatency);
		
	}
		
	private void updateResources(Task task, Vm vm, long addUsers) {
		ResourceManageable Ram = vm.getResource(Ram.class);			
		
		ResourceManageable BW = vm.getResource(Bandwidth.class);
		
		if(addUsers!=0)
		{
			
			long TotalTaskNumUsersExceptFirst = addUsers + task.getUsersDevices().size()-1;  

			long TotalRequestedPes = SimulationParameters.APPLICATIONS_LIST.get(task.getApplicationID()).getNumberOfCores() +
					TotalRequestedLinearResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.CPU, task) + 
					TotalRequestedExponentialResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.CPU, task);
			
			// Update the task resources consumption
			long TotalRequestedRam = SimulationParameters.APPLICATIONS_LIST.get(task.getApplicationID()).getRequiredRam() +
					TotalRequestedLinearResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.RAM, task) + 
					TotalRequestedExponentialResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.RAM, task);
			
			long RequestedRam = TotalRequestedRam - (long)task.getUtilizationModelRam().getUtilization();
			
			UtilizationModelDynamic utilizationModeldynamicRam = new UtilizationModelDynamic(Unit.ABSOLUTE, TotalRequestedRam);
					
			long TotalRequestedBW = SimulationParameters.APPLICATIONS_LIST.get(task.getApplicationID()).getRequiredBW() +
					TotalRequestedLinearResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.BW, task) + 
					TotalRequestedExponentialResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.BW, task);
			
			long RequestedBW = TotalRequestedBW - (long)task.getUtilizationModelBw().getUtilization();

			
			UtilizationModelDynamic utilizationModeldynamicBW = new UtilizationModelDynamic(Unit.ABSOLUTE, TotalRequestedBW);
			
			task.setUtilizationModelRam(utilizationModeldynamicRam);
			task.setUtilizationModelBw(utilizationModeldynamicBW);
			//if(task.getNumberOfPes() + RequestedPes <=0)
			//	System.out.println("Numero di Pes del task " + task.getId()+ " : "+ task.getNumberOfPes() +" e Pes richiesti: " + RequestedPes);
			
			((VmSimple) vm).setFreePesNumber(task.getVm().getFreePesNumber() + (long)task.getNumberOfPes() - TotalRequestedPes);
			
			task.setNumberOfPes(TotalRequestedPes);
					
			// Update the VM resources consumption on the base of the Task	
			
			Ram.allocateResource(RequestedRam);
			BW.allocateResource(RequestedBW);			
		}
		else
		{
			Ram.allocateResource((long)(task.getUtilizationModelRam().getUtilization()));
			BW.allocateResource((long)(task.getUtilizationModelBw().getUtilization()));	
		
			((VmSimple) vm).setFreePesNumber(vm.getFreePesNumber() - task.getNumberOfPes());
		}
    }

	private double CalculateMaxLinkLatency(DataCenter device1, DataCenter device2, long size) {
		long size_in_kbit = size *8;
		double latency = 0;
		if(!device1.equals(device2))	
			latency = PaperSettings.MIN_LATENCY_DELAY + (PaperSettings.LATENCY_DELAY * device1.getMobilityManager().distanceTo(device2));
		return latency + PaperSettings.LINK_LATENCY + (PaperSettings.LINK_LATENCY*((double)size_in_kbit/SimulationParameters.BANDWIDTH_WLAN)); 	
	}
	
	public void OptReqInit(PaperEdgeDevice LocalOrchestratorDevice) {
		        
		Vm LocalVm = LocalOrchestratorDevice.getResources().getVmList().get(0);
		
		LocalOrchestratorDevice.OptIteration+=1;
		
		if(LocalVm.getCloudletScheduler().getCloudletExecList().isEmpty()) 
		{
			System.out.println("Tempo " +simulationManager.getSimulation().clock() + ", Iterazione "+ LocalOrchestratorDevice.OptIteration +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " non manda richiesta per mancanza di applicazioni locali in esecuzione!");
			LocalOrchestratorDevice.scheduleFirstNow(LocalOrchestratorDevice,PaperEdgeDevice.EXTRACT_NEXT_IN_QUEUE);
			return;
		}

		// Scelgo un task a caso tra quelli in esecuzione sull'Edge locale
		int RandomTaskIndex = simulationManager.getRandomGenerator().nextInt(LocalVm.getCloudletScheduler().getCloudletExecList().size());		
		Task chosenTask = ((Task) LocalVm.getCloudletScheduler().getCloudletExecList().get(RandomTaskIndex).getCloudlet());
		
		PaperEdgeDevice chosenEdgeDatacenter = chooseEdgeDataCenter(LocalOrchestratorDevice, chosenTask);
		if(chosenEdgeDatacenter!=null)
		{
			System.out.println("Tempo " +simulationManager.getSimulation().clock() + ", Iterazione "+ LocalOrchestratorDevice.OptIteration +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " manda richiesta a Edge " + chosenEdgeDatacenter.getId() + 
					" per l'applicazione di ID " + chosenTask.getApplicationID());
			OrchOptRequest Request = new OrchOptRequest(LocalOrchestratorDevice.OptIteration, LocalOrchestratorDevice, chosenEdgeDatacenter, chosenTask);
			LocalOrchestratorDevice.SuspendedRequests.add(Request);		
			LocalOrchestratorDevice.SentRequest=Request;
			LocalOrchestratorDevice.scheduleNow(simulationManager.getNetworkModel(),PaperNetworkModel.OPTIMIZATION_REQUEST_SENT, Request);
			return;
		}
		
		System.out.println("Tempo " +simulationManager.getSimulation().clock() + ", Iterazione "+ LocalOrchestratorDevice.OptIteration +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " non manda richiesta perche' non ci sono Edge idonei entro la latenza massima o l'altro Edge ha già mandato una richiesta a me!");		
		LocalOrchestratorDevice.scheduleFirstNow(LocalOrchestratorDevice,PaperEdgeDevice.EXTRACT_NEXT_IN_QUEUE);
	}
	
	public void OptReqReceived(PaperEdgeDevice LocalOrchestratorDevice, OrchOptRequest Request) {
		LocalOrchestratorDevice.ExecutingRequest = Request;
		System.out.println("Tempo " +simulationManager.getSimulation().clock() + ", Iterazione "+ Request.getOptIteration() +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " riceve richiesta dall'Edge " + 
		+ Request.getOriginRequest().getId() + " per l'applicazione di ID " + Request.getOriginTask().getApplicationID());
		LocalOrchestratorDevice.scheduleNow(simulationManager.getNetworkModel(), PaperNetworkModel.OPTIMIZATION_REQUEST_RESPONSE_SENT, Request);	
	}
	
	public void OptReqResponseReceived(PaperEdgeDevice LocalOrchestratorDevice, OrchOptRequest Request) {				
		PaperEdgeDevice RemoteOrchestratorDevice = Request.getDestRequest();
		
		Task local_t = Request.getOriginTask();
		ArrayList<Integer> IDsList = RemoteOrchestratorDevice.getApplicationIDsInExecution();
		
		if(!IDsList.contains(local_t.getApplicationID()))
		{		
			System.out.println("Tempo " +simulationManager.getSimulation().clock() + ", Iterazione "+ Request.getOptIteration() +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " riceve risposta alla propria richiesta, dall'Edge " + 
					+ Request.getDestRequest().getId() + " per l'applicazione di ID " + Request.getOriginTask().getApplicationID() + ": richiesta non confermata perche' l'altro Edge non ha un task con lo stesso AppID in esecuzione!");
			LocalOrchestratorDevice.SentRequest=null;
			LocalOrchestratorDevice.scheduleNow(simulationManager.getNetworkModel(), PaperNetworkModel.OPTIMIZATION_REQUEST_NOT_CONFIRMED_SENT, Request);
			LocalOrchestratorDevice.scheduleFirstNow(LocalOrchestratorDevice,PaperEdgeDevice.EXTRACT_NEXT_IN_QUEUE);
			return;
		}
		
		//search the remote task with the same application ID ( it exists )
		Task remote_t = null;
		ListIterator<CloudletExecution> CLE_list = RemoteOrchestratorDevice.getResources().getVmList().get(0).getCloudletScheduler().getCloudletExecList().listIterator();
		while(CLE_list.hasNext())
		{
			remote_t = (Task) CLE_list.next().getCloudlet();
			if(remote_t.getApplicationID()==local_t.getApplicationID())
				break;
		}	
		
		Vm VMChoose = RemoteOrchestratorDevice.getResources().getVmList().get(0);
			
		ArrayList <PaperEdgeDevice> offloadableUsers = offloadableUsers(remote_t, local_t, VMChoose);
		
		if(offloadableUsers.isEmpty())		
		{	
				System.out.println("Tempo " +simulationManager.getSimulation().clock() + ", Iterazione "+ Request.getOptIteration() +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " riceve risposta alla propria richiesta, dall'Edge " + 
						+ Request.getDestRequest().getId() + " per l'applicazione di ID " + Request.getOriginTask().getApplicationID() + ": richiesta non confermata perche' l'altro Edge non ha abbastanza risorse per aggiungere i miei utenti o non è conveniente!");
			
				System.out.printf( "Tempo " + simulationManager.getSimulation().clock() + ", Iterazione "+ Request.getOptIteration() +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " ha rilevato che l'Orchestratore dell'Edge " + RemoteOrchestratorDevice.getId()+ " aveva le seguenti risorse prima del check : Vm %d CPU Usage: %6.2f%% (%2d free vCPUs. Running Cloudlets: #%d). RAM usage: %.2f%% (%d MB). Numero di utenti da aggiungere: " + offloadableUsers.size() + "\n",
						VMChoose.getId(), VMChoose.getCpuPercentUtilization()*100.0, VMChoose.getFreePesNumber(),
						VMChoose.getCloudletScheduler().getCloudletExecList().size(),
						VMChoose.getRam().getPercentUtilization()*100, VMChoose.getRam().getAllocatedResource());
				LocalOrchestratorDevice.SentRequest=null;
				LocalOrchestratorDevice.scheduleNow(simulationManager.getNetworkModel(),PaperNetworkModel.OPTIMIZATION_REQUEST_NOT_CONFIRMED_SENT, Request);
				LocalOrchestratorDevice.scheduleFirstNow(LocalOrchestratorDevice,PaperEdgeDevice.EXTRACT_NEXT_IN_QUEUE);
				return;	 
		}
		
		Vm local_vm = local_t.getVm();
				
		 System.out.printf( "Tempo " + simulationManager.getSimulation().clock() + ", Iterazione "+ Request.getOptIteration() +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " ha le seguenti risorse PRIMA dell'offloading di " + offloadableUsers.size() +  " utenti del task " + local_t.getId() + " con " + local_t.getUsersDevices().size() + " utenti: Vm %d CPU Usage: %6.2f%% (%2d free vCPUs. Running Cloudlets: #%d). RAM usage: %.2f%% (%d MB).\n",
				 local_vm.getId(), local_vm.getCpuPercentUtilization()*100.0, local_vm.getFreePesNumber(),
				 local_vm.getCloudletScheduler().getCloudletExecList().size(),
				 local_vm.getRam().getPercentUtilization()*100, local_vm.getRam().getAllocatedResource());
		
		ResourceManageable Ram = local_vm.getResource(Ram.class);			
		ResourceManageable BW = local_vm.getResource(Bandwidth.class);
		
		int remained_local_users = local_t.getUsersDevices().size() - offloadableUsers.size();
				
		// If there are no more local users ( I offloaded all of them), I remove the local AppID and the cloudlet and I deallocate all the resources for the VM ( not the tasks, they are destroyed! )
		if(remained_local_users == 0) {
			Ram.deallocateResource((long)(local_t.getUtilizationModelRam().getUtilization()));
			BW.deallocateResource((long)(local_t.getUtilizationModelBw().getUtilization()));
			((VmSimple) local_vm).setFreePesNumber(local_t.getVm().getFreePesNumber() + (long)local_t.getNumberOfPes());
			LocalOrchestratorDevice.removeApplicationIDsInExecution(local_t.getApplicationID());
			local_vm.getCloudletScheduler().cloudletCancel(local_t);
		}
		else {
			
			long LocalRemainedRequestedRam = SimulationParameters.APPLICATIONS_LIST.get(local_t.getApplicationID()).getRequiredRam() + TotalRequestedLinearResource(remained_local_users-1, Application.RESOURCE_TYPE.RAM, local_t) + TotalRequestedExponentialResource(remained_local_users-1, Application.RESOURCE_TYPE.RAM, local_t);
			long LocalRemainedRequestedCPU = SimulationParameters.APPLICATIONS_LIST.get(local_t.getApplicationID()).getNumberOfCores() + TotalRequestedLinearResource(remained_local_users-1, Application.RESOURCE_TYPE.CPU, local_t) + TotalRequestedExponentialResource(remained_local_users-1, Application.RESOURCE_TYPE.CPU, local_t);
			long LocalRemainedRequestedBW = SimulationParameters.APPLICATIONS_LIST.get(local_t.getApplicationID()).getRequiredBW() + TotalRequestedLinearResource(remained_local_users-1, Application.RESOURCE_TYPE.BW, local_t) + TotalRequestedExponentialResource(remained_local_users-1, Application.RESOURCE_TYPE.BW, local_t);
		
			UtilizationModelDynamic utilizationModeldynamicRam = new UtilizationModelDynamic(Unit.ABSOLUTE, LocalRemainedRequestedRam);
			UtilizationModelDynamic utilizationModeldynamicBW = new UtilizationModelDynamic(Unit.ABSOLUTE, LocalRemainedRequestedBW);
			
			local_t.setUtilizationModelRam(utilizationModeldynamicRam);
			local_t.setUtilizationModelBw(utilizationModeldynamicBW);
			
			
			Ram.deallocateResource((long)(local_t.getUtilizationModelRam().getUtilization()) - LocalRemainedRequestedRam);
			BW.deallocateResource((long)(local_t.getUtilizationModelBw().getUtilization()) - LocalRemainedRequestedBW);
			((VmSimple) local_vm).setFreePesNumber(local_t.getVm().getFreePesNumber() + (long)local_t.getNumberOfPes() - LocalRemainedRequestedCPU);
			local_t.setNumberOfPes(LocalRemainedRequestedCPU);
		}	

		 System.out.printf( "Tempo " + simulationManager.getSimulation().clock() + ", Iterazione "+ Request.getOptIteration() +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " ha le seguenti risorse DOPO la deallocazione del task " + local_t.getId() + " con " + local_t.getUsersDevices().size() + " utenti: Vm %d CPU Usage: %6.2f%% (%2d free vCPUs. Running Cloudlets: #%d). RAM usage: %.2f%% (%d MB).\n",
				 local_vm.getId(), local_vm.getCpuPercentUtilization()*100.0, local_vm.getFreePesNumber(),
				 local_vm.getCloudletScheduler().getCloudletExecList().size(),
				 local_vm.getRam().getPercentUtilization()*100, local_vm.getRam().getAllocatedResource());
		
		System.out.println("Tempo " +simulationManager.getSimulation().clock() + ", Iterazione "+ Request.getOptIteration() +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " riceve risposta alla propria richiesta, dall'Edge " + 
				+ Request.getDestRequest().getId() + " per l'applicazione di ID " + Request.getOriginTask().getApplicationID() + ": richiesta confermata!");
	 
		Request.setOffloadedDevices(offloadableUsers);
		
		local_t.getUsersDevices().removeAll(offloadableUsers);			
				
		LocalOrchestratorDevice.scheduleNow(simulationManager.getNetworkModel(), PaperNetworkModel.OPTIMIZATION_REQUEST_CONFIRMED_SENT, Request);
		//LocalOrchestratorDevice.scheduleFirstNow(LocalOrchestratorDevice,PaperEdgeDevice.EXTRACT_NEXT_IN_QUEUE);
	}

	public void OptReqResponseConfirmed(PaperEdgeDevice LocalOrchestratorDevice, OrchOptRequest Request) {
		
		System.out.println("Tempo " + simulationManager.getSimulation().clock() + ", Iterazione "+ Request.getOptIteration() +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " riceve conferma della richiesta dell'Edge " + 
				+ Request.getOriginRequest().getId() + " per l'applicazione di ID " + Request.getOriginTask().getApplicationID());
	
		
		Task local_t = null;
		ListIterator<CloudletExecution> CLE_list_Iter = LocalOrchestratorDevice.getResources().getVmList().get(0).getCloudletScheduler().getCloudletExecList().listIterator();
		while(CLE_list_Iter.hasNext())
		{
			local_t = (Task) CLE_list_Iter.next().getCloudlet();
			if(Request.getOffloadedDevices().get(0).getApplicationType()==local_t.getApplicationID())
				break;
		}
		
		Vm local_vm = LocalOrchestratorDevice.getResources().getVmList().get(0);
		 System.out.printf( "Tempo " + simulationManager.getSimulation().clock() + ", Iterazione "+ Request.getOptIteration() +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " ha le seguenti risorse PRIMA  della allocazione di nuovi "+ Request.getOffloadedDevices().size() + " utenti: Vm %d CPU Usage: %6.2f%% (%2d free vCPUs. Running Cloudlets: #%d). RAM usage: %.2f%% (%d MB).\n",
				 local_vm.getId(), local_vm.getCpuPercentUtilization()*100.0, local_vm.getFreePesNumber(),
				 local_vm.getCloudletScheduler().getCloudletExecList().size(),
				 local_vm.getRam().getPercentUtilization()*100, local_vm.getRam().getAllocatedResource());
		
		// Aggiorno le risorse locali
		updateResources(local_t, local_t.getVm(),Request.getOffloadedDevices().size());	

		local_t.getUsersDevices().addAll(Request.getOffloadedDevices());	
		/*
		// Update the orchestrator of each offloaded users
		ListIterator<PaperEdgeDevice> UsersDevicesIter = Request.getOffloadedDevices().listIterator();
		while(UsersDevicesIter.hasNext())
		{ 
			PaperEdgeDevice UserDevice = UsersDevicesIter.next();
			UserDevice
			
		}
		*/
		 System.out.printf( "Tempo " + simulationManager.getSimulation().clock() + ", Iterazione "+ Request.getOptIteration() +": Orchestratore dell'Edge " + LocalOrchestratorDevice.getId() + " ha le seguenti risorse DOPO la allocazione di nuovi "+ Request.getOffloadedDevices().size() + " utenti: Vm %d CPU Usage: %6.2f%% (%2d free vCPUs. Running Cloudlets: #%d). RAM usage: %.2f%% (%d MB).\n",
				 local_vm.getId(), local_vm.getCpuPercentUtilization()*100.0, local_vm.getFreePesNumber(),
				 local_vm.getCloudletScheduler().getCloudletExecList().size(),
				 local_vm.getRam().getPercentUtilization()*100, local_vm.getRam().getAllocatedResource());
		 
		Request.getOriginRequest().SentRequest=null;		
		Request.getOriginRequest().scheduleFirstNow(Request.getOriginRequest(),PaperEdgeDevice.EXTRACT_NEXT_IN_QUEUE);

	}
	
	// scelgo un Edge a caso tra quelli che non violano la latenza massima di almeno uno dei miei utenti e lui e quell'utente sono nei rispettivi range, e 
	// se non mi ha già mandato una richiesta prima che lo faccia io ( sempre vero che mando o prima io o prima lui, simulatore sequenziale )
	private PaperEdgeDevice chooseEdgeDataCenter(PaperEdgeDevice LocalOrchestratorDevice, Task t) {

		ArrayList<DataCenter> ValidOrchList = new ArrayList<DataCenter>();
		ListIterator<DataCenter> OrchList = simulationManager.getDataCentersManager().getOrchestratorsList().listIterator();
		while(OrchList.hasNext())
		{
			PaperEdgeDevice EdgeDataCenter = (PaperEdgeDevice) OrchList.next();

			if(!EdgeDataCenter.equals(LocalOrchestratorDevice))
			{
				// Controllo se i due datacenter sono nei rispettivi range
				if(sameLocation(EdgeDataCenter,LocalOrchestratorDevice,LocalOrchestratorDevice.getRange()) || sameLocation(EdgeDataCenter,LocalOrchestratorDevice,EdgeDataCenter.getRange()))
				{
					ListIterator<PaperEdgeDevice> UsersDevicesIter = t.getUsersDevices().listIterator();

					while(UsersDevicesIter.hasNext())
					{ 
						PaperEdgeDevice UserDevice = UsersDevicesIter.next();

						// Check if there is a  user device in range of the chosen Edge and its max link latency with the Edge is not violated 
						if(sameLocation(EdgeDataCenter,UserDevice,Math.min(EdgeDataCenter.getRange(), SimulationParameters.EDGE_DEVICES_RANGE)))
						{

							//double TotalLatency = CalculateMaxLinkLatency(UserDevice, LocalOrchestratorDevice ,Math.max(Math.max(t.getContainerSize(),t.getFileSize()),t.getOutputSize())) +
							//		CalculateMaxLinkLatency(LocalOrchestratorDevice, EdgeDataCenter,Math.max(Math.max(t.getContainerSize(),t.getFileSize()),t.getOutputSize()));
							double OtherOrchestratorLatency = CalculateMaxLinkLatency(UserDevice, EdgeDataCenter,Math.max(Math.max(t.getContainerSize(),t.getFileSize()),t.getOutputSize()));

							// Check if the latency between the actual user device and the chosen Edge is not violated
							if ( //(TotalLatency <= SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getMaxLinkLatency()) && 
									OtherOrchestratorLatency <=  SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getMaxLinkLatency())
							{
								// Check it the other orchestrator sent a request to me before I'll do the same
								if(EdgeDataCenter.SentRequest!=null)
								{
									if(EdgeDataCenter.SentRequest.getDestRequest().getId() != LocalOrchestratorDevice.getId())
									{
										ValidOrchList.add(EdgeDataCenter);
										break;
									}
								}
								else 
								{
									ValidOrchList.add(EdgeDataCenter);
									break;
								}
							}
						}
					}						
				}
			}
		}

		if(ValidOrchList.isEmpty())
			return null;

		return (PaperEdgeDevice) ValidOrchList.get(simulationManager.getRandomGenerator().nextInt(ValidOrchList.size()));	
	}
	
	private long TotalRequestedLinearResource(long UsersExceptFirst, Application.RESOURCE_TYPE ResType, Task t)
	{
		PaperEdgeDevice DataCenter = (PaperEdgeDevice) t.getOrchestrator();
		
		// Somma dei primi users-2 numeri interi. MCU_{A,1,1}*coeff*users 
		long SumUsersExceptFirstAndSecond = (UsersExceptFirst*(UsersExceptFirst-1))/2;
		switch(ResType)
		{	
			case CPU: 
					if(DataCenter.getCoefficient("MCU_CPU_exponential")>1) 
						return 0;
					return (long)Math.ceil(SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getNumberOfCoresIncremented()*
							(UsersExceptFirst + DataCenter.getCoefficient("MCU_CPU_linear")*SumUsersExceptFirstAndSecond));
			case RAM:
					if(DataCenter.getCoefficient("MCU_RAM_exponential")>1) 
						return 0;
					//  ram_incr * ((N-1) + ram_incr * ((N-2)*(N-1)/2))
					return (long)Math.ceil(SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getRamIncremented()*
							(UsersExceptFirst + DataCenter.getCoefficient("MCU_RAM_linear")*SumUsersExceptFirstAndSecond));
			default:
					if(DataCenter.getCoefficient("MCU_bandwidth_exponential")>1) 
						return 0;
					return (long)Math.ceil(SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getBandwidthIncremented()*
							(UsersExceptFirst + DataCenter.getCoefficient("MCU_bandwidth_linear")*SumUsersExceptFirstAndSecond));
		}
	}
	
	private long TotalRequestedExponentialResource(long UsersExceptFirst, Application.RESOURCE_TYPE ResType, Task t )
	{
		PaperEdgeDevice DataCenter = (PaperEdgeDevice) t.getOrchestrator();
		double Exp=0;
		switch(ResType)
		{	
		
			// Calcolo MCU_{A,1,1}*coeff*users^exp
			case CPU: if(DataCenter.getCoefficient("MCU_CPU_exponential")<=1) 
						return 0;
					  // Somma delle prime (users-2)^exp
					  for(long i=1;i<UsersExceptFirst;i++)
						 Exp+=Math.pow(i,DataCenter.getCoefficient("MCU_CPU_exponential"));
					  return (long)Math.ceil(SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getNumberOfCoresIncremented()*
								(UsersExceptFirst + DataCenter.getCoefficient("MCU_CPU_linear")*Exp));
						  
			case RAM: if(DataCenter.getCoefficient("MCU_RAM_exponential")<=1)
						return 0;
					  // Somma delle prime (users-2)^exp
			  		  for(long i=1;i<UsersExceptFirst;i++)
			  			 Exp+=Math.pow(i,DataCenter.getCoefficient("MCU_RAM_exponential"));
					  return (long)Math.ceil(SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getRamIncremented()*
								(UsersExceptFirst + DataCenter.getCoefficient("MCU_RAM_linear")*Exp));
			default: if(DataCenter.getCoefficient("MCU_bandwidth_exponential")<=1) 
						return 0;
  					 // Somma delle prime k-2 potenze di b, con k utenti 
					 for(long i=1;i<UsersExceptFirst;i++)
						 Exp+=Math.pow(i,DataCenter.getCoefficient("MCU_bandwidth_exponential"));
					 return (long)Math.ceil(SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getBandwidthIncremented()*
								(UsersExceptFirst + DataCenter.getCoefficient("MCU_bandwidth_linear")*Exp));
		}
	}
	
	
	// Cost in resource of add a user to task t, after k yet served users, where k >= 1 
	private long MCU_A_1_k(Application.RESOURCE_TYPE ResType, Task t, long k) {
		if(k==0)
			return 0;
		PaperEdgeDevice DataCenter = (PaperEdgeDevice) t.getOrchestrator();
		long users = k-1;
		switch(ResType)
		{	
			case CPU:
				return (long)Math.ceil(DataCenter.getCoefficient("MCU_CPU_linear") * Math.pow(users,DataCenter.getCoefficient("MCU_CPU_exponential")) * SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getNumberOfCoresIncremented());  
			case RAM:
				return (long)Math.ceil(DataCenter.getCoefficient("MCU_RAM_linear") * Math.pow(users,DataCenter.getCoefficient("MCU_RAM_exponential")) * SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getRamIncremented());  
			default:
				return (long)Math.ceil(DataCenter.getCoefficient("MCU_bandwidth_linear") * Math.pow(users,DataCenter.getCoefficient("MCU_bandwidth_exponential")) * SimulationParameters.APPLICATIONS_LIST.get(t.getApplicationID()).getBandwidthIncremented());  
		}
	}
	
	ArrayList<PaperEdgeDevice> offloadableUsers(Task RemoteTask, Task LocalTask, Vm remoteVM) {

		ListIterator<PaperEdgeDevice> UsersDevicesIter = LocalTask.getUsersDevices().listIterator();

		ArrayList<PaperEdgeDevice> OffloadableUsers = new ArrayList<PaperEdgeDevice>(LocalTask.getUsersDevices().size());

		ArrayList<PaperEdgeDevice> TempOffloadableUsers = new ArrayList<PaperEdgeDevice>(LocalTask.getUsersDevices().size());


		PaperEdgeDevice RemoteEdgeDatacenter = (PaperEdgeDevice) RemoteTask.getOrchestrator();

		long RemoteFreePes = remoteVM.getFreePesNumber();
		long RemoteFreeRam = remoteVM.getRam().getAvailableResource();
		long RemoteFreeBW = remoteVM.getBw().getAvailableResource();

		long NumRemoteUsers = RemoteTask.getUsersDevices().size();
		long NumLocalUsersExceptFirst = LocalTask.getUsersDevices().size()-1;

		long TotalLocalRamSaved = 0;		
		long TotalLocalBWSaved = 0;
		long TotalLocalCpuSaved = 0;
		long TotalRemoteRamRequested = 0;
		long TotalRemoteBWRequested = 0;
		long TotalRemoteCpuRequested = 0;

		while(UsersDevicesIter.hasNext())
		{ 
			PaperEdgeDevice UserDevice = UsersDevicesIter.next();

			TotalRemoteRamRequested += (long)Math.ceil(SimulationParameters.APPLICATIONS_LIST.get(RemoteTask.getApplicationID()).getRamIncremented() + MCU_A_1_k(Application.RESOURCE_TYPE.RAM, RemoteTask, NumRemoteUsers));
			TotalRemoteBWRequested += (long)Math.ceil(SimulationParameters.APPLICATIONS_LIST.get(RemoteTask.getApplicationID()).getBandwidthIncremented() + MCU_A_1_k(Application.RESOURCE_TYPE.BW, RemoteTask, NumRemoteUsers));		
			TotalRemoteCpuRequested += (long)Math.ceil((SimulationParameters.APPLICATIONS_LIST.get(RemoteTask.getApplicationID()).getNumberOfCoresIncremented() + MCU_A_1_k(Application.RESOURCE_TYPE.CPU, RemoteTask, NumRemoteUsers)));

			if(NumLocalUsersExceptFirst == 0) {
				TotalLocalRamSaved += SimulationParameters.APPLICATIONS_LIST.get(LocalTask.getApplicationID()).getRequiredRam();
				TotalLocalBWSaved += SimulationParameters.APPLICATIONS_LIST.get(LocalTask.getApplicationID()).getRequiredBW();
				TotalLocalCpuSaved += SimulationParameters.APPLICATIONS_LIST.get(LocalTask.getApplicationID()).getNumberOfCores();
			}
			else {
				TotalLocalRamSaved += (long)Math.ceil(SimulationParameters.APPLICATIONS_LIST.get(LocalTask.getApplicationID()).getRamIncremented() + MCU_A_1_k(Application.RESOURCE_TYPE.RAM, LocalTask, NumLocalUsersExceptFirst));
				TotalLocalBWSaved += (long)Math.ceil(SimulationParameters.APPLICATIONS_LIST.get(LocalTask.getApplicationID()).getBandwidthIncremented() + MCU_A_1_k(Application.RESOURCE_TYPE.BW, LocalTask, NumLocalUsersExceptFirst));		
				TotalLocalCpuSaved += (long)Math.ceil((SimulationParameters.APPLICATIONS_LIST.get(LocalTask.getApplicationID()).getNumberOfCoresIncremented() + MCU_A_1_k(Application.RESOURCE_TYPE.CPU, LocalTask, NumLocalUsersExceptFirst)));
			}

			//Check if the offloading of the actual user is possible: only if the remote Edge has enough resources
			if((TotalRemoteCpuRequested <= RemoteFreePes) && (TotalRemoteRamRequested <= RemoteFreeRam) && (TotalRemoteBWRequested <= RemoteFreeBW)) {	

				// Check if the chosen user device is in range respect to the remote Edge
				if(sameLocation(RemoteEdgeDatacenter,UserDevice, Math.min(RemoteEdgeDatacenter.getRange(), SimulationParameters.EDGE_DEVICES_RANGE))) {

					double RemoterOrchestratorUserLatency = CalculateMaxLinkLatency(UserDevice, RemoteEdgeDatacenter,Math.max(Math.max(LocalTask.getContainerSize(),LocalTask.getFileSize()),LocalTask.getOutputSize()));

					// Check if the chosen user device link with the remote Edge respects the max link latency: the user could be offloaded to the other Edge!
					if (RemoterOrchestratorUserLatency <= SimulationParameters.APPLICATIONS_LIST.get(LocalTask.getApplicationID()).getMaxLinkLatency()) {

						TempOffloadableUsers.add(UserDevice);
						NumRemoteUsers++;
						NumLocalUsersExceptFirst--;

						// Check if the offloading of the temporaneous users is convenient
						if (((TotalRemoteRamRequested < TotalLocalRamSaved) && (TotalRemoteCpuRequested <= TotalLocalCpuSaved) && (TotalRemoteBWRequested <= TotalLocalBWSaved)) || 
							((TotalRemoteRamRequested <= TotalLocalRamSaved) && (TotalRemoteCpuRequested < TotalLocalCpuSaved) && (TotalRemoteBWRequested <= TotalLocalBWSaved)) || 
							((TotalRemoteRamRequested <= TotalLocalRamSaved) && (TotalRemoteCpuRequested <= TotalLocalCpuSaved) && (TotalRemoteBWRequested < TotalLocalBWSaved))) {
							OffloadableUsers.addAll(TempOffloadableUsers);
							TempOffloadableUsers.clear();
						}
					}
				}
				else {
					System.out.println("Range tra Edge" + RemoteEdgeDatacenter.getId() + " e Used device " + UserDevice.getId() + " violato di " + (Math.min(RemoteEdgeDatacenter.getRange(), SimulationParameters.EDGE_DEVICES_RANGE) - RemoteEdgeDatacenter.getMobilityManager().distanceTo(UserDevice)));
				}
			}
			else 
				break;
		}
		return OffloadableUsers;
	}

	
	// Calcola se è conveniente fare l'offloading dei task locali sull'edge remoto
	protected boolean offloadingIsConvenient(Task RemoteTask, Task LocalTask) 
	{	
		long RemoteTotalTaskNewNumUsersExceptFirst = LocalTask.getUsersDevices().size() + RemoteTask.getUsersDevices().size()-1;  

		long RemoteTotalNewUsersPesCost = 
				SimulationParameters.APPLICATIONS_LIST.get(RemoteTask.getApplicationID()).getNumberOfCores() +
				TotalRequestedLinearResource(RemoteTotalTaskNewNumUsersExceptFirst, Application.RESOURCE_TYPE.CPU, RemoteTask) + 
				TotalRequestedExponentialResource(RemoteTotalTaskNewNumUsersExceptFirst, Application.RESOURCE_TYPE.CPU, RemoteTask) -
				RemoteTask.getNumberOfPes(); 

		long RemoteTotalNewUsersRamCost =  SimulationParameters.APPLICATIONS_LIST.get(RemoteTask.getApplicationID()).getRequiredRam() +
				TotalRequestedLinearResource(RemoteTotalTaskNewNumUsersExceptFirst, Application.RESOURCE_TYPE.RAM, RemoteTask) + 
				TotalRequestedExponentialResource(RemoteTotalTaskNewNumUsersExceptFirst, Application.RESOURCE_TYPE.RAM, RemoteTask) 
				- (long)RemoteTask.getUtilizationModelRam().getUtilization();
		
		long RemoteTotalNewUsersBWCost = SimulationParameters.APPLICATIONS_LIST.get(RemoteTask.getApplicationID()).getRequiredBW()+
				TotalRequestedLinearResource(RemoteTotalTaskNewNumUsersExceptFirst, Application.RESOURCE_TYPE.BW, RemoteTask) + 
				TotalRequestedExponentialResource(RemoteTotalTaskNewNumUsersExceptFirst, Application.RESOURCE_TYPE.BW, RemoteTask) 
				- (long)RemoteTask.getUtilizationModelBw().getUtilization();

		if((LocalTask.getNumberOfPes()>=RemoteTotalNewUsersPesCost) 
				&& (LocalTask.getUtilizationModelRam().getUtilization()>= RemoteTotalNewUsersRamCost) 
					&& (LocalTask.getUtilizationModelBw().getUtilization()>= RemoteTotalNewUsersBWCost))
			return true;
				
		return false;
	}
	
	protected Result offloadingIsPossible(Task task, Vm vm, List<PaperEdgeDevice> addUsers) 
	{	
			
		long TotalRequestedPes = SimulationParameters.APPLICATIONS_LIST.get(task.getApplicationID()).getNumberOfCores();
		long TotalRequestedRam =  SimulationParameters.APPLICATIONS_LIST.get(task.getApplicationID()).getRequiredRam();
		long TotalRequestedBW =  SimulationParameters.APPLICATIONS_LIST.get(task.getApplicationID()).getRequiredBW();
				
		if(!addUsers.isEmpty())
		{
			long TotalTaskNumUsersExceptFirst = addUsers.size() + task.getUsersDevices().size()-1;  
	
			TotalRequestedPes += TotalRequestedLinearResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.CPU, task) +
					TotalRequestedExponentialResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.CPU, task) - task.getNumberOfPes();	
			//System.out.println("MCU lineare per " + TotalTaskNumUsersExceptFirst + " utenti per la CPU è " + TotalRequestedLinearResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.CPU, task));
			
			//System.out.println("MCU esponenziale per " + TotalTaskNumUsersExceptFirst + " utenti per la CPU è " + TotalRequestedExponentialResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.CPU, task));

			
			TotalRequestedRam += TotalRequestedLinearResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.RAM, task) + 
					TotalRequestedExponentialResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.RAM, task) - (long)task.getUtilizationModelRam().getUtilization();
		    //System.out.println("MCU lineare per " + TotalTaskNumUsersExceptFirst + " utenti per la RAM è " + TotalRequestedLinearResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.RAM, task));
			
			//System.out.println("MCU esponenziale per " + TotalTaskNumUsersExceptFirst + " utenti per la RAM è " + TotalRequestedExponentialResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.RAM, task));
	
			TotalRequestedBW +=  TotalRequestedLinearResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.BW, task) + 
					TotalRequestedExponentialResource(TotalTaskNumUsersExceptFirst, Application.RESOURCE_TYPE.BW, task) - (long)task.getUtilizationModelBw().getUtilization();
		}
		else
			addUsers.add((PaperEdgeDevice)task.getEdgeDevice());
				
		if((TotalRequestedPes<=vm.getFreePesNumber()) && ((vm.getRam().getAvailableResource() - TotalRequestedRam) >= 0) && ((vm.getBw().getAvailableResource() - TotalRequestedBW ) >= 0))		
			return Result.OK;
			
		return Result.NOT_ENOUGH_RESOURCES;
	}
	
	@Override
	public void resultsReturned(Task task) {
		//How to get the task execution status, (if failed or succeed, which can be used for reinforcement learning based algorithms)
		if (task.getStatus() == Status.FAILED) 
		{
			System.err.println("PaperEdgeOrchestrator, task " + task.getId() + " has been failed, failure reason is: "
					+ task.getFailureReason());
		}
		else
		{

			System.out.println("PaperEdgeOrchestrator, task " + task.getId() + " has been successfully executed");
			((PaperEdgeDevice)task.getOrchestrator()).removeApplicationIDsInExecution(task.getApplicationID());
		}
		
	}
	
	private void onClockTickListener(EventInfo evt) {
		  /*vmList.forEach(vm ->
		    System.out.printf(
		      "\t\tTime %6.1f: Vm %d CPU Usage: %6.2f%% (%2d vCPUs. Running Cloudlets: #%d). RAM usage: %.2f%% (%d MB). Storage usage: %d MB \n",
		      evt.getTime(), vm.getId(), vm.getCpuPercentUtilization()*100.0, vm.getNumberOfPes(),
		      vm.getCloudletScheduler().getCloudletExecList().size(),
		      vm.getRam().getPercentUtilization()*100, vm.getRam().getAllocatedResource(),
		      vm.getStorage().getAllocatedResource())
		  );*/
		}
}
