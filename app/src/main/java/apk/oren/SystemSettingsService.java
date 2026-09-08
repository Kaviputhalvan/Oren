package apk.oren;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

import java.util.Locale;

public class SystemSettingsService extends Service {

	private static final String EXTRA_PERSIST = "persist";
	private static final String EXTRA_ORIENT = "orient";
	private static final String EXTRA_ROTATION = "rotation";

	public static final String P = "P";
	public static final String N = "N";

	public static final String L = "L";
	public static final String A = "A";

	public static final int PORTRAIT = 0;
	public static final int LANDSCAPE = 1;
	public static final int REVERSE_PORTRAIT = 2;
	public static final int REVERSE_LANDSCAPE = 3;

	private static final long REGULATE_INTERVAL = 500L;

	private final Handler handler = new Handler();

	private String persist = N;
	private String orient = L;
	private int rotation = PORTRAIT;

	private final Runnable regulateRunnable = new Runnable() {
		@Override
		public void run() {

			if (P.equals(persist)) {
				regulate();

				handler.postDelayed(
						this,
						REGULATE_INTERVAL
				);
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		LoggerService.info(
				this,
				"SystemSettingsService",
				"Service created"
		);
	}

	@Override
	public int onStartCommand(
			Intent intent,
			int flags,
			int startId) {

		LoggerService.info(
				this,
				"SystemSettingsService",
				"Service start requested"
		);

		if (!hasWriteSettingsPermission()) {

			LoggerService.warning(
					this,
					"SystemSettingsService",
					"WRITE_SETTINGS permission not granted"
			);

			requestWriteSettingsPermission();

			return START_STICKY;
		}

		readArguments(intent);

		LoggerService.info(
				this,
				"SystemSettingsService",
				"Arguments: persist=" + persist
						+ ", orient=" + orient
						+ ", rotation=" + rotation
		);

		handler.removeCallbacks(
				regulateRunnable
		);

		apply();

		if (P.equals(persist)) {

			LoggerService.info(
					this,
					"SystemSettingsService",
					"Persistent regulation enabled"
			);

			handler.post(
					regulateRunnable
			);

		} else {

			LoggerService.info(
					this,
					"SystemSettingsService",
					"Normal mode enabled"
			);
		}

		return START_STICKY;
	}

	private void readArguments(Intent intent) {

		if (intent == null) {

			LoggerService.debug(
					this,
					"SystemSettingsService",
					"No intent arguments"
			);

			return;
		}

		String p =
				intent.getStringExtra(EXTRA_PERSIST);

		String o =
				intent.getStringExtra(EXTRA_ORIENT);

		if (p != null) {
			persist =
					p.toUpperCase(Locale.ROOT);
		}

		if (o != null) {
			orient =
					o.toUpperCase(Locale.ROOT);
		}

		if (intent.hasExtra(EXTRA_ROTATION)) {

			rotation =
					intent.getIntExtra(
							EXTRA_ROTATION,
							PORTRAIT
					);

			LoggerService.debug(
					this,
					"SystemSettingsService",
					"Requested rotation=" + rotation
			);

		} else {

			rotation =
					getCurrentRotation();

			LoggerService.debug(
					this,
					"SystemSettingsService",
					"No rotation supplied; using current rotation="
							+ rotation
			);
		}

		if (!P.equals(persist) &&
			!N.equals(persist)) {

			LoggerService.warning(
					this,
					"SystemSettingsService",
					"Invalid persist mode; using N"
			);

			persist = N;
		}

		if (!L.equals(orient) &&
			!A.equals(orient)) {

			LoggerService.warning(
					this,
					"SystemSettingsService",
					"Invalid orientation mode; using L"
			);

			orient = L;
		}

		if (!isValidRotation(rotation)) {

			LoggerService.warning(
					this,
					"SystemSettingsService",
					"Invalid rotation; using current rotation"
			);

			rotation =
					getCurrentRotation();
		}
	}

	private int getCurrentRotation() {

		try {

			return Settings.System.getInt(
					getContentResolver(),
					Settings.System.USER_ROTATION,
					PORTRAIT
			);

		} catch (Throwable e) {

			LoggerService.error(
					this,
					"SystemSettingsService",
					"Failed to read current rotation: "
							+ e.getClass().getSimpleName()
			);

			return PORTRAIT;
		}
	}

	private boolean isValidRotation(int value) {

		return value >= PORTRAIT &&
				value <= REVERSE_LANDSCAPE;
	}

	private void apply() {

		if (!hasWriteSettingsPermission()) {

			LoggerService.error(
					this,
					"SystemSettingsService",
					"Cannot apply settings: WRITE_SETTINGS permission missing"
			);

			return;
		}

		try {

			if (L.equals(orient)) {

				Settings.System.putInt(
						getContentResolver(),
						Settings.System.ACCELEROMETER_ROTATION,
						0
				);

				Settings.System.putInt(
						getContentResolver(),
						Settings.System.USER_ROTATION,
						rotation
				);

				LoggerService.ok(
						this,
						"SystemSettingsService",
						"Locked orientation applied: rotation="
								+ rotation
				);

			} else if (A.equals(orient)) {

				Settings.System.putInt(
						getContentResolver(),
						Settings.System.ACCELEROMETER_ROTATION,
						1
				);

				LoggerService.ok(
						this,
						"SystemSettingsService",
						"Automatic rotation enabled"
				);
			}

		} catch (Throwable e) {

			LoggerService.error(
					this,
					"SystemSettingsService",
					"Failed to apply settings: "
							+ e.getClass().getSimpleName()
			);
		}
	}

	private void regulate() {

		if (!P.equals(persist))
			return;

		if (!L.equals(orient))
			return;

		if (!hasWriteSettingsPermission())
			return;

		try {

			int auto =
					Settings.System.getInt(
							getContentResolver(),
							Settings.System.ACCELEROMETER_ROTATION,
							1
					);

			int current =
					Settings.System.getInt(
							getContentResolver(),
							Settings.System.USER_ROTATION,
							PORTRAIT
					);

			if (auto != 0 ||
				current != rotation) {

				LoggerService.warning(
						this,
						"SystemSettingsService",
						"Regulation corrected system state: auto="
								+ auto
								+ ", current="
								+ current
								+ ", required="
								+ rotation
				);

				Settings.System.putInt(
						getContentResolver(),
						Settings.System.ACCELEROMETER_ROTATION,
						0
				);

				Settings.System.putInt(
						getContentResolver(),
						Settings.System.USER_ROTATION,
						rotation
				);

				LoggerService.ok(
						this,
						"SystemSettingsService",
						"Persistent orientation restored"
				);
			}

		} catch (Throwable e) {

			LoggerService.error(
					this,
					"SystemSettingsService",
					"Regulation failed: "
							+ e.getClass().getSimpleName()
			);
		}
	}

	private boolean hasWriteSettingsPermission() {

		return Settings.System.canWrite(this);
	}

	private void requestWriteSettingsPermission() {

		LoggerService.info(
				this,
				"SystemSettingsService",
				"Opening WRITE_SETTINGS permission screen"
		);

		try {

			Intent intent =
					new Intent(
							Settings.ACTION_MANAGE_WRITE_SETTINGS
					);

			intent.setData(
					Uri.parse(
							"package:" + getPackageName()
					)
			);

			intent.addFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK
			);

			startActivity(intent);

		} catch (Throwable e) {

			LoggerService.error(
					this,
					"SystemSettingsService",
					"Failed to open WRITE_SETTINGS screen: "
							+ e.getClass().getSimpleName()
			);
		}
	}

	public boolean setMode(
			String persistMode,
			String orientMode) {

		return setMode(
				persistMode,
				orientMode,
				null
		);
	}

	public boolean setMode(
			String persistMode,
			String orientMode,
			Integer requestedRotation) {

		LoggerService.info(
				this,
				"SystemSettingsService",
				"setMode requested: persist="
						+ persistMode
						+ ", orient="
						+ orientMode
						+ ", rotation="
						+ requestedRotation
		);

		if (!hasWriteSettingsPermission()) {

			LoggerService.warning(
					this,
					"SystemSettingsService",
					"setMode rejected: WRITE_SETTINGS permission missing"
			);

			requestWriteSettingsPermission();

			return false;
		}

		if (persistMode == null ||
			orientMode == null) {

			LoggerService.warning(
					this,
					"SystemSettingsService",
					"setMode rejected: null mode"
			);

			return false;
		}

		persistMode =
				persistMode.toUpperCase(
						Locale.ROOT
				);

		orientMode =
				orientMode.toUpperCase(
						Locale.ROOT
				);

		if (!P.equals(persistMode) &&
			!N.equals(persistMode)) {

			LoggerService.warning(
					this,
					"SystemSettingsService",
					"setMode rejected: invalid persist mode="
							+ persistMode
			);

			return false;
		}

		if (!L.equals(orientMode) &&
			!A.equals(orientMode)) {

			LoggerService.warning(
					this,
					"SystemSettingsService",
					"setMode rejected: invalid orient mode="
							+ orientMode
			);

			return false;
		}

		persist = persistMode;
		orient = orientMode;

		if (requestedRotation == null) {

			rotation =
					getCurrentRotation();

		} else {

			rotation =
					requestedRotation;
		}

		if (!isValidRotation(rotation)) {

			LoggerService.warning(
					this,
					"SystemSettingsService",
					"setMode rejected: invalid rotation="
							+ rotation
			);

			return false;
		}

		handler.removeCallbacks(
				regulateRunnable
		);

		apply();

		if (P.equals(persist)) {

			handler.post(
					regulateRunnable
			);

			LoggerService.ok(
					this,
					"SystemSettingsService",
					"Persistent mode active"
			);

		} else {

			LoggerService.ok(
					this,
					"SystemSettingsService",
					"Normal mode active"
			);
		}

		return true;
	}

	public boolean setPersistentLocked(
			int rotation) {

		LoggerService.info(
				this,
				"SystemSettingsService",
				"setPersistentLocked rotation=" + rotation
		);

		return setMode(
				P,
				L,
				rotation
		);
	}

	public boolean setNormalLocked(
			int rotation) {

		LoggerService.info(
				this,
				"SystemSettingsService",
				"setNormalLocked rotation=" + rotation
		);

		return setMode(
				N,
				L,
				rotation
		);
	}

	public boolean setPersistentAuto() {

		LoggerService.info(
				this,
				"SystemSettingsService",
				"setPersistentAuto"
		);

		return setMode(
				P,
				A,
				null
		);
	}

	public boolean setNormalAuto() {

		LoggerService.info(
				this,
				"SystemSettingsService",
				"setNormalAuto"
		);

		return setMode(
				N,
				A,
				null
		);
	}

	@Override
	public void onDestroy() {

		handler.removeCallbacks(
				regulateRunnable
		);

		LoggerService.warning(
				this,
				"SystemSettingsService",
				"Service destroyed; regulation stopped"
		);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}
}