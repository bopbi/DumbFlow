package com.bobbyprabowo.dumbflow

class Random {

    companion object {
        fun getRandomString(length: Int): String {
            val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return (1..length)
                .map { charset.random() }
                .joinToString("")
        }
    }
}
