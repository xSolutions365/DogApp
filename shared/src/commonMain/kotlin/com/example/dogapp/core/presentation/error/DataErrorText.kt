package com.example.dogapp.core.presentation.error

import com.example.dogapp.core.domain.DataError
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.error_client
import dogapp.shared.generated.resources.error_no_connection
import dogapp.shared.generated.resources.error_not_found
import dogapp.shared.generated.resources.error_request_timeout
import dogapp.shared.generated.resources.error_serialization
import dogapp.shared.generated.resources.error_server
import dogapp.shared.generated.resources.error_too_many_requests
import dogapp.shared.generated.resources.error_unauthorized
import dogapp.shared.generated.resources.error_unknown
import org.jetbrains.compose.resources.StringResource

fun DataError.toStringResource(): StringResource =
    when (this) {
        DataError.Remote.RequestTimeout -> Res.string.error_request_timeout
        DataError.Remote.NoConnection -> Res.string.error_no_connection
        DataError.Remote.Unauthorized -> Res.string.error_unauthorized
        DataError.Remote.NotFound -> Res.string.error_not_found
        DataError.Remote.TooManyRequests -> Res.string.error_too_many_requests
        DataError.Remote.Serialization -> Res.string.error_serialization
        is DataError.Remote.Client -> Res.string.error_client
        is DataError.Remote.Server -> Res.string.error_server
        DataError.Remote.Unknown -> Res.string.error_unknown
    }
