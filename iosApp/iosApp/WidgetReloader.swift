//
//  WidgetReloader.swift
//  iosApp
//
//  Created by Steven Kideckel on 2024-12-12.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import WidgetKit
import ComposeApp

class IosWidgetReloader : WidgetReloader {
    func reloadWidgets() {
        WidgetCenter.shared.reloadAllTimelines()
    }
}
