package org.geogebra.common.main.exam;

import java.util.Date;

import org.geogebra.common.factories.FormatFactory;
import org.geogebra.common.factories.UtilFactory;
import org.geogebra.common.kernel.commands.CmdGetTime;
import org.geogebra.common.kernel.commands.CommandDispatcher;
import org.geogebra.common.kernel.commands.filter.CommandFilter;
import org.geogebra.common.kernel.commands.filter.ExamCommandFilter;
import org.geogebra.common.kernel.commands.selector.NoCASCommandSelectorFactory;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Localization;
import org.geogebra.common.main.Translation;
import org.geogebra.common.main.exam.event.CheatingEvent;
import org.geogebra.common.main.exam.event.CheatingEvents;
import org.geogebra.common.main.exam.output.OutputFilter;
import org.geogebra.common.main.localization.CommandErrorMessageBuilder;
import org.geogebra.common.main.settings.Settings;
import org.geogebra.common.util.GTimer;
import org.geogebra.common.util.GTimerListener;
import org.geogebra.common.util.TimeFormatAdapter;
import org.geogebra.common.util.debug.Log;

public class ExamEnvironment {

	private static final long EXAM_START_TIME_NOT_STARTED = -1;

	/** how long notification for protocol saved is shown */
	static public final int EXAM_PROTOCOL_SAVED_NOTIFICATION_DURATION = 5000;

	/** exam start timestamp (milliseconds) */
	long examStartTime = EXAM_START_TIME_NOT_STARTED;

	private CheatingEvents cheatingEvents;
	private long closed = -1;

	private boolean hasGraph = false;

	private TimeFormatAdapter timeFormatter;
	private CommandFilter nonExamCommandFilter;
	private static OutputFilter outputFilter = new OutputFilter();

	/**
	 * application
	 */
	protected App app;
	private Localization localization;

	private long ignoreBlurUntil = -1;
	private boolean temporaryBlur;

	private AllowCommands allow;
	/**
	 *
	 * @param app
	 *            application
	 */
	public ExamEnvironment(App app) {
		this.app = app;
		this.localization = app.getLocalization();
		cheatingEvents = new CheatingEvents();
		allow = new AllowCommands(app.getKernel().getAlgebraProcessor().getCommandDispatcher());
	}

	/**
	 * @return exam start timestamp
	 */
	public long getStart() {
		return examStartTime;
	}

	/**
	 *
	 * @return true if exam is started
	 */
	public boolean isStarted() {
		return examStartTime > 0;
	}

	/**
	 * @param time
	 *            timestamp in milliseconds
	 */
	public void setStart(long time) {
		examStartTime = time;
		closed = -1;
	}

	/**
	 * Start cheating when window left
	 */
	public void windowLeft() {
		if (isStarted()) {
			cheatingEvents.addWindowLeftEvent();
		}
	}

	/**
	 * Start cheating when window left, checks if the blur events are
	 * temporarily allowed
	 */
	public void checkedWindowLeft() {
		if (ignoreBlurUntil > System.currentTimeMillis()) {
			temporaryBlur = true;
			return;
		}
		windowLeft();
	}

	/**
	 * Log end of cheating.
	 */
	public void stopCheating() {
		this.temporaryBlur = false;
		if (getStart() > 0) {
			cheatingEvents.addWindowEnteredEvent();
			Log.debug("STOPPED CHEATING");
		}
	}

	/**
	 * @return whether some cheating events occured since exam mode was started
	 */
	public boolean isCheating() {
		return !cheatingEvents.isEmpty();
	}

	/**
	 * @return whether the exam was ended
	 */
	public boolean isClosed() {
		return closed != -1;
	}

	/**
	 * @param translation The translation identifier from the Translation enum.
	 * @return The translation identified by the Translation parameter.
	 */
	public String getTranslatedString(Translation translation) {
		switch (translation) {
			case EXAM_MODE:
				return localization.getMenu("exam_menu_entry");
			case OK:
				return localization.getMenu("OK");
			case ALERT:
				return localization.getMenu("exam_alert");
			case SHOW_TO_TEACHER:
				return localization.getMenu("exam_log_show_screen_to_teacher");
			case DATE:
				return localization.getMenu("exam_start_date");
			case START_TIME:
				return localization.getMenu("exam_start_time");
			case END_TIME:
				return localization.getMenu("exam_end_time");
			case ACTIVITY:
				return localization.getMenu("exam_activity");
			case EXAM_STARTED:
				return localization.getMenu("exam_started");
			case EXAM_ENDED:
				return localization.getMenu("exam_ended");
			case EXIT:
				return localization.getMenu("Exit");
			case DURATION:
				return localization.getMenu("Duration");
		}
		return null;
	}

