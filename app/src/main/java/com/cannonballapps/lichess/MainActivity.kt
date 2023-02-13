package com.cannonballapps.lichess

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.cannonballapps.lichess.ui.theme.LichessTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues


class MainActivity : ComponentActivity() {

    companion object {
        private const val RC_AUTH = 4
    }

    // todo delete these
    private val meFlow = MutableStateFlow("null")
    private val meToken = MutableStateFlow("null")

    private val _userAuthStateFlow: MutableStateFlow<UserOAuthState> = MutableStateFlow(UserOAuthState.NotAuthorized)
    private val userAuthStateFlow: StateFlow<UserOAuthState> = _userAuthStateFlow.asStateFlow()

    private val authService by lazy { AuthorizationService(this) }

    private var authState: AuthState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LichessTheme {
                // A surface container using the 'background' color from the theme

//                val email = meFlow.collectAsState()
//                val token = meToken.collectAsState()

                val userAuthState = userAuthStateFlow.collectAsState()

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column() {
                        Button(
                            onClick = ::authenticateWithLichess,
                        ) {
                            Text(text = "authenticate")
                        }
                        Button(
                            onClick = { makeEmailRequest() },
                        ) {
                            Text(text = "show email")
                        }
                        when (val authState = userAuthState.value) {
                            UserOAuthState.NotAuthorized -> {
                                Text(text = "Not authorized")
                            }
                            UserOAuthState.RequestingAccessCode -> {
                                Text(text = "Requesting access code")
                            }
                            is UserOAuthState.AcquiringAccessToken -> {
                                Text(text = "Acquiring access token")
                            }
                            is UserOAuthState.Authorized -> {
                                Text(text = "Authorized: ${authState.accessToken}")
                            }
                            is UserOAuthState.Error -> {
                                Text(text = "Error")
                            }
                        }
                    }
                }
            }
        }

    }

    private fun makeEmailRequest() {
        meFlow.value = "fetching"

        val lichessApi = LichessRetrofitHelper.getInstance().create(LichessApi::class.java)

        // launching a new coroutine
        lifecycleScope.launch {
            val result = lichessApi.getAccountEmail("Bearer ${meToken.value}")
            if (result != null) {
                meFlow.value = result.body().toString()
            }
        }

    }

    private fun authenticateWithLichess() {
        val serviceConfig = AuthorizationServiceConfiguration(
            // todo extract oauth config into json
            Uri.parse("https://lichess.org/oauth"),  // authorization endpoint
            Uri.parse("https://lichess.org/api/token"), // token endpoint
        )
        // todo create data classes to wrap around AuthState
        authState = AuthState(serviceConfig)

        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            "com.cannonballapps.lichess",
            ResponseTypeValues.CODE,  // the response_type value: we want a code


            Uri.parse("com.cannonballapps.lichess:/lichesstm"), // the redirect URI to which the auth response is sent


        )

        val authRequest = authRequestBuilder
            .setScope("email:read")
            .build()

        _userAuthStateFlow.value = UserOAuthState.RequestingAccessCode
        doAuthorization(authRequest)
    }

    private fun doAuthorization(authRequest: AuthorizationRequest) {
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        startActivityForResult(authIntent, RC_AUTH)
    }

    // todo replace with registering result listener
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_AUTH) {
            val resp = AuthorizationResponse.fromIntent(data!!)
            val ex = AuthorizationException.fromIntent(data)

            authState?.apply {
                this.update(resp, ex)
            }

            _userAuthStateFlow.value = UserOAuthState.AcquiringAccessToken("u")
            authService.performTokenRequest(
                resp!!.createTokenExchangeRequest()
            ) { resp, ex ->
                if (resp != null) {
                    // todo save token to local storage + encryption
                    val oAuthState = if (resp.accessToken != null) {
                        UserOAuthState.Authorized(accessToken = resp.accessToken!!)
                    } else {
                        UserOAuthState.Error(message = "something went wrong")
                    }
                    _userAuthStateFlow.value = oAuthState
                } else {
                    // authorization failed, check ex for more details
                }
            }

        } else {
            // ...
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LichessTheme {
        Greeting("Android")
    }
}