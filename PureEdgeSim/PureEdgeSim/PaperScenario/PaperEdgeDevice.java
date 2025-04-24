package PaperScenario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.Random;

import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

import com.mechalikh.pureedgesim.simulationcore.SimulationManager;
import com.mechalikh.pureedgesim.tasksgenerator.Application;
import com.mechalikh.pureedgesim.datacentersmanager.DataCenter;
import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters; 

 
    
public class PaperEdgeDevice extends DataCenter {
	protected static final int UPDATE_STATUS = 2000; // Avoid conflicting with CloudSim Plus Tags
	protected static final int OPTIMIZATION_REQUEST_INIT = 2001;
	protected static final int OPTIMIZATION_REQUEST_RECEIVED = 2002;
	protected static final int OPTIMIZATION_REQUEST_RESPONSE = 2003;
	protected static final int OPTIMIZATION_REQUEST_CONFIRMED = 2004;
	protected static final int OPTIMIZATION_REQUEST_NOT_CONFIRMED = 2005;
	protected static final int EXTRACT_NEXT_IN_QUEUE = 2006;
	protected static final int PRINT_LOG = 2007;

	protected ArrayList<Integer> ApplicationIDsInExecution;
	protected PriorityQueue<OrchOptRequest> SuspendedRequests;
	protected OrchOptRequest ExecutingRequest = null;
	protected OrchOptRequest SentRequest = null;
	protected int OptIteration;
	private HashMap<String,Double> ResourcesCoefficients;	
	
	Comparator<OrchOptRequest> ReqComparator = new Comparator<OrchOptRequest>() {
        @Override
        public int compare(OrchOptRequest req1, OrchOptRequest req2) {
        if(req1.getOptIteration()!=req2.getOptIteration())
        	return req1.getOptIteration() - req2.getOptIteration();
     
        return (int) (req1.getOriginRequest().getId()-req2.getOriginRequest().getId());
        }
    };
	   
	public PaperEdgeDevice(SimulationManager simulationManager, List<? extends Host> hostList,
			List<? extends Vm> vmList) {
		super(simulationManager, hostList, vmList);
		
		Random ran = simulationManager.getRandomGenerator();
		ResourcesCoefficients = new HashMap<String,Double>(SimulationParameters.APPLICATIONS_LIST.size()*6);
		
		ListIterator<Application> AppsIter = SimulationParameters.APPLICATIONS_LIST.listIterator();
		
		while(AppsIter.hasNext()) {
			Application app = AppsIter.next();
			
			double McuBWLinCoeff = app.getMcuBWLin()==0.0? 0.0 : app.getMcuBWLin() + app.getMcuBWLinStdDev() * ran.nextGaussian();
			ResourcesCoefficients.put("MCU_bandwidth_linear", McuBWLinCoeff);
			
			double McuBWExpCoeff = app.getMcuBWExp()==1.0? 1.0 : app.getMcuBWExp() + app.getMcuBWExpStdDev() * ran.nextGaussian();			
			ResourcesCoefficients.put("MCU_bandwidth_exponential", McuBWExpCoeff);
			
			double McuRamLinCoeff = app.getMcuRamLin()==0.0? 0.0 : app.getMcuRamLin() + app.getMcuRamLinStdDev() * ran.nextGaussian();
			ResourcesCoefficients.put("MCU_RAM_linear", McuRamLinCoeff);
			
			double McuRamExpCoeff = app.getMcuRamExp()==1.0? 1.0 : app.getMcuRamExp() + app.getMcuRamExpStdDev() * ran.nextGaussian();			
			ResourcesCoefficients.put("MCU_RAM_exponential", McuRamExpCoeff);
			
			double McuCpuLinCoeff = app.getMcuCpuLin()==0.0? 0.0 : app.getMcuCpuLin() + app.getMcuCpuLinStdDev() * ran.nextGaussian();
			ResourcesCoefficients.put("MCU_CPU_linear", McuCpuLinCoeff);
			
			double McuCpuExpCoeff = app.getMcuCpuExp()==1.0? 1.0 : app.getMcuCpuExp() + app.getMcuCpuExpStdDev() * ran.nextGaussian();			
			ResourcesCoefficients.put("MCU_CPU_exponential", McuCpuExpCoeff);					
		}
		
		ApplicationIDsInExecution = new ArrayList<Integer>();
		SuspendedRequests = new PriorityQueue<OrchOptRequest>(ReqComparator);
		ExecutingRequest = null;
		OptIteration = 0;		
	}
	
	public double getCoefficient(String resource_coeff_name) {
		return Double.valueOf(ResourcesCoefficients.get(resource_coeff_name));
	}
	
