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
package de.ovgu.featureide.ui.mpl;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import de.ovgu.featureide.core.mpl.MPLPlugin;

/**
 * A decorator for MPL projects.
 *
 * @author Sebastian Krieter
 */
public class InterfaceProjectDecorator implements ILightweightLabelDecorator {

	private static final ImageDescriptor OVERLAY = MPLUIPlugin.getDefault().getImageDescriptor("icons/FeatureProjectDecorator.png");

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (MPLPlugin.getDefault().isInterfaceProject((IProject) element)) {
			decoration.addOverlay(OVERLAY, IDecoration.TOP_LEFT);
		}
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {}

	@Override
	public void removeListener(ILabelProviderListener listener) {}

	@Override
	public void dispose() {}
}