	/**
	 * @return The exam date in localized format.
	 */
	public String getDate() {
		// eg "23 October 2015"
		// don't use \\S for 23rd (not used in eg French)
		return CmdGetTime.buildLocalizedDate("\\j \\F \\Y", new Date(examStartTime),
				localization);
	}

	/**
	 * @return The exam start time in localized format.
	 */
	public String getStartTime() {
		return getLocalizedTimeOnly(localization, examStartTime);
	}

	/**
	 * @return The exam end time in localized format.
	 */
	public String getEndTime() {
		return getLocalizedTimeOnly(localization, closed);
	}

	/**
	 * @param withEndTime Whether the returned log string should contain the elapsed time as exam end time.
	 * @return The (cheating) activity log.
	 */
	public String getActivityLog(boolean withEndTime) {
		if (cheatingEvents.isEmpty()) {
			return "";
		}
		ExamLogBuilder logBuilder = new ExamLogBuilder();
		appendLogTimes(localization, logBuilder, withEndTime);
		return logBuilder.toString().trim();
	}

	private static String getLocalizedTimeOnly(Localization loc, long time) {
		// eg "14:08:48"
		return CmdGetTime.buildLocalizedDate("\\H:\\i:\\s", new Date(time),
				loc);
	}

	private static String getLocalizedDateOnly(Localization loc, long time) {
		// eg "Fri 23 October 2015"
		// don't use \\S for 23rd (not used in eg French)
		return CmdGetTime.buildLocalizedDate("\\D, \\j \\F \\Y", new Date(time),
				loc);
	}

	/**
	 * @param loc
	 *            localization
	 * @param settings
	 *            settings
	 * @param builder
	 *            log builder
	 */
	protected void appendSettings(Localization loc, Settings settings,
			ExamLogBuilder builder) {
		// Deactivated Views
		boolean supportsCAS = settings.getCasSettings().isEnabled();
		boolean supports3D = settings.supports3D();

		if (!hasGraph) {
			StringBuilder sb = new StringBuilder();
			if (!supportsCAS || !supports3D) {
				sb.append(loc.getMenu("exam_views_deactivated"));
				sb.append(": ");
			}
			if (!supportsCAS) {
				sb.append(loc.getMenu("Perspective.CAS"));
			}
			if (!supportsCAS && !supports3D) {
				sb.append(", ");
			}
			if (!supports3D) {
				sb.append(loc.getMenu("Perspective.3DGraphics"));
			}
			builder.addLine(sb);
		}
	}

	private void appendStartEnd(Localization loc, ExamLogBuilder builder,
			boolean showEndTime) {
		// Exam Start Date
		builder.addField(loc.getMenu("exam_start_date"),
				getLocalizedDateOnly(loc, examStartTime));
		// Exam Start Time
		builder.addField(loc.getMenu("exam_start_time"),
				getLocalizedTimeOnly(loc, examStartTime));

		// Exam End Time
		if (showEndTime && closed > 0) {
			builder.addField(loc.getMenu("exam_end_time"), getLocalizedTimeOnly(loc, closed));
		}
	}

	/**
	 *
	 * @return elapsed time
	 */
	public String getElapsedTime() {
		return timeToString(System.currentTimeMillis());
	}

	/**
	 * @param loc
	 *            localization
	 * @param builder
	 *            log builder
	 * @param withEndTime
	 *            true if add end timestamp
	 */
	public void appendLogTimes(Localization loc, ExamLogBuilder builder,
			boolean withEndTime) {
		// Log times
		StringBuilder sb = new StringBuilder();
		sb.append("0:00");
		sb.append(' ');
		sb.append(loc.getMenu("exam_started"));

		builder.addLine(sb);

        for (CheatingEvent cheatingEvent : cheatingEvents.getEvents()) {
            sb.setLength(0);
            sb.append(timeToString(cheatingEvent.getTime()));
            sb.append(' ');
            sb.append(cheatingEvent.getAction().toString(loc));
            builder.addLine(sb);
        }

		if (withEndTime && closed > 0) {
			sb.setLength(0);
			sb.append(timeToString(closed)); // get exit timestamp
			sb.append(' ');
			sb.append(loc.getMenu("exam_ended"));
			builder.addLine(sb);
		}
	}

