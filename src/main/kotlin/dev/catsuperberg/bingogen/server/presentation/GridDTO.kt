package dev.catsuperberg.bingogen.server.presentation

import kotlinx.serialization.Serializable

@Serializable
data class GridDTO(
    val rows: List<List<TaskDTO>>
)
