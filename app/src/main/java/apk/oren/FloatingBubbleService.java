package apk.oren;

import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class FloatingBubbleService extends Service {

    private static final String LOG_SOURCE =
            "FloatingBubbleService";

    private static final String CHANNEL_ID =
            "oren_bubble_service";

    private static final int NOTIFICATION_ID = 1002;

    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;
    private FrameLayout containerLayout;

    private ImageView imgBaseBubble;
    private ImageView imgBubbleP;
    private ImageView imgBubbleL;
    private ImageView imgBubbleRp;
    private ImageView imgBubbleRl;

    private boolean isExpanded = false;

    private int initialX;
    private int initialY;

    private float initialTouchX;
    private float initialTouchY;

    private static final long SIDE_SNAP_DELAY =
            20_000L;

    private static final long AUTO_COLLAPSE_DELAY =
            5_000L;

    private static final long CLICK_MAX_TIME =
            300L;

    private static final int DRAG_THRESHOLD =
            10;

    private final Handler handler =
            new Handler(
                    Looper.getMainLooper()
            );

    private final Runnable autoSideRunnable =
            new Runnable() {
                @Override
                public void run() {

                    LoggerService.debug(
                            FloatingBubbleService.this,
                            LOG_SOURCE,
                            "Side snap timer fired"
                    );

                    if (containerLayout != null
                            && windowManager != null) {

                        snapToScreenEdge();

                    } else {

                        LoggerService.warning(
                                FloatingBubbleService.this,
                                LOG_SOURCE,
                                "Side snap skipped: UI unavailable"
                        );
                    }
                }
            };

    private final Runnable autoCollapseRunnable =
            new Runnable() {
                @Override
                public void run() {

                    LoggerService.debug(
                            FloatingBubbleService.this,
                            LOG_SOURCE,
                            "Auto-collapse timer fired"
                    );

                    if (isExpanded
                            && containerLayout != null) {

                        collapseMenu();

                    } else {

                        LoggerService.debug(
                                FloatingBubbleService.this,
                                LOG_SOURCE,
                                "Auto-collapse skipped: menu already collapsed"
                        );
                    }
                }
            };

    @Override
    public void onCreate() {

	super.onCreate();

	createNotificationChannel();

	Notification notification = createNotification();

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {

		startForeground(
			NOTIFICATION_ID,
			notification,
			ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
		);

	} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

		startForeground(
			NOTIFICATION_ID,
			notification
		);
	}

	LoggerService.info(
		this,
		LOG_SOURCE,
		"Service created"
	);
}

    private Notification createNotification() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            return new Notification.Builder(
                    this,
                    CHANNEL_ID
            )
                    .setContentTitle("Oren")
                    .setContentText(
                            "Floating bubble running"
                    )
                    .setSmallIcon(
                            R.drawable.iconcfg
                    )
                    .setOngoing(true)
                    .build();

        } else {

            return new Notification.Builder(this)
                    .setContentTitle("Oren")
                    .setContentText(
                            "Floating bubble running"
                    )
                    .setSmallIcon(
                            R.drawable.iconcfg
                    )
                    .setOngoing(true)
                    .build();
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Oren Bubble",
                            NotificationManager
                                    .IMPORTANCE_MIN
                    );

            channel.setDescription(
                    "Oren floating bubble service"
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {

                manager.createNotificationChannel(
                        channel
                );
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        LoggerService.debug(
                this,
                LOG_SOURCE,
                "onBind called"
        );

        return null;
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId) {

        LoggerService.info(
                this,
                LOG_SOURCE,
                "Service start requested: startId="
                        + startId
        );

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {

            LoggerService.warning(
                    this,
                    LOG_SOURCE,
                    "Overlay permission not granted"
            );

            Toast.makeText(
                    this,
                    "Overlay permission required.",
                    Toast.LENGTH_LONG
            ).show();

            try {

                Intent permissionIntent =
                        new Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse(
                                        "package:"
                                                + getPackageName()
                                )
                        );

                permissionIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                startActivity(
                        permissionIntent
                );

                LoggerService.info(
                        this,
                        LOG_SOURCE,
                        "Overlay permission screen opened"
                );

            } catch (Throwable e) {

                LoggerService.error(
                        this,
                        LOG_SOURCE,
                        "Failed to open overlay permission screen: "
                                + e.getClass()
                                .getSimpleName()
                );
            }

            stopSelf();

            return START_NOT_STICKY;
        }

        if (containerLayout == null) {

            LoggerService.debug(
                    this,
                    LOG_SOURCE,
                    "Initializing bubble UI"
            );

            initBubbleUI();

        } else {

            LoggerService.debug(
                    this,
                    LOG_SOURCE,
                    "Bubble UI already initialized"
            );
        }

        return START_STICKY;
    }

    private void initBubbleUI() {

        try {

            windowManager =
                    (WindowManager)
                            getSystemService(
                                    WINDOW_SERVICE
                            );

            if (windowManager == null) {

                LoggerService.error(
                        this,
                        LOG_SOURCE,
                        "WindowManager unavailable"
                );

                stopSelf();
                return;
            }

            int layoutFlag;

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O) {

                layoutFlag =
                        WindowManager.LayoutParams
                                .TYPE_APPLICATION_OVERLAY;

                LoggerService.debug(
                        this,
                        LOG_SOURCE,
                        "Using TYPE_APPLICATION_OVERLAY"
                );

            } else {

                layoutFlag =
                        WindowManager.LayoutParams
                                .TYPE_PHONE;

                LoggerService.debug(
                        this,
                        LOG_SOURCE,
                        "Using TYPE_PHONE"
                );
            }

            int collapsedSizePx =
                    dpToPx(46);

            windowParams =
                    new WindowManager.LayoutParams(
                            collapsedSizePx,
                            collapsedSizePx,
                            layoutFlag,
                            WindowManager.LayoutParams
                                    .FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT
                    );

            windowParams.gravity =
                    Gravity.TOP
                            | Gravity.START;

            windowParams.x = 100;
            windowParams.y = 300;

            containerLayout =
                    new FrameLayout(this);

            containerLayout.setClipChildren(
                    false
            );

            containerLayout.setClipToPadding(
                    false
            );

            imgBaseBubble =
                    createBubbleView(
                            R.drawable.bubble_base,
                            46
                    );

            imgBubbleP =
                    createBubbleView(
                            R.drawable.bubble_p,
                            33
                    );

            imgBubbleL =
                    createBubbleView(
                            R.drawable.bubble_l,
                            33
                    );


            imgBubbleRl =
                    createBubbleView(
                            R.drawable.bubble_rl,
                            33
                    );
					
					imgBubbleRp =
                    createBubbleView(
                            R.drawable.bubble_rp,
                            33
                    );

            containerLayout.addView(
                    imgBubbleP
            );

            containerLayout.addView(
                    imgBubbleL
            );


            containerLayout.addView(
                    imgBubbleRl
            );
			
			containerLayout.addView(
                    imgBubbleRp
            );

            containerLayout.addView(
                    imgBaseBubble
            );

            hideChildBubbles();

            setupTouchAndLongClickListeners();

            imgBubbleP.setOnClickListener(
                    v -> {

                        LoggerService.info(
                                this,
                                LOG_SOURCE,
                                "Portrait bubble clicked"
                        );

                        resetInactivityTimers();

                        onBubblePClick();
                    }
            );

            imgBubbleL.setOnClickListener(
                    v -> {

                        LoggerService.info(
                                this,
                                LOG_SOURCE,
                                "Landscape bubble clicked"
                        );

                        resetInactivityTimers();

                        onBubbleLClick();
                    }
            );

            

            imgBubbleRl.setOnClickListener(
                    v -> {

                        LoggerService.info(
                                this,
                                LOG_SOURCE,
                                "Reverse landscape bubble clicked"
                        );

                        resetInactivityTimers();

                        onBubbleRlClick();
                    }
            );
			imgBubbleRp.setOnClickListener(
                    v -> {

                        LoggerService.info(
                                this,
                                LOG_SOURCE,
                                "Reverse portrait bubble clicked"
                        );

                        resetInactivityTimers();

                        onBubbleRpClick();
                    }
            );
			

            windowManager.addView(
                    containerLayout,
                    windowParams
            );

            LoggerService.ok(
                    this,
                    LOG_SOURCE,
                    "Bubble UI added at x="
                            + windowParams.x
                            + ", y="
                            + windowParams.y
            );

            resetInactivityTimers();

        } catch (Throwable e) {

            LoggerService.error(
                    this,
                    LOG_SOURCE,
                    "Failed to initialize bubble UI: "
                            + e.getClass()
                            .getSimpleName()
            );

            containerLayout = null;
            windowManager = null;
            windowParams = null;
        }
    }

    private ImageView createBubbleView(
            int imageResId,
            int sizeDp) {

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        dpToPx(sizeDp),
                        dpToPx(sizeDp)
                );

        params.gravity =
                Gravity.CENTER;

        ImageView imageView =
                new ImageView(this);

        imageView.setImageResource(
                imageResId
        );

        imageView.setLayoutParams(
                params
        );

        imageView.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        return imageView;
    }

    private void setupTouchAndLongClickListeners() {

    imgBaseBubble.setOnTouchListener(
            new View.OnTouchListener() {

                private long touchStartTime;
                private boolean moved;

                @Override
                public boolean onTouch(
                        View v,
                        MotionEvent event) {

                    switch (event.getActionMasked()) {

                        case MotionEvent.ACTION_DOWN:

                            stopInactivityTimers();

                            touchStartTime =
                                    System.currentTimeMillis();

                            moved = false;

                            initialX = windowParams.x;
                            initialY = windowParams.y;

                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();

                            LoggerService.debug(
                                    FloatingBubbleService.this,
                                    LOG_SOURCE,
                                    "Bubble touch DOWN"
                            );

                            return true;

                        case MotionEvent.ACTION_MOVE:

                            int deltaX =
                                    (int) (
                                            event.getRawX()
                                                    - initialTouchX
                                    );

                            int deltaY =
                                    (int) (
                                            event.getRawY()
                                                    - initialTouchY
                                    );

                            if (Math.abs(deltaX) > DRAG_THRESHOLD
                                    || Math.abs(deltaY) > DRAG_THRESHOLD) {

                                if (!moved) {

                                    LoggerService.debug(
                                            FloatingBubbleService.this,
                                            LOG_SOURCE,
                                            "Bubble drag started"
                                    );
                                }

                                moved = true;

                                windowParams.x =
                                        initialX + deltaX;

                                windowParams.y =
                                        initialY + deltaY;

                                if (windowManager != null
                                        && containerLayout != null) {

                                    try {

                                        windowManager.updateViewLayout(
                                                containerLayout,
                                                windowParams
                                        );

                                    } catch (Throwable e) {

                                        LoggerService.error(
                                                FloatingBubbleService.this,
                                                LOG_SOURCE,
                                                "Failed to update bubble position: "
                                                        + e.getClass()
                                                        .getSimpleName()
                                        );
                                    }
                                }
                            }

                            return true;

                        case MotionEvent.ACTION_UP:

                            long clickDuration =
                                    System.currentTimeMillis()
                                            - touchStartTime;

                            int diffX =
                                    Math.abs(
                                            (int) (
                                                    event.getRawX()
                                                            - initialTouchX
                                            )
                                    );

                            int diffY =
                                    Math.abs(
                                            (int) (
                                                    event.getRawY()
                                                            - initialTouchY
                                            )
                                    );

                            boolean isTouch =
                                    !moved
                                            && diffX < DRAG_THRESHOLD
                                            && diffY < DRAG_THRESHOLD;

                            if (isTouch) {

                                if (clickDuration >= 1000L) {

                                    LoggerService.info(
                                            FloatingBubbleService.this,
                                            LOG_SOURCE,
                                            "Bubble long-click: destroying bubble"
                                    );

                                    destroyBubble();
									stopService(
    new Intent(
        FloatingBubbleService.this,
        SystemSettingsService.class
    )
);
									enableAutoRotation();

                                } else if (clickDuration
                                        < CLICK_MAX_TIME) {

                                    LoggerService.info(
                                            FloatingBubbleService.this,
                                            LOG_SOURCE,
                                            "Bubble clicked: duration="
                                                    + clickDuration
                                                    + "ms"
                                    );

                                    if (isExpanded) {

                                        collapseMenu();

                                    } else {

                                        expandMenu();
                                    }

                                } else {

                                    resetInactivityTimers();
                                }

                            } else {

                                LoggerService.debug(
                                        FloatingBubbleService.this,
                                        LOG_SOURCE,
                                        "Bubble drag/touch ended: x="
                                                + windowParams.x
                                                + ", y="
                                                + windowParams.y
                                );

                                resetInactivityTimers();
                            }

                            return true;

                        case MotionEvent.ACTION_CANCEL:

                            LoggerService.debug(
                                    FloatingBubbleService.this,
                                    LOG_SOURCE,
                                    "Bubble touch CANCEL"
                            );

                            resetInactivityTimers();

                            return true;
                    }

                    return true;
                }
            }
    );
}

