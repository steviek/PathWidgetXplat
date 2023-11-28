//
//  RefreshIntent.swift
//  iosApp
//
//  Created by Steven Kideckel on 2023-10-22.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import AppIntents
import WidgetKit

struct RefreshIntent : AppIntent {
    static var title: LocalizedStringResource = "Refresh"
    static var description = IntentDescription("Updates all the widgets")
    
    func perform() async throws -> some IntentResult {
        WidgetCenter.shared.reloadAllTimelines()
        return .result()
    }
}
