/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2019  FeatureIDE team, University of Magdeburg, Germany
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
package de.ovgu.featureide.fm.ui.handlers;

import java.nio.file.Path;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.EclipseFileSystem;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelFormat;
import de.ovgu.featureide.fm.core.job.IJob;
import de.ovgu.featureide.fm.core.job.IRunner;
import de.ovgu.featureide.fm.core.job.LongRunningMethod;
import de.ovgu.featureide.fm.core.job.LongRunningWrapper;
import de.ovgu.featureide.fm.core.job.SliceFeatureModel;
import de.ovgu.featureide.fm.core.job.util.JobFinishListener;
import de.ovgu.featureide.fm.ui.handlers.base.AFileHandler;
import de.ovgu.featureide.fm.ui.wizards.AbstractWizard;
import de.ovgu.featureide.fm.ui.wizards.FeatureModelSlicingWizard;
import de.ovgu.featureide.fm.ui.wizards.WizardConstants;

public class FeatureModelSlicingHandler extends AFileHandler {

	@SuppressWarnings("unchecked")
	@Override
	protected void singleAction(final IFile file) {
		final FeatureModelManager manager = FeatureModelManager.getInstance(EclipseFileSystem.getPath(file.getProject()));
		final IFeatureModel featureModel = manager.getObject();
		if (featureModel != null) {
			final AbstractWizard wizard = new FeatureModelSlicingWizard("Feature-Model Slicing");
			wizard.putData(WizardConstants.KEY_IN_FEATUREMODEL, featureModel);
			if (Window.OK == new WizardDialog(Display.getCurrent().getActiveShell(), wizard).open()) {
				final Collection<String> selectedFeatures = (Collection<String>) wizard.getData(WizardConstants.KEY_OUT_FEATURES);
				final LongRunningMethod<IFeatureModel> method = new SliceFeatureModel(featureModel, selectedFeatures, true);

				final IRunner<IFeatureModel> runner = LongRunningWrapper.getRunner(method, "Slicing Feature Model");
				runner.addJobFinishedListener(new JobFinishListener<IFeatureModel>() {

					@Override
					public void jobFinished(IJob<IFeatureModel> finishedJob) {
						final Path modelFile = EclipseFileSystem.getPath(file);
						final Path filePath = modelFile.getFileName();
						final Path root = modelFile.getRoot();
						if ((filePath != null) && (root != null)) {
							String fileName = filePath.toString();
							final int extIndex = fileName.lastIndexOf('.');
							final XmlFeatureModelFormat format = new XmlFeatureModelFormat();
							fileName = ((extIndex > 0) ? fileName.substring(0, extIndex) : fileName) + "_sliced_" + System.currentTimeMillis() + "."
								+ format.getSuffix();
							FeatureModelManager.save(featureModel, root.resolve(modelFile.subpath(0, modelFile.getNameCount() - 1)).resolve(fileName), format);
						}
					}
				});
				runner.schedule();
			}
		}

	}

}
