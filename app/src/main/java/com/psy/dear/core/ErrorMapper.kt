package com.psy.dear.core

import com.psy.dear.R
import retrofit2.HttpException
import java.io.IOException

object ErrorMapper {
    fun map(throwable: Throwable?): Int {
        val error = throwable ?: return R.string.error_unknown
        return when (error) {
            is UnauthorizedException -> R.string.error_unauthorized
            is IOException -> R.string.error_network
            is HttpException -> {
                when (error.code()) {
                    401, 403, 404 -> R.string.error_unauthorized
                    else -> R.string.error_unknown
                }
            }
            else -> R.string.error_unknown
        }
    }
}
