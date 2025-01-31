//
//  AppDelegate.swift
//  iosApp
//
//  Created by Steven Kideckel on 2024-02-06.
//  Copyright © 2024 orgName. All rights reserved.
//
import ComposeApp
import Foundation
import FirebaseCore
import FirebaseAnalytics
import FirebaseCrashlytics

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        FirebaseApp.configure()
        NotificationCenter.default.addObserver(
            self, selector: #selector(handleNotification),
            name: Notification.Name("logEvent"),
            object: nil
        )
        
        Analytics().appLaunched()

        IosLocationProvider().requestDelegate = LocationHelper()
        
        var firstDayOfWeek: String? = nil
        if #available(iOS 16, *) {
            firstDayOfWeek = Locale.current.firstDayOfWeek.rawValue
        }
        IOSPlatform().setFirstDayOfWeek(firstDayOfWeek: firstDayOfWeek)
        
        NativeHolder().initialize(
            widgetReloader: IosWidgetReloader(),
            nonFatalReporter: { e in
                Crashlytics.crashlytics()
                    .record(error: e.asError())
            }
        )
        
        return true
    }
    
    @objc
    func handleNotification(notification: NSNotification) {
        guard var userInfo = notification.userInfo as? [String: Any] else {
            return
        }
    
        let eventName = userInfo["event_name"] as! String
        userInfo.removeValue(forKey: "event_name")
        
        FirebaseAnalytics.Analytics.logEvent(eventName, parameters: userInfo)
    }
}
