import edu.stanford.nlp.simple.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

class Term {
	String word;
	int document, count;
	double frequency, inverseDocumentFrequency, weight;

	Term(String word, int document, int count) {
		this.word = word;
		this.document = document;
		this.count = count;
	}

	void computeWeight() {
		this.weight = frequency * inverseDocumentFrequency;
	}

	@Override
	public String toString() {
		return "[Document: " + document + ", Word: " + word + ", Count: " + count
		+ ", TF: " + frequency + ", IDF: " + inverseDocumentFrequency + ", TD-IDF: " + weight + "]";
	}
}

public class Main extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override 
	public void start(Stage stage) {
		// writeTopics(folderMatrices); USED PREVIOUS IMPLEMENTATION OF FILTERED DOCUMENTS

		Term[][] allDocumentsMatrix = flipMatrix(constructMatrix(filteredDocuments()));
		int[][] clusterMatrix = kMeansCluster(allDocumentsMatrix, 3);

		for (int i = 0; i < clusterMatrix[0].length; i++) {
			System.out.println("\n\nK-Cluster " + (i + 1) + ":\n");

			for (int j = 0; j < clusterMatrix.length; j++)
				if (clusterMatrix[j][i] != -1)
					System.out.println("Document " + clusterMatrix[j][i]);
		}

		stage.setTitle("Scatter Chart Sample");
		final NumberAxis xAxis = new NumberAxis(1, 3, 1);
		final NumberAxis yAxis = new NumberAxis(1, 23, 1);
		final ScatterChart<Number, Number> sc = new ScatterChart<>(xAxis, yAxis);
        xAxis.setLabel("Cluster");
        yAxis.setLabel("Document");
        sc.setTitle("K-Means Clustering");

        for (int i = 0; i < clusterMatrix[0].length; i++) {
        	XYChart.Series cluster = new XYChart.Series();
        	cluster.setName("Cluster " + (i + 1));

        	for (int j = 0; j < clusterMatrix.length; j++)	
        		if (clusterMatrix[j][i] != -1)
        			cluster.getData().add(new XYChart.Data(i + 1, j + 1));

        	sc.getData().addAll(cluster);
        }

        Scene scene = new Scene(sc, 500, 400);
        stage.setScene(scene);
        stage.show();

	}

	// all that functionality!!!

	static void writeTopics(List<Term[][]> folderMatrices) {
		try {
			PrintWriter writer = new PrintWriter("topics.txt", "UTF-8");
			writer.write("Below are the top keywords relavent to each document folder.\n");

			int folderCount = 1;

			for (Term[][] documentMatrix : folderMatrices) {
				writer.write("\nFolder C" + folderCount + ":\n");
				
				for (int i = 0; i < 10; i++)
					writer.write(documentMatrix[0][i].word + "\n");

				folderCount += 3;
			}

			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
	}

	static Scanner readFile(String fileName) {
		try {
			return new Scanner(new File(fileName));
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	static List<String> getStopWords() {
		Scanner stopWordReader = readFile("stopwords.txt");
		List<String> stopWords = new ArrayList<>();

		while (stopWordReader.hasNextLine())
			stopWords.add(stopWordReader.nextLine());

		return stopWords;
	}

	static Document createDocument(Scanner fileReader) {
		String contents = "";

		while (fileReader.hasNextLine())
			contents += fileReader.nextLine() + '\n';

		return new Document(contents);
	}

	static List<Sentence> filteredSentences(Document d, List<String> stopWords) {
		List<Sentence> filteredSentences = new ArrayList<>();
		
		for (Sentence s : d.sentences()) {
			List<String> nerTags = s.mentions();
			s = new Sentence(s.toString().trim().replace("\n", "").replaceAll(" +", " "));

			String sentenceWithNER = "";

			int index = 0;

			// System.out.println("\nTAGS: " + nerTags);
			// System.out.println("SENTENCE: " + s.toString().toLowerCase() + "\n");

			for (String tag : nerTags) {
				int newIndex = s.toString().toLowerCase().substring(index).indexOf(tag.toLowerCase()) + index;

				// System.out.println("TAG: " + tag);
				// System.out.println("OLD INDEX: " + index);
				// System.out.println("NEW INDEX: " + newIndex);

				if (tag.split(" ").length > 1 && newIndex > index) {

					sentenceWithNER += s.toString().substring(index, newIndex) + String.join("_", tag.split(" "));
					index = sentenceWithNER.length();
				}
			}

			sentenceWithNER += s.toString().substring(index, s.toString().length());

			s = new Sentence(sentenceWithNER);

			// System.out.println("NEW SENTENCE: " + s.toString());

			List<String> words = s.lemmas();
			List<String> validWords = new ArrayList<>();

			for (String word : words)
				if (!stopWords.contains(word.toLowerCase()))
					validWords.add(word);

			filteredSentences.add(new Sentence(validWords));
		}

		return filteredSentences;
	}

	static List<List<Sentence>> filteredDocuments() {
		List<List<Sentence>> filteredDocuments = new ArrayList<>();
		List<String> stopWords = getStopWords();

		for (int i = 1; i < 8; i += 3)
			for (int j = 1; j < 9; j++)
			filteredDocuments.add(filteredSentences(createDocument(readFile("data/C" + i + "/article0" + j + ".txt")), stopWords));

		return filteredDocuments;
	}

	static Term[][] constructMatrix(List<List<Sentence>> documents) {

		// compute basic matrix of terms + count per document

		List<String> uniqueWords = new ArrayList<>();

		for (List<Sentence> sentences : documents)
			for (Sentence s : sentences)
				for (String word : s.words())
					if (!uniqueWords.contains(word))
						uniqueWords.add(word);

		Term[][] documentMatrix = new Term[documents.size()][uniqueWords.size()];

		for (int i = 0; i < documentMatrix.length; i++) {
			for (int j = 0; j < documentMatrix[i].length; j++) {
				String keyword = uniqueWords.get(j);
				int count = 0;

				for (Sentence s : documents.get(i))
					count += Collections.frequency(s.words(), keyword);

				Term t = new Term(uniqueWords.get(j), i, count);

				documentMatrix[i][j] = t;
			}
		}

		// compute term frequency

		for (int i = 0; i < documentMatrix.length; i++) {
			List<String> uniqueWordsForDocument = new ArrayList<>();
			
			for (Sentence s : documents.get(i))
				for (String word : s.words())
					if (!uniqueWordsForDocument.contains(word))
						uniqueWordsForDocument.add(word);

			int totalTermsInDocument = uniqueWordsForDocument.size();

			for (int j = 0; j < documentMatrix[i].length; j++)
				documentMatrix[i][j].frequency = (double) documentMatrix[i][j].count / (double) totalTermsInDocument;
		}

		// compute inverse document frequency

		for (int j = 0; j < documentMatrix[0].length; j++) {
			String keyword = documentMatrix[0][j].word;
			int numberOfDocumentsContainingTerm = 0;

			for (int i = 0; i < documentMatrix.length; i++)
				if (documentMatrix[i][j].count > 0)
					numberOfDocumentsContainingTerm++;

			for (int i = 0; i < documentMatrix.length; i++) {
				documentMatrix[i][j].inverseDocumentFrequency = Math.log10((double) documentMatrix.length / (double) numberOfDocumentsContainingTerm);
			}
		}

		// compute TD-IDF score

		for (int i = 0; i < documentMatrix.length; i++)
			for (int j = 0; j < documentMatrix[i].length; j++)
				documentMatrix[i][j].computeWeight();

		return documentMatrix;
	}

	// dat bubble sort

	static void swap(Term[][] documentMatrix, int i, int j, int k) {
		Term temp = documentMatrix[i][j];
		documentMatrix[i][j] = documentMatrix[i][k];
		documentMatrix[i][k] = temp;
	}

	static void transformByTDIDF(Term[][] documentMatrix) {
		for (int i = 0; i < documentMatrix.length; i++)
			for (int j = 0; j < documentMatrix[i].length - 1; j++)
				for (int k = j + 1; k < documentMatrix[i].length; k++)
					if (documentMatrix[i][j].inverseDocumentFrequency > documentMatrix[i][k].inverseDocumentFrequency)
						swap(documentMatrix, i, j, k);
	}

	static Term[][] flipMatrix(Term[][] documentMatrix) {
		Term[][] flippedMatrix = new Term[documentMatrix[0].length][documentMatrix.length];

		for (int i = 0; i < documentMatrix.length; i++)
			for (int j = 0; j < documentMatrix[i].length; j++)
				flippedMatrix[j][i] = documentMatrix[i][j];

		return flippedMatrix;
	}

	static void printMatrix(Term[][] documentMatrix) {
		for (int i = 0; i < documentMatrix.length; i++) {
			System.out.println("\n\nDocument " + i + "\n==========\n");

			for (int j = 0; j < documentMatrix[i].length; j++)
				System.out.print(documentMatrix[i][j].inverseDocumentFrequency + " ");
		}
	}

	static int[][] kMeansCluster(Term[][] allDocumentMatrix, int k) {
		int[][] clusterMatrix = new int[allDocumentMatrix[0].length][k];

		for (int i = 0; i < clusterMatrix.length; i++)
			for (int j = 0; j < clusterMatrix[i].length; j++)
				clusterMatrix[i][j] = -1;

		// first pass through original matrix and initial assignment to clusters

		for (int i = 0; i < allDocumentMatrix[0].length; i++) {
			double mean = getDocumentMean(allDocumentMatrix, i);
			double[] clusterMeans = getClusterMeans(allDocumentMatrix, clusterMatrix, i);
			int closestCluster = getClosestCluster(allDocumentMatrix, clusterMeans, mean);
			clusterMatrix[i][closestCluster] = i;

			// remove from old cluster

			for (int j = 0; j < clusterMeans.length; j++) 
				if (j != closestCluster && clusterMatrix[i][j] != -1)
					clusterMatrix[i][j] = -1;
		}

		// second pass through cluster matrix to constantly update

		boolean updates = true;
		int updateCap = 0;

		while (updates && updateCap < 1000) {
			updates = false;

			for (int i = 0; i < clusterMatrix[0].length; i++) {
				for (int j = 0; j < clusterMatrix.length; j++) {
					if (clusterMatrix[j][i] != -1) {
						double mean = getDocumentMean(allDocumentMatrix, clusterMatrix[j][i]);
						double[] clusterMeans = getClusterMeans(allDocumentMatrix, clusterMatrix, clusterMatrix[j][i]);
						int closestCluster = getClosestCluster(allDocumentMatrix, clusterMeans, mean);

						// swap if better cluster found

						if (closestCluster != i) {
							// System.out.println("Swapping document " + clusterMatrix[j][i] + " from " + i + " to " + closestCluster);

							clusterMatrix[clusterMatrix[j][i]][closestCluster] = clusterMatrix[j][i];
							clusterMatrix[clusterMatrix[j][i]][i] = -1;
							updates = true;
						}
					}
				}
			}

			updateCap++;
		}

		return clusterMatrix;
	}

	static double getDocumentMean(Term[][] allDocumentMatrix, int document) {
		double totalTDIDF = 0;

		for (int i = 0; i < allDocumentMatrix.length; i++)			
			totalTDIDF += allDocumentMatrix[i][document].inverseDocumentFrequency;

		return totalTDIDF / allDocumentMatrix.length;
	}

	static double[] getClusterMeans(Term[][] allDocumentMatrix, int[][] clusterMatrix, int document) {
		double[] clusterMeans = new double[clusterMatrix[0].length];

		for (int i = 0; i < clusterMatrix[0].length; i++) {
			double clusterTDIDF = 0;
			int documentIncluded = 0;
		
			for (int j = 0; j < clusterMatrix.length; j++) {
				if (clusterMatrix[j][i] == document) {
					documentIncluded = 1;
					continue;
				}

				if (clusterMatrix[j][i] != -1)
					for (int k = 0; k < allDocumentMatrix.length; k++)
						clusterTDIDF += allDocumentMatrix[k][clusterMatrix[j][i]].inverseDocumentFrequency;
			}

			clusterMeans[i] = clusterTDIDF / (clusterMatrix[i].length - documentIncluded);
		}

		return clusterMeans;
	}

	static int getClosestCluster(Term[][] allDocumentMatrix, double[] clusterMeans, double mean) {
		double closestDistance = Double.MAX_VALUE;
		int closestCluster = -1;

		for (int i = 0; i < clusterMeans.length; i++) {
			double distance = euclideanDistance(mean, clusterMeans[i]);
			// double distance = cosineDistance(mean, clusterMeans[i]);

			if (distance < closestDistance) {
				closestDistance = distance;
				closestCluster = i;
			}
		}

		return closestCluster;
	}

	static double euclideanDistance(double pointA, double pointB) {
		return Math.pow(Math.pow(pointA, 2) + Math.pow(pointB, 2), 0.5);
	}

	static double cosineDistance(double pointA, double pointB) {
		return (pointA * pointB) / (Math.pow(Math.pow(pointA, 2), 0.5) * Math.pow(Math.pow(pointB, 2), 0.5));
	}


}