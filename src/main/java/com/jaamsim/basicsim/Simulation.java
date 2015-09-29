/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2002-2011 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.jaamsim.basicsim;

import java.io.File;

import javax.swing.JFrame;

import com.jaamsim.events.EventManager;
import com.jaamsim.input.BooleanInput;
import com.jaamsim.input.DirInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.Input;
import com.jaamsim.input.InputAgent;
import com.jaamsim.input.IntegerInput;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
import com.jaamsim.input.ValueInput;
import com.jaamsim.math.Vec3d;
import com.jaamsim.ui.EditBox;
import com.jaamsim.ui.EntityPallet;
import com.jaamsim.ui.FrameBox;
import com.jaamsim.ui.GUIFrame;
import com.jaamsim.ui.LogBox;
import com.jaamsim.ui.ObjectSelector;
import com.jaamsim.ui.OutputBox;
import com.jaamsim.ui.PropertyBox;
import com.jaamsim.units.DistanceUnit;
import com.jaamsim.units.TimeUnit;
import com.jaamsim.units.Unit;

/**
 * Simulation provides the basic structure for the Entity model lifetime of earlyInit,
 * startUp and doEndAt.  The initial processtargets required to start the model are
 * added to the eventmanager here.  This class also acts as a bridge to the UI by
 * providing controls for the various windows.
 */
public class Simulation extends Entity {

	// Key Inputs tab
	@Keyword(description = "The duration of the simulation run in which all statistics will be recorded.",
	         example = "Simulation Duration { 8760 h }")
	private static final ValueInput runDuration;

	@Keyword(description = "The initialization interval for the simulation run. The model will run "
			+ "for the InitializationDuration interval and then clear the statistics and execute for the "
			+ "specified RunDuration interval. The total length of the simulation run will be the sum of "
			+ "the InitializationDuration and RunDuration inputs.",
	         example = "Simulation Initialization { 720 h }")
	private static final ValueInput initializationTime;

	@Keyword(description = "Indicates whether an output report will be printed at the end of the simulation run.",
	         example = "Simulation PrintReport { TRUE }")
	private static final BooleanInput printReport;

	@Keyword(description = "The directory in which to place the output report. Defaults to the "
			+ "directory containing the configuration file for the run.",
			example = "Simulation ReportDirectory { 'c:\reports\' }")
	private static final DirInput reportDirectory;

	@Keyword(description = "The length of time represented by one simulation tick.",
	         example = "Simulation TickLength { 1e-6 s }")
	private static final ValueInput tickLengthInput;

	@Keyword(description = "Indicates whether to close the program on completion of the simulation run.",
	         example = "Simulation ExitAtStop { TRUE }")
	private static final BooleanInput exitAtStop;

	@Keyword(description = "Global seed that sets the substream for each probability distribution. "
			+ "Must be an integer >= 0. GlobalSubstreamSeed works together with each probability "
			+ "distribution's RandomSeed keyword to determine its random sequence. It allows the "
			+ "user to change all the random sequences in a model with a single input.",
	         example = "Simulation GlobalSubstreamSeed { 5 }")
	private static final IntegerInput globalSeedInput;

	// GUI tab
	@Keyword(description = "An optional list of units to be used for displaying model outputs.",
	         example = "Simulation DisplayedUnits { h kt }")
	private static final EntityListInput<? extends Unit> displayedUnits;

	@Keyword(description = "If TRUE, a dragged object will be positioned to the nearest grid point.",
	         example = "Simulation SnapToGrid { TRUE }")
	private static final BooleanInput snapToGrid;

	@Keyword(description = "The distance between snap grid points.",
	         example = "Simulation SnapGridSpacing { 1 m }")
	private static final ValueInput snapGridSpacing;

	@Keyword(description = "The distance moved by the selected entity when the an arrow key is pressed.",
	         example = "Simulation IncrementSize { 1 cm }")
	private static final ValueInput incrementSize;

	@Keyword(description = "A Boolean to turn on or off real time in the simulation run",
	         example = "Simulation RealTime { TRUE }")
	private static final BooleanInput realTime;

	@Keyword(description = "The real time speed up factor",
	         example = "Simulation RealTimeFactor { 1200 }")
	private static final IntegerInput realTimeFactor;

	public static final int DEFAULT_REAL_TIME_FACTOR = 1;
	public static final int MIN_REAL_TIME_FACTOR = 1;
	public static final int MAX_REAL_TIME_FACTOR= 1000000;

