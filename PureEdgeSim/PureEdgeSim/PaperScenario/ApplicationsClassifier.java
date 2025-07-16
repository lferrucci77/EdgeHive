package PaperScenario;

//import java.io.BufferedWriter;
//import java.io.File;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
//import java.util.Arrays;

import com.univocity.parsers.common.processor.core.AbstractObjectProcessor;
import com.univocity.parsers.csv.CsvParser;
//import com.opencsv.CSVReader;
//import com.opencsv.exceptions.CsvException;
//import com.opencsv.exceptions.CsvValidationException;
import com.univocity.parsers.csv.CsvParserSettings;

import com.univocity.parsers.common.*;

import java.util.Random;

final class KMeans {

   /***********************************************************************
    * Data structures
    **********************************************************************/
   // user-defined parameters
   private int k;                // number of centroids
   private double[][] points;    // n-dimensional data points. 

   // optional parameters
   private int iterations;       // number of times to repeat the clustering. Choose run with lowest WCSS
   private boolean pp;           // true --> KMeans++. false --> basic random sampling
   private double epsilon;       // stops running when improvement in error < epsilon
   private boolean useEpsilon;   // true  --> stop running when marginal improvement in WCSS < epsilon
                                 // false --> stop running when 0 improvement
   private boolean L1norm;       // true --> L1 norm to calculate distance; false --> L2 norm

   // calculated from dimension of points[][]
   private int m;                // number of data points   (# of pixels for PhenoRipper)  
   private int n;                // number of dimensions    (# of channels for PhenoRipper)

   // output
   private double[][] centroids; // position vectors of centroids                      dim(2): (k) by (number of channels)
   private int[] assignment;     // assigns each point to nearest centroid [0, k-1]    dim(1): (number of pixels)
   private double WCSS;          // within-cluster sum-of-squares. Cost function to minimize

   // timing information
   private long start;
   private long end;
   
   /***********************************************************************
    * Constructors
    **********************************************************************/
   
   /**
    * Empty constructor is private to ensure that clients have to use the 
    * Builder inner class to create a KMeans object.
    */
   private KMeans() {} 

   /**
    * The proper way to construct a KMeans object: from an inner class object.
    * @param builder See inner class named Builder
    */
   private KMeans(Builder builder) {
      // start timing
      start = System.currentTimeMillis();
      
      // use information from builder
      k = builder.k;
      points = builder.points;
      iterations = builder.iterations;
      pp = builder.pp;
      epsilon = builder.epsilon;
      useEpsilon = builder.useEpsilon;
      L1norm = builder.L1norm;

      // get dimensions to set last 2 fields
      m = points.length;
      n = points[0].length;

      // run KMeans++ clustering algorithm
      run();
      
      end = System.currentTimeMillis();
   }


   /**
    * Builder class for constructing KMeans objects.
    * 
    * For descriptions of the fields in this (inner) class, see outer class.
    */
   public static class Builder {
      // required
      private final int k;
      private final double[][] points;

      // optional (default values given)
      private int iterations     = 10;
      private boolean pp         = true;
      private double epsilon     = .001;
      private boolean useEpsilon = true;
      private boolean L1norm = true;

      /**
       * Sets required parameters and checks that are a sufficient # of distinct
       * points to run KMeans.
       */
      public Builder(int k, double[][] points) {
         // check dimensions are valid
         if (k > points.length)
            throw new IllegalArgumentException("Required: # of points >= # of clusters");
         
         // check that there is a sufficient # of distinct points to run KMeans
         HashSet<double[]> hashSet = new HashSet<double[]>(k);
         int distinct = 0;
      
         for (int i = 0; i < points.length; i++) {
            if (!hashSet.contains(points[i])) {
               distinct++;
               if (distinct >= k)
                  break;
               hashSet.add(points[i]);
            }
         }
         
         if (distinct < k)
            throw new IllegalArgumentException("Required: # of distinct points >= # of clusters");
         
         this.k = k;
         this.points = points;
      }

      
      /**
       * Sets optional parameter. Default value is 50. 
       */
      public Builder iterations(int iterations) {
         if (iterations < 1) 
            throw new IllegalArgumentException("Required: non-negative number of iterations. Ex: 50");
         this.iterations = iterations;
         return this;
      }

