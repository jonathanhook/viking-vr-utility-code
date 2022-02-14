package uoy.com.vikingpowersaver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.*;
import android.hardware.*;
import android.os.*;
import android.app.KeyguardManager;
import android.view.View;

public class VikingPowerSaverActivity extends AppCompatActivity implements SensorEventListener
{
    private float HPSENSITIVITY;
    private float SENSITIVITY;
    private long INITIALIZE_TIME;
    private String VIKING_PACKAGE;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private double[] lp;
    private double[] hp;
    private long startTime;
    private boolean initiaized = false;
    private boolean isUnityLoaded = false;

    private void Initialize()
    {
        initiaized = true;

        lp = new double[3];
        hp = new double[3];

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        HPSENSITIVITY = Float.parseFloat(getResources().getString(R.string.hp_sensitivity));
        SENSITIVITY = Float.parseFloat(getResources().getString(R.string.sensitivity));
        INITIALIZE_TIME = Long.parseLong(getResources().getString(R.string.initialize_time));
        VIKING_PACKAGE = getResources().getString(R.string.viking_package);
    }

    private void LoadUnityApp()
    {
        if(!isUnityLoaded)
        {
            isUnityLoaded = true;

            KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
            keyguardLock.disableKeyguard();

            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
            wakeLock.acquire();

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(VIKING_PACKAGE);
            startActivity(launchIntent);
        }
    }

    private void ReturnFromUnity()
    {
        if(isUnityLoaded)
        {
            isUnityLoaded = false;
            startTime = System.currentTimeMillis();

            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);

            PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
            PowerManager.WakeLock lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorRead");
            lock.acquire();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viking_power_saver);

        if(!initiaized)
        {
            Initialize();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        ReturnFromUnity();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            float[] a = event.values;
            lp[0] = ((1.0f - HPSENSITIVITY) * lp[0]) + (HPSENSITIVITY * a[0]);
            lp[1] = ((1.0f - HPSENSITIVITY) * lp[0]) + (HPSENSITIVITY * a[1]);
            lp[2] = ((1.0f - HPSENSITIVITY) * lp[2]) + (HPSENSITIVITY * a[2]);

            hp[0] = a[0] - lp[0];
            hp[0] = a[1] - lp[1];
            hp[0] = a[2] - lp[2];

            double magnitude = Math.sqrt(hp[0] * hp[0] + hp[1] * hp[1] + hp[2] * hp[2]);

            if (System.currentTimeMillis() - startTime > INITIALIZE_TIME && magnitude > SENSITIVITY)
            {
                LoadUnityApp();
            }
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}
