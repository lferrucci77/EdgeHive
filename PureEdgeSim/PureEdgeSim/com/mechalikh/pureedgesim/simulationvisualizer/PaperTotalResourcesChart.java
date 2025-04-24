package com.mechalikh.pureedgesim.simulationvisualizer;

import java.awt.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.cloudbus.cloudsim.cloudlets.CloudletExecution;
import org.cloudbus.cloudsim.vms.Vm;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.markers.SeriesMarkers;

import com.mechalikh.pureedgesim.simulationcore.SimulationManager;
import com.mechalikh.pureedgesim.datacentersmanager.DataCenter;
import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters;
import com.mechalikh.pureedgesim.tasksgenerator.Task;

import PaperScenario.PaperEdgeDevice;

public class PaperTotalResourcesChart extends Chart {
	private List<Double> edgeUsage = new ArrayList<>();
	private List<Double> edgeRamUsage = new ArrayList<>();
	private List<Double> edgeBWUsage = new ArrayList<>();

	protected List<Double> currentIteration = new ArrayList<>();
	private double iteration = 0.0;
	
	public PaperTotalResourcesChart(String title, String xAxisTitle, String yAxisTitle, SimulationManager simulationManager) {
		super(title, xAxisTitle, yAxisTitle, simulationManager);
		getChart().getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
		updateSize(SimulationParameters.INITIALIZATION_TIME, null, 0.0, null);
	}

	protected void update() {

		ListIterator<DataCenter> ListOrchIter =  simulationManager.getDataCentersManager().getOrchestratorsList().listIterator();
		while(ListOrchIter.hasNext())
		{
			DataCenter Orchestrator = ListOrchIter.next();	
			if(((PaperEdgeDevice)Orchestrator).getOptIteration() == iteration )
			{
				currentIteration.add(iteration);
				edgeDataCentersResourcesUsage();
				iteration++;
				break;
			}
		}
	}

	private void edgeDataCentersResourcesUsage() {

		double edCpuUsage = 0;
		double edCpuTotal = 0;
		double edRamTotal = 0;
		double edRamUsage = 0;
		double edBWUsage = 0;
		double edBWTotal = 0;

		// Only if Edge computing is used
		ListIterator<DataCenter> ListOrchIter =  simulationManager.getDataCentersManager().getOrchestratorsList().listIterator();
		while(ListOrchIter.hasNext())
		{
			DataCenter Orchestrator = ListOrchIter.next();		
			Vm VM = Orchestrator.getVmList().get(0);

			edCpuTotal += VM.getNumberOfPes();
			edRamTotal += VM.getRam().getCapacity();
			edBWTotal += VM.getBw().getCapacity();
			
			ListIterator<CloudletExecution> CLE_list =	Orchestrator.getVmList().get(0).getCloudletScheduler().getCloudletExecList().listIterator();
			while(CLE_list.hasNext())
			{
				Task t = (Task) CLE_list.next().getCloudlet();	
				
				edCpuUsage += t.getNumberOfPes();
				edBWUsage += t.getUtilizationOfBw();
				edRamUsage+=t.getUtilizationOfRam();
			}
		}

		edgeUsage.add(edCpuUsage*100/edCpuTotal);
		edgeRamUsage.add(edRamUsage*100/edRamTotal);
		edgeBWUsage.add(edBWUsage*100/edBWTotal);
		
		updateSeries(getChart(), "VCpus", toArray(currentIteration), toArray(edgeUsage), SeriesMarkers.NONE, Color.BLACK);
		
		updateSeries(getChart(), "Ram", toArray(currentIteration), toArray(edgeRamUsage), SeriesMarkers.NONE, Color.BLUE);
		
		updateSeries(getChart(), "Bandwidth", toArray(currentIteration), toArray(edgeBWUsage), SeriesMarkers.NONE, Color.RED);
	}


}
