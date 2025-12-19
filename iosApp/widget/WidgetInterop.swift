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
    func toSharedStationChoice() -> ComposeApp.StationChoice {
        let station: Station? = switch (self) {
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
        
        return if let station = station {
            ComposeApp.StationChoiceFixed(station: station)
        } else {
            ComposeApp.StationChoiceClosest()
        }
    }
    
    func getCommuteWidgetDestinationName(closestStationId: String?) -> String {
        IosResourceProvider().getCommuteWidgetDisplayName(choice: toSharedStationChoice(), closestStationId: closestStationId)
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
    func fetchDepartureBoardWidgetDataAsync(
        stationLimit: Int32,
        stations: [StationChoice],
        lines: [Line],
        filter: TrainFilter,
        sort: StationSort
    ) async -> FetchResult {
        let config = PathWidgetConfigurationDepartureBoard(
            stationLimit: stationLimit,
            stationChoices: stations.map { $0.toSharedStationChoice() },
            lines: lines,
            sort: sort,
            filter: filter
        )
        return await fetchWidgetDataAsync(config: config)
    }
    
    func fetchWidgetDataAsyncCommute(
        originStation: StationChoice,
        destinationStation: StationChoice,
        filter: TrainFilter,
        lines: [Line]
    ) async -> FetchResult {
        let config = PathWidgetConfigurationCommute(
            origin: originStation.toSharedStationChoice(),
            destination: destinationStation.toSharedStationChoice()
        )
        
        return await fetchWidgetDataAsync(config: config)
    }
    
    func fetchWidgetDataAsync(config: PathWidgetConfiguration) async -> FetchResult {
        do {
            return try await withCheckedThrowingContinuation { continuation in
                WidgetDataFetcher().fetchWidgetData(
                    config: config,
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
