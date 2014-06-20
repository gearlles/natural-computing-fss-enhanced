package com.gearlles.fss.enhanced.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FSSSearch {
	
	private Logger logger = LoggerFactory.getLogger(FSSSearch.class);
	
	private List<Fish> school;
	private int dimensions;
	private int schoolSize;
	private double RANGE = 5.12;
	
	private Random rand = new Random();

	private int numberOfFishIncreasedWeightLastIteration;
	private boolean positiveC;
	private double bestFitness;

	private double ALPHA;
	private double BETA;
	private double C;
	private double STEP_IND = 0.02d;
	
	public FSSSearch() {
		this.school = new ArrayList<Fish>();
		this.dimensions = 30;
		this.schoolSize = 30;
		this.bestFitness = Double.MAX_VALUE;
		this.numberOfFishIncreasedWeightLastIteration = 0;
		this.positiveC = true;
		
		this.ALPHA = 0.01d;
		this.BETA = 0.4d;
		this.C = 0.1d;
		
		initialize();
	}
	
	private void initialize() {
		for (int i = 0; i < schoolSize; i++) {
			double position[] = new double[dimensions];
			
			for (int j = 0; j < dimensions; j++) {
				position[j] = rand.nextDouble() * 2 * RANGE - RANGE;
			}
			
			Fish fish = new Fish();
			fish.setPosition(position);
			fish.setFitness(calculateFitness(position));
			fish.setWeight(rand.nextInt(301) + 300);
			
			double[] newPosition = individualmovement(fish);
			
			fish.setOldPosition(fish.getPosition());
			fish.setOldFitness(fish.getFitness());
			
			fish.setPosition(newPosition);
			fish.setFitness(calculateFitness(newPosition));
			
			fish.setDeltaFitness(fish.getFitness() - fish.getOldFitness());
			
			double[] deltaPosition = new double[dimensions];
			for (int j = 0; j < dimensions; j++) {
				deltaPosition[j] = fish.getPosition()[j] - fish.getOldPosition()[j];
			}
			fish.setDeltaPosition(deltaPosition);
			fish.setWeight(fish.getWeight() + fish.getDeltaFitness() * (-1));  // deltaFitness < 0 .: better location .: weight should increase
			
			school.add(fish);
		}
	}

	public double iterateOnce(int it) {
		
		int numberOfFishIncreasedWeightIteration = 0;
		
		for (int i = 1; i < school.size(); i++) {
			Fish fish = school.get(i);
			
			// 1. Evaluate fish displacement 
			double[] deltaPosition = new double[dimensions];
			for (int j = 0; j < dimensions; j++) {
				deltaPosition[j] = fish.getPosition()[j] - fish.getOldPosition()[j];
			}
			
			fish.setDeltaPosition(deltaPosition);
			
			// 2. Evaluate fitness variation
			double deltaFitness = calculateFitness(fish.getPosition()) - calculateFitness(fish.getOldPosition());
			fish.setDeltaFitness(deltaFitness);
			
//			logger.debug("" + calculateFitness(fish.getPosition()));
			
			if (calculateFitness(fish.getPosition()) < bestFitness)
			{
				bestFitness = calculateFitness(fish.getPosition());
			}
			
			// 3. Feed the fish
			fish.setOldWeight(fish.getWeight());
			double newWeight = fish.getWeight() + deltaFitness * (-1); // deltaFitness < 0 .: better location .: weight should increase
			if (newWeight < 1) {
				newWeight = 1;
			} else if (newWeight > 1000) {
				newWeight = 1000;
			}
			fish.setWeight(newWeight); 
			
			// 4. Evaluate weight variation
			fish.setDeltaWeight(fish.getWeight() - fish.getOldWeight());
			
			if (fish.getDeltaWeight() > 0) {
				numberOfFishIncreasedWeightIteration++;
			}
		}
		
		// 5. Calculate barycenter
		double[] barycenter = new double[dimensions];
		double totalWeight = 0;
		
		for (int i = 0; i < school.size(); i++) {
			Fish _fish = school.get(i);
			
			for (int j = 0; j < dimensions; j++) {
				barycenter[j] += _fish.getPosition()[j] * _fish.getWeight();
			}
			
			totalWeight += _fish.getWeight();
		}
		
		for (int i = 0; i < dimensions; i++) {
			barycenter[i] /= totalWeight;
		}
		
		// 6. Update fish position
		for (int i = 1; i < school.size(); i++) {
			Fish fish = school.get(i);
			
			double[] currentPosition = fish.getPosition();
			double[] individualMovementTerm = individualMovementTerm(fish);
			double[] instinctiveMovementTerm = collectiveInstinctiveMovementTerm(fish);
			double[] volitiveCollectiveMovementTerm = collectiveVolitiveMovementTerm(fish, barycenter);
			
			double[] newPosition = new double[dimensions];
			for (int j = 0; j < dimensions; j++) {
				newPosition[j] = currentPosition[j] + individualMovementTerm[j] + instinctiveMovementTerm[j] + volitiveCollectiveMovementTerm[j];
			}
			
			fish.setOldPosition(fish.getPosition());
			fish.setPosition(newPosition);
		}
		
		// update C
		if (numberOfFishIncreasedWeightIteration < numberOfFishIncreasedWeightLastIteration) {
			if (!positiveC) {
				C *= 1 + ALPHA;
				positiveC = true;
			} else {
				C *= 1 - ALPHA;
				positiveC = false;
			}
		} else {
			if (positiveC) {
				C *= 1 + ALPHA;
			} else {
				C *= 1 - ALPHA;
			}
		}
		
		if (C < 0.001d) {
			C = 0.001d;
		} else if (C > 0.999d) {
			C = 0.999d; 
		}
		
		numberOfFishIncreasedWeightLastIteration = numberOfFishIncreasedWeightIteration;
		
		logger.debug(String.format("%d\t%f", it, bestFitness));
		
		return bestFitness;
	}
	
	private double[] individualMovementTerm(Fish fish) {
		double[] individualMovementTerm = new double[dimensions];
		double[] deltaPosition = fish.getDeltaPosition();
		for (int i = 0; i < dimensions; i++) {
			individualMovementTerm[i] = BETA * C * deltaPosition[i];
		}
		return individualMovementTerm;
	}
	
	private double[] collectiveInstinctiveMovementTerm(Fish fish) {
		double[] collectiveInstinctiveMovementTerm = new double[dimensions];
		double totalWeight = 0;
		
		for (int i = 0; i < school.size(); i++) {
			Fish _fish = school.get(i);
			
			for (int j = 0; j < dimensions; j++) {
				collectiveInstinctiveMovementTerm[j] += C * rand.nextDouble() * _fish.getDeltaPosition()[j] * _fish.getWeight();
			}
			
			totalWeight += _fish.getWeight();
		}
		
		for (int i = 0; i < dimensions; i++) {
			collectiveInstinctiveMovementTerm[i] /= totalWeight;
		}
		
		return collectiveInstinctiveMovementTerm;
	}
	
	private double[] collectiveVolitiveMovementTerm(Fish fish, double[] barycenter) {
		double result[] = new double[dimensions];
		double sumDeltaWeight = 0;
		
		for (int i = 0; i < schoolSize; i++) {
			sumDeltaWeight += school.get(i).getDeltaWeight();
		}
		
		for (int i = 0; i < dimensions; i++) {
			result[i] = C * rand.nextDouble() * Math.signum(sumDeltaWeight) * (fish.getPosition()[i] - barycenter[i]);
		}
		return result;
	}
	
	private double[] individualmovement(Fish fish) {
		double[] newPosition = new double[dimensions];
		double[] oldPosition = fish.getPosition();
		double[] randArray = new double[dimensions];
		
		for (int i = 0; i < randArray.length; i++) {
			randArray[i] = rand.nextDouble() * 2 - 1; 
		}
		
		for (int i = 0; i < dimensions; i++) {
			newPosition[i] = oldPosition[i] + randArray[i] * STEP_IND ;
		}
		
		return newPosition;
	}
	
	private double calculateFitness(double[] inputs) {
		double res = 10 * inputs.length;
		for (int i = 0; i < inputs.length; i++)
			res += inputs[i] * inputs[i] - 10
					* Math.cos(2 * Math.PI * inputs[i]);
		return res;
	}
	
	@SuppressWarnings("unused")
	private double calculateFitnessSphere(double[] inputs) {
		double res = 0;
		for (int i = 0; i < inputs.length; i++)
			res += Math.pow(inputs[i], 2);
		return res;
	}

	public List<Fish> getSchool() {
		return this.school;
	}
	
	public static void main(String[] args) {
		
		int iterations = 30000;
		
		double[] best = new double[iterations];
		
		for (int i = 0; i < 30; i++) {
			FSSSearch s = new FSSSearch();
			for (int j = 0; j < best.length; j++) {
				best[j] += s.iterateOnce(j);
			}
		}
		
		for (int i = 0; i < best.length; i++) {
			best[i] /= 30;
		}
		
		XYSeries series = new XYSeries("Fitness");
		for (int i = 0; i < iterations; i++) {
			series.add(i, best[i]);
		}
		
		
		
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart("Fish School Search II", // Title
				"Iteration", // x-axis Label
				"Best Fitness", // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				true, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
				);
		try {
			ChartUtilities.saveChartAsJPEG(new File("C:\\Users\\Gearlles\\Desktop\\chart.jpg"), chart, 500, 300);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public double getRANGE() {
		return RANGE;
	}
}
