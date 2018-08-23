package org.geogebra.web.html5.js;

import org.geogebra.common.util.debug.Log;
import org.geogebra.web.html5.Browser;
import org.geogebra.web.html5.css.GuiResourcesSimple;
import org.geogebra.web.html5.util.Dom;
import org.geogebra.web.html5.util.PDFEncoderW;
import org.geogebra.web.html5.util.ScriptLoadCallback;
import org.geogebra.web.resources.JavaScriptInjector;
import org.geogebra.web.resources.StyleInjector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.user.client.Window.Location;

/**
 * @author gabor
 * 
 *         injects the javascript resources
 *
 */
public class ResourcesInjector {

	private static boolean resourcesInjected = false;
	private static ResourcesInjector instance;

	/**
	 * Inject all JS/CSS resources
	 */
	public static void injectResources() {
		if (resourcesInjected) {
			return;
		}
		resourcesInjected = true;
		// always need English properties available, eg Function.sin
		fixComputedStyle();
		// insert zip.js
		JavaScriptInjector.inject(GuiResourcesSimple.INSTANCE.zipJs());

		if (instance == null) {
			instance = (ResourcesInjector) GWT.create(ResourcesInjector.class);
		}

		JavaScriptInjector.inject(GuiResourcesSimple.INSTANCE.visibilityJs());
		JavaScriptInjector.inject(GuiResourcesSimple.INSTANCE.domvas());

		StyleInjector.inject(GuiResourcesSimple.INSTANCE.reset());
		StyleInjector.inject(GuiResourcesSimple.INSTANCE.modernStyleGlobal());

		injectLTRstyles();
		injectScss();
		instance.injectResourcesGUI();

		Browser.setWebWorkerSupported(Location
				.getParameter("GeoGebraDebug") == null
				&& Browser.checkWorkerSupport(GWT
						.getModuleBaseURL()));
		if (!Browser.webWorkerSupported()) {
			loadCodecs();
		}
		JavaScriptInjector.inject(GuiResourcesSimple.INSTANCE.arrayBufferJs());
		// strange, but iPad can blow it away again...
		if (Browser.checkIfFallbackSetExplicitlyInArrayBufferJs()
				&& Browser.webWorkerSupported()) {
			loadCodecs();
		}
		JavaScriptInjector.inject(GuiResourcesSimple.INSTANCE.dataViewJs());
	}

	/**
	 * Load PAKO or Deflate
	 */
	public static void loadCodecs() {
		// if (AppW.USE_PAKO) {
			Log.debug("loading zipjs/pako");
		if (!PDFEncoderW.pakoLoaded()) {
				JavaScriptInjector.inject(GuiResourcesSimple.INSTANCE.pakoJs());
			}
			JavaScriptInjector
					.inject(GuiResourcesSimple.INSTANCE.pakoCodecJs());
		// } else {
		// Log.debug("loading zipjs/inflate+deflate.js");
		// JavaScriptInjector.inject(GuiResourcesSimple.INSTANCE.deflateJs());
		// JavaScriptInjector.inject(GuiResourcesSimple.INSTANCE.inflateJs());
		// }
	}

	/** Works around https://bugzilla.mozilla.org/show_bug.cgi?id=548397 */
	private static native void fixComputedStyle() /*-{
		var oldCS = $wnd.getComputedStyle;
		$wnd.getComputedStyle = function(el) {
			return oldCS(el) || el.style;
		}
	}-*/;

	/**
	 * Inject resources for GUI, overridden in ReTeX injector (to add JQuery +
	 * JqueryUI for sliders)
	 * 
	 */
	protected void injectResourcesGUI() {
		// overridden elsewhere
	}

	private static void injectLTRstyles() {
		StyleInjector.inject(GuiResourcesSimple.INSTANCE.generalStyleLTR());
		StyleInjector
				.inject(GuiResourcesSimple.INSTANCE.avStyleLTR());
	}

	private static void injectScss() {
		StyleInjector
				.inject(GuiResourcesSimple.INSTANCE.colorsScss());
		StyleInjector.inject(GuiResourcesSimple.INSTANCE.layoutScss());
		StyleInjector.inject(GuiResourcesSimple.INSTANCE.evStyleScss());
	}

	/**
	 * removes the added resources
	 */
	public static void removeResources() {
		resourcesInjected = false;
		// this list is live
		NodeList<Element> resources = Dom
				.getElementsByClassName(StyleInjector.CLASSNAME);
		while (resources.getLength() > 0) {
			resources.getItem(resources.getLength() - 1).removeFromParent();
		}

		// this is not :-) Love DOM!
		NodeList<Element> scripts = Dom
		        .querySelectorAll("script[src$=\"cache.js\"]");
		for (int i = 0; i < scripts.getLength(); i++) {
			scripts.getItem(i).removeFromParent();
		}
	}

	/**
	 * @param el
	 *            script element
	 * @param handler
	 *            if script loaded, calls the callback that implements interface
	 *            ScriptLoadHandler
	 */
	public static native void addLoadHandler(ScriptElement el,
			ScriptLoadCallback handler) /*-{
		el
				.addEventListener(
						"load",
						function() {
							handler.@org.geogebra.web.html5.util.ScriptLoadCallback::onLoad()();
						}, false);
		el
				.addEventListener(
						"error",
						function() {
							handler.@org.geogebra.web.html5.util.ScriptLoadCallback::onError()();
						}, false);
	}-*/;

	/**
	 * @param fontsCssUrl
	 *            font CSS url
	 */
	public static void loadFont(String fontsCssUrl) {
		if (instance != null) {
			instance.loadWebFont(fontsCssUrl);
		}
	}

	/**
	 * Load Mathsans font if needed + additional fonts if specified by param.
	 * 
	 * @param dataParamFontsCssUrl
	 *            font CSS url
	 */
	protected void loadWebFont(String dataParamFontsCssUrl) {
		// intentionally
	}
}
