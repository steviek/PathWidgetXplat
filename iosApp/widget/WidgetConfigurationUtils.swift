//
//  WidgetConfigurationUtils.swift
//  widget2Extension
//
//  Created by Steven Kideckel on 2023-10-18.
//  Copyright © 2023 orgName. All rights reserved.
//

import WidgetKit

class WidgetConfigurationUtils {
    static func getWidgetLimit(family: WidgetFamily) -> Int {
        return switch (family) {
        case .systemSmall:
             1
        case .systemMedium:
             2
        case .systemLarge:
             4
        case .systemExtraLarge:
             4
        case .accessoryCircular:
             1
        case .accessoryRectangular:
             1
        case .accessoryInline:
             1
        @unknown default:
             1
        }
    }
}