      /**
       * Sets optional parameter. Default value is true.
       */
      public Builder pp(boolean pp) {
         this.pp = pp;
         return this;
      }

      /**
       * Sets optional parameter. Default value is .001.
       */
      public Builder epsilon(double epsilon) {
         if (epsilon < 0.0)
            throw new IllegalArgumentException("Required: non-negative value of epsilon. Ex: .001"); 

         this.epsilon = epsilon;
         return this;
      }

      /**
       * Sets optional parameter. Default value is true.
       */
      public Builder useEpsilon(boolean useEpsilon) {
         this.useEpsilon = useEpsilon;
         return this;
      }
      
      /**
       * Sets optional parameter. Default value is true
       */
      public Builder useL1norm(boolean L1norm) {
         this.L1norm = L1norm;
         return this;
      }

      /**
       * Build a KMeans object
       */
      public KMeans build() {
         return new KMeans(this);
      }
   }


   /***********************************************************************
    * KMeans clustering algorithm
    **********************************************************************/

   /** 
    * Run KMeans algorithm
    */
   private void run() {
      // for choosing the best run
      double bestWCSS = Double.POSITIVE_INFINITY;
      double[][] bestCentroids = new double[0][0];
      int[] bestAssignment = new int[0];

      // run multiple times and then choose the best run
      for (int n = 0; n < iterations; n++) {
         cluster();

         // store info if it was the best run so far
         if (WCSS < bestWCSS) {
            bestWCSS = WCSS;
            bestCentroids = centroids;
            bestAssignment = assignment;
         }
      }

      // keep info from best run
      WCSS = bestWCSS;
      centroids = bestCentroids;
      assignment = bestAssignment;
   }


   /**
    * Perform KMeans clustering algorithm once.
    */
   private void cluster() {
      // continue to re-cluster until marginal gains are small enough
      chooseInitialCentroids();
      WCSS = Double.POSITIVE_INFINITY; 
      double prevWCSS;
      do {  
         assignmentStep();   // assign points to the closest centroids

         updateStep();       // update centroids

         prevWCSS = WCSS;    // check if cost function meets stopping criteria
         calcWCSS();
      } while (!stop(prevWCSS));
   }


   /** 
    * Assigns to each data point the nearest centroid.
    */
   private void assignmentStep() {
      assignment = new int[m];

      double tempDist;
      double minValue;
      int minLocation;

      for (int i = 0; i < m; i++) {
         minLocation = 0;
         minValue = Double.POSITIVE_INFINITY;
         for (int j = 0; j < k; j++) {
            tempDist = distance(points[i], centroids[j]);
            if (tempDist < minValue) {
               minValue = tempDist;
               minLocation = j;
            }
         }

         assignment[i] = minLocation;
      }

   }


   /** 
    * Updates the centroids.
    */
   private void updateStep() {
      // reuse memory is faster than re-allocation
      for (int i = 0; i < k; i++)
         for (int j = 0; j < n; j++)
            centroids[i][j] = 0;
      
      int[] clustSize = new int[k];

      // sum points assigned to each cluster
      for (int i = 0; i < m; i++) {
         clustSize[assignment[i]]++;
         for (int j = 0; j < n; j++)
            centroids[assignment[i]][j] += points[i][j];
      }
      
      // store indices of empty clusters
      HashSet<Integer> emptyCentroids = new HashSet<Integer>();

      // divide to get averages -> centroids
      for (int i = 0; i < k; i++) {
         if (clustSize[i] == 0)
            emptyCentroids.add(i);

         else
            for (int j = 0; j < n; j++)
               centroids[i][j] /= clustSize[i];
      }
      
      // gracefully handle empty clusters by assigning to that centroid an unused data point
      if (emptyCentroids.size() != 0) {
         HashSet<double[]> nonemptyCentroids = new HashSet<double[]>(k - emptyCentroids.size());
         for (int i = 0; i < k; i++)
            if (!emptyCentroids.contains(i))
               nonemptyCentroids.add(centroids[i]);
         
         Random r = new Random();
         for (int i : emptyCentroids)
            while (true) {
               int rand = r.nextInt(points.length);
               if (!nonemptyCentroids.contains(points[rand])) {
                  nonemptyCentroids.add(points[rand]);
                  centroids[i] = points[rand];
                  break;
               }
            }

      }
      
   }


