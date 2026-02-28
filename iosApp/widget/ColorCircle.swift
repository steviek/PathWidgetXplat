//
//  ColorCircle.swift
//  widgetExtension
//
//  Created by Steven Kideckel on 2024-06-19.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp
import WidgetKit

struct ColorCircle: View {
    
    @Environment(\.colorScheme) var colorScheme
    
    let size: CGFloat
    let colors: [ColorWrapper]
    
    var body: some View {
        let isDark = colorScheme == .dark
        let color1 = colors.first?.toColor(isDark: isDark) ?? Color.black
        let color2 = colors.count > 1 ? colors[1].toColor(isDark: isDark) : color1
        ZStack {
            Image(systemName: "circle.fill")
                .resizable()
                .desaturatedInLiquidGlass()
                .foregroundStyle(color1)
                .clipShape(Circle())
                .frame(width: size, height: size)
            
            Image(systemName: "circle.fill")
                .resizable()
                .desaturatedInLiquidGlass()
                .foregroundStyle(color2)
                .clipShape(SemiCircle())
                .rotationEffect(.degrees(90))
                .frame(width: size, height: size)

        }
        .overlay(isDark ? Circle().stroke(Color.white, lineWidth: 1) : nil)
        .frame(width: size, height: size)
        
    }
    
    private struct SemiCircle: Shape {
        func path(in rect: CGRect) -> Path {
            var path = Path()
            
            path.move(to: CGPoint(x: rect.minX, y: rect.midY))
            path.addArc(center: CGPoint(x: rect.midX, y: rect.midY), radius: rect.width / 2, startAngle: .degrees(0), endAngle: .degrees(180), clockwise: true)
            path.closeSubpath()
            
            return path
        }
    }
}