	/**
	 * NEW LOG DIALOG
	 *
	 * @param loc
	 *            localization
	 * @param settings
	 *            settings
	 * @return log text
	 */
	public String getLog(Localization loc, Settings settings) {
		ExamLogBuilder sb = new ExamLogBuilder();
		getLog(loc, settings, sb);
		return sb.toString();
	}

	/**
	 * @param loc
	 *            localization
	 * @param settings
	 *            settings
	 * @param sb
	 *            log builder
	 */
	public void getLog(Localization loc, Settings settings,
			ExamLogBuilder sb) {
		if (!app.isUnbundled()) {
			appendSettings(loc, settings, sb);
		}
		appendStartEnd(loc, sb, true);
		sb.addField(loc.getMenu("exam_activity"), "");
		appendLogTimes(loc, sb, true);
	}

	/**
	 * @param loc
	 *            localization
	 * @return exam start and end
	 */
	public String getLogStartEnd(Localization loc) {
		return getLogStartEnd(loc, true);
	}

	/**
	 * @param loc
	 *            localization
	 * @param showEndTime
	 *            whether to include end time
	 * @return log with start and end of the exam
	 */
	private String getLogStartEnd(Localization loc, boolean showEndTime) {
		ExamLogBuilder sb = new ExamLogBuilder();
		appendStartEnd(loc, sb, showEndTime);
		return sb.toString();
	}

	/**
	 * @param loc
	 *            localization
	 * @return log times with description separated by newline
	 */
	public String getLogTimes(Localization loc) {
		return getLogTimes(loc, true);
	}

	/**
	 * @param loc
	 *            localization
	 * @param showEndTime
	 *            whether to show end time
	 * @return log times with description separated by newline
	 */
	public String getLogTimes(Localization loc, boolean showEndTime) {
		ExamLogBuilder sb = new ExamLogBuilder();
		appendLogTimes(loc, sb, showEndTime);
		return sb.toString();
	}

	public void setHasGraph(boolean hasGraph) {
		this.hasGraph = hasGraph;
	}

	/**
	 * @param timestamp
	 *            relative timestamp
	 * @return MM:SS
	 */
	public String timeToString(long timestamp) {
		if (examStartTime < 0) {
			return "0:00";
		}
		int secs = (int) ((timestamp - examStartTime) / 1000);
		int mins = secs / 60;
		secs -= mins * 60;
		String secsS = secs + "";
		if (secs < 10) {
			secsS = "0" + secsS;
		}
		return mins + ":" + secsS;
	}

	/**
	 * store end time
	 */
	public void storeEndTime() {
		this.closed = System.currentTimeMillis();
	}

	/**
	 * Store end time
	 */
	public void exit() {
		storeEndTime();
		allow.restoreCommandSelector();
	}

	/**
	 * close exam mode and reset CAS etc.
	 *
	 */
	public void closeExam() {
		examStartTime = EXAM_START_TIME_NOT_STARTED;
		disableExamCommandFilter();
		setShowSyntax(true);

		app.fileNew();
	}

	private void setShowSyntax(boolean showSyntax) {
		CommandErrorMessageBuilder builder = localization.getCommandErrorMessageBuilder();
		builder.setShowingSyntax(showSyntax);
		if (!showSyntax) {
			Log.printStacktrace("");
		}
	}

	/**
	 * @return calculator name for status bar
	 */
	public String getCalculatorNameForStatusBar() {
		return localization.getMenu(app.getConfig().getAppNameShort());
	}

	/**
	 * @return calculator name for exam log header
	 */
	public String getCalculatorNameForHeader() {
		return localization.getMenu(app.getConfig().getAppName());
	}

	/**
	 * Run this when unlocked task detected; notifies about cheating
	 */
	public void taskUnlocked() {
		if (getStart() > 0) {
			cheatingEvents.addScreenUnlockedEvent();
			Log.debug("STARTED CHEATING: task unlocked");
		}
	}

	/**
	 * If task was previously unlocked, add cheating end to the log
	 */
	public void taskLocked() {
		if (getStart() > 0) {
			cheatingEvents.addScreenLockedEvent();
			Log.debug("STOPPED CHEATING: task locked");
		}
	}

	/**
	 * Add airplane mode cheating event
	 */
	public void airplaneModeTurnedOff() {
		if (getStart() > 0) {
			cheatingEvents.addAirplaneModeDisabledEvent();
		}
	}

