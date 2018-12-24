package com.weathergolite.randy_lin.weathergolite;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class Weather {

    private String[] Wx;            // weather info in text. e.g.,多雲.晴
    private String[] weatherCode;   // weather code mather Wx
    private String[] AT;            // apparent temperature (體感溫度)
    private String[] T;             // temperature
    private String[] RH;            // relative humidity (相對濕度)
    private String[] PoP6h;         // Probability of Precipitation (降雨率)
    private String[] Wind;
    private String[] WindInfo;
    private String[] time;
    private int size;

    public boolean getWeather(String geoloction) throws JSONException {
        if (geoloction == null) return false;
        JSONObject jObj = null;
        jObj = new JSONObject(getResult(geoloction));
        JSONArray weatherElement = jObj.getJSONObject("records")
                .getJSONArray("locations")
                .getJSONObject(0)
                .getJSONArray("location")
                .getJSONObject(0)
                .getJSONArray("weatherElement");
        JSONArray[] jAry = new JSONArray[weatherElement.length()];
        for (int i = 0; i < jAry.length; i++) {
            try {
                jAry[i] = weatherElement.getJSONObject(i).getJSONArray("time");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        size = jAry[0].length();
        int halfSize = size >> 1;
        Wx = new String[size];
        weatherCode = new String[size];
        AT = new String[size];
        T = new String[size];
        RH = new String[size];
        PoP6h = new String[halfSize];
        Wind = new String[size];
        WindInfo = new String[size];
        time = new String[size];
        for (int i = 0, halfi = 0; i < size; i++, halfi = i >> 1) {
            Wx[i] = jAry[0].getJSONObject(i).getJSONArray("elementValue").getJSONObject(0).getString("value");
            weatherCode[i] = jAry[0].getJSONObject(i).getJSONArray("elementValue").getJSONObject(1).getString("value");
            AT[i] = jAry[1].getJSONObject(i).getJSONArray("elementValue").getJSONObject(0).getString("value");
            T[i] = jAry[2].getJSONObject(i).getJSONArray("elementValue").getJSONObject(0).getString("value");
            RH[i] = jAry[3].getJSONObject(i).getJSONArray("elementValue").getJSONObject(0).getString("value");
            if (halfi < 11)
                PoP6h[halfi] = jAry[4].getJSONObject(halfi).getJSONArray("elementValue").getJSONObject(0).getString("value");
            /*------------------------  Json data got something wrong, waiting for Opendata  source be fixed. ------------------------*/
            /*Wind[i] = jAry[5].getJSONObject(i)
                    .getJSONArray("parameter")
                    .getJSONObject(2)
                    .getString("parameterValue");
            WindInfo[i] = jAry[5].getJSONObject(i)
                    .getJSONArray("parameter")
                    .getJSONObject(1)
                    .getString("parameterValue");*/
            time[i] = (String) jAry[1].getJSONObject(i).get("dataTime");
            //Log.e(i+" "+halfi+" "+time[i]," 溫度 "+T[i]+","+AT[i]+" 天氣 "+Wx[i]+" 濕度 "+RH[i]+" 降雨 "+PoP6h[halfi]);
            if (halfi > halfSize - 2) PoP6h[halfi] = PoP6h[halfSize - 2];
        }
        return true;
    }

    private String getResult(String geoloction) {
        final String GET_CWB_OPENDATA_REST_URL = "http://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-093?";
        final String AUTHORIZATION_VALUE = "CWB-5E972971-9EE4-49BC-8FAE-D9B516D0B672";
        final String locationIDFormat = "F-D0047-0%s";
        final String ELEMENTNAME = "AT,Wx,PoP6h,Wind,RH,T";

        String[] Location = geoloction.split(",");
        String url = GET_CWB_OPENDATA_REST_URL
                + "Authorization=" + AUTHORIZATION_VALUE + "&locationId=" + String.format(locationIDFormat, getlocaiotnID(Location[0]))
                + "&locationName=" + Location[1]
                + "&elementName=" + ELEMENTNAME + "&sort=time";
        StringBuilder sb = new StringBuilder();
        try {
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10 * 1000);
            connection.connect();

            String inputLine;
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String getlocaiotnID(String location) {
        switch (location) {
            case "宜蘭縣":
                return "01";
            case "桃園市":
                return "05";
            case "新竹縣":
                return "09";
            case "苗栗縣":
                return "13";
            case "彰化縣":
                return "17";
            case "南投縣":
                return "21";
            case "雲林縣":
                return "25";
            case "嘉義縣":
                return "29";
            case "屏東縣":
                return "33";
            case "台東縣":
                return "37";
            case "花蓮縣":
                return "41";
            case "澎湖縣":
                return "45";
            case "基隆市":
                return "49";
            case "新竹市":
                return "53";
            case "嘉義市":
                return "57";
            case "台北市":
                return "61";
            case "高雄市":
                return "65";
            case "新北市":
                return "69";
            case "台中市":
                return "73";
            case "台南市":
                return "77";
            case "連江縣":
                return "81";
            case "金門縣":
                return "85";
        }
        return null;
    }

    public boolean isEmpty() {
        return Wx == null || weatherCode == null || AT == null ||
                T == null || RH == null || PoP6h == null ||
                Wind == null || WindInfo == null || time == null;
    }

    public String[] getWx() {
        return Wx;
    }

    public String[] getWeatherCode() {
        return weatherCode;
    }

    public String[] getAT() {
        return AT;
    }

    public String[] getT() {
        return T;
    }

    public String[] getRH() {
        return RH;
    }

    public String[] getPoP6h() {
        return PoP6h;
    }

    public String[] getWind() {
        return Wind;
    }

    public String[] getWindInfo() {
        return WindInfo;
    }

    public String[] getTime() {
        return time;
    }

    public int size() {
        return size;
    }

}
