package ch.rhosys.realtime.data.local.repository

import ch.rhosys.realtime.data.local.db.dao.PlaceDao
import ch.rhosys.realtime.data.local.db.entity.PlaceEntity
import ch.rhosys.realtime.data.local.db.entity.toEntity
import ch.rhosys.realtime.domain.PlaceRepository
import ch.rhosys.realtime.domain.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomPlaceRepository @Inject constructor(
    private val dao: PlaceDao,
) : PlaceRepository {

    override fun getPlaces(): Flow<List<Place>> =
        dao.getAllPlaces().map { list -> list.map { it.toDomain() } }

    override suspend fun upsertPlace(
        name: String,
        lat: Double,
        lng: Double,
        sortOrder: Int,
        label: String?,
        photoUri: String?,
        gridX: Int,
        gridY: Int,
        gridWidth: Int,
        gridHeight: Int,
    ): Long = dao.insert(
        PlaceEntity(
            name = name,
            lat = lat,
            lng = lng,
            sortOrder = sortOrder,
            label = label,
            photoUri = photoUri,
            gridX = gridX,
            gridY = gridY,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
        )
    )

    override suspend fun updatePlace(place: Place) = dao.update(place.toEntity())

    override suspend fun deletePlace(id: Long) = dao.deleteById(id)
}
