package com.cr7.budgetapp.ui.screens.signin

import android.content.Context
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.LifecycleCoroutineScope
import com.cr7.budgetapp.R
import com.cr7.budgetapp.ui.screens.main.TAG
import com.cr7.budgetapp.ui.theme.BudgetAppTheme
import com.cr7.budgetapp.ui.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.Base64


@Composable
fun SignInScreen(activityContext: Context, lifecycleScope: LifecycleCoroutineScope, authViewModel: AuthViewModel) {

    var clicked by remember { mutableStateOf(false) }
    val credentialManager = CredentialManager.create(activityContext)

    // On credential receive handler
    fun handleSignIn(result: GetCredentialResponse, updateClicked: (Boolean) -> Unit) {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        // Sign into firebase with the acquired credentials
                        authViewModel.signInWithGoogle(idToken = googleIdTokenCredential.idToken)


                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                        updateClicked(false)
                    }
                }
                else  {
                    // Catch any unrecognized credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                    updateClicked(false)
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    fun generateNonce(length: Int = 32): String {
        val nonce = ByteArray(length)
        SecureRandom().nextBytes(nonce)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(nonce)
    }

    // Creating a google sign in request
    val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder("141196640136-beb6307f33752kof2j2q9t7m2jbbmjvm.apps.googleusercontent.com")
        .setNonce(generateNonce())
        .build()

    // Create a request to get credentials
    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(signInWithGoogleOption)
        .build()

    Screen(clicked = clicked, updateClicked = {clicked = it}) {
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = activityContext,
                )
                handleSignIn(result) { clicked = it }
            } catch (e: GetCredentialException) {
                Log.e(TAG, e.toString())
                clicked = false
            }
        }
    }
}

@Composable
fun Screen(clicked: Boolean, updateClicked: (Boolean) -> Unit, onClicked: () -> Unit) {
    BudgetAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Text(
                text = "Hello Android !",
            )
            GoogleButton(updateClicked = {updateClicked(it)},clicked = clicked, loadingText = "Signing In to your account...") {
                onClicked()
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 33)
@Composable
fun PreviewScreen() {
    Screen(clicked = false, updateClicked = {}) {

    }
}

@Composable
fun GoogleButton(
    modifier: Modifier = Modifier,
    text: String = "Sign Up with Google",
    loadingText: String = "Creating Account...",
    icon: Int = R.drawable.ic_google_logo,
    shape: Shape = MaterialTheme.shapes.medium,
    borderColor: Color = Color.LightGray,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    progressIndicatorColor: Color = MaterialTheme.colorScheme.primary,
    clicked: Boolean,
    updateClicked: (Boolean) -> Unit,
    onClicked: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                updateClicked(!clicked)
                onClicked()
            },
        shape = shape,
        border = BorderStroke(width = 1.dp, color = borderColor),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = 12.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearOutSlowInEasing
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "Google Button",
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (clicked) loadingText else text)
            if (clicked) {
                Spacer(modifier = Modifier.width(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(16.dp)
                        .width(16.dp),
                    strokeWidth = 2.dp,
                    color = progressIndicatorColor
                )
            }
        }
    }
}