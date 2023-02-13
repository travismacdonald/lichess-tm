package com.cannonballapps.lichess


sealed interface UserOAuthState {

    object NotAuthorized : UserOAuthState

    object RequestingAccessCode : UserOAuthState

    data class AcquiringAccessToken  (
        val accessCode: String,
    ) : UserOAuthState

    data class Authorized(
        val accessToken: String,
    ) : UserOAuthState

    data class Error(
        val message: String,
    ) : UserOAuthState
}