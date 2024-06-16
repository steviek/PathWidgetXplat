package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.api.StationSort
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.widget.WidgetData.StationData
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class StationDataComparator(
    order: StationSort?,
    now: LocalDateTime = now().toLocalDateTime(TimeZone.currentSystemDefault()),
) : Comparator<StationData> {

    // TODO: Fix this
    private val delegate = StationComparator(order, null, now)

    override fun compare(data1: StationData, data2: StationData): Int {
        val first = Stations.All.firstOrNull { it.pathApiName == data1.id } ?: return 0
        val second = Stations.All.firstOrNull { it.pathApiName == data2.id } ?: return 0
        return delegate.compare(first, second)
    }
}
