package com.jon.assignment4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Assignment4 {

	private static final File LIB = new File("lib");
	private static HashMap<String, LinkedHashSet<DataInstance>> clusterMap = new HashMap<>(); //Map data instance to each cluster. Can get clusters from clusterMap::keySet
	private static LinkedHashSet<DataInstance> instances = new LinkedHashSet<>();

	private static void euclideanDistance(DataInstance p1, DataInstance p2) {
		double petalLength = Math.sqrt(((p1.getPetalLength() - p2.getPetalLength()) * (p1.getPetalLength() - p2.getPetalLength()))
                + ((p1.getPetalLength() - p2.getPetalLength()) * (p1.getPetalLength() - p2.getPetalLength())));

		double petalWidth = Math.sqrt(((p1.getPetalWidth() - p2.getPetalWidth()) * (p1.getPetalWidth() - p2.getPetalWidth()))
                + ((p1.getPetalWidth() - p2.getPetalWidth()) * (p1.getPetalWidth() - p2.getPetalWidth())));

        double sepalLength = Math.sqrt(((p1.getSepalLength() - p2.getSepalLength()) * (p1.getSepalLength() - p2.getSepalLength()))
                + ((p1.getSepalLength() - p2.getSepalLength()) * (p1.getSepalLength() - p2.getSepalLength())));

		double sepalWidth = Math.sqrt(((p1.getSepalWidth() - p2.getSepalWidth()) * (p1.getSepalWidth() - p2.getSepalWidth()))
                + ((p1.getSepalWidth() - p2.getSepalWidth()) * (p1.getSepalWidth() - p2.getSepalWidth())));

        System.out.printf("Instance " + p1.getInstance() + "-" + p2.getInstance() + "\nSepal Length Distance: %.1f \nSepal Width Distance: %.1f \nPetal Length Distance: %.1f \nPetal Width Distance: %.1f\n\n", sepalLength, sepalWidth, petalLength, petalWidth);
	}

	private static void cohesion() { //TODO: This is fucked up still, it's close tho
		for (Map.Entry<String, LinkedHashSet<DataInstance>> entry : clusterMap.entrySet()) {
			for (DataInstance instance : entry.getValue()) {
				double difference = 0.0;
				Set<DataInstance> otherInstances = entry.getValue().stream().filter(value -> value.getInstance() != instance.getInstance()).collect(Collectors.toSet());
				for (DataInstance otherInstance : otherInstances) {
					difference += Math.abs(instance.getValue() - otherInstance.getValue());
				}

				double cohesion = (difference / otherInstances.size());
				instance.setAi(cohesion); //TODO: Just set values and output at end after everything's been computed
				System.out.println("The Cohesion for data instance " + instance.getInstance() + " in " + entry.getKey() + " is " + cohesion);
			}
		}

	}

	private static void separation() { //Untested
		ArrayList<Double> separations = new ArrayList<>(); //Need ArrayList since might have duplicate values

		for (DataInstance instance : instances) {
			for (Map.Entry<String, LinkedHashSet<DataInstance>> entry : clusterMap.entrySet()) {
				if (!entry.getKey().equalsIgnoreCase(instance.getCluster())) {
					double difference = 0.0;

					for (DataInstance entryInstance : entry.getValue()) {
						difference += Math.abs(instance.getValue() - entryInstance.getValue());
					}

					separations.add(difference / entry.getValue().size());
				}
			}

			double lowest = Double.MAX_VALUE;
			for (double value : separations) {
				lowest = Math.min(lowest, value);
			}
			System.out.println("Lowest: " + lowest);
			instance.setBi(lowest);
		}

	}

	private static void silhouetteCoefficient() {
		for (DataInstance instance : instances) {
			instance.setSi(1 - (instance.getAi() / instance.getBi()));
		}
	}

	private static void processLine(String line) {
		String[] lineData = line.split(",");
		int instance = Integer.parseInt(lineData[0]);
		double sepLength = Double.parseDouble(lineData[1]);
		double sepWidth = Double.parseDouble(lineData[2]);
		double petLength = Double.parseDouble(lineData[3]);
		double petWidth = Double.parseDouble(lineData[4]);
		String cluster = lineData[5];

		DataInstance dataInstance = new DataInstance(cluster, instance, sepLength, sepWidth, petLength, petWidth);
		instances.add(dataInstance);
		clusterMap.computeIfAbsent(cluster, v -> new LinkedHashSet<>()).add(dataInstance);
	}

	private static void readFile(File arffFile) {
		boolean isData = false;

		try (BufferedReader br = new BufferedReader(new FileReader(arffFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("@data")) {
					isData = true;
				} else if (isData) {
					processLine(line);
				}
			}

			DataInstance prev = null;
			for (String cluster : clusterMap.keySet()) { //Get cluster keys
				System.out.println("Processing data instances for " + cluster);
				for (DataInstance instance : clusterMap.get(cluster)) { //Iterate through each data instance in each cluster mapped
					if (prev != null) {
						euclideanDistance(prev, instance);
					}

					prev = instance;
				}
			}
			cohesion();
			separation();
			silhouetteCoefficient();
			//TODO: Print everything out
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			clusterMap.clear();
		}
	}

	public static void main(String... args) {
		try {
			if (!LIB.exists()) {
				System.out.println("Missing library folder with ARFF files!");
				System.exit(0);
			}

			for (File file : Objects.requireNonNull(LIB.listFiles(), "Empty Lib directory, need ARFF files!")) {
				//				readFile(file);
				if (file.getName().equalsIgnoreCase("3.arff")) {
					readFile(file);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

class DataInstance {

	private final String cluster;
	private final int instance;
	private final double sepalWidth;
	private final double petalWidth;
	private final double sepalLength;
	private final double petalLength;
	private Double ai;
	private Double bi;
	private Double si;
	private final double value;

	public DataInstance(String cluster, int instance, double sepalLength, double sepalWidth, double petalLength, double petalWidth) {
		this.cluster = cluster;
		this.instance = instance;
		this.sepalLength = sepalLength;
		this.sepalWidth = sepalWidth;
		this.petalLength = petalLength;
		this.petalWidth = petalWidth;
		this.value = sepalLength + sepalWidth + petalLength + petalWidth;
		this.ai = 0.0;
		this.bi = 0.0;
		this.si = 0.0;
	}

	public String getCluster() {
		return cluster;
	}

	public int getInstance() {
		return instance;
	}

	public Double getSepalWidth() {
		return sepalWidth;
	}

	public Double getPetalWidth() {
		return petalWidth;
	}

	public Double getSepalLength() {
		return sepalLength;
	}

	public Double getPetalLength() {
		return petalLength;
	}

	public double getValue() {
		return value;
	}

	public Double getAi() {
		return ai;
	}

	public void setAi(Double ai) {
		this.ai = ai;
	}

	public Double getBi() {
		return bi;
	}

	public void setBi(Double bi) {
		this.bi = bi;
	}

	public Double getSi() {
		return si;
	}

	public void setSi(Double si) {
		this.si = si;
	}

	//TODO: For testing, remove before submission
	@Override
	public String toString() {
		return instance + ", " + sepalLength + ", " + sepalWidth + ", " + petalLength + ", " + petalWidth + ", " + cluster;
	}

}