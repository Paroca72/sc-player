package com.sccomponents.playerbutton;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Play a media
 */

public class ScPlayerButton extends View {

    // ***************************************************************************************
    // Constants

    private static final String BACKGROUND_COLOR = "#FCC81A";
    private static final String FOREGROUND_COLOR = "#FFFFFF";
    private static final String PERMISSION = "RECORD_AUDIO";

    private static final int UPDATE_FREQUENCY = 10; // Hertz
    private static final float FONT_SIZE = 11.0f;
    private static final float VOLUME = 0.7f;


    // ***************************************************************************************
    // Privates attributes

    private String mSource = null;
    private int mColor = Color.WHITE;
    private float mFontSize = ScPlayerButton.FONT_SIZE;
    private float mVolume = ScPlayerButton.VOLUME;


    // ***************************************************************************************
    // Privates variable

    private static Drawable mPlayIcon = null;
    private static Drawable mStopIcon = null;

    private MediaPlayer mPlayer = null;
    private Visualizer mVisualizer = null;

    private int mPosition = 0;
    private long mMediaDuration = 0;
    private byte[] mWaveToken = null;
    private Rect mDrawingArea = null;

    private ScheduledExecutorService mExecutor = null;
    private GestureDetector mDetector = null;
    private OnEventListener mEventListener = null;

    // Temp variable
    private Paint mTimePaint = null;
    private Paint mWavePaint = null;
    private Rect mGenericRect = null;
    private Path mGenericPath = null;


    // ***************************************************************************************
    // Classes

