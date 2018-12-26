package com.weathergolite.randy_lin.weathergolite;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    private static final int BACK_TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
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
    private ExpandableListView favoriteExplv;
    private ImageView weatherImage;
    private ImageButton favoriteBtn;
    private Button clearAllBtn;
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
    private String[] spinnerLocation;
    private ArrayList<String> xVals;
    private ArrayList<Entry> TyVals;
    private ArrayList<BarEntry> RPyVals;
    private ArrayList<String> favoriteList;
    private MyExpandableListAdapter favoriteExpAdapter;
    private Toast toastMsg;
    private SharedPreferences locationKeepSP;
    private SharedPreferences.Editor locationKeepEditor;
    private WeatherAsyncTask weatherAsyncTask;
    private DrawerLayout drawer;
    private int favoriteCount;
    private int h;
    private int index;
    private int[] weather_icon;
    private int autoPosition;  // 0-spinner custom 1-auto 2-favorite custom
    private Runnable updateTimer = new Runnable() {
        public void run() {
            int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (h != nowHour) {
                index = 1;
                h = nowHour;
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
    private long mBackPressed;

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
        locationKeepSP = getSharedPreferences("location", MODE_PRIVATE);
        locationKeepEditor = locationKeepSP.edit();
        favoriteList = new ArrayList<String>();
        favoriteCount = locationKeepSP.getInt("count", 0);
        if (favoriteCount > 0) {
            for (int i = 0; i < favoriteCount; i++)
                favoriteList.add(locationKeepSP.getString("value[" + i + "]", "").replace(",", ", "));
            Collections.sort(favoriteList);
        }
        favoriteExplv = this.findViewById(R.id.favorite_location_explv);
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
        clearAllBtn = this.findViewById(R.id.favorite_clearall_btn);
        favoriteExpAdapter = new MyExpandableListAdapter(this, "My Favorite", favoriteList);
        favoriteExplv.setAdapter(favoriteExpAdapter);
        favoriteExplv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
                if (favoriteExplv.isGroupExpanded(groupPosition) || isFavoriteEmpty())
                    clearAllBtn.setVisibility(View.GONE);
                else
                    clearAllBtn.setVisibility(View.VISIBLE);
                return false;
            }
        });
        favoriteExplv.setOnItemLongClickListener(new ExpandableListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int pos, long id) {
                if (pos == 0) return true;  //Explv group
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(getResources().getString(R.string.favoriteSingleClearCheck, favoriteExplv.getItemAtPosition(pos).toString()))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < favoriteCount; i++) {
                                    if (locationKeepSP.getString("value[" + i + "]", "").equals(favoriteExplv.getItemAtPosition(pos).toString().replace(" ", ""))) {
                                        printSP();
                                        favoriteList.remove(pos - 1);
                                        locationKeepEditor.remove("value[" + (favoriteCount - 1) + "]");
                                        favoriteCount--;
                                        locationKeepEditor.remove("value[" + i + "]");
                                        String nextItem;
                                        for (int j = i; j < favoriteCount; j++) {
                                            nextItem = locationKeepSP.getString("value[" + (j + 1) + "]", "");
                                            locationKeepEditor.putString("value[" + j + "]", nextItem);
                                        }
                                        locationKeepEditor.putInt("count", favoriteCount);
                                        locationKeepEditor.apply();
                                        updateAdapter();
                                        makeToast("成功刪除");
                                        printSP();
                                    }
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                return true;
            }
        });
        favoriteExplv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                ((RadioGroup) findViewById(R.id.position_radio)).clearCheck();
                geoLocation = favoriteExpAdapter.getChild(groupPosition, childPosition).toString().split(",");
                autoPosition = 2;
                drawer.closeDrawers();
                return false;
            }
        });

        favoriteBtn = this.findViewById(R.id.favorite_btn);
        favoriteBtn.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (favoriteCount > 0) {
                    for (int i = 0; i < favoriteCount; i++) {
                        if (locationKeepSP.getString("value[" + i + "]", "").equals(geoLocation[0] + "," + geoLocation[1])) {
                            makeToast("該地點已存在於 My Favorite");
                            return;
                        }
                    }
                }
                locationKeepEditor.putString("value[" + favoriteCount + "]", geoLocation[0] + "," + geoLocation[1]);
                favoriteList.add(locationKeepSP.getString("value[" + favoriteCount + "]", "").replace(",", ", "));
                locationKeepEditor.putInt("count", ++favoriteCount);
                locationKeepEditor.apply();
                updateAdapter();
                makeToast("成功新增");
                printSP();
            }
        });
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
        final Spinner countrySpinner = (Spinner) findViewById(R.id.locationCountry_spinner);
        ArrayAdapter<CharSequence> adapList = ArrayAdapter.createFromResource(MainActivity.this, R.array.Country, R.layout.spinner_center_textview);
        adapList.setDropDownViewResource(R.layout.spinner_dropdown);
        countrySpinner.setAdapter(adapList);
        final Spinner citySpinner = (Spinner) findViewById(R.id.locationCity_spinner);
        countrySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<CharSequence> adapList2 = ArrayAdapter.createFromResource(MainActivity.this, country2id(parent.getSelectedItem().toString()), R.layout.spinner_center_textview);
                adapList2.setDropDownViewResource(R.layout.spinner_dropdown);
                citySpinner.setAdapter(adapList2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        DisplayMetrics monitorsize = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(monitorsize);
        int mPopupWindowHeight = monitorsize.heightPixels / 3;
        setPopupWindowHeight(countrySpinner, mPopupWindowHeight);
        setPopupWindowHeight(citySpinner, mPopupWindowHeight);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        AppBarLayout.LayoutParams tparams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        tparams.setMargins(0, getStatusBarHeight() - 20, 0, 0);
        toolbar.setLayoutParams(tparams);

        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (autoPosition == 0)
                    spinnerLocation = new String[]{countrySpinner.getSelectedItem().toString(), citySpinner.getSelectedItem().toString()};
                requestPermissions(
                        new String[]{
                                ACCESS_COARSE_LOCATION,
                                ACCESS_FINE_LOCATION},
                        123);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (isFavoriteEmpty())
                    favoriteExplv.setGroupIndicator(null);
                favoriteExplv.setIndicatorBounds(favoriteExplv.getRight() - 90, favoriteExplv.getRight() - 10);
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return false;
            }
        });

        RadioGroup postionRadioGroup = findViewById(R.id.position_radio);
        postionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.position_auto:
                        autoPosition = 1;
                        break;
                    case R.id.position_custom:
                        autoPosition = 0;
                        break;
                }
            }
        });
        xVals = new ArrayList<>();
        TyVals = new ArrayList<>();
        RPyVals = new ArrayList<>();
        h = -1;
        autoPosition = 1;
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

    private boolean isFavoriteEmpty() {
        return locationKeepSP.getString("value[" + 0 + "]", "").isEmpty();
    }

    public void favoriteClearAll(View view) {
        if (!isFavoriteEmpty()) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.favoriteAllClearCheck)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            locationKeepEditor.clear().apply();
                            updateAdapter();
                            makeToast("所有位置都被清除囉");
                            clearAllBtn.setVisibility(View.GONE);
                            //favoriteExplv.collapseGroup(0);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
    }

    private void printSP() {
        Log.e("Loading Shared Prefs", "-----------------------------------");
        Log.i("----------------", "---------------------------------------");
        SharedPreferences preference = getSharedPreferences("location", MODE_PRIVATE);
        Map<String, ?> prefMap = preference.getAll();
        Object prefObj;
        Object prefValue = null;
        for (String key : prefMap.keySet()) {
            prefObj = prefMap.get(key);
            if (prefObj instanceof String) prefValue = preference.getString(key, "STRING_ERROR");
            if (prefObj instanceof Integer) prefValue = preference.getInt(key, 0);
            Log.i(String.format("Shared Preference : %s - %s", "location", key),
                    String.valueOf(prefValue));
        }
        Log.i("----------------", "---------------------------------------");
        Log.i("Finished Shared Prefs", "----------------------------------");
    }

    private void updateAdapter() {
        favoriteList.clear();
        favoriteCount = locationKeepSP.getInt("count", 0);
        if (favoriteCount > 0) {
            for (int i = 0; i < favoriteCount; i++)
                favoriteList.add(locationKeepSP.getString("value[" + i + "]", "").replace(",", ", "));
            Collections.sort(favoriteList);
        }
        favoriteExplv.collapseGroup(0);
        favoriteExplv.expandGroup(0);
        if (isFavoriteEmpty()) {
            clearAllBtn.setVisibility(View.GONE);
            favoriteExplv.setGroupIndicator(null);
        } else {
            clearAllBtn.setVisibility(View.VISIBLE);
            favoriteExplv.setGroupIndicator(ResourcesCompat.getDrawable(getResources(), R.drawable.favorite_expandlist_indicator, null));
        }

        //favoriteExpAdapter.refresh(favoriteExplv, 0);
    }

    private void setPopupWindowHeight(Spinner mSpinner, int height) {
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(mSpinner);
            popupWindow.setHeight(height);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private int country2id(String c) {
        switch (c) {
            case "宜蘭縣":
                return R.array.宜蘭縣;
            case "桃園市":
                return R.array.桃園市;
            case "新竹縣":
                return R.array.新竹縣;
            case "苗栗縣":
                return R.array.苗栗縣;
            case "彰化縣":
                return R.array.彰化縣;
            case "南投縣":
                return R.array.南投縣;
            case "雲林縣":
                return R.array.雲林縣;
            case "嘉義縣":
                return R.array.嘉義縣;
            case "屏東縣":
                return R.array.屏東縣;
            case "台東縣":
                return R.array.台東縣;
            case "花蓮縣":
                return R.array.花蓮縣;
            case "澎湖縣":
                return R.array.澎湖縣;
            case "基隆市":
                return R.array.基隆市;
            case "新竹市":
                return R.array.新竹市;
            case "嘉義市":
                return R.array.嘉義市;
            case "台北市":
                return R.array.台北市;
            case "高雄市":
                return R.array.高雄市;
            case "新北市":
                return R.array.新北市;
            case "台中市":
                return R.array.台中市;
            case "台南市":
                return R.array.台南市;
            case "連江縣":
                return R.array.連江縣;
            case "金門縣":
                return R.array.金門縣;
        }
        return -1;
    }

    private void detectDevice() {
        boolean isNetworkConnected = isNetworkConnected(this);
        boolean isWifiConnected = ((WifiManager) Objects.requireNonNull(this.getApplicationContext().getSystemService(Context.WIFI_SERVICE))).isWifiEnabled();
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
            NetworkInfo mNetworkInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 123:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    switch (autoPosition) {
                        case 0: //spinner custom
                            geoLocation = spinnerLocation;
                            Log.e("@@@@@@@@", geoLocation[0] + "," + geoLocation[1]);
                            weatherAsyncTask = new WeatherAsyncTask(
                                    MainActivity.this,
                                    geoLocation[0] + "," + geoLocation[1]);
                            weatherAsyncTask.execute();
                            break;
                        case 1:  //auto postion
                            Location location = getLastKnowLocation();
                            try {
                                //geoLocation = new Geolocation(getApplicationContext()).getGeolocation(new LatLng(location.getLatitude(), location.getLongitude())).split(",");
                                geoLocation = new Geolocation(getApplicationContext()).getGeolocation(new LatLng(24.970128, 121.266241)).split(",");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            weatherAsyncTask = new WeatherAsyncTask(
                                    MainActivity.this,
                                    geoLocation[0] + "," + geoLocation[1]);
                            weatherAsyncTask.execute();
                            break;
                        case 2: //favorite custom
                            if (geoLocation != null) {
                                weatherAsyncTask = new WeatherAsyncTask(
                                        MainActivity.this,
                                        geoLocation[0] + "," + geoLocation[1]);
                                weatherAsyncTask.execute();
                            }
                            break;
                    }
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (mBackPressed + BACK_TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "再一次「返回」離開", Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
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
            xVals.add(time[j].substring(5, 7) + "/" + time
                    [j].substring(8, 11) + "\n" + (changeT && i == 0 ? String.format("%02d:00", h) : time[j].substring(11, 16)));
        }
    }

    private void setWeatherInfo() {
        if (time == null) return;
        findViewById(R.id.linearlayout).setBackgroundResource((h > 17 || h < 6) ? R.drawable.background_main_2 : R.drawable.background_main);
        if (geoLocation != null)
            locationT.setText(geoLocation[1]);
        if (T[index] != null)
            temperatureT.setText(T[index] + "°C");
        if (AT[index] != null)
            temperatureAT.setText("體感溫度: " + AT[index] + "°C");
        if (PoP6h[index] != null)
            RprobabilityT.setText(PoP6h[index] + "%");
        if (RH[index] != null)
            humidityT.setText(RH[index] + "%");
        if (Wind[index] != null) {
            windspeedT.setText(Wind[index] + "m/s");
            if (Wind[index].contains("<="))
                windspeedT.setTextSize(17);
            windinfoT.setText(WindInfo[index]);
        }
        if (WeatherCode != null)
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

    public void printSP(View view) {
        printSP();
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            getConnectivityStatus(context);
        }

        public void getConnectivityStatus(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = Objects.requireNonNull(cm).getActiveNetworkInfo();
            if (activeNetwork != null) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                        || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    requestPermissions(
                            new String[]{
                                    ACCESS_COARSE_LOCATION,
                                    ACCESS_FINE_LOCATION},
                            123);
                }
            }
        }
    }

    private class WeatherAsyncTask extends AsyncTask<Void, Void, Void> {
        String mlocation;
        private Context mContext;
        private ProgressDialog progressDialog;

        WeatherAsyncTask(Context c, String loc) {
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
                Log.e("Pos", "custom " + mlocation);
                getWeatherInfo(mlocation);
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
