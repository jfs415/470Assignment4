package com.jon.assignment4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Assignment4 {

	private static final File LIB = new File("lib");
	private static HashMap<String, HashSet<DataInstance>> clusterMap = new HashMap<>(); //Map data instance to each cluster. Can get clusters from clusterMap::keySet

	private static Double euclideanDistance(DataInstance p1, DataInstance p2, String type) { //Untested, not sure this is what he wants
		return type.equalsIgnoreCase("length")
				? Math.sqrt((p2.getPetalLength() - p1.getPetalLength()) * (p2.getPetalLength() - p1.getPetalLength())
                        + (p2.getSepalLength() - p1.getSepalLength()) * (p2.getSepalLength() - p1.getSepalLength()))
                : Math.sqrt((p2.getPetalWidth() - p1.getPetalWidth()) * (p2.getPetalWidth() - p1.getPetalWidth())
                        + (p2.getSepalWidth() - p1.getSepalWidth()) * (p2.getSepalWidth() - p1.getSepalWidth()));
	}

	private static void cohesion(DataInstance instance1, DataInstance instance2) {

	}

	private static void silhouetteCoefficient(DataInstance instance1, DataInstance instance2) {

	}

	private static void processLine(String line) {
		String[] lineData = line.split(",");
		int instance = Integer.parseInt(lineData[0]);
		double sepLength = Double.parseDouble(lineData[1]);
		double sepWidth = Double.parseDouble(lineData[2]);
		double petLength = Double.parseDouble(lineData[3]);
		double petWidth = Double.parseDouble(lineData[4]);
		String cluster = lineData[5];
		clusterMap.computeIfAbsent(lineData[lineData.length - 1], v -> new HashSet<>()).add(new DataInstance(cluster, instance, sepLength, sepWidth, petLength, petWidth));
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

			for (File file : Objects.requireNonNull(LIB.listFiles())) {
				readFile(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

class DataInstance {

	private String cluster;
	private int instance;
	private double sepalWidth;
	private double petalWidth;
	private double sepalLength;
	private double petalLength;

	public DataInstance(String cluster, int instance, double sepalWidth, double petalWidth, double sepalLength, double petalLength) {
		this.cluster = cluster;
		this.instance = instance;
		this.sepalLength = sepalLength;
		this.petalLength = petalLength;
		this.petalWidth = petalWidth;
		this.sepalWidth = sepalWidth;
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

}