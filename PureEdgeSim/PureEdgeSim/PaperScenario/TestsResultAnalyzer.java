package PaperScenario;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

//import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

public class TestsResultAnalyzer {

	final static int[] apps = {8};
	final static int[][] users = {{2000}};
	final static double[] ratios = {25.0};
	final static double ripetizioni = 5.0;
	final static double[] coefficient = {0.0};
	final static String path_suffix = "LIN";
	final static String[] dist_suffix = {"UR"};

	final static String[] File_suffix = {"Latency","Resources","Tasks","ResourcesLatencyXApp","BWInitialStats","CpuInitialStats","RamInitialStats","BWFinalStats","CpuFinalStats","RamFinalStats"};
	final static String[] File_stat_suffix = {"BWInitialStats","CpuInitialStats","RamInitialStats","BWFinalStats","CpuFinalStats","RamFinalStats"};

	static String FolderBase = "F:\\OneDrive - University of Pisa\\Unipi\\Dazzi Unipi\\Gecon 2024\\TestsGecon2024SI\\PaperScenario_Settings_";
	static CsvParser parser;

	public static void main(String[] args) {

		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		settings.setMaxCharsPerColumn(25);
		settings.setMaxColumns(15);
		settings.setInputBufferSize(8192);
		settings.setHeaderExtractionEnabled(false);

		for ( int i=0; i<apps.length;i++)
		{
			for (int num_users:users[i])
			{
				for(double ratio: ratios)
				{
					for (double coeff:coefficient)
					{
						for(String dist_suffix:dist_suffix)
						{
							String FolderName = FolderBase + apps[i] + "_"+ num_users + "_" + ratio + "_" + coeff + "_"+ path_suffix + "_" + dist_suffix + "\\Paper-Devices-" +  num_users +"-Applications-" + apps[i]+"\\";
							File folder = new File(FolderName);
							try 
							{
								if(folder.isDirectory())
								{
									File[] files = folder.listFiles();
									
									for(String suffix:File_suffix)
									{		
										List<Object[]> new_data =  new ArrayList<Object[]>();
										List<Object[]> new_data_stats =  new ArrayList<Object[]>();

										boolean first_time=true;
										boolean first_time_stats=true;
										
										for (File file:files)
										{

											if (file.getName().endsWith(suffix+".txt"))
											{
												RowListProcessor rowListProcessor = new RowListProcessor();
												settings.setProcessor(rowListProcessor);
												parser = new CsvParser(settings);	
												parser.parse(new FileReader(file));
												List<String[]> data = rowListProcessor.getRows();	
												int num_edge=0;
																							
												for (String[] elem : data)												
												{
													if(first_time_stats)
													{
																												
														String[] new_elem = new String[elem.length];												
														for(int j=0;j<6;j++)
															if(j<3)
																new_elem[j] = elem[j];
															else																																									
																new_elem[j] = Double.toString(Double.parseDouble(elem[j])/ripetizioni);	
														new_data_stats.add(new_elem);
													}
													else
													{
														String[] new_elem = (String[]) new_data_stats.get(num_edge);
				
														
														for(int j=3;j<6;j++)																																																																					
															new_elem[j] = Double.toString(Double.parseDouble(new_elem[j]) + (Double.parseDouble(elem[j])/ripetizioni));	
														num_edge++;
													}

												}			
												first_time_stats=false;
												String FileName = FolderName + "Paper-Devices-" + num_users + "-" + apps[i] + "-"+ suffix + "-MergedResults-" + ratio + "-" + coeff + "-LIN-" + dist_suffix + ".csv";
												Writer outputWriter = new FileWriter(FileName);											
												CsvWriter writer = new CsvWriter(outputWriter, new CsvWriterSettings());											
												writer.writeRowsAndClose(new_data_stats);	
											}

											
											if (file.getName().endsWith(suffix+".csv"))
											{								
												RowListProcessor rowListProcessor = new RowListProcessor();
												settings.setProcessor(rowListProcessor);
												parser = new CsvParser(settings);	
												parser.parse(new FileReader(file));
												List<String[]> data = rowListProcessor.getRows();

												int num_row=0;

												for (String[] elem : data)												
												{																					
													if(first_time)
													{															
														String[] new_elem = new String[((elem.length-1)*3)+1];																									
														new_elem[0] = elem[0]; 

														// Iteration, AppID, Cores ( totale x app ), % Cores (media totale x app), RAM (totale x app), % RAM (media totale x app), Latency in seconds ( media totale, x app ), % Latency ( totale, on Max Latency, per App )

														for(int j=1,k=1;j<elem.length;j++,k+=3)												
														{													
															if(j==1)	
															{
																if(suffix.equals("ResourcesLatencyXApp"))														
																	new_elem[j] = elem[j];																			
															}
															else			
																if(suffix.equals("ResourcesLatencyXApp"))														
																	new_elem[j] = Double.toString(Double.parseDouble(elem[j])/ripetizioni);

															if(suffix.equals("Tasks"))		
															{
																new_elem[k] = Double.toString((double)Math.round(Double.parseDouble(elem[j])/ripetizioni));
																new_elem[k+1] = Double.toString((double)Math.round(Double.parseDouble(elem[j])));
																new_elem[k+2] = Double.toString((double)Math.round(Double.parseDouble(elem[j])));
															}
															else
															{															
																new_elem[k] = Double.toString(Double.parseDouble(elem[j])/ripetizioni);
																new_elem[k+1] = Double.toString(Double.parseDouble(elem[j]));
																new_elem[k+2] = Double.toString(Double.parseDouble(elem[j]));
															}																							
														}
														new_data.add(new_elem);
													}
													else
													{
														String[] new_elem = (String[]) new_data.get(num_row);

														if(suffix.equals("ResourcesLatencyXApp"))
														{													
															for ( int j=2; j<elem.length;j++ )													
																new_elem[j] = Double.toString(Double.parseDouble(new_elem[j]) + (Double.parseDouble(elem[j])/ripetizioni));													
														}
														else 
														{

															for ( int j=1,k=1; j<elem.length;j++,k+=3)
															{

																Double elem_value = Double.parseDouble(elem[j]);
																Double new_elem_value = Double.parseDouble(new_elem[k]);
																Double new_elem_value_max = Double.parseDouble(new_elem[k+2]);
																Double new_elem_value_min = Double.parseDouble(new_elem[k+1]);

																if(suffix.equals("Tasks"))	
																{
																	new_elem[k] = Double.toString((double) Math.round(new_elem_value + elem_value/ripetizioni));
																	if(elem_value<new_elem_value_min)
																		new_elem[k+1] = elem[j];
																	if(new_elem_value_max<elem_value)
																		new_elem[k+2] = elem[j];
																}
																else
																{																
																	new_elem[k] = Double.toString(new_elem_value + elem_value/ripetizioni);
																	if(elem_value<new_elem_value_min)
																		new_elem[k+1] = elem[j];
																	if(new_elem_value_max<elem_value)
																		new_elem[k+2] = elem[j];
																}

															}
														}
														num_row++;
													}
												}
												first_time=false;
												String FileName = FolderName + "Paper-Devices-" + num_users + "-" + apps[i] + "-"+ suffix + "-MergedResults-" + ratio + "-" + coeff + "-LIN-" + dist_suffix + ".csv";
												Writer outputWriter = new FileWriter(FileName);											
												CsvWriter writer = new CsvWriter(outputWriter, new CsvWriterSettings());											
												writer.writeRowsAndClose(new_data);		
											}								
										}
									}
								}				
							} 
							catch ( IOException e ) 
							{
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}	
}
