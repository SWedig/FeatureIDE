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
package de.ovgu.featureide.examples.elevator.sim;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.ovgu.featureide.examples.elevator.core.controller.ControlUnit;
import de.ovgu.featureide.examples.elevator.core.controller.ITickListener;
import de.ovgu.featureide.examples.elevator.core.controller.ITriggerListener;
import de.ovgu.featureide.examples.elevator.core.model.Elevator;
import de.ovgu.featureide.examples.elevator.ui.MainWindow;
import de.ovgu.featureide.examples.elevator.core.controller.Request;

/**
 * 
 * This class implements the main method for the elevator simulation.
 * 
 * @author FeatureIDE Team
 */
public class SimulationUnit {

	private ITriggerListener triggerListener;

	private static MainWindow getMainWindow() {
		return new MainWindow(sim);
	}

	private void start(int maxFloor) {
		original(maxFloor);
		setTriggerListener(controller);
	}

	private ITickListener getTickListener() {
		return new TickListener();
	}

	public void setTriggerListener(ITriggerListener listener) {
		this.triggerListener =
			listener;
	}

	public void floorRequest(Request floorRequest) {
		this.triggerListener.trigger(floorRequest);
	}

	public int getCurrentFloor() {
		return controller.getCurrentFloor();
	}

	private static class TickListener implements ITickListener {

		@Override
		public void onRequestFinished(Elevator elevator, Request request) {}
	}
}