private void enableAutoRotation() {

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        && !Settings.System.canWrite(this)) {

    Intent intent = new Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:" + getPackageName())
    );

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);

    return;
}

            Settings.System.putInt(
                    getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION,
                    1
            );

        } catch (Exception ignored) {
        }
    }
private void destroyBubble() {

    try {

        stopInactivityTimers();

        if (windowManager != null
                && containerLayout != null) {

            try {

                windowManager.removeView(containerLayout);

            } catch (Throwable ignored) {
            }

            containerLayout = null;
        }

        isExpanded = false;

        LoggerService.info(
                FloatingBubbleService.this,
                LOG_SOURCE,
                "Bubble destroyed"
        );

    } catch (Throwable e) {

        LoggerService.error(
                FloatingBubbleService.this,
                LOG_SOURCE,
                "Bubble destroy failed: "
                        + e.getClass().getSimpleName()
        );
    }
}

    private void expandMenu() {

        if (isExpanded) {

            LoggerService.debug(
                    this,
                    LOG_SOURCE,
                    "Expand ignored: menu already expanded"
            );

            return;
        }

        LoggerService.info(
                this,
                LOG_SOURCE,
                "Expanding bubble menu"
        );

        isExpanded = true;

        resizeWindowContainer(200);

        imgBaseBubble.setImageResource(
                R.drawable.bubble_close
        );

        showChildBubble(
                imgBubbleP,
                0,
                -dpToPx(73)
        );

        showChildBubble(
                imgBubbleL,
                -dpToPx(73),
                0
        );

        showChildBubble(
                imgBubbleRp,
				0,
                dpToPx(73)
                
        );

        showChildBubble(
                imgBubbleRl,
               
                dpToPx(73),
				0
        );

        resetInactivityTimers();

        LoggerService.ok(
                this,
                LOG_SOURCE,
                "Bubble menu expanded"
        );
    }

    private void collapseMenu() {

        if (!isExpanded) {

            LoggerService.debug(
                    this,
                    LOG_SOURCE,
                    "Collapse requested while already collapsed"
            );

            resetInactivityTimers();

            return;
        }

        LoggerService.info(
                this,
                LOG_SOURCE,
                "Collapsing bubble menu"
        );

        isExpanded = false;

        imgBaseBubble.setImageResource(
                R.drawable.bubble_base
        );

        resetChildBubble(imgBubbleP);
        resetChildBubble(imgBubbleL);
        
        resetChildBubble(imgBubbleRl);
		resetChildBubble(imgBubbleRp);

        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        if (!isExpanded
                                && containerLayout != null
                                && windowManager != null) {

                            resizeWindowContainer(
                                    46
                            );
                        }
                    }
                },
                220L
        );

        resetInactivityTimers();

        LoggerService.ok(
                this,
                LOG_SOURCE,
                "Bubble menu collapsed"
        );
    }

    private void resizeWindowContainer(
            int sizeDp) {

        if (windowParams == null
                || containerLayout == null
                || windowManager == null) {

            LoggerService.warning(
                    this,
                    LOG_SOURCE,
                    "Resize skipped: UI unavailable"
            );

            return;
        }

        int sizePx =
                dpToPx(sizeDp);

        windowParams.width =
                sizePx;

        windowParams.height =
                sizePx;

        try {

            windowManager.updateViewLayout(
                    containerLayout,
                    windowParams
            );

            LoggerService.debug(
                    this,
                    LOG_SOURCE,
                    "Bubble container resized to "
                            + sizeDp
                            + "dp"
            );

        } catch (Throwable e) {

            LoggerService.error(
                    this,
                    LOG_SOURCE,
                    "Failed to resize bubble: "
                            + e.getClass()
                            .getSimpleName()
            );
        }
    }

    private void showChildBubble(
            ImageView view,
            float translationX,
            float translationY) {

        view.setVisibility(
                View.VISIBLE
        );

        view.animate()
                .translationX(
                        translationX
                )
                .translationY(
                        translationY
                )
                .alpha(1.0f)
                .setDuration(250L)
                .start();
    }

    private void resetChildBubble(
            ImageView view) {

        view.animate()
                .translationX(0)
                .translationY(0)
                .alpha(0.0f)
                .setDuration(200L)
                .withEndAction(
                        new Runnable() {
                            @Override
                            public void run() {

                                view.setVisibility(
                                        View.GONE
                                );
                            }
                        }
                )
                .start();
    }

    private void hideChildBubbles() {

        ImageView[] views = {
                imgBubbleP,
                imgBubbleL,
                imgBubbleRl,
				imgBubbleRp
        };

        for (ImageView view : views) {

            view.setVisibility(
                    View.GONE
            );

            view.setAlpha(
                    0.0f
            );

            view.setTranslationX(
                    0
            );

            view.setTranslationY(
                    0
            );
        }

        LoggerService.debug(
                this,
                LOG_SOURCE,
                "Child bubbles hidden"
        );
    }

    private void stopInactivityTimers() {

        handler.removeCallbacks(
                autoSideRunnable
        );

        handler.removeCallbacks(
                autoCollapseRunnable
        );

        LoggerService.debug(
                this,
                LOG_SOURCE,
                "Inactivity timers stopped"
        );
    }

    private void resetInactivityTimers() {

        stopInactivityTimers();

        handler.postDelayed(
                autoSideRunnable,
                SIDE_SNAP_DELAY
        );

        LoggerService.debug(
                this,
                LOG_SOURCE,
                "Side snap timer scheduled: "
                        + SIDE_SNAP_DELAY
                        + "ms"
        );

        if (isExpanded) {

            handler.postDelayed(
                    autoCollapseRunnable,
                    AUTO_COLLAPSE_DELAY
            );

            LoggerService.debug(
                    this,
                    LOG_SOURCE,
                    "Auto-collapse timer scheduled: "
                            + AUTO_COLLAPSE_DELAY
                            + "ms"
            );
        }
    }

    private void snapToScreenEdge() {

        if (windowManager == null
                || containerLayout == null
                || windowParams == null) {

            LoggerService.warning(
                    this,
                    LOG_SOURCE,
                    "Snap skipped: UI unavailable"
            );

            return;
        }

        int screenWidth =
                getResources()
                        .getDisplayMetrics()
                        .widthPixels;

        int middleX =
                screenWidth / 2;

        int targetX;

        if (windowParams.x
                + windowParams.width / 2
                < middleX) {

            targetX = 0;

        } else {

            targetX =
                    screenWidth
                            - windowParams.width;
        }

        LoggerService.info(
                this,
                LOG_SOURCE,
                "Snapping bubble: fromX="
                        + windowParams.x
                        + ", targetX="
                        + targetX
        );

        ValueAnimator animator =
                ValueAnimator.ofInt(
                        windowParams.x,
                        targetX
                );

        animator.setDuration(
                300L
        );

        animator.setInterpolator(
                new DecelerateInterpolator()
        );

        animator.addUpdateListener(
                animation -> {

                    if (windowManager != null
                            && containerLayout != null
                            && windowParams != null) {

                        windowParams.x =
                                (int)
                                        animation
                                                .getAnimatedValue();

                        try {

                            windowManager
                                    .updateViewLayout(
                                            containerLayout,
                                            windowParams
                                    );

                        } catch (Throwable e) {

                            LoggerService.error(
                                    this,
                                    LOG_SOURCE,
                                    "Snap update failed: "
                                            + e.getClass()
                                            .getSimpleName()
                            );
                        }
                    }
                }
        );

        animator.start();

        handler.postDelayed(
                autoSideRunnable,
                SIDE_SNAP_DELAY
        );

        LoggerService.ok(
                this,
                LOG_SOURCE,
                "Side snap animation started"
        );
    }

    private int dpToPx(int dp) {

        return (int) (
                dp
                        * getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }

    private void onBubblePClick() {

        LoggerService.info(
                this,
                LOG_SOURCE,
                "Portrait action selected"
        );
		Intent i = new Intent(this, SystemSettingsService.class);
        i.putExtra("persist", SystemSettingsService.P);
        i.putExtra("orient", SystemSettingsService.L);
        i.putExtra("rotation", SystemSettingsService.PORTRAIT);
        startService(i);
    }

    private void onBubbleLClick() {

        LoggerService.info(
                this,
                LOG_SOURCE,
                "Landscape action selected"
        );
		Intent i = new Intent(this, SystemSettingsService.class);
        i.putExtra("persist", SystemSettingsService.P);
        i.putExtra("orient", SystemSettingsService.L);
        i.putExtra("rotation", SystemSettingsService.LANDSCAPE);
        startService(i);
    }

    private void onBubbleRpClick() {

        LoggerService.info(
                this,
                LOG_SOURCE,
                "Reverse portrait action selected"
        );
		Intent i = new Intent(this, SystemSettingsService.class);
        i.putExtra("persist", SystemSettingsService.P);
        i.putExtra("orient", SystemSettingsService.L);
        i.putExtra("rotation", SystemSettingsService.REVERSE_PORTRAIT);
        startService(i);
    }

    private void onBubbleRlClick() {

        LoggerService.info(
                this,
                LOG_SOURCE,
                "Reverse landscape action selected"
        );
		Intent i = new Intent(this, SystemSettingsService.class);
        i.putExtra("persist", SystemSettingsService.P);
        i.putExtra("orient", SystemSettingsService.L);
        i.putExtra("rotation", SystemSettingsService.REVERSE_LANDSCAPE);
        startService(i);
    }

    @Override
    public void onDestroy() {

        LoggerService.warning(
                this,
                LOG_SOURCE,
                "Service destroying"
        );

        stopInactivityTimers();

        if (containerLayout != null
                && windowManager != null) {

            try {

                windowManager.removeView(
                        containerLayout
                );

                LoggerService.ok(
                        this,
                        LOG_SOURCE,
                        "Bubble view removed"
                );

            } catch (Throwable e) {

                LoggerService.error(
                        this,
                        LOG_SOURCE,
                        "Failed to remove bubble view: "
                                + e.getClass()
                                .getSimpleName()
                );
            }
        }

        containerLayout = null;
        windowManager = null;
        windowParams = null;

        super.onDestroy();

        LoggerService.info(
                this,
                LOG_SOURCE,
                "Service destroyed"
        );
    }
}