	@Keyword(description = "The time at which the simulation will be paused.",
	         example = "Simulation PauseTime { 200 h }")
	private static final ValueInput pauseTime;

	@Keyword(description = "Indicates whether the Model Builder tool should be shown on startup.",
	         example = "Simulation ShowModelBuilder { TRUE }")
	private static final BooleanInput showModelBuilder;

	@Keyword(description = "Indicates whether the Object Selector tool should be shown on startup.",
	         example = "Simulation ShowObjectSelector { TRUE }")
	private static final BooleanInput showObjectSelector;

	@Keyword(description = "Indicates whether the Input Editor tool should be shown on startup.",
	         example = "Simulation ShowInputEditor { TRUE }")
	private static final BooleanInput showInputEditor;

	@Keyword(description = "Indicates whether the Output Viewer tool should be shown on startup.",
	         example = "Simulation ShowOutputViewer { TRUE }")
	private static final BooleanInput showOutputViewer;

	@Keyword(description = "Indicates whether the Output Viewer tool should be shown on startup.",
	         example = "Simulation ShowPropertyViewer { TRUE }")
	private static final BooleanInput showPropertyViewer;

	@Keyword(description = "Indicates whether the Log Viewer tool should be shown on startup.",
	         example = "Simulation ShowLogViewer { TRUE }")
	private static final BooleanInput showLogViewer;

	@Keyword(description = "Time at which the simulation run is started (hh:mm).",
	         example = "Simulation StartTime { 2160 h }")
	private static final ValueInput startTimeInput;

	// Hidden keywords
	@Keyword(description = "If the value is TRUE, then the input report file will be printed after "
			+ "loading the configuration file.  The input report can always be generated when "
			+ "needed by selecting \"Print Input Report\" under the File menu.",
	         example = "Simulation PrintInputReport { TRUE }")
	private static final BooleanInput printInputReport;

	@Keyword(description = "This is placeholder description text",
	         example = "This is placeholder example text")
	private static final BooleanInput traceEventsInput;

	@Keyword(description = "This is placeholder description text",
	         example = "This is placeholder example text")
	private static final BooleanInput verifyEventsInput;

	private static double timeScale; // the scale from discrete to continuous time
	private static double startTime; // simulation time (seconds) for the start of the run (not necessarily zero)
	private static double endTime;   // simulation time (seconds) for the end of the run

	private static Simulation myInstance;

	private static String modelName = "JaamSim";

