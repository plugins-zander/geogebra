package org.geogebra.web.html5.video;

import java.util.ArrayList;

import org.geogebra.common.kernel.geos.GeoVideo;
import org.geogebra.web.html5.css.GuiResourcesSimple;
import org.geogebra.web.resources.JavaScriptInjector;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;

/**
 * Represents a placeholder for YouTube videos.
 * 
 * @author Laszlo Gal
 *
 */
public class YouTubeVideoPlayer extends VideoPlayer {
	private static boolean youTubeAPI;
	private JavaScriptObject ytPlayer;
	private static ArrayList<YouTubeVideoPlayer> waiting = new ArrayList<>();

	/**
	 * Constructor.
	 * 
	 * @param video
	 *            the video object.
	 * @param id
	 *            The id of the player frame.
	 */
	public YouTubeVideoPlayer(GeoVideo video, int id) {
		super(video, id);
		initYouTubeApi();
		if (youTubeAPI) {
			createPlayerDeferred();
		} else {
			waiting.add(this);
		}
		getElement().setAttribute("allowfullscreen", "1");
	}
	
	private void createPlayerDeferred() {
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
				setYouTubePlayer(createYouTubePlayer(video.getYouTubeId()));
			}
		});
	}

	/**
	 * Updates the player based on video object.
	 */
	@Override
	public void update() {
		Style style = getElement().getStyle();
		style.setLeft(getVideo().getAbsoluteScreenLocX(), Unit.PX);
		style.setTop(getVideo().getAbsoluteScreenLocY(), Unit.PX);
		setWidth(getVideo().getWidth() + "px");
		setHeight(getVideo().getHeight() + "px");
		if (getVideo().isBackground()) {
			addStyleName("background");
		} else {
			removeStyleName("background");
		}
		video.getKernel().getApplication().getActiveEuclidianView().repaintView();

	}

	/**
	 * 
	 * @return the JS YouTube player itself.
	 */
	public JavaScriptObject getYouTubePlayer() {
		return ytPlayer;
	}

	/**
	 * 
	 * sets the JS YouTube player.
	 * 
	 * @param ytPlayer
	 *            to set.
	 */
	public void setYouTubePlayer(JavaScriptObject ytPlayer) {
		this.ytPlayer = ytPlayer;
	}

	/**
	 * Initializes YouTube API.
	 */
	public static void initYouTubeApi() {
		if (youTubeAPI) {
			return;
		}
		JavaScriptInjector.inject(GuiResourcesSimple.INSTANCE.youtube());
		loadYouTubeApi();
	}

	private static void onAPIReady() {
		youTubeAPI = true;
		for (YouTubeVideoPlayer player : waiting) {
			player.createPlayerDeferred();
		}
		waiting.clear();
	}

	/**
	 * 
	 * @return if YouTube API is present and ready to use.
	 */
	public static boolean hasYouTubeApi() {
		return youTubeAPI;
	}

	private static native void loadYouTubeApi() /*-{
		$wnd.youtube_api_ready = function() {
			@org.geogebra.web.html5.video.YouTubeVideoPlayer::onAPIReady()();
		}

		var tag = document.createElement('script');
		tag.id = 'youtube-iframe';
		tag.src = 'https://www.youtube.com/iframe_api';
		var firstScriptTag = $doc.getElementsByTagName('script')[0];
		firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
	}-*/;

	private static void onPlayerStateChange() {
		// implement later;
	}

	private native JavaScriptObject createYouTubePlayer(String youtubeId) /*-{
		var that = this;
		var ytPlayer = new $wnd.YT.Player(
				that.@org.geogebra.web.html5.video.VideoPlayer::playerId,
				{
					videoId : youtubeId,
					events : {
						'onReady' : function(event) {
							that.@org.geogebra.web.html5.video.YouTubeVideoPlayer::onReady()();
						}
					}
				});
		return ytPlayer;
	}-*/;

	@Override
	public boolean isValid() {
		return isFrameValid();
	}

	/**
	 * Play the video.
	 */
	@Override
	public void play() {
		video.play();
		if (video.isPlaying()) {
			play(ytPlayer);
		}
		update();
	}

	/**
	 * Pause the video.
	 */
	@Override
	public void pause() {
		video.pause();
		pause(ytPlayer);
		update();
	}

	private native void play(JavaScriptObject player) /*-{
		player.playVideo();
	}-*/;

	private native void pause(JavaScriptObject player) /*-{
		player.pauseVideo();
	}-*/;
}

