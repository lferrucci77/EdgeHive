package PaperScenario;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;



public class DatasetToApplication 
{
	static Random randLatency = new Random();
	
	final static double original_cpu = 96; // 6 volte i Cores originali macchina Alibaba
	final static double original_mem = 128000; // 6 volte la Memoria originale macchina Alibaba
	
	//final double scale_value = 16; // valore di scalatura
	final static double ratio = 54.2; // 1950 cores aumentati di "ratio"-volte partendo da 18
	final String add_name = "TestHighLatency";
	static int[][] num_users_for_apps_set = {{3000,6000,9000},
					 	 		 	  		 {6000,12000,18000}};
	static double[] percentages = {12.5,25,50}; // Costo per un utente rispetto al costo fisso di partenza
	
	static double[][] Lat_Values = {{(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07},
									{(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07,(randLatency.nextDouble() * 0.03) + 0.07}};
	
	static double[][] CPU_Perc = {{0.330926372,0.08334390539,0.181329321,0.05985705364,0.1591974938,0.2027909131,0.07983981246,0.08503793294},
								  {0.10483849000497508,0.1178277286434139,0.5397229871283974,0.05967176411497346,0.2349918888783106,0.34891938477667367,0.06933299502604912,0.14624663093194518,0.22201745785432356,0.30926525172886205,0.07717723839521,0.05475811435413764,0.06393111845107534,0.1694188959877899,0.2721515609600327,0.1284186080203213}};

	static double[][] Mem_Perc = {{0.7013367779,0.5960883339,0.650470319,0.3639063218,0.7984711242,0.5045609991,0.475778968,0.7095893095},
								  {0.6453065167320496,0.8203624519587833,0.7867835916039445,0.24440645880276043,0.4817970032632728,0.6092459832076881,0.4916443905244094,0.562038074982861,0.642656491671426,0.7254428903254422,0.7217042024306147,0.399403824132783,0.587674884331459,0.7238978723290495,0.8211804753613048,0.4451907855739113}};
	static String FolderBase = "C:\\Users\\Ferrucci\\eclipse-workspace\\PureEdgeSimJournal.zip_expanded\\PureEdgeSim\\PureEdgeSim\\PaperScenario_Settings";
	static String[] OtherFiles = {"cloud.xml","edge_devices.xml","edge_datacenters.xml"};
	
	public DatasetToApplication() 
	{
		// TODO Auto-generated constructor stub
	}

	private static void generate_applications_file(int app_id, double[] CPU_Perc, double[] Mem_Perc, double percentage, String FolderName, double[] LatValues) throws IOException
	{					
		
		File file = new File(FolderName + "\\applications.xml");
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write( "<?xml version=\"1.0\"?>\r\n"
				+ "<applications>\r\n");
		String CPU_fixed,CPU_user,Mem_fixed,Mem_user;
		
		for(int j=0; j < CPU_Perc.length; j++)
		{

			CPU_fixed =  Integer.toString((int) Math.ceil((CPU_Perc[j] * original_cpu * ratio )/ 100));
			CPU_user =  Integer.toString((int) Math.ceil((CPU_Perc[j] * original_cpu * ratio *percentage)/ 10000));

			Mem_fixed =  Integer.toString((int)Math.ceil((Mem_Perc[j] * original_mem)/ 100 ));
			Mem_user = Integer.toString((int)Math.ceil((Mem_Perc[j] * original_mem * percentage)/ 10000 ));

			bw.write( "    <application name=\"App" + (j+1) + "\">\r\n"
					+ "        <rate>1</rate>\r\n"
					+ "        <usage_percentage>0</usage_percentage> <!-- percentage of devices using this type of applications --> \r\n"
					+ "        <max_delay>1000000</max_delay> <!-- latency in seconds -->\r\n"
					+ "        <container_size>200</container_size> <!--application/container size in kilobytes -->\r\n"
					+ "        <request_size>200</request_size> <!-- the offloading request that will be sent to the orchestrator and then to the device where the task will be offloaded in kilobytes -->\r\n"
					+ "        <results_size>200</results_size> <!-- the results of the offlaoded task in kilobytes -->\r\n" 
					+ "        <task_length>-1</task_length> <!--MI: million instructions -->\r\n"												
					+ "        <required_bandwidth>5000</required_bandwidth> <!--required bandwidth, in kbit/s -->\r\n"
					+ "        <bandwidth_increment>1000</bandwidth_increment> <!--increment of bandwidth, in kbit/s, per user -->\r\n"
					+ "        <required_ram>" + Mem_fixed +"</required_ram> <!--required ram, in megabytes -->\r\n"
					+ "        <ram_increment>"+Mem_user+"</ram_increment> <!-- increment of ram needed, in megabytes, per user -->\r\n"
					+ "        <required_core>"+ CPU_fixed +"</required_core>\r\n"
					+ "        <core_increment>"+CPU_user+"</core_increment> <!-- increment of cpu core needed, per user -->	\r\n"
					+ "        <max_link_latency>"+ LatValues[j]+ "</max_link_latency> <!-- max latency from the container of the app to users, in seconds -->	\r\n"
					+ "    </application>\r\n");
		}
		bw.write( "</applications>");
		bw.close();
	}
	
	private static void generate_simulation_parameters_file(int num_users, String FolderName) throws IOException
	{					
		
		String[][] replacement = {{"min_number_of_edge_devices=375","max_number_of_edge_devices=375","edge_device_counter_size=375"},
								  {"min_number_of_edge_devices=" + num_users, "max_number_of_edge_devices=" + num_users,"edge_device_counter_size=" + num_users}};

		List<String> lines = Files.readAllLines(Paths.get(FolderBase + "\\simulation_parameters.properties"));		
		
		File file = new File(FolderName + "\\simulation_parameters.properties");
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for(String line: lines)	
			bw.write(StringUtils.replaceEach(line,replacement[0],replacement[1]) + "\r\n");
		bw.close();
	}
	
	private static void copy_other_parameter_files(String FolderName) throws IOException
	{
		for(String file: OtherFiles)
		{
			File original_file = new File(FolderBase + "\\" + file);
			File new_file = new File(FolderName + "\\" + file);

			FileUtils.copyFile(original_file,new_file);		
		}
	}

	public static void main(String[] args) 
	{			
		try 
		{		
			for(double percentage: percentages)
			{				
				for(int i =0; i < CPU_Perc.length; i++)		
				{	
					for(int num_users: num_users_for_apps_set[i])
					{
						String FolderName = FolderBase + "_" + CPU_Perc[i].length + "_"+ num_users + "_" + percentage;
						Files.createDirectories(Paths.get(FolderName));
						generate_applications_file(i, CPU_Perc[i], Mem_Perc[i], percentage, FolderName, Lat_Values[i]);
						generate_simulation_parameters_file(num_users,FolderName);
						copy_other_parameter_files(FolderName);
					}
				}
			}
			
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}
}
