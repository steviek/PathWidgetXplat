//
//  LocationHelper.swift
//  iosApp
//
//  Created by Steven Kideckel on 2024-07-21.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import ComposeApp
import CoreLocation

class LocationHelper : NSObject, IosLocationProviderRequestDelegate, CLLocationManagerDelegate {
    
    private let locationManager = CLLocationManager()
    
    var isWidget = false
    
    override init() {
        super.init()

        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }
    
    func requestLocation() {
        if (isWidget && !locationManager.isAuthorizedForWidgetUpdates) {
            IosLocationProvider().onLocationCheckCompleted(result: LocationCheckResultNoPermission())
            return
        }

        locationManager.requestLocation()
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.first {
            IosLocationProvider().onLocationCheckCompleted(result: LocationCheckResultSuccess(location: Location(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude)))
        } else {
            IosLocationProvider().onLocationCheckCompleted(result: LocationCheckResultNoProvider())
        }
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        IosLocationProvider().onAuthorizationChanged(status: toKotlinEnum(manager.authorizationStatus))
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: any Error) {
        IosLocationProvider().onLocationCheckCompleted(result: LocationCheckResultFailure(throwable: KotlinRuntimeException(message: error.localizedDescription)))
    }
    
    func hasLocationPermission() -> Bool {
        if (isWidget && !locationManager.isAuthorizedForWidgetUpdates) {
            return false
        }
        
        switch locationManager.authorizationStatus {
        case .notDetermined:
            return false
        case .restricted:
            return false
        case .denied:
            return false
        case .authorizedAlways:
            return true
        case .authorizedWhenInUse:
            return true
        @unknown default:
            return false
        }
    }
    
    func requestLocationPermission() {
        locationManager.requestWhenInUseAuthorization()
    }
    
    private func toKotlinEnum(_ status: CLAuthorizationStatus) -> IosLocationProvider.IosLocationAuthorizationStatus {
        switch status {
        case .notDetermined:
            return .notdetermined
        case .restricted:
            return .restricted
        case .denied:
            return .denied
        case .authorizedAlways:
            return .always
        case .authorizedWhenInUse:
            return .wheninuse
        @unknown default:
            return .notdetermined
        }
    }
}
