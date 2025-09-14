//
//  WidgetInterop.swift
//  widget2Extension
//
//  Created by Steven Kideckel on 2023-10-23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import ComposeApp

extension SortOrder {
    func toStationSort() -> StationSort {
        return switch(self) {
        case .alphabetical:
            StationSort.alphabetical
        case .njAm:
            StationSort.njam
        case .nyAm:
            StationSort.nyam
        }
    }
}

extension StationChoice {
    func toStation() -> Station? {
        return switch (self) {
        case .closest:
            nil
        case .exp:
            Stations().ExchangePlace
        case .grove:
            Stations().GroveStreet
        case .harrison:
            Stations().Harrison
        case .hoboken:
            Stations().Hoboken
        case .jsq:
            Stations().JournalSquare
        case .newark:
            Stations().Newark
        case .newport:
            Stations().Newport
        case .christopher:
            Stations().ChristopherStreet
        case .ninth:
            Stations().NinthStreet
        case .fourteenth:
            Stations().FourteenthStreet
        case .twentyThird:
            Stations().TwentyThirdStreet
        case .thirtyThird:
            Stations().ThirtyThirdStreet
        case .wtc:
            Stations().WorldTradeCenter
        }
    }
}

extension LineChoice {
    func toLine() -> Line {
        return switch (self) {
        case .hob33:
            Line.hoboken33rd
        case .hobWtc:
            Line.hobokenwtc
        case .jsq33:
            Line.journalsquare33rd
        case .nwkWtc:
            Line.newarkwtc
        }
    }
}

extension Filter {
    func toTrainFilter() -> TrainFilter {
        return switch (self) {
        case .all:
            TrainFilter.all
        case .interstate:
            TrainFilter.interstate
        }
    }
}

extension WidgetDataFetcher {
    func fetchWidgetDataAsync(
        originStation: StationChoice,
        destinationStation: StationChoice,
        filter: TrainFilter,
        sort: StationSort,
        lines: [Line]
    ) async -> FetchResult {
        // Convert station choices to actual stations
        let origin = originStation.toStation()
        let destination = destinationStation.toStation()
        
        // If either station is nil (closest) or both stations are the same, return error
        guard let originStn = origin, let destStn = destination, originStn != destStn else {
            return FetchResult(data: nil, hadInternet: true, hasError: true, hasPathError: false)
        }
        
        do {
            return try await withCheckedThrowingContinuation { continuation in
                fetchWidgetData(
                    stationLimit: 1, // We only want to show one station
                    stations: [originStn, destStn], // Only show origin station
                    lines: lines,
                    sort: sort,
                    filter: filter,
                    includeClosestStation: false, // We're using specific stations
                    staleness: widgetFetchStaleness(force: false),
                    onSuccess: { data in
                        continuation.resume(returning: FetchResult(data: data, hadInternet: true, hasError: false, hasPathError: false))
                    },
                    onFailure: { (e, hadInternet, isPathError, data) in
                        continuation.resume(
                            returning: FetchResult(
                                data: data,
                                hadInternet: hadInternet.toBool(),
                                hasError: true,
                                hasPathError: isPathError.toBool()
                            )
                        )
                    }
                )
            }
        } catch {
            return FetchResult(data: nil, hadInternet: true, hasError: true, hasPathError: false)
        }

    }
}
