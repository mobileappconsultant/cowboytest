package com.example.bluetoothtestapp.utils

import java.lang.Exception

sealed class Resources<T> {
    data class Success<T>(val data: T) : Resources<T>()
    data class Failure<T>(val msg: String, val exception: Exception? = null) : Resources<T>()
    data class Loading<T>(val loading: Boolean) : Resources<T>()
}
