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
    func toStation() -> Station {
        return switch (self) {
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
        limit: Int32,
        stations: [Station],
        filter: TrainFilter,
        sort: StationSort
    ) async -> FetchResult {
        do {
            return try await withCheckedThrowingContinuation { continuation in
                fetchWidgetData(
                    limit: limit,
                    stations: stations,
                    sort: sort,
                    filter: filter,
                    force: false,
                    onSuccess: { data in
                        continuation.resume(returning: FetchResult(data: data, hasError: false))
                    },
                    onFailure: { data in
                        continuation.resume(returning: FetchResult(data: data, hasError: true))
                    }
                )
            }
        } catch {
            return FetchResult(data: nil, hasError: true)
        }
        
    }
}
