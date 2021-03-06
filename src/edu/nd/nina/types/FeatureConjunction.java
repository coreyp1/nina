/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

/** 
 @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package edu.nd.nina.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class FeatureConjunction {
	private static Logger logger = Logger.getLogger(FeatureConjunction.class
			.getName());

	static private final String conjunctionString = "_&_";
	static private final String negationString = "!";
	static private final Pattern conjunctionPattern = Pattern
			.compile(conjunctionString);

	String name;
	Alphabet dictionary;
	int[] features;
	boolean[] negations; // true here means Feature must be present
	int index = -1; // -1 if this conjunction isn't yet part of Alphabet

	/**
	 * If negations[i] is true, insist that the feature has non-zero value; if
	 * false, insist that it has zero value. Note: Does not check to make sure
	 * that it hasn't already been added. If negations[] is null, then assume
	 * all negations[i] are true.
	 */
	public FeatureConjunction(String name, Alphabet dictionary, int[] features,
			boolean[] negations, boolean checkSorted, boolean copyFeatures,
			boolean copyNegations) {
		assert (negations == null || features.length == negations.length);
		this.dictionary = dictionary;
		if (copyFeatures) {
			this.features = new int[features.length];
			System.arraycopy(features, 0, this.features, 0, features.length);
		} else {
			this.features = features;
		}
		if (copyNegations && negations != null) {
			this.negations = new boolean[negations.length];
			System.arraycopy(negations, 0, this.negations, 0, negations.length);
		} else {
			this.negations = negations;
		}
		if (checkSorted) {
			for (int i = this.features.length - 1; i >= 0; i--) {
				boolean swapped = false;
				for (int j = 0; j < i; j++) {
					if (features[i - 1] > features[i]) {
						int tmpf = this.features[i];
						this.features[i] = this.features[i - 1];
						this.features[i - 1] = tmpf;
						if (negations != null) {
							boolean tmpb = this.negations[i];
							this.negations[i] = this.negations[i - 1];
							this.negations[i - 1] = tmpb;
						}
						swapped = true;
					} else if (features[i - 1] == features[i])
						throw new IllegalArgumentException(
								"Same Feature cannot occur twice.");
				}
				if (!swapped)
					break;
			}
		}
		if (name != null)
			this.name = name;
		else {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < this.features.length; i++) {
				if (negations != null && this.negations[i] == false)
					sb.append(negationString);
				if (i > 0)
					sb.append(conjunctionString);
				sb.append(dictionary.lookupObject(features[i]).toString());
			}
			// Shouldn't sb.toString() be saved in this.name here? -akm 1/08
		}
	}

	public FeatureConjunction(String name, Alphabet dictionary, int[] features,
			boolean[] negations, boolean checkSorted) {
		this(name, dictionary, features, negations, checkSorted, true, true);
	}

	public FeatureConjunction(String name, Alphabet dictionary, int[] features,
			boolean[] negations) {
		this(name, dictionary, features, negations, true);
	}

	public static boolean isValidConjunction(int[] features) {
		for (int i = 1; i < features.length; i++)
			if (features[i - 1] >= features[i])
				return false;
		return true;
	}

	// Always in "Alphabet index" order
	// xxx This one doesn't check for duplicates among sub-constituents in the
	// conjunction, as
	// the next method does.
	public static String getName(Alphabet dictionary, int[] features,
			boolean[] negations) {
		// if (true) {
		if (negations != null)
			for (int i = 0; i < negations.length; i++)
				if (negations[i])
					throw new UnsupportedOperationException(
							"Doesn't yet check for sub-duplicates with negations.");
		return getName(dictionary, features);
		// }

		// Split apart any feature[i] that is itself a conjunction feature
		// int[] featureIndices = getFeatureIndices (dictionary,
		// dictionary.lookupObject(
		// xxx Add code here to do the sorting...
		// Make sure the the features area sorted
		/*
		 * for (int i = 1; i < features.length; i++) if (features[i-1] >=
		 * features[i]) throw new IllegalArgumentException
		 * ("feature index not sorted, or contains duplicate"); StringBuffer sb
		 * = new StringBuffer (); for (int i = 0; i < features.length; i++) { if
		 * (i > 0) sb.append (conjunctionString); if (negations != null &&
		 * negations[i]) sb.append (negationString); sb.append
		 * (dictionary.lookupObject(features[i]).toString()); }
		 * 
		 * return sb.toString();
		 */
	}

	// Always in "Alphabet index" order
	public static String getName(Alphabet dictionary, int[] features) {
		// Split apart any feature[i] that is itself a conjunction feature
		for (int i = 0; i < features.length; i++) {
			int[] featureIndices = getFeatureIndices(dictionary,
					(String) dictionary.lookupObject(features[i]));
			if (featureIndices.length > 1) {
				int newLength = features.length - 1 + featureIndices.length;
				int[] newFeatures = new int[newLength];
				int n = 0;
				for (int j = 0; j < i; j++)
					newFeatures[n++] = features[j];
				for (int j = 0; j < featureIndices.length; j++)
					newFeatures[n++] = featureIndices[j];
				for (int j = i + 1; j < features.length; j++)
					newFeatures[n++] = features[j];
				Arrays.sort(newFeatures);
				return getName(dictionary, newFeatures);
			}
		}
		// xxx Add code here to do the sorting...
		// Make sure the the features area sorted, and remove any duplicates
		for (int i = 1; i < features.length; i++) {
			if (features[i - 1] == features[i]) {
				// Remove duplicate and try again
				int[] newFeatures = new int[features.length - 1];
				int n = 0;
				for (int j = 0; j < i; j++)
					newFeatures[n++] = features[j];
				for (int j = i + 1; j < features.length; j++)
					newFeatures[n++] = features[j];
				return getName(dictionary, newFeatures);
			}
			if (features[i - 1] > features[i])
				throw new IllegalArgumentException("feature indices not sorted");
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < features.length; i++) {
			if (i > 0)
				sb.append(conjunctionString);
			sb.append(dictionary.lookupObject(features[i]).toString());
		}
		return sb.toString();
	}

	public static boolean featuresOverlap(Alphabet dictionary, int feature1,
			int feature2) {
		if (feature1 == feature2)
			return true;
		int[] fis1 = getFeatureIndices(dictionary,
				(String) dictionary.lookupObject(feature1));
		int[] fis2 = getFeatureIndices(dictionary,
				(String) dictionary.lookupObject(feature2));
		for (int i = 0, j = 0; i < fis1.length; i++) {
			assert (i >= fis1.length - 2 || fis1[i] < fis1[i + 1]);
			assert (j >= fis2.length - 2 || fis2[j] < fis2[j + 1]);
			while (fis2[j] < fis1[i] && j < fis2.length - 1)
				j++;
			if (fis1[i] == fis2[j])
				return true;
		}
		return false;
	}

	// Always in "Alphabet index" order
	public static String getName(Alphabet dictionary, int feature1, int feature2) {
		if (feature1 < feature2)
			return getName(dictionary, new int[] { feature1, feature2 });
		else
			return getName(dictionary, new int[] { feature2, feature1 });
		// assert (feature1 != feature2);
		// String string1 = dictionary.lookupObject(feature1).toString();
		// String string2 = dictionary.lookupObject(feature2).toString();
		// if (feature1 < feature2)
		// return string1 + conjunctionString + string2;
		// else
		// return string2 + conjunctionString + string1;
	}

	public static int[] getFeatureIndices(Alphabet dictionary,
			String featureConjunctionName) {
		String[] featureNames = conjunctionPattern
				.split(featureConjunctionName);
		int[] ret = new int[featureNames.length];
		for (int i = 0; i < featureNames.length; i++) {
			assert (!featureNames[i].startsWith(negationString));
			ret[i] = dictionary.lookupIndex(featureNames[i], false);

			logger.fine(i + "th feature: " + featureNames[i] + " in "
					+ featureConjunctionName);

			assert (ret[i] != -1) : "Couldn't find index for " + i
					+ "th feature: " + featureNames[i] + " in "
					+ featureConjunctionName;
		}
		java.util.Arrays.sort(ret);
		return ret;
	}

	public FeatureConjunction(Alphabet dictionary, int[] features,
			boolean[] negations) {
		this(getName(dictionary, features, negations), dictionary, features,
				negations, true);
	}

	public FeatureConjunction(Alphabet dictionary, int[] features) {
		this(getName(dictionary, features, null), dictionary, features, null,
				true, true, false);
	}

	public boolean satisfiedBy(FeatureVector fv) {
		if (fv.getAlphabet() != dictionary)
			throw new IllegalArgumentException("Vocabularies do not match.");
		int fvsize = fv.numLocations();
		int fvl = 0;
		for (int fcl = 0; fcl < features.length; fcl++) {
			int fcli = features[fcl];
			while (fvl < fvsize && fv.indexAtLocation(fvl) < fcli)
				fvl++;
			if (fvl < fvsize && fv.indexAtLocation(fvl) == fcli
					&& fv.valueAtLocation(fvl) != 0) {
				// The fcli'th Feature of the FeatureConjunction is present in
				// the FeatureVector
				if (negations != null && negations[fcl] == false)
					// but this Feature was negated in the FeatureConjunction,
					// so not satisfied
					return false;
			} else if (negations == null || negations[fcl] == true)
				// The fcli'th Feature of the FeatureConjunction is not present
				// in the FeatureVector
				// and this Feature was unnegated in the FeatureConjunction, so
				// not satisfied
				return false;
		}
		return true;
	}

	public int getIndex() {
		return index;
	}

	public void addTo(AugmentableFeatureVector fv, double value,
			FeatureSelection fs) {
		// xxx This could be simplified for the special case of a
		// FeatureConjunction with only one conjunct
		if (this.satisfiedBy(fv)) {
			index = fv.getAlphabet().lookupIndex(name);
			// Make sure that this feature is selected
			if (fs != null)
				fs.add(index);
			if (index >= 0 && fv.value(index) > 0)
				// Don't add features that are already there
				return;
			assert (index != -1);
			fv.add(index, value);
		}
	}

	public void addTo(AugmentableFeatureVector fv, double value) {
		addTo(fv, value, null);
	}

	public void addTo(AugmentableFeatureVector fv) {
		this.addTo(fv, 1.0);
	}

	public static class List {
		ArrayList<FeatureConjunction> conjunctions;

		public List() {
			this.conjunctions = new ArrayList<FeatureConjunction>();
		}

		public int size() {
			return conjunctions.size();
		}

		public FeatureConjunction get(int i) {
			return (FeatureConjunction) conjunctions.get(i);
		}

		public void add(FeatureConjunction fc) {
			if (conjunctions.size() > 0
					&& fc.dictionary != conjunctions.get(0).dictionary)
				throw new IllegalArgumentException("Alphabet does not match.");
			conjunctions.add(fc);
		}

		public void addTo(AugmentableFeatureVector fv, double value,
				FeatureSelection fs) {
			// xxx Make this more efficient
			for (int i = 0; i < conjunctions.size(); i++)
				((FeatureConjunction) conjunctions.get(i)).addTo(fv, value, fs);
		}

		public void addTo(AugmentableFeatureVector fv, double value) {
			addTo(fv, value, null);
		}
	}
}
