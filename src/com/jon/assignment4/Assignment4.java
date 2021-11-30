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

	private static void printDistances() {
		DataInstance prev = null;
		for (String cluster : clusterMap.keySet()) { //Get cluster keys
			System.out.println("Processing data instances for " + cluster);
			for (DataInstance instance : clusterMap.get(cluster)) { //Iterate through each data instance in each cluster mapped
				if (prev != null) { //Print Euclidean distances for each point
					System.out.println("Distance from: " + prev.getInstance() + " -> " + instance.getInstance() + " " + euclideanDistance(prev, instance));
				}

				prev = instance;
			}
			System.out.println("\n");
		}
	}

	private static double euclideanDistance(DataInstance p1, DataInstance p2) {
		double sepalLength = Math.pow((p1.getSepalLength() - p2.getSepalLength()), 2);
		double sepalWidth = Math.pow((p1.getSepalWidth() - p2.getSepalWidth()), 2);
		double petalLength = Math.pow((p1.getPetalLength() - p2.getPetalLength()), 2);
		double petalWidth = Math.pow((p1.getPetalWidth() - p2.getPetalWidth()), 2);

		return Math.sqrt((sepalLength + sepalWidth + petalLength + petalWidth));
	}

	private static void cohesion() {
		for (Map.Entry<String, LinkedHashSet<DataInstance>> entry : clusterMap.entrySet()) { //Iterate through each entry in clusterMap
			for (DataInstance instance : entry.getValue()) {
				double difference = 0.0;

				//Create set without the data instance above
				Set<DataInstance> otherInstances = entry.getValue().stream().filter(value -> value.getInstance() != instance.getInstance()).collect(Collectors.toSet());

				for (DataInstance otherInstance : otherInstances) {
					difference += euclideanDistance(instance, otherInstance);
				}

				double cohesion = (difference / otherInstances.size());
				instance.setAi(cohesion);
			}
		}

	}

	private static void separation() {
		ArrayList<Double> separations = new ArrayList<>(); //Need ArrayList since might have duplicate values

		for (DataInstance instance : instances) { //Iterate through instances Set
			for (Map.Entry<String, LinkedHashSet<DataInstance>> entry : clusterMap.entrySet()) { //Iterate through LinkedHashSet containing each clusters data points
				if (!entry.getKey().equalsIgnoreCase(instance.getCluster())) { //Check to make sure not the same cluster
					double difference = 0.0;

					for (DataInstance entryInstance : entry.getValue()) {
						difference += euclideanDistance(instance, entryInstance);
					}

					separations.add(difference / entry.getValue().size());
				}
			}

			//Calculate lowest to be set as bi value
			double lowest = Double.MAX_VALUE;
			for (double value : separations) {
				lowest = Math.min(lowest, value);
			}
			instance.setBi(lowest);
			separations.clear(); //Clear before starting next cluster
		}

	}

	private static void silhouetteCoefficient() {
		for (DataInstance instance : instances) {
			instance.setSi(1 - (instance.getAi() / instance.getBi()));
		}
	}

	/**
	 * Parse the comma separated values in each data line.
	 * Creates a new DataInstance Object from the parsed values.
	 *
	 * @param line
	 *          Data line from ARFF file
	 */

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
				if (line.contains("@data")) { //Search for start of the data
					isData = true;
				} else if (isData) { //Process each data line
					processLine(line);
				}
			}

			//			printDistances(); //Uncomment this to print the euclidean distances for each point

			//Set the ai, bi and si values for each data instance
			cohesion();
			separation();
			silhouetteCoefficient();

			//Uncomment this line to see the ai, bi and si for each data point instance
			//			instances.forEach(System.out::println);

			for (Map.Entry<String, LinkedHashSet<DataInstance>> entry : clusterMap.entrySet()) {
				double clusterAverage = 0.0;

				for (DataInstance instance : entry.getValue()) {
					clusterAverage += instance.getSi();
				}

				System.out.println("The Average Silhouette Coefficient for " + entry.getKey() + " is: " + (clusterAverage / entry.getValue().size()));
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally { //Clear before reading another file
			clusterMap.clear();
			instances.clear();
		}
	}

	public static void main(String... args) {
		try {
			if (!LIB.exists()) {
				System.out.println("Missing library folder with ARFF files!");
				System.exit(0);
			}

			for (File file : Objects.requireNonNull(LIB.listFiles(), "Empty Lib directory, need ARFF files!")) {
				System.out.println("PROCESSING FILE: " + file.getName());
				readFile(file);
				//				if (file.getName().equalsIgnoreCase("3.arff")) { //Uncomment to read in 3.arff to validate
				//					readFile(file);
				//				}
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

	public DataInstance(String cluster, int instance, double sepalLength, double sepalWidth, double petalLength, double petalWidth) {
		this.cluster = cluster;
		this.instance = instance;
		this.sepalLength = sepalLength;
		this.sepalWidth = sepalWidth;
		this.petalLength = petalLength;
		this.petalWidth = petalWidth;
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

	@Override
	public String toString() {
		return cluster + " Instance " + instance + "\nAI: " + ai + "\nBI: " + bi + "\nSI: " + si + "\n";
	}

}