/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2017  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 *
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.ui.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import de.ovgu.featureide.core.CorePlugin;
import de.ovgu.featureide.core.IFeatureProject;
import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.ui.UIPlugin;

/**
 * Context Completion
 *
 * @author Reimar Schr�ter
 * @author Marcus Pinnecke
 */
@SuppressWarnings("restriction")
public class AnnoCompletion implements IJavaCompletionProposalComputer {

	/**
	 *
	 */
	private static final Image FEATURE_ICON = UIPlugin.getImage("FeatureIconSmall.ico");

	public AnnoCompletion() {}

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext arg0, IProgressMonitor arg1) {
		return Collections.emptyList();
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionEnded() {

	}

	@Override
	public void sessionStarted() {}

	public List<CompletionProposal> getComplForFeatures(final IFeatureProject featureProject, final CharSequence prefix) {
		final LinkedList<CompletionProposal> ret_List = new LinkedList<CompletionProposal>();

		final Iterable<String> featureNames = FeatureUtils.getConcreteFeatureNames(featureProject.getFeatureModel());
		for (final String string : featureNames) {
			CompletionProposal pr = null;
			pr = CompletionProposal.create(CompletionProposal.LABEL_REF, prefix.length());
			pr.setName(string.toCharArray());
			pr.setCompletion(string.toCharArray());

			if (string.startsWith(prefix.toString())) {
				ret_List.add(pr);
			}
		}
		return ret_List;
	}

	public List<CompletionProposal> getComplForAnnotations(final CharSequence prefix) {
		final LinkedList<CompletionProposal> completionProposalList = new LinkedList<CompletionProposal>();

		final Iterable<String> directives = Directives.getAllDirectives();
		for (final String string : directives) {
			CompletionProposal pr = null;
			pr = CompletionProposal.create(CompletionProposal.LABEL_REF, prefix.length());
			pr.setName(string.toCharArray());
			pr.setCompletion(string.toCharArray());

			if (string.startsWith(prefix.toString())) {
				completionProposalList.add(pr);
			}
		}
		return completionProposalList;
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext arg0, IProgressMonitor arg1) {

		JavaContentAssistInvocationContext context = null;
		if (arg0 instanceof JavaContentAssistInvocationContext) {
			context = (JavaContentAssistInvocationContext) arg0;
		}

		final IFile file =
			((IFileEditorInput) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput()).getFile();
		final IFeatureProject featureProject = CorePlugin.getFeatureProject(file);

		if ((context == null) || (file == null) || (featureProject == null)) {
			return Collections.emptyList();
		}

		if (isContextValid(context) == Status.ShowNothing) {
			return Collections.emptyList();
		}

		CharSequence prefix = "";
		try {
			prefix = context.computeIdentifierPrefix();
		} catch (final BadLocationException e) {
			e.printStackTrace();
		}

		final List<CompletionProposal> completionProp = getComplForFeatures(featureProject, prefix);
		final List<CompletionProposal> completionDirectives = getComplForAnnotations(prefix);

		final ArrayList<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
		if (isContextValid(context) == Status.ShowFeatures) {
			for (final CompletionProposal prop : completionProp) {

				final LazyJavaCompletionProposal curFeature = new LazyJavaCompletionProposal(prop, context);
				curFeature.setImage(FEATURE_ICON);
				// curFeature.setReplacementLength(prop.getCompletion().length - prefix.length());
				curFeature.setReplacementString(new String(prop.getCompletion()).replace(prefix, ""));
				curFeature.setReplacementOffset(context.getInvocationOffset());

				list.add(curFeature);
			}
		} else if (isContextValid(context) == Status.ShowDirectives) {
			for (final CompletionProposal prop : completionDirectives) {

				final LazyJavaCompletionProposal curAnnotation = new LazyJavaCompletionProposal(prop, context);
				// curFeature.setReplacementLength(prop.getCompletion().length - prefix.length());
				curAnnotation.setReplacementString(new String(prop.getCompletion()).replace("#" + prefix, ""));
				curAnnotation.setReplacementOffset(context.getInvocationOffset());

				list.add(curAnnotation);
			}
		}
		return list;
	}

	private boolean hasDirectives(String lineContent) {

		final List<String> allDirectives = Directives.getAllDirectives();
		for (final String div : allDirectives) {
			if (lineContent.contains(div)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param context
	 */
	private Status isContextValid(JavaContentAssistInvocationContext context) {
		Status status = Status.ShowNothing;
		try {
			final int line = context.getDocument().getLineOfOffset(context.getInvocationOffset());
			final int offsetOfLine = context.getDocument().getLineOffset(line);
			final int lineLength = context.getDocument().getLineLength(line);
			final String lineContent = context.getDocument().get(offsetOfLine, lineLength);
			final boolean startsWithHashTag = lineContent.trim().substring(2).trim().contains("#");
			final boolean hasDirectives = hasDirectives(lineContent);
			final boolean showFeaturesAfterDirectives = lineContent.contains("#if");
			if (startsWithHashTag && !hasDirectives) {
				status = Status.ShowDirectives;
			} else {
				status = Status.ShowNothing;
			}
			if (showFeaturesAfterDirectives) {
				status = Status.ShowFeatures;
			}

		} catch (final BadLocationException e1) {
			e1.printStackTrace();
		}
		return status;
	}

	public enum Status {
		ShowFeatures, ShowDirectives, ShowNothing
	}
}