    /**
     * Gesture detector for single tap on component
     */
    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }

    }


    // ***************************************************************************************
    // Constructors

    public ScPlayerButton(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScPlayerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScPlayerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    // ***************************************************************************************
    // Privates methods

    /**
     * Init the component.
     * Retrieve all attributes with the default values if needed.
     * Check the values for internal use and create the painters.
     *
     * @param context  the owner context
     * @param attrs    the attribute set
     * @param defStyle the style
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context
                .obtainStyledAttributes(attrs, R.styleable.ScPlayerButton, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mSource = attrArray.getString(
                R.styleable.ScPlayerButton_source);
        this.mColor = attrArray.getColor(
                R.styleable.ScPlayerButton_color, Color.parseColor(ScPlayerButton.FOREGROUND_COLOR));
        this.mFontSize = attrArray.getDimension(
                R.styleable.ScPlayerButton_fontSize, this.dipToPixel(ScPlayerButton.FONT_SIZE));
        this.mVolume = attrArray.getFloat(
                R.styleable.ScPlayerButton_volume, ScPlayerButton.VOLUME);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // SETTINGS

        // Check the background
        ColorDrawable background = (ColorDrawable) this.getBackground();
        if (background == null)
            this.setBackgroundColor(Color.parseColor(ScPlayerButton.BACKGROUND_COLOR));

        //--------------------------------------------------
        // INIT

        this.mDetector = new GestureDetector(this.getContext(), new SingleTapConfirm());
        this.mExecutor = Executors.newSingleThreadScheduledExecutor();
        this.mMediaDuration = this.getMediaDuration(this.mSource);

        this.mTimePaint = new Paint();
        this.mTimePaint.setAntiAlias(true);
        this.mTimePaint.setTypeface(Typeface.DEFAULT);

        this.mWavePaint = new Paint();
        this.mWavePaint.setAntiAlias(true);
        this.mWavePaint.setStrokeWidth(2.0f);
        this.mWavePaint.setStyle(Paint.Style.STROKE);

        this.mGenericRect = new Rect();
        this.mDrawingArea = new Rect();
        this.mGenericPath = new Path();

        this.setClickable(true);
        this.setSelected(false);
    }

    /**
     * Convert Dip to Pixel using the current display metrics.
     *
     * @param dip the start value in Dip
     * @return the correspondent value in Pixels
     */
    private float dipToPixel(float dip) {
        // Get the display metrics
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        // Calc the conversion by the screen density
        return dip * metrics.density;
    }

    /**
     * Retrieve the media duration in milliseconds
     *
     * @param source the media path
     * @return the duration in milliseconds
     */
    private long getMediaDuration(String source) {
        try {
            // Try to get the media duration
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this.getContext(), Uri.parse(source));
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Long.parseLong(durationStr);

        } catch (Exception ex) {
            // Print the error on the stack and return
            ex.printStackTrace();
            return 0;
        }
    }

    /**
     * Format milliseconds to string in the below format:
     * - If have hours: HH:MM
     * - If NO have hours: MM:SS
     *
     * @param duration the duration in milliseconds
     * @return the format time
     */
    private String formatTime(long duration) {
        // Get the tokens
        long seconds = (duration / 1000) % 60;
        long minutes = (seconds / 60) % 60;
        long hours = (seconds / (60 * 60)) % 24;

        // Format
        if (hours == 0)
            return (minutes < 10 ? "0" + minutes : minutes) + ":" +
                    (seconds < 10 ? "0" + seconds : seconds);
        else
            return (hours < 10 ? "0" + hours : hours) + ":" +
                    (minutes < 10 ? "0" + minutes : minutes);
    }

    /**
     * Check the requested permissions
     *
     * @return true if granted
     */
    private boolean checkRequestedPermission() {
        String permission = "android.permission." + ScPlayerButton.PERMISSION;
        int res = getContext().checkCallingOrSelfPermission(permission);
        return res == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Load the icons.
     * NOTE that this variable is static so the icon will loaded just one
     * time for all the ScPlayerButton instances.
     */
    private void loadIcons() {
        // Check if the icon is already loaded
        if (ScPlayerButton.mPlayIcon == null) {
            // Create a wrap of DrawableCompat to set tint when needs
            Drawable drawable = ContextCompat
                    .getDrawable(this.getContext(), R.drawable.ic_play_arrow_white_24dp);
            ScPlayerButton.mPlayIcon = DrawableCompat.wrap(drawable).mutate();
        }

        // Check if the icon is already loaded
        if (ScPlayerButton.mStopIcon == null) {
            // Create a wrap of DrawableCompat to set tint when needs
            Drawable drawable = ContextCompat
                    .getDrawable(this.getContext(), R.drawable.ic_stop_white_24dp);
            ScPlayerButton.mStopIcon = DrawableCompat.wrap(drawable).mutate();
        }
    }

    /**
     * Compare two string minding the null values.
     *
     * @param str1 first
     * @param str2 second
     * @return true if equal
     */
    public boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }


    // **************************************************************************************
    // Manager media player

    /**
     * Init a new media player.
     *
     * @param mediaPath the media path
     * @param volume    the player volume
     * @return the new media player
     */
    private MediaPlayer initMediaPlayer(String mediaPath, float volume) {
        // Create a new media player object
        MediaPlayer player = MediaPlayer.create(this.getContext(), Uri.parse(mediaPath));

        // Settings and return
        player.seekTo(this.mPosition);
        player.setLooping(false);
        player.setVolume(volume, volume);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Stop all
                stopPlayMedia();

                // Release the button state
                setSelected(false);
                invalidate();
            }
        });
        return player;
    }

    /**
     * Release the media player.
     *
     * @param player the player
     */
    private void releaseMediaPlayer(MediaPlayer player) {
        if (player != null) {
            player.stop();
            player.release();
        }
    }

    /**
     * Init a new visualizer.
     * NOTE that if the app not has the permission requested the visualizer
     * will be always NULL.
     *
     * @param player the owner
     * @return the new visualizer
     */
    private Visualizer initVisualizer(MediaPlayer player) {
        // Check for permission
        if (!this.checkRequestedPermission())
            return null;

        // Holder
        int frequency = ScPlayerButton.UPDATE_FREQUENCY * 1000;
        if (frequency > Visualizer.getMaxCaptureRate())
            frequency = Visualizer.getMaxCaptureRate();

        // Settings and return
        Visualizer visualizer = new Visualizer(player.getAudioSessionId());
        visualizer.setCaptureSize(1024);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer,
                                              byte[] bytes,
                                              int samplingRate) {
                mWaveToken = bytes;
                postInvalidate();
            }

            @Override
            public void onFftDataCapture(
                    Visualizer visualizer,
                    byte[] bytes,
                    int samplingRate) {
                // NOP
            }

        }, frequency, true, false);

        visualizer.setEnabled(true);
        return visualizer;
    }

    /**
     * Release the visualizer.
     *
     * @param visualizer the visualizer
     */
    private void releaseVisualizer(Visualizer visualizer) {
        if (visualizer != null) {
            visualizer.setEnabled(false);
            visualizer.release();
        }
    }

    /**
     * Start to update the component at fixed rate.
     * This is required if the app not have the requested permission to
     * showing the wave form since we need to update the time label while
     * the player is running.
     */
    private ScheduledExecutorService initUpdate() {
        // Start new one
        long milliseconds = (long) ((1 / (float) ScPlayerButton.UPDATE_FREQUENCY) * 1000);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                postInvalidate();
            }
        }, 0L, milliseconds, TimeUnit.MILLISECONDS);

        return executor;
    }

    /**
     * Stop the update service
     */
    private void releaseUpdate(ScheduledExecutorService executor) {
        if (executor != null)
            executor.shutdownNow();
    }

    /**
     * Start to play the media
     *
     * @param mediaPath the path to media
     */
    private void startPlayMedia(String mediaPath) {
        try {
            // Stop if active
            this.stopPlayMedia();

            // Create the player and start to play the media
            if (mediaPath != null) {
                // Player
                this.mPlayer = this.initMediaPlayer(mediaPath, this.mVolume);
                this.mVisualizer = this.initVisualizer(this.mPlayer);

                // If no granted for showing the wave form we must force to
                // update the layout periodically to refresh the time
                if (this.mVisualizer == null)
                    this.mExecutor = this.initUpdate();

                // Play
                this.mPlayer.start();

                // Event
                if (this.mEventListener != null)
                    this.mEventListener.onStartPlay(this.mPlayer);
            }

        } catch (
                Exception ex)

        {
            // Print the error on the stack
            ex.printStackTrace();
        }
    }

    /**
     * Stop to play the media
     */
    private void stopPlayMedia() {
        // If exists stop the play
        if (this.mPlayer != null && this.mPlayer.isPlaying()) {
            // Stop and release
            this.releaseUpdate(this.mExecutor);
            this.releaseVisualizer(this.mVisualizer);
            this.releaseMediaPlayer(this.mPlayer);

            // To null
            this.mExecutor = null;
            this.mVisualizer = null;
            this.mPlayer = null;

            // Event
            if (this.mEventListener != null)
                this.mEventListener.onStopPlay();
        }
    }

    /**
     * Manage the click event
     */
    private void fireClick() {
        // Toggle the current selected state
        this.setSelected(!this.isSelected());
        this.invalidate();

        // If selected start to play the sound
        if (this.isSelected()) {
            // Play the media
            this.startPlayMedia(this.mSource);

        } else {
            // Stop all running process
            this.stopPlayMedia();
        }
    }


    // **************************************************************************************
    // Draw methods

    /**
     * Draw the media duration and fix the remain area.
     * Please note when playing this label will show the elapsed time.
     *
     * @param canvas canvas
     * @param area   bounds
     * @return the rect area
     */
    private Rect drawTime(Canvas canvas, Rect area) {
        // Set the painter
        this.mTimePaint.setColor(this.mColor);
        this.mTimePaint.setTextSize(this.mFontSize);

        // Get the time to display
        long time = this.mMediaDuration;
        if (this.isSelected() &&
                this.mPlayer != null && this.mPlayer.isPlaying())
            time = this.mPlayer.getCurrentPosition();

        // Format the duration and get the dimension
        String timeFormatted = this.formatTime(time);
        this.mTimePaint.getTextBounds(timeFormatted, 0, timeFormatted.length(), this.mGenericRect);

        // Calculate the position
        int x = (area.width() - this.mGenericRect.width()) / 2;
        int y = area.bottom;

        // Draw the text on the canvas
        int margin = 20;
        canvas.drawText(timeFormatted, x, y - margin, this.mTimePaint);

        // Reduce the drawing area
        return new Rect(
                area.left, area.top,
                area.right, area.bottom - this.mGenericRect.height() - margin
        );
    }

    /**
     * Draw the wave form on the canvas.
     *
     * @param canvas the canvas
     * @param area   the bounds
     * @param data   the data to visualize
     */
    private void drawWave(Canvas canvas, Rect area, byte[] data) {
        // Reset the path
        this.mGenericPath.reset();

        // Apply margin
        int margin = 10;
        area.set(
                area.left + margin, area.top + margin,
                area.right - margin, area.bottom - margin
        );

        // Check for empty values
        if (data == null) {
            // Create an horizontal line
            this.mGenericPath.moveTo(area.left, area.centerY());
            this.mGenericPath.lineTo(area.right, area.centerY());

        } else {
            // Calculate the scale
            float xScale = area.width() / (float) data.length;
            float yScale = area.height() / 255.0f;

            // Create the path
            for (int index = 0; index < data.length; index++) {
                // Get the signed value
                int value = data[index] & 0xff;

                // Calculate the position
                float x = area.left + index * xScale;
                float y = area.top + value * yScale;

                // Add to path the new position
                if (index == 0)
                    this.mGenericPath.moveTo(x, y);
                else
                    this.mGenericPath.lineTo(x, y);
            }
        }

        // Draw the path on canvas
        this.mWavePaint.setColor(this.mColor);
        canvas.drawPath(this.mGenericPath, this.mWavePaint);
    }

    /**
     * Draw the choice icon by the button status.
     * If not pressed will draw a "play" icon.
     * If pressed will draw a "stop" icon.
     *
     * @param canvas the canvas
     * @param area   the bounds
     */
    private void drawIcon(Canvas canvas, Rect area) {
        // Load the icons
        this.loadIcons();

        // Calculate the proportional area
        int left = area.left;
        int top = area.top;
        int right = area.right;
        int bottom = area.bottom;

        if (area.width() > area.height()) {
            left = area.centerX() - area.height() / 2;
            right = area.centerX() + area.height() / 2;

        } else {
            top = area.centerY() + area.width() / 2;
            bottom = area.centerY() - area.width() / 2;
        }

        // Draw the icon inside the area
        Drawable icon = this.isSelected() ?
                ScPlayerButton.mStopIcon : ScPlayerButton.mPlayIcon;
        DrawableCompat.setTint(icon, this.mColor);
        icon.setBounds(left, top, right, bottom);
        icon.draw(canvas);
    }


    // **************************************************************************************
    // Override

    /**
     * Manage the single click event
     *
     * @param e the event
     * @return always true
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // Single click
        if (this.mDetector.onTouchEvent(e))
            this.fireClick();

        return true;
    }

    /**
     * Draw the component by the settings
     *
     * @param canvas to draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Get the drawing area
        this.mDrawingArea.set(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the time and get back the new drawing area reduced by the
        // time label height
        this.mDrawingArea = this.drawTime(canvas, this.mDrawingArea);

        // By the status
        if (this.isSelected() && this.checkRequestedPermission())
            // If pressed the sound is playing so draw the wave
            this.drawWave(canvas, this.mDrawingArea, this.mWaveToken);
        else
            // If not pressed not playing the media draw the icon
            this.drawIcon(canvas, this.mDrawingArea);
    }

    /**
     * Take the measure of the component
     *
     * @param widthMeasureSpec  measured width
     * @param heightMeasureSpec measured height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Find the global padding
        int widthGlobalPadding = this.getPaddingLeft() + this.getPaddingRight();
        int heightGlobalPadding = this.getPaddingTop() + this.getPaddingBottom();

        // Get suggested dimensions
        int width = View.getDefaultSize(this.getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = View.getDefaultSize(this.getSuggestedMinimumHeight(), heightMeasureSpec);

        // If have some dimension to wrap will use the path boundaries for have the right
        // dimension summed to the global padding.
        if (this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT)
            width = Math.round(this.dipToPixel(46) + widthGlobalPadding);
        if (this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT)
            height = Math.round(this.dipToPixel(46) + heightGlobalPadding);

        // Set the calculated dimensions
        this.setMeasuredDimension(width, height);
    }


    // ***************************************************************************************
    // Instance state

    /**
     * Save the current instance state
     *
     * @return the state
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        // Call the super and get the parent state
        Parcelable superState = super.onSaveInstanceState();

        // Create a new bundle for store all the variables
        Bundle state = new Bundle();
        // Save all starting from the parent state
        state.putParcelable("PARENT", superState);
        state.putString("mSource", this.mSource);
        state.putInt("mColor", this.mColor);
        state.putFloat("mFontSize", this.mFontSize);

        // Return the new state
        return state;
    }

    /**
     * Restore the current instance state
     *
     * @param state the state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Implicit conversion in a bundle
        Bundle savedState = (Bundle) state;

        // Recover the parent class state and restore it
        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        // Now can restore all the saved variables values
        this.mSource = savedState.getString("mSource");
        this.mColor = savedState.getInt("mColor");
        this.mFontSize = savedState.getFloat("mFontSize");
    }


    // ***************************************************************************************
    // Public methods

    /**
     * Get back the media duration.
     *
     * @return in milliseconds
     */
    @SuppressWarnings("unused")
    public long getDuration() {
        return this.mMediaDuration;
    }

    /**
     * Get the media playing status.
     *
     * @return true if playing
     */
    @SuppressWarnings("unused")
    public boolean isPlaying() {
        return this.mPlayer != null && this.mPlayer.isPlaying();
    }

    /**
     * Start to play the current media
     */
    @SuppressWarnings("unused")
    public void play() {
        this.startPlayMedia(this.mSource);
    }

    /**
     * Stop to play the current media
     */
    @SuppressWarnings("unused")
    public void stop() {
        this.stopPlayMedia();
    }


    // ***************************************************************************************
    // Public properties

    /**
     * Return the sound source path
     *
     * @return a path
     */
    @SuppressWarnings("unused")
    public String getSource() {
        return this.mSource;
    }

    /**
     * Set the sound source path
     *
     * @param value the path
     */
    @SuppressWarnings("unused")
    public void setSource(String value) {
        // Check if value is changed
        if (!this.equals(this.mSource, value)) {
            // Store the new value
            this.mSource = value;
            // Retrieve the new duration
            this.mMediaDuration = this.getMediaDuration(value);
        }
    }


    /**
     * Return the current foreground color
     *
     * @return a color
     */
    @SuppressWarnings("unused")
    public int getColor() {
        return this.mColor;
    }

    /**
     * Set the new current foreground color
     *
     * @param value the new color
     */
    @SuppressWarnings("unused")
    public void setColor(int value) {
        // Check if value is changed
        if (this.mColor != value) {
            // Store the new value
            this.mColor = value;
            this.invalidate();
        }
    }


    /**
     * Return the current font size
     *
     * @return a size in pixel
     */
    @SuppressWarnings("unused")
    public float getFontSize() {
        return this.mFontSize;
    }

    /**
     * Set the current font size
     *
     * @param value the new sizde in pixel
     */
    @SuppressWarnings("unused")
    public void setFontSize(float value) {
        // Check if value is changed
        if (this.mFontSize != value) {
            // Store the new value
            this.mFontSize = value;
            this.invalidate();
        }
    }


    /**
     * Return the current media player volume
     *
     * @return the volume
     */
    @SuppressWarnings("unused")
    public float getVolume() {
        return this.mVolume;
    }

    /**
     * Set the current media player volume
     *
     * @param value the new volume
     */
    @SuppressWarnings("unused")
    public void setVolume(float value) {
        // Check if value is changed
        if (this.mVolume != value) {
            // Store the new value
            this.mVolume = value;

            // Set directly in the player
            if (this.mPlayer.isPlaying())
                this.mPlayer.setVolume(value, value);
        }
    }


    /**
     * Return the current media player position.
     *
     * @return the position
     */
    @SuppressWarnings("unused")
    public int getPosition() {
        return this.mPosition;
    }

    /**
     * Set the current media player position
     *
     * @param value the new volume
     */
    @SuppressWarnings("unused")
    public void setPosition(int value) {
        // Fix the value
        if (value < 0) value = 0;

        // Check if value is changed
        if (this.mPosition != value) {
            // Store the new value
            this.mPosition = value;

            // Set directly in the player
            if (this.mPlayer.isPlaying())
                this.mPlayer.seekTo(value);
        }
    }


    // *******************************************************************************************
    // Public listener and interface

    /**
     * Generic event listener
     */
    @SuppressWarnings("all")
    public interface OnEventListener {

        /**
         * When start to play some media.
         *
         * @param player the media player object
         */
        void onStartPlay(MediaPlayer player);

        /**
         * When stop to play the media
         */
        void onStopPlay();

    }

    /**
     * Set the generic event listener
     *
     * @param listener the listener
     */
    @SuppressWarnings("unused")
    public void setOnEventListener(OnEventListener listener) {
        this.mEventListener = listener;
    }

}
