package PaperScenario;

import java.util.ArrayList;

import com.mechalikh.pureedgesim.tasksgenerator.Task;

public class OrchOptRequest {

	private int OptIteration;	
	private PaperEdgeDevice OriginRequest, DestRequest;
	private Task OriginTask;
	private ArrayList<PaperEdgeDevice> OffloadedDevices = null;
	private boolean answered = false;
	
	public OrchOptRequest(int OptIteration, PaperEdgeDevice OriginRequest, PaperEdgeDevice DestRequest, Task OriginTask) {		
		this.OptIteration = OptIteration;
		this.OriginRequest = OriginRequest;
		this.DestRequest = DestRequest;
		this.OriginTask = OriginTask;
	}
	
	public void setOffloadedDevices(ArrayList<PaperEdgeDevice> Devices) {
		OffloadedDevices = Devices; 
	}
	
	public ArrayList<PaperEdgeDevice> getOffloadedDevices() {
		return OffloadedDevices;
	}

	public boolean isanswered() {
		return answered;
	}
	
	public Task getOriginTask() {
		return OriginTask;
	}

	public void setanswered(boolean answered) {
		this.answered = answered;
	}
	
	public int getOptIteration() {
		return OptIteration;
	}

	public void setOptIteration(int optIteration) {
		OptIteration = optIteration;
	}

	public PaperEdgeDevice getOriginRequest() {
		return OriginRequest;
	}

	public void setOriginRequest(PaperEdgeDevice originRequest) {
		OriginRequest = originRequest;
	}

	public PaperEdgeDevice getDestRequest() {
		return DestRequest;
	}

	public void setDestRequest(PaperEdgeDevice destRequest) {
		DestRequest = destRequest;
	}
	
	
}
