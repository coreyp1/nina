package edu.nd.nina.io;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import edu.nd.nina.DirectedGraph;
import edu.nd.nina.Graph;
import edu.nd.nina.Type;
import edu.nd.nina.UndirectedGraph;
import edu.nd.nina.alg.CalculateStatistics;
import edu.nd.nina.alg.StatVal;
import edu.nd.nina.graph.DirectedSubgraph;
import edu.nd.nina.graph.TypedSimpleGraph;
import edu.nd.nina.graph.UndirectedSubgraph;
import edu.nd.nina.structs.Pair;

public class PrintStatistics {

	private static Logger logger = Logger.getLogger(PrintStatistics.class
			.getName());

	public static <V extends Comparable<V>, E> void PrintGraphStatTable(
			final Graph<V, E> graph, String filename) {
		PrintGraphStatTable(graph, filename, "");
	}

	public static <V extends Comparable<V>, E> void PrintTypedGraphStatTable(
			final Graph<V, E> graph, String fileName, String desc) {
		PrintWriter pw = new PrintWriter(System.out);
		if (!fileName.isEmpty()) {
			try {
				pw = new PrintWriter(String.format("%s.html", fileName));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		logger.info("Begin print TypedGraph stat table");

		Set<Class<?>> types = graph.getTypes();

		logger.info("Processing full graph");

		computeStatistics(graph, pw, "Full Graph");

		int i = 0;
		for (Class<?> t1 : types) {
			i++;
			int j = 0;
			for (Class<?> t2 : types) {
				j++;
				if (i < j)
					continue;

				Set<V> v_t = new HashSet<V>();

				logger.info("Processing " + t1.getSimpleName() + " - "
						+ t2.getSimpleName());

				v_t.addAll(graph.getAllMatchingType(t1));
				v_t.addAll(graph.getAllMatchingType(t2));

				Graph<V, E> subgraph;
				if (graph instanceof UndirectedGraph) {
					subgraph = new UndirectedSubgraph<V, E>(
							(UndirectedGraph<V, E>) graph, v_t, null);
				} else {
					subgraph = new DirectedSubgraph<V, E>(
							(DirectedGraph<V, E>) graph, v_t, null);
				}

				computeStatistics(subgraph, pw,
						t1.getSimpleName() + t2.getSimpleName());

			}
		}

		pw.close();
	}

	public static <V extends Comparable<V>, E> void PrintGraphStatTable(
			final Graph<V, E> graph, String fileName, String desc) {

		PrintWriter pw = new PrintWriter(System.out);
		if (!fileName.isEmpty()) {
			try {
				pw = new PrintWriter(String.format("%s.html", fileName));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		computeStatistics(graph, pw, desc);

		pw.close();
	}

	private static <V extends Comparable<V>, E> void computeStatistics(
			Graph<V, E> graph, PrintWriter pw, String desc) {

		Hashtable<StatVal, Float> valStatH = new Hashtable<StatVal, Float>();
		Hashtable<StatVal, Vector<Pair<Float, Float>>> distrStatH = new Hashtable<StatVal, Vector<Pair<Float, Float>>>();

		CalculateStatistics.calcBasicStat(graph, false, valStatH);
		// diameter
		CalculateStatistics.calcDiameter(graph, 10, valStatH, distrStatH);
		// degrees
		CalculateStatistics.calcDegreeDistribution(graph, distrStatH);
		// components
		CalculateStatistics.calcConnectedComponents(graph, distrStatH);
		// clustering coefficient
		CalculateStatistics.calcClusteringCoefficient(graph, 10, valStatH);

		// CalculateStatistics.calcTriangleParticipation(graph, distrStatH);

		print(valStatH, distrStatH, pw, desc);
	}

	private static <V extends Comparable<V>, E> void print(
			Hashtable<StatVal, Float> valStatH,
			Hashtable<StatVal, Vector<Pair<Float, Float>>> distrStatH,
			PrintWriter pw, String desc) {
		pw.printf("\n");
		pw.printf("<table id=\"datatab\" summary=\"Dataset statistics\">\n");
		pw.printf("  <tr> <th colspan=\"2\">Dataset statistics " + desc
				+ "</th> </tr>\n");
		for (Entry<StatVal, Float> e : valStatH.entrySet()) {
			pw.printf("  <tr><td>%s</td> <td>%.4f</td></tr>\n", e.getKey()
					.toString(), e.getValue());
		}
		for (Entry<StatVal, Vector<Pair<Float, Float>>> e : distrStatH
				.entrySet()) {
			pw.printf("  <tr><td>%s</td> <td>", e.getKey());
			for (Pair<Float, Float> p : e.getValue()) {
				pw.printf("%.4f - %.4f<br/>", p.p1, p.p2);
			}
			pw.printf("</td></tr>\n");

		}

		pw.printf("</table>\n");
		pw.flush();
	}

	public static <V extends Type, E> void PrintCrazyCCF(
			TypedSimpleGraph graph, String fileName, String desc) {
		PrintWriter pw = new PrintWriter(System.out);
		if (!fileName.isEmpty()) {
			try {
				pw = new PrintWriter(String.format("%s.html", fileName));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		Hashtable<StatVal, Vector<Pair<Float, Float>>> distrStatH = new Hashtable<StatVal, Vector<Pair<Float, Float>>>();

		logger.info("Begin print TypedGraph stat table");

		Set<Class<?>> types = graph.getTypes();

		logger.info("Processing full graph");
		
		int i = 0;
		for (Class<?> t1 : types) {
			i++;
			int j = 0;
			for (Class<?> t2 : types) {
				j++;
	//			if (i < j)
//					continue;

				Set<Type> v_t = new HashSet<Type>();

				logger.info("Processing " + t1.getSimpleName() + " - "
						+ t2.getSimpleName());

				v_t.addAll(graph.getAllMatchingType(t1));
				v_t.addAll(graph.getAllMatchingType(t2));

				Graph<Type, E> subgraph;
				if (graph instanceof UndirectedGraph) {
					subgraph = new UndirectedSubgraph<Type, E>(
							(UndirectedGraph<Type, E>) graph, v_t, null);
				} else {
					subgraph = new DirectedSubgraph<Type, E>(
							(DirectedGraph<Type, E>) graph, v_t, null);
				}

				CalculateStatistics.calcJaccardCoefficient(subgraph, 1000, t1, distrStatH);

				print(new Hashtable<StatVal, Float>(), distrStatH, pw, t1.getSimpleName() + " - "
						+ t2.getSimpleName());
				
			}
		}

		pw.close();
	}
	
	
	public static <V extends Type, E> void PrintCrazyAssortativity(
			TypedSimpleGraph graph, String fileName, String desc) {
		PrintWriter pw = new PrintWriter(System.out);
		if (!fileName.isEmpty()) {
			try {
				pw = new PrintWriter(String.format("%s.html", fileName));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		Hashtable<StatVal, Vector<Pair<Float, Float>>> distrStatH = new Hashtable<StatVal, Vector<Pair<Float, Float>>>();

		logger.info("Begin print TypedGraph stat table");

		Set<Class<?>> types = graph.getTypes();

		logger.info("Processing full graph");
		
		int i = 0;
		for (Class<?> t1 : types) {
			i++;
			int j = 0;
			for (Class<?> t2 : types) {
				j++;
	//			if (i < j)
//					continue;

				Set<Type> v_t = new HashSet<Type>();

				logger.info("Processing " + t1.getSimpleName() + " - "
						+ t2.getSimpleName());

				v_t.addAll(graph.getAllMatchingType(t1));
				v_t.addAll(graph.getAllMatchingType(t2));

				Graph<Type, E> subgraph;
				if (graph instanceof UndirectedGraph) {
					subgraph = new UndirectedSubgraph<Type, E>(
							(UndirectedGraph<Type, E>) graph, v_t, null);
				} else {
					subgraph = new DirectedSubgraph<Type, E>(
							(DirectedGraph<Type, E>) graph, v_t, null);
				}

				CalculateStatistics.calcJaccardAssortativity(subgraph, 1000, t1, distrStatH);

				print(new Hashtable<StatVal, Float>(), distrStatH, pw, t1.getSimpleName() + " - "
						+ t2.getSimpleName());
				
			}
		}

		pw.close();
	}
	
}
