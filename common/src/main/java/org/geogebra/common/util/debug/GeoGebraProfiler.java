package org.geogebra.common.util.debug;

/**
 * @author gabor
 * 
 *         Uses console.profile where possible. Abstract implementation, because
 *         of Common usages.
 *
 */
public abstract class GeoGebraProfiler {

	private static volatile GeoGebraProfiler instance = null;

	private static volatile int repaints, repaintTime, drags, dragTime;

	private static volatile int algebra, event, hits, cascades;

	private static volatile long algebraTime, eventTime, hitTime, cascadeTime;

	private static Object lock = new Object();

	/**
	 */
	public abstract void profile();

	/**
	 * see:
	 * https://developers.google.com/chrome-developer-tools/docs/console-api
	 * #consoleprofileend
	 */
	public abstract void profileEnd();

	/**
	 * @param label
	 * 
	 *            see:
	 *            https://developers.google.com/chrome-developer-tools/docs/
	 *            console-api#consoletimelabel
	 */
	public abstract void time(String label);

	/**
	 * @param label
	 * 
	 *            see:
	 *            https://developers.google.com/chrome-developer-tools/docs/
	 *            console-api#consoletimeend
	 */
	public abstract void timeEnd(String label);

	/**
	 * see
	 * https://developers.google.com/chrome-developer-tools/docs/console-api#
	 * consoletrace
	 */
	public abstract void trace();

	/**
	 * @return GeoGebraProfiler(Web/Desktop) instance
	 */
	public static GeoGebraProfiler getInstance() {
		if (instance == null) {
			instance = new SilentProfiler();
			Log.warn("trying to profile without profiler");
		}
		return instance;
	}

	/**
	 * @param inst
	 *            GeoGebraProfiler inst from Web or Desktop
	 */
	public static void init(GeoGebraProfiler inst) {
		synchronized (lock) {
			instance = inst;
		}
	}

	public static void addRepaint(long time) {
		synchronized (lock) {
			repaints++;
			repaintTime += time;
			if (false) {
				Log.debug("Profile Repaint: " + repaints + " x "
						+ (repaintTime / repaints) + " = " + repaintTime);
				if (hits > 0) {
					Log.debug("Profile Hits: " + hits + " x " + (hitTime / hits)
							+ " = " + hitTime);
				}
				if (cascades > 0) {
					Log.debug("Profile Cascades: " + cascades + " x "
							+ (cascadeTime / cascades) + " = " + cascadeTime);
				}
				if (algebra > 0) {
					Log.debug("Profile Algebra: " + algebra + " x "
							+ (algebraTime / algebra) + " = " + algebraTime);
				}
				if (event > 0) {
					Log.debug("Profile EventDispatcher: " + event + " x "
							+ (eventTime / event) + " = " + eventTime);
				}
				if (drags > 0) {
					Log.debug("Profile Dragging: \nNumber of handled drag events: " + drags + "\n"
							+ "Average duration of one drag event: "
							+ ((float) dragTime / (float) drags) + " ms \n"
							+ "Number of repaints: " + repaints + "\n"
							+ "Average duration of one repaint: " + ((float) repaintTime / repaints) +
							" ms");
				}
			}
		}
	}

	public static void printMeasurmentData() {
		Log.debug("Profile Dragging: \nNumber of handled drag events: " + drags + "\n"
				+ "Average duration of one drag event: "
				+ ((float) dragTime / (float) drags) + " ms \n" + "Number of repaints: "
				+ repaints + "\n" + "Average duration of one repaint: "
				+ ((float) repaintTime / repaints) + " ms");
	}

	public static void addHit(long l) {
		synchronized (lock) {
			hitTime += l;
			hits++;
		}

	}

	public static void addUpdateCascade(long l) {
		synchronized (lock) {
			cascades++;
			cascadeTime += l;
		}

	}

	public static void addAlgebra(long l) {
		synchronized (lock) {
			algebra++;
			algebraTime += l;
		}
	}

	public static void addEvent(long l) {
		synchronized (lock) {
			event++;
			eventTime += l;
		}
	}

	/**
	 * @param t
	 *            drag duration
	 */
	public static void incrementDragTime(int t) {
		synchronized (lock) {
			dragTime += t;
		}

	}

	public static void incrementDrags() {
		synchronized (lock) {
			drags++;
		}
	}
}
