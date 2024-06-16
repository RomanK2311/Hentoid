package me.devsaki.hentoid.database.domains

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ShuffleRecord(
    @Id
    var id: Long = 0,
    val contentId: Long
) {
    constructor() : this(-1)
    constructor(contentId: Long) : this(0, contentId)
}