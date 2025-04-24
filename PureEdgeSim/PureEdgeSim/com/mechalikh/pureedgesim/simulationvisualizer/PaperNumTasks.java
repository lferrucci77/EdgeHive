package com.mechalikh.pureedgesim.simulationvisualizer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.markers.SeriesMarkers;

import com.mechalikh.pureedgesim.simulationcore.SimulationManager;
import com.mechalikh.pureedgesim.datacentersmanager.DataCenter;
import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters;

import PaperScenario.PaperEdgeDevice;

public class PaperNumTasks extends Chart {
	
	private List<Double> edTasksTotalList = new ArrayList<>();

	protected List<Double> currentIteration = new ArrayList<>();
	private double iteration = 1.0;
	
	public PaperNumTasks(String title, String xAxisTitle, String yAxisTitle, SimulationManager simulationManager) {
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
				edgeDataCentersTotalNumTasks();
				iteration++;
				break;
			}
		}
	}

	private void edgeDataCentersTotalNumTasks() {

		double edTasksTotal = 0;

		// Only if Edge computing is used
		ListIterator<DataCenter> ListOrchIter =  simulationManager.getDataCentersManager().getOrchestratorsList().listIterator();
		while(ListOrchIter.hasNext())
		{
			DataCenter Orchestrator = ListOrchIter.next();		
			
			edTasksTotal +=	Orchestrator.getVmList().get(0).getCloudletScheduler().getCloudletExecList().size();
		
		}

		edTasksTotalList.add(edTasksTotal);
		
		updateSeries(getChart(), "Tasks Numbers", toArray(currentIteration), toArray(edTasksTotalList), SeriesMarkers.NONE, Color.BLACK);

	}


}
