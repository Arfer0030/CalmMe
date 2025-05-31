package com.example.calmme.commons

sealed class Resource<T> {
    data class Loading<T>(val isLoading: Boolean = true) : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}