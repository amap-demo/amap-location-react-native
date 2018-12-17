package com.amaplocation.rn;

import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author hongming.wang
 * @date 2018/8/13
 * @mail hongming.whm@alibaba-inc.com
 */
public class AMapLocationModule extends ReactContextBaseJavaModule {

    AMapLocationClient locationClient = null;
    AMapLocationClientOption locationOption = null;
    /**
     * 用于发送时间到JavaScript
     */
    DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter = null;

    public AMapLocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        locationOption = new AMapLocationClientOption();
    }

    @Override
    public String getName() {
        return "AMapLocation";
    }


    /**
     * 开始定位
     * @param locationParams 定位参数
     *               <p>
     *                  示例中只传了3个参数,实际开发中可以根据需求自行控制传入的参数
     *               </p>
     */
    @ReactMethod
    public void startLocation(String locationParams){
        if(null == locationOption){
            locationOption = new AMapLocationClientOption();
        }
        //获取RCTDeviceEventEmitter
        if(null == eventEmitter){
            eventEmitter = getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        }
        /**
         * 设置定位模式为高精度模式，实际开发中可以根据需要自行设置
         */
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        if(null != locationParams){
            try {
                JSONObject json = new JSONObject(locationParams);
                /**
                 * 是否单次定位
                 */
                locationOption.setOnceLocation(json.optBoolean("onceLocation", false));
                /**
                 * 是否需要逆地理信息
                 */
                locationOption.setNeedAddress(json.optBoolean("needAddress", true));
                /**
                 * 定位间隔，单位毫秒
                 */
                locationOption.setInterval(json.optInt("interval", 2000));
            } catch (Throwable e){

            }
        }
        if(null == locationClient){
            locationClient = new AMapLocationClient(getReactApplicationContext());
        }
        locationClient.setLocationOption(locationOption);
        locationClient.setLocationListener(locationListener);
        locationClient.startLocation();
    }

    /**
     * 停止定位
     */
    @ReactMethod
    public void stopLocation() {
        if (null != locationClient) {
            locationClient.stopLocation();
        }
    }

    /**
     * 销毁
     */
    @ReactMethod
    public void destroyLocation() {
        if (null != locationClient) {
            locationClient.stopLocation();
            locationClient.unRegisterLocationListener(locationListener);
            locationClient.onDestroy();
        }
        locationClient = null;
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != locationClient) {

                int errorCode = location.getErrorCode();
                WritableMap resultMap = Arguments.createMap();
                resultMap.putString("callbackTime", formatUTC(System.currentTimeMillis(), null));
                if (errorCode == AMapLocation.LOCATION_SUCCESS) {
                    resultMap.putInt("code", 0);
                    resultMap.putDouble("lat", location.getLatitude());
                    resultMap.putDouble("lon", location.getLongitude());
                    resultMap.putString("addr", location.getAddress());
                    resultMap.putString("locTime", formatUTC(location.getTime(), null));
                } else {
                    resultMap.putInt("code", location.getErrorCode());
                    resultMap.putString("errorInfo", location.getErrorInfo());
                    resultMap.putString("errorDetail", location.getLocationDetail());
                }

                if(null != eventEmitter){
                    /**
                     * 发送事件到JavaScript
                     * 使用此种方式回调可以持续回调
                     */
                    eventEmitter.emit("locationChanged", resultMap);
                }
            }
        }
    };

    /**
     * 格式化时间
     * @param l
     * @param strPattern
     * @return
     */
    private String formatUTC(long l, String strPattern) {
        if (TextUtils.isEmpty(strPattern)) {
            strPattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat  sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
        return sdf == null ? "NULL" : sdf.format(l);
    }
}
