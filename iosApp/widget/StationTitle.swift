//
//  StationTitle.swift
//  widgetExtension
//
//  Created by Steven Kideckel on 2024-06-19.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct StationTitle: View {
    let title: String
    let width: CGFloat
    let maxHeight: CGFloat
    
    var body: some View {
        HStack(spacing: 0) {
            //Spacer()
            Text(
                WidgetDataFormatter().formatHeadSign(
                    title: title,
                    fits: {
                        let titleSpace = width - 16
                        let textWidth = measureTextWidth(
                            maxSize: CGSize(width: width, height: maxHeight),
                            text: $0,
                            font: UIFont.systemFont(ofSize: 14, weight: .medium)
                        )
                        return (textWidth <= titleSpace).toKotlinBoolean()
                    }
                )
            )
            .multilineTextAlignment(.leading)
            .font(Font.system(size: 14, weight: .medium))
            //Spacer()
        }
    }
}
