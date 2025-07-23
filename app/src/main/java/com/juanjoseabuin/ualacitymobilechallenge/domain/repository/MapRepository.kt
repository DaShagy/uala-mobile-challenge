package com.juanjoseabuin.ualacitymobilechallenge.domain.repository

import com.juanjoseabuin.ualacitymobilechallenge.domain.model.StaticMapConfig

interface MapRepository {
    suspend fun getStaticMap(config: StaticMapConfig): ByteArray?
}