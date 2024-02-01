package com.datascout.datascout.dto

data class Response<T>(

    val data: T? = null,
    val error: String? = null,
    val success: Boolean = true


) {
    constructor(data: T?) : this(data, null)
    constructor(error: String) : this(null, error, false)
    constructor() : this(null, null, true)
}