package ch.rhosys.realtime.domain

import ch.rhosys.realtime.data.remote.dto.LocationsResponseDto
import ch.rhosys.realtime.domain.model.Connection
import ch.rhosys.realtime.domain.model.Departure
import ch.rhosys.realtime.domain.model.SearchEndpoint
import java.time.LocalDate
import java.time.LocalTime

interface TransportRepository {
    // date/time default to "now"; isArrivalTime false means "depart after", true means
    // "arrive by" — mirrors transport.opendata.ch's own date/time/isArrivalTime params.
    suspend fun getConnections(
        from: SearchEndpoint,
        to: SearchEndpoint,
        date: LocalDate = LocalDate.now(),
        time: LocalTime = LocalTime.now(),
        isArrivalTime: Boolean = false,
    ): List<Connection>
    suspend fun getStationboard(station: String): List<Departure>
    suspend fun getLocations(query: String): LocationsResponseDto
    suspend fun getLocationsByCoordinate(lat: Double, lng: Double): LocationsResponseDto
}
