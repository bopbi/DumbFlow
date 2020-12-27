package com.bobbyprabowo.dumbflow.domain

import kotlinx.coroutines.flow.Flow

interface GetData {

    fun execute() : Flow<String>
}
