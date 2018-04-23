package com.weathergolite.randy_lin.weathergolite;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        }

        @SuppressLint("MissingPermission")
        public void onProviderDisabled(String provider) {
        }

        @SuppressLint("MissingPermission")
        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private Handler handler = new Handler();
    private LocationManager locationManager;
    private NetworkChangeReceiver networkChangeReceiver;
    private Weather weather;
    private TextView locationT;
    private TextView temperatureT;
    private TextView temperatureAT;
    private TextView RprobabilityT;
    private TextView humidityT;
    private TextView windspeedT;
    private TextView windinfoT;
    private ImageView weatherImage;
    private LineChart Tchart;
    private BarChart RPchart;
    private String[] geoLocation;
    private String[] time;
    private String[] T;
    private String[] AT;
    private String[] PoP6h;
    private String[] RH;
    private String[] Wind;
    private String[] WindInfo;
    private String[] WeatherCode;
    private int[] weather_icon;
    private ArrayList<String> xVals;
    private ArrayList<Entry> TyVals;
    private ArrayList<BarEntry> RPyVals;
    private Toast toastMsg;
    private int h;
    private int index;
    private Runnable updateTimer = new Runnable() {
        public void run() {
            Time t = new Time();
            t.setToNow();
            if (h != t.hour) {
                index = 1;
                h = t.hour;
                if (!isNetworkConnected(getApplicationContext())) return;
                weather = new Weather();
                requestPermissions(
                        new String[]{
                                ACCESS_COARSE_LOCATION,
                                ACCESS_FINE_LOCATION},
                        123);
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        detectDevice();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        locationT = this.findViewById(R.id.location);
        temperatureT = this.findViewById(R.id.temperature);
        temperatureAT = this.findViewById(R.id.Atemperature);
        RprobabilityT = this.findViewById(R.id.RProbability);
        humidityT = this.findViewById(R.id.humidity);
        windspeedT = this.findViewById(R.id.windspeed);
        windinfoT = this.findViewById(R.id.windinfo);
        weatherImage = this.findViewById(R.id.weather_icon);
        Tchart = this.findViewById(R.id.Tchart);
        RPchart = this.findViewById(R.id.RPchart);
        final MyScrollView p = this.findViewById(R.id.scrollView);
        p.setOnScrollListener(new MyScrollView.OnScrollListener() {

            @Override
            public void onScroll(int oldY, int newY) {
                if (oldY < 100 && newY >= 100) {
                    RPchart.animateY(1000);
                    RPchart.invalidate();
                }
            }
        });

        RelativeLayout locationTitle = (RelativeLayout) this.findViewById(R.id.locationTitle);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) locationTitle.getLayoutParams();
        params.setMargins(0, getStatusBarHeight() + 15, 0, 0);
        locationTitle.setLayoutParams(params);

        xVals = new ArrayList<>();
        TyVals = new ArrayList<>();
        RPyVals = new ArrayList<>();
        h = -1;
        weather_icon = new int[]{
                0,
                R.drawable.weather_icon_main_1,
                R.drawable.weather_icon_main_2,
                R.drawable.weather_icon_main_3,
                R.drawable.weather_icon_main_4,
                R.drawable.weather_icon_main_5,
                R.drawable.weather_icon_main_6,
                R.drawable.weather_icon_main_7,
                R.drawable.weather_icon_main_night_1,
                R.drawable.weather_icon_main_night_5
        };
    }

    private void detectDevice() {
        boolean isNetworkConnected = isNetworkConnected(this);
        boolean isWifiConnected = ((WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
        if (!isNetworkConnected && !isWifiConnected) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("偵測裝置到尚未開啟WiFi或行動網路，這可能會使大部分功能無法使用。");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 123:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Location location = getLastKnowLocation();
                    if (location == null) return;
                    WeatherAsyncTask weatherAsyncTask = new WeatherAsyncTask(this, location);
                    weatherAsyncTask.execute();
                } else {
                    makeToast("拒絕授予權限，將使得大部分功能無法使用。");
                    if (!shouldShowRequestPermissionRationale(permissions[0])) {
                        Snackbar snackbar =
                                Snackbar.make(this.findViewById(R.id.snakcontainer_main), "前往取得GPS位置權限",
                                        Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("GOGO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivityForResult(intent, 111);
                            }
                        });
                        snackbar.show();
                    }
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 111) {
            requestPermissions(
                    new String[]{
                            ACCESS_COARSE_LOCATION,
                            ACCESS_FINE_LOCATION},
                    123);
        }
    }

    @SuppressLint("MissingPermission")
    public Location getLastKnowLocation() {
        Location location = null;
        try {
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            boolean isNetworkEnabled = isNetworkConnected(this);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            1000,
                            0, locationListener);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }

                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                1000,
                                0, locationListener);
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (location == null) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return location;
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.removeCallbacks(updateTimer);
        handler.post(updateTimer);
    }

    @Override
    protected void onResume() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onResume();
        if (!isNetworkConnected(this)) {
            networkChangeReceiver = new NetworkChangeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            this.registerReceiver(networkChangeReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(updateTimer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            this.unregisterReceiver(networkChangeReceiver);
            networkChangeReceiver = null;
        }
    }

    private void makeToast(CharSequence msg) {
        if (toastMsg != null)
            toastMsg.cancel();
        toastMsg = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toastMsg.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && getFragmentManager().getBackStackEntryCount() == 0) { // 攔截返回鍵
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("確定要結束應用程式嗎?")
                    .setNegativeButton("確定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    finish();
                                }
                            })
                    .setPositiveButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                }
                            }).show();
        }
        return true;
    }


    private void getWeatherInfo(String geolocation) throws JSONException {
        geoLocation = geolocation.split(",");
        if (!weather.getWeather(geolocation)) return;
        time = weather.getTime();
        T = weather.getT();
        AT = weather.getAT();
        PoP6h = weather.getPoP6h();
        RH = weather.getRH();
        Wind = weather.getWind();
        WindInfo = weather.getWindInfo();
        WeatherCode = weather.getWeatherCode();

        boolean changeT = false;
        if (index < 1) index = 1;

        for (; index < weather.size(); ++index) {
            if (Integer.valueOf(time[index - 1].substring(11, 13)) == h) {
                index--;
                break;
            }
            if (Integer.valueOf(time[index].substring(11, 13)) > h && Integer.valueOf(time[index - 1].substring(11, 13)) < h
                    || (h > 21 && Integer.valueOf(time[index].substring(11, 13)) == 0)) {
                index--;
                changeT = true;
                break;
            }
        }

        TyVals.clear();
        RPyVals.clear();
        xVals.clear();

        for (int i = 0, j = index; i < weather.size() && j < weather.size(); ++i, ++j) {
            TyVals.add(new Entry(Integer.valueOf(T[j]), i));  //建立Entry放入Y軸，一個entry代表一個顯示的值
            RPyVals.add(new BarEntry(Integer.valueOf(PoP6h[j >> 1]), i));
            xVals.add(time[j].substring(5, 7) + "/" + time[j].substring(8, 11) + "\n" + (changeT && i == 0 ? String.format("%02d:00", h) : time[j].substring(11, 16)));
        }
    }

    private void setWeatherInfo() {
        ((LinearLayout) findViewById(R.id.linearlayout)).setBackgroundResource((h > 17 || h < 6) ? R.drawable.background_main_2 : R.drawable.background_main);
        locationT.setText(geoLocation[1]);
        temperatureT.setText(T[index] + "°C");
        temperatureAT.setText("體感溫度: " + AT[index] + "°C");
        RprobabilityT.setText(PoP6h[index] + "%");
        humidityT.setText(RH[index] + "%");
        if (Wind[index] != null) {
            windspeedT.setText(Wind[index] + "m/s");
            if (Wind[index].indexOf("<=") >= 0)
                windspeedT.setTextSize(17);
            windinfoT.setText(WindInfo[index]);
        }
        weatherImage.setImageResource(weather_icon[weather_code(WeatherCode[index])]);
        setTchart();
        setRPcahrt();
    }

    private void setTchart() {
        Tchart.clear();
        LineDataSet dataSet = new LineDataSet(TyVals, "溫度");
        dataSet.setColors(new int[]{Color.WHITE});  //設置折線顏色，可多段不同色
        dataSet.setLineWidth(2f);
        dataSet.setHighlightEnabled(false); //設置點擊該筆資料時強調
        dataSet.setCircleSize(5);
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setCircleColorHole(Color.TRANSPARENT);
        dataSet.setValueTextSize(15f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new myValueFormatter("°C"));

        LineData data = new LineData(xVals, dataSet);
        Tchart.setNoDataTextDescription("溫度資訊獲取失敗");
        Tchart.setDescription("");  //設置圖表資訊
        Tchart.setData(data);
        Tchart.setDragEnabled(true);
        Tchart.setDrawGridBackground(false);
        Tchart.setExtraOffsets(20f, 10f, 18f, 30f);
        Tchart.setScaleEnabled(false); //不給縮放
        Tchart.getLegend().setEnabled(false);
        Tchart.setVisibleXRangeMaximum(3.5f); //X軸能見範圍
        Tchart.setXAxisRenderer(new CustomXAxisRenderer(Tchart.getViewPortHandler(), Tchart.getXAxis(), Tchart.getTransformer(YAxis.AxisDependency.LEFT)));
        Tchart.animateX(1000);  //動畫效果
        Tchart.setDragDecelerationFrictionCoef(0.9f); //拖曳動畫速度 [0,1)

        XAxis TxAxis = Tchart.getXAxis();
        TxAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  //設置X軸顯示位置
        TxAxis.setDrawGridLines(false); //隱藏網格線(X軸)
        TxAxis.setTextSize(14f);
        TxAxis.setLabelsToSkip(0);
        //TxAxis.setSpaceBetweenLabels(500);
        TxAxis.setTextColor(Color.WHITE);

        YAxis TyAxisleft = Tchart.getAxisLeft();
        TyAxisleft.setDrawGridLines(false); //隱藏網格線(Y軸)
        TyAxisleft.setEnabled(false);
        Tchart.getAxisRight().setEnabled(false); //隱藏右邊Y軸資訊
    }

    private void setRPcahrt() {
        RPchart.clear();
        BarDataSet dataSet = new BarDataSet(RPyVals, "降雨機率");
        dataSet.setColors(new int[]{Color.WHITE});
        dataSet.setHighlightEnabled(false);
        dataSet.setValueTextSize(15f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new myValueFormatter("%"));
        dataSet.setBarSpacePercent(70f);

        BarData data = new BarData(xVals, dataSet);
        XAxis RPxAxis = RPchart.getXAxis();
        YAxis RPyAxisleft = RPchart.getAxisLeft();

        RPchart.setNoDataTextDescription("降雨機率資訊獲取失敗");
        RPchart.setDescription("");
        RPyAxisleft.setAxisMaxValue(100f);
        RPchart.setData(data);
        RPchart.setDragEnabled(true);
        RPchart.setDrawGridBackground(false);
        RPchart.setExtraOffsets(15f, 10f, 0f, 30f);
        RPchart.setScaleEnabled(false);
        RPchart.getLegend().setEnabled(false);
        RPchart.setVisibleXRangeMaximum(4f);
        RPchart.setXAxisRenderer(new CustomXAxisRenderer(RPchart.getViewPortHandler(), RPchart.getXAxis(), RPchart.getTransformer(YAxis.AxisDependency.LEFT)));
        RPchart.animateY(1000);
        RPchart.setDragDecelerationFrictionCoef(0.9f);

        RPxAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        RPxAxis.setDrawGridLines(false);
        RPxAxis.setTextSize(14f);
        RPxAxis.setLabelsToSkip(0);
        //RPxAxis.setSpaceBetweenLabels(0);
        RPxAxis.setTextColor(Color.WHITE);

        RPyAxisleft.setDrawGridLines(false);
        RPyAxisleft.setEnabled(false);
        RPchart.getAxisRight().setEnabled(false);
    }

    private int weather_code(String code) {
        String[][] code_table = {
                {},
                {"01"},
                {"03", "05", "06"},
                {"43", "44", "45", "46"},
                {"04", "12", "13", "24", "26", "29", "34", "49", "57"},
                {"02", "07", "08"},
                {"17", "18", "31", "36", "58", "59"},
                {"60", "61"}
        };

        for (int i = 1; i < code_table.length; ++i)
            for (String num : code_table[i])
                if (num.equals(code)) {
                    if (h > 17 || h < 6) {
                        switch (i) {
                            case 1:
                                return 8;
                            case 5:
                                return 9;
                            default:
                                break;
                        }
                    }
                    return i;
                }
        return 0;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            getConnectivityStatus(context);
        }

        public void getConnectivityStatus(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                        || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    weather = new Weather();
                    Location location = getLastKnowLocation();
                    if (location == null) return;
                    WeatherAsyncTask weatherAsyncTask = new WeatherAsyncTask(MainActivity.this, location);
                    weatherAsyncTask.execute();
                }
            }
        }
    }

    private class WeatherAsyncTask extends AsyncTask<Void, Void, Void> {
        Location mlocation;
        private Context mContext;
        private ProgressDialog progressDialog;

        WeatherAsyncTask(Context c, Location loc) {
            mContext = c;
            mlocation = loc;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("正在載入天氣資訊...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.e("Pos", mlocation.getLatitude() + "," + mlocation.getLongitude());
                getWeatherInfo(new Geolocation(getApplicationContext()).getGeolocation(new LatLng(mlocation.getLatitude(), mlocation.getLongitude())));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setWeatherInfo();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            progressDialog.dismiss();
        }

    }

}
