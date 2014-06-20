package com.gearlles.fss.enhanced.core;

public class Fish {
	
	private double[] position;
	private double[] oldPosition;
	private double[] deltaPosition;
	
	private double weight;
	private double oldWeight;
	private double deltaWeight;
	
	private double fitness;
	private double oldFitness;
	private double deltaFitness;
	
	public Fish() {
	}
	
	public double[] getPosition() {
		return position;
	}

	public void setPosition(double[] position) {
		this.position = position;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getDeltaFitness() {
		return deltaFitness;
	}

	public void setDeltaFitness(double deltaFitness) {
		this.deltaFitness = deltaFitness;
	}

	public double[] getDeltaPosition() {
		return deltaPosition;
	}

	public void setDeltaPosition(double[] deltaPosition) {
		this.deltaPosition = deltaPosition;
	}
	
	@Override
	public String toString() {
		return String.format("Position: (%f, %f). Weight = %f", position[0], position[1], weight);
	}

	public double getDeltaWeight() {
		return deltaWeight;
	}

	public void setDeltaWeight(double deltaWeight) {
		this.deltaWeight = deltaWeight;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public double[] getOldPosition() {
		return oldPosition;
	}

	public void setOldPosition(double[] oldPosition) {
		this.oldPosition = oldPosition;
	}

	public double getOldWeight() {
		return oldWeight;
	}

	public void setOldWeight(double oldWeight) {
		this.oldWeight = oldWeight;
	}

	public double getOldFitness() {
		return oldFitness;
	}

	public void setOldFitness(double oldFitness) {
		this.oldFitness = oldFitness;
	}
}