   /***********************************************************************
    * Choose initial centroids
    **********************************************************************/
   /**
    * Uses either plusplus (KMeans++) or a basic randoms sample to choose initial centroids
    */
   private void chooseInitialCentroids() {
      if (pp)
         plusplus();
      else
         basicRandSample();
   }

   /** 
    * Randomly chooses (without replacement) k data points as initial centroids. 
    */
   private void basicRandSample() {
      centroids = new double[k][n];
      double[][] copy = points;

      Random gen = new Random();

      int rand;
      for (int i = 0; i < k; i++) {
         rand = gen.nextInt(m - i);
         for (int j = 0; j < n; j++) {
            centroids[i][j] = copy[rand][j];       // store chosen centroid
            copy[rand][j] = copy[m - 1 - i][j];    // ensure sampling without replacement
         }
      }
   }

   /** 
    * Randomly chooses (without replacement) k data points as initial centroids using a
    * weighted probability distribution (proportional to D(x)^2 where D(x) is the 
    * distance from a data point to the nearest, already chosen centroid). 
    */
   // TODO: see if some of this code is extraneous (can be deleted)
   private void plusplus() {
      centroids = new double[k][n];       
      double[] distToClosestCentroid = new double[m];
      double[] weightedDistribution  = new double[m];  // cumulative sum of squared distances

      Random gen = new Random();
      int choose = 0;

      for (int c = 0; c < k; c++) {

         // first centroid: choose any data point
         if (c == 0)
            choose = gen.nextInt(m);

         // after first centroid, use a weighted distribution
         else {

            // check if the most recently added centroid is closer to any of the points than previously added ones
            for (int p = 0; p < m; p++) {
               // gives chosen points 0 probability of being chosen again -> sampling without replacement
               double tempDistance = Distance.L2(points[p], centroids[c - 1]); // need L2 norm here, not L1

               // base case: if we have only chosen one centroid so far, nothing to compare to
               if (c == 1)
                  distToClosestCentroid[p] = tempDistance;

               else { // c != 1 
                  if (tempDistance < distToClosestCentroid[p])
                     distToClosestCentroid[p] = tempDistance;
               }

               // no need to square because the distance is the square of the euclidean dist
               if (p == 0)
                  weightedDistribution[0] = distToClosestCentroid[0];
               else weightedDistribution[p] = weightedDistribution[p-1] + distToClosestCentroid[p];

            }

            // choose the next centroid
            double rand = gen.nextDouble();
            for (int j = m - 1; j > 0; j--) {
               // TODO: review and try to optimize
               // starts at the largest bin. EDIT: not actually the largest
               if (rand > weightedDistribution[j - 1] / weightedDistribution[m - 1]) { 
                  choose = j; // one bigger than the one above
                  break;
               }
               else // Because of invalid dimension errors, we can't make the forloop go to j2 > -1 when we have (j2-1) in the loop.
                  choose = 0;
            }
         }  

         // store the chosen centroid
         for (int i = 0; i < n; i++)
            centroids[c][i] = points[choose][i];
      }   
   }


   /***********************************************************************
    * Cutoff to stop clustering
    **********************************************************************/    

   /**
    * Calculates whether to stop the run
    * @param prevWCSS error from previous step in the run
    * @return
    */
   private boolean stop(double prevWCSS) {
      if (useEpsilon)
         return epsilonTest(prevWCSS);
      else
         return prevWCSS == WCSS; // TODO: make comment (more exact, but could be much slower)
      // could this take infinite amount of time? double compare...
      // I think not because WCSS is calc in same way as prevWCSS (if data structs don't change)
   }