	/**
	 * Add airrplane mode stop-cheating event
	 */
	public void airplaneModeTurnedOn() {
		if (getStart() > 0) {
			cheatingEvents.addAirplaneModeEnabledEvent();
		}
	}

	/**
	 * Add Wifi cheating event
	 */
	public void wifiEnabled() {
		if (getStart() > 0) {
			cheatingEvents.addWifiEnabledEvent();
		}
	}

	/**
	 * Add Wifi stop-cheating event
	 */
	public void wifiDisabled() {
		if (getStart() > 0) {
			cheatingEvents.addWifiDisabledEvent();
		}
	}

	/**
	 * Add Bluetooth cheating event
	 */
	public void bluetoothEnabled() {
		if (getStart() > 0) {
			cheatingEvents.addBluetoothEnabledEvent();
		}
	}

	/**
	 * Add Bluetooth stop-cheating event
	 */
	public void bluetoothDisabled() {
		if (getStart() > 0) {
			cheatingEvents.addBluetoothDisabledEvent();
		}
	}

    /**
     *
     * @return the localized elapsed time string
     */
	public String getElapsedTimeLocalized() {
        return  timeToStringLocalized(System.currentTimeMillis());
    }

    /**
     *
     * @param timestamp current timestamp in millis
     * @return the localized formatted time string
     */
    private String timeToStringLocalized(long timestamp) {
		if (timeFormatter == null) {
			timeFormatter = FormatFactory.getPrototype().getTimeFormat();
		}
        if (examStartTime < 0) {
            return timeFormatter.format(localization.getLocale(), "%02d:%02d", 0);
        }

        int millis = (int) (timestamp - examStartTime);

        return timeFormatter.format(localization.getLocale(), "%02d:%02d", millis);
    }

	/**
	 * @return number of cheating events
	 */
	public int getEventCount() {
		return cheatingEvents.size();
	}

	/**
	 * Saves the current command filter into the nonExamCommandFilter field and sets the exam
	 * command filter for the duration of the exam mode.
	 */
	private void enableExamCommandFilter() {
		CommandDispatcher commandDispatcher =
				app.getKernel().getAlgebraProcessor().getCommandDispatcher();
		nonExamCommandFilter = commandDispatcher.getCommandFilter();
		commandDispatcher.setCommandFilter(new ExamCommandFilter());
	}

	/**
	 * Prepares the exam for starting.
	 */
	public void setupExamEnvironment() {
		enableExamCommandFilter();
		restrictCommands();
		setShowSyntax(false);
	}

	private void restrictCommands() {
		if (app.getSettings().getCasSettings().isEnabled()) {
			return;
		}
		CommandDispatcher commandDispatcher =
				app.getKernel().getAlgebraProcessor().getCommandDispatcher();
		commandDispatcher.saveCommandSelector();
		commandDispatcher.setCommandSelector(new NoCASCommandSelectorFactory().createCommandSelector());
	}


	/**
	 * Disables the exam command filter by setting the nonExamCommandFilter to the CommandDispatcher
	 */
	private void disableExamCommandFilter() {
		app
				.getKernel()
				.getAlgebraProcessor()
				.getCommandDispatcher()
				.setCommandFilter(nonExamCommandFilter);
	}

	/**
	 * @param eqn
	 *            equation
	 * @return whether the equation should be hidden
	 */
	public static boolean isProtectedEquation(GeoElement eqn) {
		return !outputFilter.isAllowed(eqn);
	}

	/**
	 * @param ignoreBlurFor
	 *            expiration timestamp of blur free pass
	 */
	public void setIgnoreBlurInterval(int ignoreBlurFor) {
		this.ignoreBlurUntil = System.currentTimeMillis() + ignoreBlurFor;
		GTimer timer = UtilFactory.getPrototype()
				.newTimer(new GTimerListener() {

					@Override
					public void onRun() {
						onBlurTimer();
					}
				}, ignoreBlurFor);
		timer.start();
	}

	/**
	 * Handler for blur timer
	 */
	protected void onBlurTimer() {
		if (temporaryBlur) {
			windowLeft();
		}
	}
	
	public void enableCAS() {
		allow.enableCAS();
	}
	
	public void disableCAS() {
		allow.disableCAS();
	}

	public void saveCommandSelector() {
		allow.saveCommandSelector();
	}
	
	public void restoreCommandSelector() {
		allow.restoreCommandSelector();
	}	
}
