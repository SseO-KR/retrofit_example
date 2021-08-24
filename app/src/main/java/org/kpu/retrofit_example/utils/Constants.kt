package org.kpu.retrofit_example.utils

object Constants {
    const val TAG : String = "로그"
}

enum class SEARCH_TYPE {
    PHOTO, USER
}

enum class RESPONSE_STATE{
    OKAY, FAIL
}

object API {
    const val BASE_URL : String = "https://api.unsplash.com/"

    const val CLIENT_ID : String = "EwgQnNjoUanf9VoCQ3b0ZPcruJGJMwgLNYLRY8b6OZ8"

    const val SEARCH_PHOTO : String = "search/photos"
    const val SEARCH_USERS : String = "search/users"
}