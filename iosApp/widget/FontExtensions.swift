//
//  FontExtensions.swift
//  widgetExtension
//
//  Created by Assistant on 2024-12-19.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import UIKit

extension Font {
    /// Creates a font similar to Arima using system fonts
    /// Arima is a variable font family, so we'll use Avenir which has similar characteristics
    static func arimaStyle(size: CGFloat, weight: Font.Weight = .regular) -> Font {
        let uiWeight: UIFont.Weight
        switch weight {
        case .ultraLight:
            uiWeight = .ultraLight
        case .thin:
            uiWeight = .thin
        case .light:
            uiWeight = .light
        case .regular:
            uiWeight = .regular
        case .medium:
            uiWeight = .medium
        case .semibold:
            uiWeight = .semibold
        case .bold:
            uiWeight = .bold
        case .heavy:
            uiWeight = .heavy
        case .black:
            uiWeight = .black
        default:
            uiWeight = .regular
        }
        
        return Font(UIFont(name: "Iowan Old Style", size: size) ?? UIFont.systemFont(ofSize: size, weight: uiWeight))
    }
    
    /// Creates a bold Arima-style font
    static func arimaStyleBold(size: CGFloat) -> Font {
        return arimaStyle(size: size, weight: .bold)
    }
    
    /// Creates a medium weight Arima-style font
    static func arimaStyleMedium(size: CGFloat) -> Font {
        return arimaStyle(size: size, weight: .medium)
    }
}

extension UIFont {
    /// Creates a UIFont similar to Arima using Avenir
    static func arimaStyle(size: CGFloat, weight: UIFont.Weight = .regular) -> UIFont {
        return UIFont(name: "Avenir", size: size) ?? UIFont.systemFont(ofSize: size, weight: weight)
    }
    
    /// Creates a bold Arima-style UIFont
    static func arimaStyleBold(size: CGFloat) -> UIFont {
        return arimaStyle(size: size, weight: .bold)
    }
    
    /// Creates a medium weight Arima-style UIFont
    static func arimaStyleMedium(size: CGFloat) -> UIFont {
        return arimaStyle(size: size, weight: .medium)
    }
}


