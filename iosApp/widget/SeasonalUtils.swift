//
//  SeasonalUtils.swift
//  widgetExtension
//
//  Created by Assistant on 2024-12-19.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Foundation
import ComposeApp

struct SeasonalUtils {
    
    /// Returns the appropriate background image name for the current season
    static func getSeasonalBackgroundName(for date: Date) -> String {
        let season = SeasonUtils().getSeasonForInstant(instant: date.toKotlinInstant())
        switch season {
        case .spring:
            return "SpringBackground"
        case .summer:
            return "SummerBackground"
        case .fall:
            return "FallBackground"
        case .winter:
            return "WinterBackground"
        default:
            return "WinterBackground"
        }
    }
    
    /// Returns the appropriate text color for the current season
    /// Summer and Fall: white, Spring and Winter: custom dark colors
    static func getSeasonalTextColor(for date: Date) -> Color {
        let season = SeasonUtils().getSeasonForInstant(instant: date.toKotlinInstant())
        switch season {
        case .spring:
            return Color(red: 0.173, green: 0.184, blue: 0.118)
        case .summer:
            return .white
        case .fall:
            return .white
        case .winter:
            return Color(red: 0.294, green: 0.239, blue: 0.043)
        default:
            return .white
        }
    }
}
