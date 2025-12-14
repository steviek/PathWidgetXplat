//
//  widgetBundle.swift
//  widget
//
//  Created by Steven Kideckel on 2023-11-27.
//  Copyright © 2023 orgName. All rights reserved.
//

import WidgetKit
import SwiftUI
import ComposeApp

@main
struct PathWidgetBundle: WidgetBundle {

    init() {
        let locationHelper = LocationHelper()
        locationHelper.isWidget = true
        IosLocationProvider().requestDelegate = locationHelper
        
        var firstDayOfWeek: String? = nil
        if #available(iOS 16, *) {
            firstDayOfWeek = Locale.current.firstDayOfWeek.rawValue
        }
        IOSPlatform().setFirstDayOfWeek(firstDayOfWeek: firstDayOfWeek)
                
        NativeHolder().initialize(
            widgetReloader: IosWidgetReloader(),
            nonFatalReporter: { e in
                
            }
        )
    }

    var body: some Widget {
        CommuteWidget()
        DepartureWidget()
    }
}