   /**
    * Signals to stop running KMeans when the marginal improvement in WCSS
    * from the last step is small.
    * @param prevWCSS error from previous step in the run
    * @return
    */
   private boolean epsilonTest(double prevWCSS) {
      return epsilon > 1 - (WCSS / prevWCSS);
   }

   /***********************************************************************
    * Utility functions
    **********************************************************************/
   /**
    * Calculates distance between two n-dimensional points.
    * @param x
    * @param y
    * @return
    */
   private double distance(double[] x, double[] y) {
      return L1norm ? Distance.L1(x, y) : Distance.L2(x, y);
   }
   
   private static class Distance {

      /**
       * L1 norm: distance(X,Y) = sum_i=1:n[|x_i - y_i|]
       * <P> Minkowski distance of order 1.
       * @param x
       * @param y
       * @return
       */
      public static double L1(double[] x, double[] y) {
         if (x.length != y.length) throw new IllegalArgumentException("dimension error");
         double dist = 0;
         for (int i = 0; i < x.length; i++) 
            dist += Math.abs(x[i] - y[i]);
         return dist;
      }
      
      /**
       * L2 norm: distance(X,Y) = sqrt(sum_i=1:n[(x_i-y_i)^2])
       * <P> Euclidean distance, or Minkowski distance of order 2.
       * @param x
       * @param y
       * @return
       */
      public static double L2(double[] x, double[] y) {
         if (x.length != y.length) throw new IllegalArgumentException("dimension error");
         double dist = 0;
         for (int i = 0; i < x.length; i++)
            dist += Math.abs((x[i] - y[i]) * (x[i] - y[i]));
         return dist;
      }
   }
   
   /** 
    * Calculates WCSS (Within-Cluster-Sum-of-Squares), a measure of the clustering's error.
    */
   private void calcWCSS() {
      double WCSS = 0;
      int assignedClust;

      for (int i = 0; i < m; i++) {
         assignedClust = assignment[i];
         WCSS += distance(points[i], centroids[assignedClust]);
      }     

      this.WCSS = WCSS;
   }

   /***********************************************************************
    * Accessors
    ***********************************************************************/
   public int[] getAssignment() {
      return assignment;
   }

   public double[][] getCentroids() {
      return centroids;
   }

   public double getWCSS() {
      return WCSS;
   }
   
   public String getTiming() {
      return "KMeans++ took: " + (double) (end - start) / 1000.0 + " seconds";
   }
}


final class DoubleListProcessor<T extends Context> extends AbstractObjectProcessor<T> {

	private HashMap<String,double[]> rows;
	private String[] headers;
	private final int expectedOutputRowCount;


	/**
	 * Creates a new processor of {@code Object[]} rows with varying types.
	 */
	public DoubleListProcessor() {
		this(0);
	}

	/**
	 * Creates a new processor of {@code Object[]} rows with varying types.
	 *
	 * @param expectedRowCount expected number of rows as output.
	 */
	public DoubleListProcessor(int expectedOutputRowCount) {
		this.expectedOutputRowCount = expectedOutputRowCount <= 0 ? 10000 : expectedOutputRowCount;
	}


	@Override
	public void processStarted(T context) {
		super.processStarted(context);
		rows = new HashMap<String,double[]>(expectedOutputRowCount); //double[expectedRowCount][2]
	}

	/**
	 * Stores the row extracted by the parser
	 *
	 * @param row     the data extracted by the parser for an individual record and converted to an Object array.
	 * @param context A contextual object with information and controls over the current state of the parsing process
	 */
	@Override
	public void rowProcessed(Object[] row, T context) {
		if((row[4]!=null)&&(row[5]!=null)&&(row[1]!=null))			
		{
			double[] old_row = rows.get(row[1].toString());
			if (old_row != null)				
			{				
				old_row[0] += Double.parseDouble((String)row[4]); 
				old_row[1] += Double.parseDouble((String)row[5]);	
				old_row[2] += 1;	
				rows.put(row[1].toString(), old_row);
			}		
			else
			{
				double[] new_row = new double[3];
				new_row[0] = Double.parseDouble((String)row[4]); 
				new_row[1] = Double.parseDouble((String)row[5]);	
				new_row[2] = 1;	
				rows.put(row[1].toString(), new_row);
			}

		}
	}