	static {

		// Key Inputs tab
		runDuration = new ValueInput("RunDuration", "Key Inputs", 31536000.0d);
		runDuration.setUnitType(TimeUnit.class);
		runDuration.setValidRange(1e-15d, Double.POSITIVE_INFINITY);

		initializationTime = new ValueInput("InitializationDuration", "Key Inputs", 0.0);
		initializationTime.setUnitType(TimeUnit.class);
		initializationTime.setValidRange(0.0d, Double.POSITIVE_INFINITY);

		printReport = new BooleanInput("PrintReport", "Key Inputs", false);

		reportDirectory = new DirInput("ReportDirectory", "Key Inputs", null);
		reportDirectory.setDefaultText("Configuration File Directory");

		tickLengthInput = new ValueInput("TickLength", "Key Inputs", 1e-6d);
		tickLengthInput.setUnitType(TimeUnit.class);
		tickLengthInput.setValidRange(1e-9d, 5.0d);

		exitAtStop = new BooleanInput("ExitAtStop", "Key Inputs", false);

		globalSeedInput = new IntegerInput("GlobalSubstreamSeed", "Key Inputs", 0);
		globalSeedInput.setValidRange(0, Integer.MAX_VALUE);

		// GUI tab
		displayedUnits = new EntityListInput<>(Unit.class, "DisplayedUnits", "GUI", null);
		displayedUnits.setDefaultText("SI Units");
		displayedUnits.setPromptReqd(false);

		realTime = new BooleanInput("RealTime", "GUI", false);
		realTime.setPromptReqd(false);

		snapToGrid = new BooleanInput("SnapToGrid", "GUI", false);
		snapToGrid.setPromptReqd(false);

		snapGridSpacing = new ValueInput("SnapGridSpacing", "GUI", 0.1d);
		snapGridSpacing.setUnitType(DistanceUnit.class);
		snapGridSpacing.setValidRange(1.0e-6, Double.POSITIVE_INFINITY);
		snapGridSpacing.setPromptReqd(false);

		incrementSize = new ValueInput("IncrementSize", "GUI", 0.1d);
		incrementSize.setUnitType(DistanceUnit.class);
		incrementSize.setValidRange(1.0e-6, Double.POSITIVE_INFINITY);
		incrementSize.setPromptReqd(false);

		realTimeFactor = new IntegerInput("RealTimeFactor", "GUI", DEFAULT_REAL_TIME_FACTOR);
		realTimeFactor.setValidRange(MIN_REAL_TIME_FACTOR, MAX_REAL_TIME_FACTOR);
		realTimeFactor.setPromptReqd(false);

		pauseTime = new ValueInput("PauseTime", "GUI", Double.POSITIVE_INFINITY);
		pauseTime.setUnitType(TimeUnit.class);
		pauseTime.setValidRange(0.0d, Double.POSITIVE_INFINITY);
		pauseTime.setPromptReqd(false);

		showModelBuilder = new BooleanInput("ShowModelBuilder", "GUI", false);
		showModelBuilder.setPromptReqd(false);

		showObjectSelector = new BooleanInput("ShowObjectSelector", "GUI", false);
		showObjectSelector.setPromptReqd(false);

		showInputEditor = new BooleanInput("ShowInputEditor", "GUI", false);
		showInputEditor.setPromptReqd(false);

		showOutputViewer = new BooleanInput("ShowOutputViewer", "GUI", false);
		showOutputViewer.setPromptReqd(false);

		showPropertyViewer = new BooleanInput("ShowPropertyViewer", "GUI", false);
		showPropertyViewer.setPromptReqd(false);

		showLogViewer = new BooleanInput("ShowLogViewer", "GUI", false);
		showLogViewer.setPromptReqd(false);

		// Hidden keywords
		startTimeInput = new ValueInput("StartTime", "Key Inputs", 0.0d);
		startTimeInput.setUnitType(TimeUnit.class);
		startTimeInput.setValidRange(0.0d, Double.POSITIVE_INFINITY);

		traceEventsInput = new BooleanInput("TraceEvents", "Key Inputs", false);
		verifyEventsInput = new BooleanInput("VerifyEvents", "Key Inputs", false);

		printInputReport = new BooleanInput("PrintInputReport", "Key Inputs", false);

		// Initialize basic model information
		startTime = 0.0;
		endTime = 8760.0*3600.0;
	}

	{
		// Key Inputs tab
		this.addInput(runDuration);
		this.addInput(initializationTime);
		this.addInput(printReport);
		this.addInput(reportDirectory);
		this.addInput(tickLengthInput);
		this.addInput(exitAtStop);
		this.addInput(globalSeedInput);

		// GUI tab
		this.addInput(displayedUnits);
		this.addInput(snapToGrid);
		this.addInput(snapGridSpacing);
		this.addInput(incrementSize);
		this.addInput(realTime);
		this.addInput(realTimeFactor);
		this.addInput(pauseTime);
		this.addInput(showModelBuilder);
		this.addInput(showObjectSelector);
		this.addInput(showInputEditor);
		this.addInput(showOutputViewer);
		this.addInput(showPropertyViewer);
		this.addInput(showLogViewer);

		// Hidden keywords
		this.addInput(startTimeInput);
		this.addInput(traceEventsInput);
		this.addInput(verifyEventsInput);
		this.addInput(printInputReport);

		// Hide various keywords
		startTimeInput.setHidden(true);
		traceEventsInput.setHidden(true);
		verifyEventsInput.setHidden(true);
		printInputReport.setHidden(true);
	}

	public Simulation() {}

	public static Simulation getInstance() {
		if (myInstance == null) {
			for (Entity ent : Entity.getAll()) {
				if (ent instanceof Simulation ) {
					myInstance = (Simulation) ent;
					break;
				}
			}
		}
		return myInstance;
	}

