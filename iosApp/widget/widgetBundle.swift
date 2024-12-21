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
struct widgetBundle: WidgetBundle {

    init() {
        let locationHelper = LocationHelper()
        locationHelper.isWidget = true
        IosLocationProvider().requestDelegate = locationHelper
        NativeHolder().initialize(widgetReloader: IosWidgetReloader())
    }

    var body: some Widget {
        widget()
    }
}
