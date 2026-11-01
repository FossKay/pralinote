package com.example.pralinote.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            .clip(RoundedCornerShape(8.dp))
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .clickable {
                uriHandler.openUri(attachment.link)
            }
            .padding(
                top = 4.dp,
                bottom = 4.dp,
                start = 4.dp, // the icon applies some padding?!
                end = 8.dp,
            )
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.padding(end = 2.dp).size(18.dp),
                imageVector = Icons.Outlined.AttachFile, // TODO: adapt
                contentDescription = "Pièce jointe",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                attachment.file,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
