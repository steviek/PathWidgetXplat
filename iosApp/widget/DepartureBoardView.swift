//
//  SwiftUIView.swift
//  widget2Extension
//
//  Created by Steven Kideckel on 2023-10-18.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct DepartureBoardView: View {

    @Environment(\.colorScheme) var colorScheme

    let entry: SimpleEntry

    var body: some View {
        return ZStack {
            VStack(spacing: 0) {
                if let data = entry.data {
                    let footerHeight = max(
                        measureTextHeight(
                            maxSize: entry.size,
                            text: "Updated",
                            font: UIFont.systemFont(ofSize: 12)
                        ),
                        32
                    )
                    let innerWidth = entry.size.width - 32
                    let innerHeight = entry.size.height - 20 - footerHeight

                    DepartureBoardStations(
                        entry: entry,
                        data: data,
                        width: innerWidth,
                        height: innerHeight
                    )
                        .frame(width: innerWidth, height: innerHeight)

                    HStack(alignment: .center, spacing: 0) {
                        if (entry.hasGlobalPathAlerts) {
                            // Show the error indicator if there are alerts. Clicking anywhere will open the app, which is fine.
                            let isDark = colorScheme == .dark
                            ZStack {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .resizable()
                                    .foregroundStyle(isDark ? .red : .orange)
                                    .aspectRatio(contentMode: .fit)
                                    .frame(width: 20, height: 20)
                                    .padding(4)
                            }
                            .padding(4)
                            
                        } else {
                            Button(intent: RefreshIntent()) {
                                Image(systemName: "arrow.2.circlepath")
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                                    .frame(width: 24, height: 24)
                            }
                            .padding(4)
                            .buttonStyle(.borderless)
                            .hidden()
                        }
                        

                        Spacer()

                        Text(getFooterText())
                            .font(Font.system(size: 12))

                        Spacer()

                        Button(intent: RefreshIntent()) {
                            Image(systemName: "arrow.triangle.2.circlepath")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(width: 24, height: 24)
                        }
                        .padding(4)
                        .buttonStyle(.borderless)
                    }
                }
            }
        }
        .padding([.horizontal], 12)
        .padding([.bottom], 4)
        .padding([.top], 16)
        .frame(width: entry.size.width, height: entry.size.height, alignment: .topLeading)
    }

    private func getFooterText() -> String {
        let formattedFetchTime = WidgetDataFormatter().formatTime(instant: entry.dataFrom.toKotlinInstant())

        if entry.hasError {
            return if footerTextFits(IosResourceProvider().getErrorLong()) {
                IosResourceProvider().getErrorLong()
            } else {
                IosResourceProvider().getErrorShort()
            }
        }

        if entry.configuration.timeDisplay == .clock {
            let longText = IosResourceProvider().getUpdatedAtTime(formattedFetchTime: formattedFetchTime)

            return if footerTextFits(longText) {
                longText
            } else {
                formattedFetchTime
            }
        }

        let displayTime = WidgetDataFormatter().formatTime(instant: entry.date.toKotlinInstant())
        let fullText = IosResourceProvider().getFullRelativeUpdatedAtTime(
            displayTime: displayTime,
            dataTime: formattedFetchTime
        )
        if footerTextFits(fullText) {
            return fullText
        }

        let shorterText = IosResourceProvider().getShorterRelativeUpdatedAtTime(
            displayTime: displayTime,
            dataTime: formattedFetchTime
        )
        if footerTextFits(shorterText) {
            return shorterText
        }

        return displayTime
    }

    private func footerTextFits(_ text: String) -> Bool {
        let maxWidth = entry.size.width - (64 + 24 + 16)
        return measureTextWidth(maxSize: entry.size, text: text, font: UIFont.systemFont(ofSize: 12)) <= maxWidth

    }
}