	@Override
	public void updateForInput( Input<?> in ) {
		super.updateForInput( in );

		if(in == realTimeFactor || in == realTime) {
			updateRealTime();
			return;
		}

		if (in == pauseTime) {
			updatePauseTime();
			return;
		}

		if (in == reportDirectory) {
			InputAgent.setReportDirectory(reportDirectory.getDir());
			return;
		}

		if (in == displayedUnits) {
			if (displayedUnits.getValue() == null)
				return;
			for (Unit u : displayedUnits.getValue()) {
				Unit.setPreferredUnit(u.getClass(), u);
			}
			return;
		}

		if (in == showModelBuilder) {
			setWindowVisible(EntityPallet.getInstance(), showModelBuilder.getValue());
			return;
		}

		if (in == showObjectSelector) {
			setWindowVisible(ObjectSelector.getInstance(), showObjectSelector.getValue());
			return;
		}

		if (in == showInputEditor) {
			setWindowVisible(EditBox.getInstance(), showInputEditor.getValue());
			FrameBox.reSelectEntity();
			return;
		}

		if (in == showOutputViewer) {
			setWindowVisible(OutputBox.getInstance(), showOutputViewer.getValue());
			FrameBox.reSelectEntity();
			return;
		}

		if (in == showPropertyViewer) {
			setWindowVisible(PropertyBox.getInstance(), showPropertyViewer.getValue());
			FrameBox.reSelectEntity();
			return;
		}

		if (in == showLogViewer) {
			setWindowVisible(LogBox.getInstance(), showLogViewer.getValue());
			FrameBox.reSelectEntity();
			return;
		}
	}

	public static void clear() {
		initializationTime.reset();
		runDuration.reset();
		pauseTime.reset();
		tickLengthInput.reset();
		traceEventsInput.reset();
		verifyEventsInput.reset();
		printInputReport.reset();
		realTimeFactor.reset();
		realTime.reset();
		updateRealTime();
		exitAtStop.reset();

		startTimeInput.reset();

		showModelBuilder.reset();
		showObjectSelector.reset();
		showInputEditor.reset();
		showOutputViewer.reset();
		showPropertyViewer.reset();
		showLogViewer.reset();

		// Initialize basic model information
		startTime = 0.0;
		endTime = 8760.0*3600.0;
		myInstance = null;

		// close warning/error trace file
		InputAgent.closeLogFile();

		// Kill all entities except simulation
		while(Entity.getAll().size() > 0) {
			Entity ent = Entity.getAll().get(Entity.getAll().size()-1);
			ent.kill();
		}
	}

	/**
	 *	Initializes and starts the model
	 *		1) Initializes EventManager to accept events.
	 *		2) calls startModel() to allow the model to add its starting events to EventManager
	 *		3) start EventManager processing events
	 */
	public static void start(EventManager evt) {
		// Validate each entity based on inputs only
		for (int i = 0; i < Entity.getAll().size(); i++) {
			try {
				Entity.getAll().get(i).validate();
			}
			catch (Throwable e) {
				LogBox.format("%s: Validation error- %s", Entity.getAll().get(i).getName(), e.getMessage());
				GUIFrame.showErrorDialog("Input Error Detected During Validation",
				                         "%s: %-70s",
				                         Entity.getAll().get(i).getName(), e.getMessage());

				GUIFrame.instance().updateForSimulationState(GUIFrame.SIM_STATE_CONFIGURED);
				return;
			}
		}

		InputAgent.prepareReportDirectory();
		evt.clear();
		evt.setTraceListener(null);

		if( Simulation.traceEvents() ) {
			String evtName = InputAgent.getConfigFile().getParentFile() + File.separator + InputAgent.getRunName() + ".evt";
			EventRecorder rec = new EventRecorder(evtName);
			evt.setTraceListener(rec);
		}
		else if( Simulation.verifyEvents() ) {
			String evtName = InputAgent.getConfigFile().getParentFile() + File.separator + InputAgent.getRunName() + ".evt";
			EventTracer trc = new EventTracer(evtName);
			evt.setTraceListener(trc);
		}

		evt.setTickLength(tickLengthInput.getValue());
		setSimTimeScale(evt.secondsToNearestTick(3600.0d));
		FrameBox.setSecondsPerTick(tickLengthInput.getValue());

		startTime = startTimeInput.getValue();
		endTime = startTime + Simulation.getInitializationTime() + Simulation.getRunDuration();

		evt.scheduleProcessExternal(0, 0, false, new InitModelTarget(), null);
		evt.resume(evt.secondsToNearestTick(Simulation.getPauseTime()));
	}

	public static int getSubstreamNumber() {
		return globalSeedInput.getValue();
	}

	public static boolean getPrintReport() {
		return printReport.getValue();
	}

	public static boolean traceEvents() {
		return traceEventsInput.getValue();
	}

	public static boolean verifyEvents() {
		return verifyEventsInput.getValue();
	}

	static void setSimTimeScale(double scale) {
		timeScale = scale;
	}

	public static double getSimTimeFactor() {
		return timeScale;
	}

	public static double getEventTolerance() {
		return (1.0d / getSimTimeFactor());
	}