	@Override
	public void processEnded(T context) {
		super.processEnded(context);
		this.headers = context.headers();

	}

	/**
	 * Returns the list of parsed and converted records
	 *
	 * @return the list of parsed and converted records
	 */
	public double[][] getRows() {
		double[][] returned_rows = new double[rows.size()][2];
		int i=0;
		for(double[] el:rows.values())
		{
			returned_rows[i][0] = el[0]/el[2];  
			returned_rows[i++][1] = el[1]/el[2];			
		}
		return returned_rows;
	}
	
	/**
	 * Returns the record headers. This can be either the headers defined in {@link CommonSettings#getHeaders()} or the headers parsed in the file when {@link CommonSettings#getHeaders()}  equals true
	 *
	 * @return the headers of all records parsed.
	 */
	public String[] getHeaders() {
		return headers;
	}
}


public class ApplicationsClassifier
{

	public static void main(String[] args) 
	{
		try {
			final int size = 10000;
			final int k = 16;
			double[][] data = null;			
			double [][] centroids_data = new double [k*11][2];
			int centroids_data_count = 0;
			double WCSS = 0.0;
			double[][] centroids = null;
			CsvParserSettings settings = new CsvParserSettings();
			settings.getFormat().setLineSeparator("\n");
			settings.setMaxCharsPerColumn(128);
			settings.setMaxColumns(8);
			settings.setInputBufferSize(134217728);
			settings.setHeaderExtractionEnabled(true);
			DoubleListProcessor<ParsingContext> rowProcessor;
			CsvParser parser;
			
			for ( int i=0; i<11; i++)
			{
				rowProcessor = new DoubleListProcessor<ParsingContext>(size);	
				settings.setProcessor(rowProcessor);
				parser = new CsvParser(settings);				
				String FileName = "C:\\Users\\Ferrucci\\Desktop\\Alibaba\\MSResource_" + i + ".csv";
				parser.parse(new FileReader(FileName));				
				//String[] headers = rowProcessor.getHeaders();
				data = rowProcessor.getRows();	
			    // run K-means
			    final long startTime = System.currentTimeMillis();
			    KMeans clustering = new KMeans.Builder(k, data)
			                                  .iterations(200)
			                                  .pp(true)
			                                  .epsilon(.01)
			                                  .useEpsilon(true)
			                                  .build();
			    final long endTime = System.currentTimeMillis();
			    final long elapsed = endTime - startTime;
			    System.out.println("Dataset " + i + " : MSResource_" + i + ".csv");
			    System.out.println("Clustering took " + (double) elapsed/1000 + " seconds");
			    System.out.println();

			    // get output
			    centroids = clustering.getCentroids();
			    
			    WCSS = clustering.getWCSS();
			    for (int j = 0; j < k; j++)
			    {
			    	System.out.println("(" + centroids[j][0] + ", " + centroids[j][1] + ")");
				    centroids_data[centroids_data_count][0] =  centroids[j][0];			    
				    centroids_data[centroids_data_count][1] =  centroids[j][1];
				    centroids_data_count+=1;
			    }
			    			    
			    System.out.println("\nThe within-cluster sum-of-squares (WCSS) = " + WCSS + "\n");
			    
			}
		
			KMeans clustering = new KMeans.Builder(k, centroids_data)
                    .iterations(200)
                    .pp(true)
                    .epsilon(.01)
                    .useEpsilon(true)
                    .build();
			
		    System.out.println("\n Final dataset \n");

		    // get output
		    centroids = clustering.getCentroids();
		    WCSS          = clustering.getWCSS();
		    for (int j = 0; j < k; j++)
		    {
		    	System.out.println("(" + centroids[j][0] + ", " + centroids[j][1] + ")");
		    	
		    }
		    			    
		    System.out.println("\nThe within-cluster sum-of-squares (WCSS) = " + WCSS + "\n");
			
		}
		catch(IOException e) //| CsvValidationException e ) 
		{
			e.printStackTrace();
		}
		
	}

}
