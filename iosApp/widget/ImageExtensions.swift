//
//  ImageExtensions.swift
//  iosApp
//
//  Created by Steven Kideckel on 2026-02-28.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import WidgetKit

extension Image {
    func desaturatedInLiquidGlass() -> some View {
        if #available(iOS 18.0, *) {
            return self.widgetAccentedRenderingMode(WidgetAccentedRenderingMode.desaturated)
        } else {
            return self
        }
    }
}

