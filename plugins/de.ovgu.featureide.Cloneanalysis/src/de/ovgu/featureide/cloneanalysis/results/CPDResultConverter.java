/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2019  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part odify
 * it under the terms of the GNU Lesser General Public License as published ither version 3 of the License, or
 * (at your option) any later version.
 *
 * FeatureIDE is distributed in the hope that it wi
FeatureIDE.
 *
 * FeatureIDE is free software: you can redistribute it and/or moll be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.cloneanalysis.results;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

import de.ovgu.featureide.cloneanalysis.impl.CloneOccurence;
import de.ovgu.featureide.cloneanalysis.utils.CloneAnalysisUtils;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.TokenEntry;

/**
 * CPD is optimized for speed and badly documented, which sadly results in badly formatted results.
 * 
 * This class handles the conversion of CPDs results into a better format.
 * 
 * @see CloneAnalysisResults
 * @see Clone
 * 
 * @author Konstantin Tonscheidt
 * 
 */
public class CPDResultConverter {

	/**
	 * Creates an Instance of {@link CloneAnalysisResults} and fills it with information gained from the matches found by CPD.
	 * 
	 * @param matchesFound matches found by CPD
	 * @return instance of {@link CloneAnalysisResults}
	 */
	public static CloneAnalysisResults<VariantAwareClone> convertMatchesToReadableResults(Iterator<Match> matchesFound) {
		Set<VariantAwareClone> clones = new HashSet<VariantAwareClone>();
		while (matchesFound.hasNext()) {
			final VariantAwareClone clone = convertMatchToClone(matchesFound.next());
			clones.add(clone);
		}

		CloneAnalysisResults<VariantAwareClone> results = new CloneAnalysisResults<VariantAwareClone>(clones);

		Set<FeatureRootLocation> relevantFeatures = CloneAnalysisUtils.getRelevantFeatures(results);

		results.setRelevantFeatures(relevantFeatures);
		results.setPercentageData(calculateClonedAmountPercentage(results));

		return results;
	}

	private static IClonePercentageData calculateClonedAmountPercentage(CloneAnalysisResults<VariantAwareClone> results) {
		ClonePercentageData clonePercentageData = new ClonePercentageData();
		final Set<FeatureRootLocation> relevantFeatures = results.getRelevantFeatures();
		final Map<FeatureRootLocation, Map<IFile, short[]>> featureClonedLinesPerFile = new HashMap<FeatureRootLocation, Map<IFile, short[]>>();

		for (FeatureRootLocation feature : relevantFeatures) {
			clonePercentageData.setFeatureTotalLineCount(feature, CloneAnalysisUtils.getMemberLineSum(feature.getLocation()));
			clonePercentageData.setFeatureTotalCloneLength(feature, CloneAnalysisUtils.getClonedLineCount(feature, results.getClones()));
			featureClonedLinesPerFile.put(feature, CloneAnalysisUtils.getEmptyMemberLinesMap(feature));
		}
		CloneAnalysisUtils.calculateClonedLines(featureClonedLinesPerFile, relevantFeatures, results);
		clonePercentageData.setFeatureClonedLinesPerFile(featureClonedLinesPerFile);

		return clonePercentageData;
	}

	/**
	 * Convenience Function calling {@link #checkForIntervariance(Set)}.
	 * 
	 * @see #checkForIntervariance(Set)
	 */
	/**
	 * Convenience Function calling {@link #checkForIntervariance(Set)}.
	 * 
	 * @param clone clone
	 * @return true if the clone is intervariant, false else.
	 */
	public static boolean checkForIntervariance(Clone clone) {
		return checkForIntervariance(clone.getOccurences());
	}

	/**
	 * Checks the clones occurences. If occurences exist in different projects or, in the case of FeatureProjects, in different feature folders, the clone is
	 * intervariant.
	 * 
	 * @param occurences the complete Set of the clones occurences.
	 * @return true if the clone is intervariant, false else.
	 */
	public static boolean checkForIntervariance(Set<CloneOccurence> occurences) {
		return false;
	}

	/**
	 * Creates a {@link Clone} object and fills it with information taken from the given {@link Match}.
	 * 
	 * @see Clone clone
	 * @see Match
	 * 
	 * @param match information as matches
	 * @return instance of {@link Clone}
	 */
	public static VariantAwareClone convertMatchToClone(Match match) {
		final int cloneLineCount = match.getLineCount();
		final int tokenCount = match.getTokenCount();
		final Set<CloneOccurence> occurences = new HashSet<CloneOccurence>();
		final Set<TokenEntry> tokens = match.getMarkSet();

		// System.out.println("Printing tokens");
		// System.out.println(match.getSourceCodeSlice());
		for (TokenEntry token : tokens) {
			if (token.getTokenSrcID() == null || token.getTokenSrcID().isEmpty()) {
				System.out.println("empty sourceid");
				continue;
			}
			CloneOccurence snippet = new CloneOccurence(token.getTokenSrcID(), token.getBeginLine());
			occurences.add(snippet);
		}

		final int fileCount = countFiles(occurences);

		final VariantAwareClone variantAwareClone = new VariantAwareClone(occurences, cloneLineCount, tokenCount, fileCount, match.getSourceCodeSlice());

		for (CloneOccurence occurence : variantAwareClone.getOccurences())
			occurence.setClone(variantAwareClone);

		return variantAwareClone;

	}

	/**
	 * 
	 * @param occurences Set of occurences
	 * @return the number of different files in which the Snippet occurs.
	 */
	public static int countFiles(Set<CloneOccurence> occurences) {
		Set<IPath> files = new HashSet<IPath>();
		for (CloneOccurence snippet : occurences)
			files.add(snippet.getFile());

		final int count = files.size();

		assert count > 0 : "Must be contained in one file at least.";

		return count;
	}

}
