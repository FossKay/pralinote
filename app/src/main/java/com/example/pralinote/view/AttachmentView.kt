package com.example.pralinote.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import uniffi.pronote_rs.Attachment

@Composable
fun AttachmentView(
    attachment: Attachment,
) {
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .padding(
                top = 6.dp
            )
            .clip(RoundedCornerShape(4.dp))
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .clickable {
                uriHandler.openUri(attachment.link)
            }
            .padding(
                vertical = 4.dp,
                horizontal = 8.dp,
            )
    ) {
        Text(
            attachment.file,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
