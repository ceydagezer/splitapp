package com.splitapp.common

// Every error the API returns has the same JSON shape: {"error": "..."}
// so the Android app can always read errors the same way.
data class ErrorResponse(val error: String)
