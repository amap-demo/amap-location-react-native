##  前述 

1. [高德官网申请Key](http://lbs.amap.com/dev/#/).
2.  阅读[开发指南](http://lbs.amap.com/api/android-location-sdk/locationsummary/).Android
3.  基于React Native 版本：0.56.0
4. 本工程是基于React Native环境创建，并不是创建了一个library，如需要修改成library请参考[官网](https://facebook.github.io/react-native/docs/native-modules-setup)

### React-Native 环境搭建
参考官网介绍：https://facebook.github.io/react-native/docs/getting-started.html
中文地址：https://reactnative.cn/docs/getting-started/

    本文只介绍MAC上的安装，Windows上的安装请参考官网介绍
1. 安装 Node 和 Watchman

    ```
    //node需要8.3及以上版本，如果低于该版本请进行升级
    brew install node
    brew install watchman
    ```
2. Yarn、React Native 的命令行工具（react-native-cli）
    ```
    npm install -g yarn react-native-cli
    ```
3. 创建新项目
    ```
    //如果需要指定版本，可以在末尾加上--version指定版本
    react-native init amap-location-react-native
    ```
### 配置AndroidManifest.xml
1. 注册定位SDK需求的权限

```
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.CALL_PHONE"/>
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```
2. 填写您的key

    注意此处的key是测试使用的key，在使用过程中请使用自己的key

```
<meta-data
        android:name="com.amap.api.v2.apikey"
        android:value="78ed9fba3da4ec7ae3eeac7e9d8ae34e"/>
```
3. 注册定位service

```
<service android:name="com.amap.api.location.APSService"/>
```

### 关键代码
#### 编写模块
##### AMapLocationModule.java
重写getName()方法

```
    @Override
    public String getName() {
        return "AMapLocation";
    }
```
通过@ReactMethod暴露方法给js

```
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
```
#### 注册模块
##### 编写AMapLocationPackage类实现ReactPackage接口
```
@Override
public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<NativeModule>();
    modules.add(new AMapLocationModule(reactContext));
    return modules;
}

@Override
public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
    return Collections.emptyList();
}
```
##### 注册package
在android/app/src/main/java/com/your-app-name/MainApplication.java中将AMapLocationPackage注册

```
@Override
protected List<ReactPackage> getPackages() {
  return Arrays.<ReactPackage>asList(
      new MainReactPackage(),
      new AMapLocationPackage()
  );
}
```
###Js中调用
1. 为了调用方便在根目录新建一个js文件AMapLocation.js
```
import {NativeModules} from 'react-native';
module.exports = NativeModules.AMapLocation;
```

2. App.js中引用AMapLocation
```
import AMapLocation from './AMapLocation';
```

3. 注册定位监听

```
export default class App extends Component<Props> {
  state = {}
  //注册监听
  componentWillMount(){
    this.listener = DeviceEventEmitter.addListener('locationChanged', (result) => {
      console.log(result);
        this.setState(result);
    });
  }
  //移除监听
  componentWillUnMount(){
    this.listener.remove();
    AMapLocation.destroyLocation();
  }
  //....
}
```
4. Js调用Java接口

```
//连续定位
function startContinueLocation(){
  AMapLocation.startLocation("{'onceLocation':false, 'needAddress':true,'interval': 2000}");
}
//单次定位
function startOnceLocation(){
  AMapLocation.startLocation("{'onceLocation':true, 'needAddress':true, 'interval': 2000}");
}
//停止定位
function stopLocation(){
  AMapLocation.stopLocation();
}
```
## 容易遇到的坑
1. 注册JS时间监听

    componentWillMount()和componentWillUnMount()需要写到“export default class App extends Component<Props> {..."内，否则各种错误。
2. 待补充

