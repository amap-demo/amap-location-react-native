/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {Component} from 'react';
import {Platform, StyleSheet, Text, View, Button, DeviceEventEmitter, NativeEventEmitter} from 'react-native';
import AMapLocation from './AMapLocation';


const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
  android:
    'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});

type Props = {};
var listener;
export default class App extends Component<Props> {
  state = {}
  //注册监听
  componentWillMount(){
    const locationManagerEmitter = new NativeEventEmitter(AMapLocation);
    const subscription = locationManagerEmitter.addListener('locationChanged', (result) => {
      console.log(result);
      this.setState(result);
    });

    this.listener = DeviceEventEmitter.addListener('locationChanged', (result) => {
      console.log(result);
        this.setState(result);
    });
  }
  //移除监听
  componentWillUnMount(){
    this.listener.remove();
    subscription.remove();
    AMapLocation.destroyLocation();
  }

  render() {
    return (
      <View style={styles.body}>
        <View style={styles.button}>
          <Button
            style={styles.item}
            onPress={startOnceLocation}
            title="单次定位"
            marginBottom='10'
          />
          <Button
            style={styles.item}
            marginBottom='10'
            onPress={startContinueLocation}
            title="连续定位"
          />
          <Button
            style={styles.item}
            marginBottom='10'
            onPress={stopLocation}
            title="停止定位"
          />
        </View>
        {Object.keys(this.state).map(key => (
          <View style={styles.item} key={key}>
            <Text style={styles.label}>{key}</Text>
            <Text>{this.state[key]}</Text>
          </View>
        ))}
      </View>
    );
  }
}

function startContinueLocation(){
  AMapLocation.startLocation('{"onceLocation":false, "needAddress":true,"interval": 2000}');
}

function startOnceLocation(){
  AMapLocation.startLocation('{"onceLocation":true, "needAddress":true,"interval": 2000}');
}

function stopLocation(){
  AMapLocation.stopLocation();
}


const styles = StyleSheet.create({
  body: {
    padding: 16,
  },
  item: {
    flexDirection: 'row',
    marginBottom: 4,
  },
  label: {
    color: '#0079FF',
    width: 120,
  },
  button: {
    backgroundColor:'#F079FF',
    textAlign: 'center',
    color:'#FFFFFF',
    marginBottom: 4,
    fontSize:16,
  },
}
);
