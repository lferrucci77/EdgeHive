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
package com.mechalikh.pureedgesim.tasksgenerator;

//import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters;

public class Application { 
	public static enum RESOURCE_TYPE { CPU,RAM,BW };
	
	private int rate;
	private double latency;
	private long containerSize;
	private long requestSize;
	private long resultsSize;
	private double taskLength;
	private int numberOfCores;
	private double usagePercentage;
	private double numberOfCoresIncremented;
	private int requiredRam;
	private int requiredBW;
	private int RamIncremented;
	private int BandwidthIncremented;
	private double McuCpuLin;
	private double McuCpuExp;
	private double McuCpuLinStdDev;
	private double McuCpuExpStdDev;
	private double McuRamLin;
	private double McuRamExp;
	private double McuRamLinStdDev;
	private double McuRamExpStdDev;
	private double McuBWLin;
	private double McuBWExp;
	private double McuBWLinStdDev;
	private double McuBWExpStdDev;
	private double maxLinkLatency;
	
	public Application(int rate, double usagePercentage, double latency, long containerSize, long requestSize, long resultsSize, double taskLength, 
			int numberOfCores, double numberOfCoresIncremented, double McuCpuLin, double McuCpuExp, double McuCpuLinStdDev, double McuCpuExpStdDev,
			int requiredRam, int RamIncremented, double McuRamLin, double McuRamExp, double McuRamLinStdDev, double McuRamExpStdDev,
			int requiredBW, int BandwidthIncremented, double McuBWLin, double McuBWExp,  double McuBWLinStdDev, double McuBWExpStdDev,
			double maxLinkLatency) { 
		setRate(rate);
		setUsagePercentage(usagePercentage); 
		setLatency(latency);
		setContainerSize(containerSize);
		setRequestSize(requestSize);
		setResultsSize(resultsSize);
		setTaskLength(taskLength);
		setNumberOfCores(numberOfCores);
		setMaxLinkLatency(maxLinkLatency);
		setNumberOfCoresIncremented(numberOfCoresIncremented);
		setRequiredRam(requiredRam);
		setRequiredBW(requiredBW);
		setBandwidthIncremented(BandwidthIncremented);
		setRamIncremented(RamIncremented);
		setMcuCpuLin(McuCpuLin);
		setMcuCpuExp(McuCpuExp);
		setMcuCpuLinStdDev(McuCpuLinStdDev);
		setMcuCpuExpStdDev(McuCpuExpStdDev);
		setMcuRamLin(McuRamLin);
		setMcuRamExp(McuRamExp);
		setMcuRamLinStdDev(McuRamLinStdDev);
		setMcuRamExpStdDev(McuRamExpStdDev);
		setMcuBWLin(McuBWLin);
		setMcuBWExp(McuBWExp);
		setMcuBWLinStdDev(McuBWLinStdDev);
		setMcuBWExpStdDev(McuBWExpStdDev);
	}

	public double getMcuCpuLinStdDev() {
		return McuCpuLinStdDev;
	}

	public void setMcuCpuLinStdDev(double mcuCpuLinStdDev) {
		McuCpuLinStdDev = mcuCpuLinStdDev;
	}

	public double getMcuCpuExpStdDev() {
		return McuCpuExpStdDev;
	}

	public void setMcuCpuExpStdDev(double mcuCpuExpStdDev) {
		McuCpuExpStdDev = mcuCpuExpStdDev;
	}

	public double getMcuRamLinStdDev() {
		return McuRamLinStdDev;
	}

	public void setMcuRamLinStdDev(double mcuRamLinStdDev) {
		McuRamLinStdDev = mcuRamLinStdDev;
	}

	public double getMcuRamExpStdDev() {
		return McuRamExpStdDev;
	}

	public void setMcuRamExpStdDev(double mcuRamExpStdDev) {
		McuRamExpStdDev = mcuRamExpStdDev;
	}

	public double getMcuBWLinStdDev() {
		return McuBWLinStdDev;
	}

	public void setMcuBWLinStdDev(double mcuBWLinStdDev) {
		McuBWLinStdDev = mcuBWLinStdDev;
	}

	public double getMcuBWExpStdDev() {
		return McuBWExpStdDev;
	}

	public void setMcuBWExpStdDev(double mcuBWExpStdDev) {
		McuBWExpStdDev = mcuBWExpStdDev;
	}

	public double getMcuCpuLin() {
		return McuCpuLin;
	}

	public void setMcuCpuLin(double mcuCpuLin) {
		McuCpuLin = mcuCpuLin;
	}

	public double getMcuCpuExp() {
		return McuCpuExp;
	}

	public void setMcuCpuExp(double mcuCpuExp) {
		McuCpuExp = mcuCpuExp;
	}

	public double getMcuRamLin() {
		return McuRamLin;
	}

	public void setMcuRamLin(double mcuRamLin) {
		McuRamLin = mcuRamLin;
	}

	public double getMcuRamExp() {
		return McuRamExp;
	}

	public void setMcuRamExp(double mcuRamExp) {
		McuRamExp = mcuRamExp;
	}

	public double getMcuBWLin() {
		return McuBWLin;
	}

	public void setMcuBWLin(double mcuBWLin) {
		McuBWLin = mcuBWLin;
	}

	public double getMcuBWExp() {
		return McuBWExp;
	}

	public void setMcuBWExp(double mcuBWExp) {
		McuBWExp = mcuBWExp;
	}

	public double getMaxLinkLatency() {
		return maxLinkLatency;
	}

	public void setMaxLinkLatency(double maxLinkLatency) {
		this.maxLinkLatency = maxLinkLatency;
	}

	public double getNumberOfCoresIncremented() {
		return numberOfCoresIncremented;
	}

	public void setNumberOfCoresIncremented(double numberOfCoresIncremented) {
		this.numberOfCoresIncremented = numberOfCoresIncremented;
	}

	public int getRequiredRam() {
		return requiredRam;
	}

	public void setRequiredRam(int requiredRam) {
		this.requiredRam = requiredRam;
	}

	public int getRequiredBW() {
		return requiredBW;
	}

	public void setRequiredBW(int requiredBW) {
		this.requiredBW = requiredBW;
	}

	public int getRamIncremented() {
		return RamIncremented;
	}

	public void setRamIncremented(int ramIncremented) {
		RamIncremented = ramIncremented;
	}

	public int getBandwidthIncremented() {
		return BandwidthIncremented;
	}

	public void setBandwidthIncremented(int bandwidthIncremented) {
		BandwidthIncremented = bandwidthIncremented;
	}
	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}


	public long getContainerSize() {
		return containerSize;
	}

	public void setContainerSize(long containerSize) {
		this.containerSize = containerSize;
	}

	public long getRequestSize() {
		return requestSize;
	}

	public void setRequestSize(long requestSize) {
		this.requestSize = requestSize;
	}

	public double getTaskLength() {
		return taskLength;
	}

	public void setTaskLength(double taskLength) {
		this.taskLength = taskLength;
	}

	public long getResultsSize() {
		return resultsSize;
	}

	public void setResultsSize(long resultsSize) {
		this.resultsSize = resultsSize;
	}

	public int getNumberOfCores() {
		return numberOfCores;
	}

	public void setNumberOfCores(int numberOfCores) {
		this.numberOfCores = numberOfCores;
	}

	public double getUsagePercentage() {
		return usagePercentage;
	}

	public void setUsagePercentage(double usagePercentage) {
		this.usagePercentage = usagePercentage;
	}

	public double getLatency() {
		return latency;
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}

}
