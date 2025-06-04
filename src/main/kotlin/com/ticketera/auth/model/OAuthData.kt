package com.ticketera.auth.model

import java.util.UUID

data class OAuthData(val email: String, val name: String, val refreshToken: UUID? = null)