	public int getOptIteration() {
		return OptIteration;
	}

	public ArrayList<Integer> getApplicationIDsInExecution() {
		return ApplicationIDsInExecution;
	}

	public void addApplicationIDsInExecution(Integer applicationID) {
		ApplicationIDsInExecution.add(applicationID);
	}
	
	public void removeApplicationIDsInExecution(Integer applicationID) {
		ApplicationIDsInExecution.remove(applicationID);
	}

	@Override
	public void startInternal() {
		super.startInternal();
		schedule(this, SimulationParameters.INITIALIZATION_TIME, UPDATE_STATUS);
	}
	
	@Override
	public void processEvent(final SimEvent ev) {
		switch (ev.getTag()) {
		case EXTRACT_NEXT_IN_QUEUE:
			if(ExecutingRequest==null)
			{
				OrchOptRequest SuspendedRequest = SuspendedRequests.peek();
				if(SuspendedRequest!=null)
					if(SuspendedRequest.getOriginRequest().equals(this))
					{
						if(SuspendedRequests.peek().isanswered())
							((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).OptReqResponseReceived(this,SuspendedRequests.remove());
					}					
					else			
						((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).OptReqReceived(this,SuspendedRequests.remove());
			}
			break;
			
		case PRINT_LOG:
			((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).PrintLog();
			if(!(simulationManager.getSimulation().clock() + (PaperSettings.ORCHESTRATION_ALGORITHM_INTERVAL*2) > SimulationParameters.SIMULATION_TIME))
				schedule(PaperSettings.ORCHESTRATION_ALGORITHM_INTERVAL,PRINT_LOG);
			else
				((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).SaveAllLogs();
			break;
		case OPTIMIZATION_REQUEST_INIT:
			((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).OptReqInit(this);
			if(!(simulationManager.getSimulation().clock() + (PaperSettings.ORCHESTRATION_ALGORITHM_INTERVAL*3) > SimulationParameters.SIMULATION_TIME))
				schedule(PaperSettings.ORCHESTRATION_ALGORITHM_INTERVAL,OPTIMIZATION_REQUEST_INIT);
			break;		
		case OPTIMIZATION_REQUEST_RECEIVED:
			SuspendedRequests.add((OrchOptRequest)ev.getData());
			if(ExecutingRequest==null)
			{
				if(SuspendedRequests.peek().getOriginRequest().equals(this))
				{
					if(SuspendedRequests.peek().isanswered())
						((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).OptReqResponseReceived(this,SuspendedRequests.remove());
				}					
				else		
					((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).OptReqReceived(this,SuspendedRequests.remove());
			}				
			break;
		case OPTIMIZATION_REQUEST_RESPONSE:
			((OrchOptRequest)ev.getData()).setanswered(true);
			if(ExecutingRequest==null)
			{
				if(SuspendedRequests.peek().getOriginRequest().equals(this))
				{
					if(SuspendedRequests.peek().isanswered())
						((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).OptReqResponseReceived(this,SuspendedRequests.remove());
				}					
				else			
					((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).OptReqReceived(this,SuspendedRequests.remove());
			}				
			break;
		case OPTIMIZATION_REQUEST_CONFIRMED:
			((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).OptReqResponseConfirmed(this, (OrchOptRequest) ev.getData());
		case OPTIMIZATION_REQUEST_NOT_CONFIRMED:
			ExecutingRequest = null;
			OrchOptRequest SospendedRequest = SuspendedRequests.peek();
			if(SospendedRequest!=null)
				if(SospendedRequest.getOriginRequest().equals(this))
				{
					if(SuspendedRequests.peek().isanswered())
						((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).OptReqResponseReceived(this,SuspendedRequests.remove());
				}					
				else			
					((PaperEdgeOrchestrator)simulationManager.getOrchestrator()).OptReqReceived(this,SuspendedRequests.remove());			
			break;
		case UPDATE_STATUS:
			updateStatus();
			if (!isDead()) {
				schedule(this, SimulationParameters.UPDATE_INTERVAL, UPDATE_STATUS);
			}

			break;
		default:
			super.processEvent(ev);
			break;
		}

	}

	
	private void updateStatus() {

		// Check if the device is dead
		if (getEnergyModel().isBatteryPowered()
				&& this.getEnergyModel().getTotalEnergyConsumption() > getEnergyModel().getBatteryCapacity()) {
			setDeath(true, simulationManager.getSimulation().clock());
		}

		// Update location
		if (getMobilityManager().isMobile()) {
			getMobilityManager().getNextLocation();
		}
	}
}
