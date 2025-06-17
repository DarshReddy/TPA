package com.tp.tpa.network

sealed class ApiResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : ApiResult<T>()
    object Loading : ApiResult<Nothing>()
    data class Error(val message: String) : ApiResult<Nothing>()
}
