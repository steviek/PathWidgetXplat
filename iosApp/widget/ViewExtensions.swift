//
//  ViewExtensions.swift
//  widgetExtension
//
//  Created by Steven Kideckel on 2024-06-16.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI

extension View {
    func measureTextHeight(maxSize: CGSize, text: String, font: UIFont) -> CGFloat {
        measureTextSize(maxSize: maxSize, text: text, font: font).height
    }
    
    func measureTextWidth(maxSize: CGSize, text: String, font: UIFont) -> CGFloat {
        measureTextSize(maxSize: maxSize, text: text, font: font).width
    }
    
    func measureTextSize(maxSize: CGSize, text: String, font: UIFont) -> CGRect {
        text.boundingRect(
            with: maxSize,
            options: [.usesLineFragmentOrigin, .usesFontLeading, .truncatesLastVisibleLine],
            attributes: [.font: font],
            context: nil
        )
    }
}
