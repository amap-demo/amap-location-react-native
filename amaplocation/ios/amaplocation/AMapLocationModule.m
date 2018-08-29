//
//  AMapLocationModule.m
//  amaplocation
//
//  Created by liubo on 2018/8/28.
//  Copyright © 2018年 Facebook. All rights reserved.
//

#import "AMapLocationModule.h"
#import <AMapLocationKit/AMapLocationKit.h>
#import <AMapFoundationKit/AMapFoundationKit.h>

@interface AMapLocation()<AMapLocationManagerDelegate>

@property (nonatomic, strong) AMapLocationManager *locationManager;

@end

@implementation AMapLocation

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents
{
  return @[@"locationChanged"];
}

RCT_EXPORT_METHOD(startLocation:(NSString *)locationParams)
{
  if (self.locationManager == nil) {
    [self initLocationManager];
  }
  NSLog(@"locationParams:%@", locationParams);
  NSError *jsonError = nil;
  NSDictionary *params = [NSJSONSerialization JSONObjectWithData:[locationParams dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingAllowFragments error:&jsonError];
  if ([[params objectForKey:@"onceLocation"] boolValue]) {
    __weak typeof(self) weakSelf = self;
    [self.locationManager requestLocationWithReGeocode:NO completionBlock:^(CLLocation *location, AMapLocationReGeocode *regeocode, NSError *error) {
      if (error) {
        return;
      }
      if (location) {
        NSDictionary *resultDic = @{@"callbackTime": [NSString stringWithFormat:@"%f", [[NSDate date] timeIntervalSince1970]],
                                    @"code": @"0",
                                    @"lat": [NSString stringWithFormat:@"%f", location.coordinate.latitude],
                                    @"lon": [NSString stringWithFormat:@"%f", location.coordinate.longitude],
                                    @"addr": @"",
                                    @"locTime": [NSString stringWithFormat:@"%f", [location.timestamp timeIntervalSince1970]]
                                    };
        
        [weakSelf sendEventWithName:@"locationChanged" body:resultDic];
      }
    }];
  } else {
    [self.locationManager stopUpdatingLocation];
    [self.locationManager startUpdatingLocation];
  }
}

RCT_EXPORT_METHOD(stopLocation)
{
  NSLog(@"stopLocation");
  [self.locationManager stopUpdatingLocation];
  [self sendEventWithName:@"locationChanged" body:@{}];
}

RCT_EXPORT_METHOD(destroyLocation)
{
  NSLog(@"destroyLocation");
  if (self.locationManager) {
    self.locationManager.delegate = nil;
    self.locationManager = nil;
  }
}

#pragma mark - Life Cycle

- (void)initLocationManager {
  self.locationManager = [[AMapLocationManager alloc] init];
  self.locationManager.delegate = self;
}

#pragma mark - Delegate

- (void)amapLocationManager:(AMapLocationManager *)manager didFailWithError:(NSError *)error {
  NSLog(@"didFailWithError:%@", error);
}

- (void)amapLocationManager:(AMapLocationManager *)manager didUpdateLocation:(CLLocation *)location {
  NSLog(@"didUpdateLocation:%@", location);
  
  NSDictionary *resultDic = @{@"callbackTime": [NSString stringWithFormat:@"%f", [[NSDate date] timeIntervalSince1970]],
                              @"code": @"0",
                              @"lat": [NSString stringWithFormat:@"%f", location.coordinate.latitude],
                              @"lon": [NSString stringWithFormat:@"%f", location.coordinate.longitude],
                              @"addr": @"",
                              @"locTime": [NSString stringWithFormat:@"%f", [location.timestamp timeIntervalSince1970]]
                              };
  
  [self sendEventWithName:@"locationChanged" body:resultDic];
}

- (void)amapLocationManager:(AMapLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
  NSLog(@"didChangeAuthorizationStatus:%d", status);
}

@end
