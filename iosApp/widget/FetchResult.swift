//
//  FetchResult.swift
//  widget2Extension
//
//  Created by Steven Kideckel on 2023-10-23.
//  Copyright © 2023 orgName. All rights reserved.
//

import ComposeApp

struct FetchResult {
    let data: DepartureBoardData?
    let hadInternet: Bool
    let hasError: Bool
    let hasPathError: Bool
}