	public static double getTickLength() {
		return tickLengthInput.getValue();
	}

	public static double getPauseTime() {
		return pauseTime.getValue();
	}

	/**
	 * Returns the start time of the run.
	 * @return - simulation time in seconds for the start of the run.
	 */
	public static double getStartTime() {
		return startTime;
	}

	/**
	 * Returns the end time of the run.
	 * @return - simulation time in seconds when the current run will stop.
	 */
	public static double getEndTime() {
		return endTime;
	}

	/**
	 * Returns the duration of the run (not including intialization)
	 */
	public static double getRunDuration() {
		return runDuration.getValue();
	}

	/**
	 * Returns the duration of the initialization period
	 */
	public static double getInitializationTime() {
		return initializationTime.getValue();
	}

	public static double getIncrementSize() {
		return incrementSize.getValue();
	}

	public static boolean isSnapToGrid() {
		return snapToGrid.getValue();
	}

	public static double getSnapGridSpacing() {
		return snapGridSpacing.getValue();
	}

	/**
	 * Returns the nearest point on the snap grid to the given coordinate.
	 * To avoid dithering, the new position must be at least one grid space
	 * from the old position.
	 * @param newPos - new coordinate for the object
	 * @param oldPos - present coordinate for the object
	 * @return newest snap grid point.
	 */
	public static Vec3d getSnapGridPosition(Vec3d newPos, Vec3d oldPos) {
		double spacing = snapGridSpacing.getValue();
		Vec3d ret = new Vec3d(newPos);
		if (Math.abs(newPos.x - oldPos.x) < spacing)
			ret.x = oldPos.x;
		if (Math.abs(newPos.y - oldPos.y) < spacing)
			ret.y = oldPos.y;
		if (Math.abs(newPos.z - oldPos.z) < spacing)
			ret.z = oldPos.z;
		return Simulation.getSnapGridPosition(ret);
	}

	/**
	 * Returns the nearest point on the snap grid to the given coordinate.
	 * @param pos - position to be adjusted
	 * @return nearest snap grid point.
	 */
	public static Vec3d getSnapGridPosition(Vec3d pos) {
		double spacing = snapGridSpacing.getValue();
		Vec3d ret = new Vec3d(pos);
		ret.x = spacing*Math.rint(ret.x/spacing);
		ret.y = spacing*Math.rint(ret.y/spacing);
		ret.z = spacing*Math.rint(ret.z/spacing);
		return ret;
	}

	static void updateRealTime() {
		GUIFrame.instance().updateForRealTime(realTime.getValue(), realTimeFactor.getValue());
	}

	static void updatePauseTime() {
		GUIFrame.instance().updateForPauseTime(pauseTime.getValueString());
	}

	public static void setModelName(String newModelName) {
		modelName = newModelName;
	}

	public static String getModelName() {
		return modelName;
	}

	public static boolean getExitAtStop() {
		return exitAtStop.getValue();
	}

	public static boolean getPrintInputReport() {
		return printInputReport.getValue();
	}

	public static void setWindowVisible(JFrame f, boolean visible) {
		f.setVisible(visible);
		if (visible)
			f.toFront();
	}

	/**
	 * Re-open any Tools windows that have been closed temporarily.
	 */
	public static void showActiveTools() {
		setWindowVisible(EntityPallet.getInstance(), showModelBuilder.getValue());
		setWindowVisible(ObjectSelector.getInstance(), showObjectSelector.getValue());
		setWindowVisible(EditBox.getInstance(), showInputEditor.getValue());
		setWindowVisible(OutputBox.getInstance(), showOutputViewer.getValue());
		setWindowVisible(PropertyBox.getInstance(), showPropertyViewer.getValue());
		setWindowVisible(LogBox.getInstance(), showLogViewer.getValue());
	}

	/**
	 * Closes all the Tools windows temporarily.
	 */
	public static void closeAllTools() {
		setWindowVisible(EntityPallet.getInstance(), false);
		setWindowVisible(ObjectSelector.getInstance(), false);
		setWindowVisible(EditBox.getInstance(), false);
		setWindowVisible(OutputBox.getInstance(), false);
		setWindowVisible(PropertyBox.getInstance(), false);
		setWindowVisible(LogBox.getInstance(), false);
	}

	@Output(name = "Configuration File",
			 description = "The present configuration file.")
	public String getConfigFileName(double simTime) {
		return InputAgent.getConfigFile().getPath();
	}
}
