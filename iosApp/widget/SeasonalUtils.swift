//
//  SeasonalUtils.swift
//  widgetExtension
//
//  Created by Assistant on 2024-12-19.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Foundation

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }

        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

struct SeasonalUtils {
    
    /// Determines the current season based on equinoxes and solstices
    /// Returns the appropriate background image name
    static func getSeasonalBackgroundName(for date: Date) -> String {
        let calendar = Calendar.current
        let year = calendar.component(.year, from: date)
        
        // Approximate dates for equinoxes and solstices for the current year
        // These are typically around March 20, June 21, September 22, December 21
        let springEquinox = calendar.date(from: DateComponents(year: year, month: 3, day: 20))!
        let summerSolstice = calendar.date(from: DateComponents(year: year, month: 6, day: 21))!
        let fallEquinox = calendar.date(from: DateComponents(year: year, month: 9, day: 22))!
        let winterSolstice = calendar.date(from: DateComponents(year: year, month: 12, day: 21))!

        switch date {
        case springEquinox..<summerSolstice:
            return "SpringBackground"
        case summerSolstice..<fallEquinox:
            return "SummerBackground"
        case fallEquinox..<winterSolstice:
            return "FallBackground"
        default:
            return "WinterBackground"
        }
    }
    
    /// Determines the appropriate text color based on the current season
    /// Summer and Fall: white, Spring and Winter: black
    static func getSeasonalTextColor(for date: Date) -> Color {
        let calendar = Calendar.current
        let year = calendar.component(.year, from: date)
        
        // Approximate dates for equinoxes and solstices for the current year
        let springEquinox = calendar.date(from: DateComponents(year: year, month: 3, day: 20))!
        let summerSolstice = calendar.date(from: DateComponents(year: year, month: 6, day: 21))!
        let fallEquinox = calendar.date(from: DateComponents(year: year, month: 9, day: 22))!
        let winterSolstice = calendar.date(from: DateComponents(year: year, month: 12, day: 21))!

        switch date {
        case springEquinox..<summerSolstice:
            return Color(red: 0.173, green: 0.184, blue: 0.118) // Spring: black
        case summerSolstice..<fallEquinox:
            return .white // Summer: white
        case fallEquinox..<winterSolstice:
            return .white // Fall: white
        default:
            return Color(red: 0.294, green: 0.239, blue: 0.043) // Winter: dark brown
        }
    }
}
