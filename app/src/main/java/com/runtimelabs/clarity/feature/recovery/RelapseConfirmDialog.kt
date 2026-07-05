package com.runtimelabs.clarity.feature.recovery

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.runtimelabs.clarity.R

/**
 * The one confirmation gate before a streak resets — deliberately not
 * phrased as a confession ("Yes, I relapsed"). The person is logging
 * something that happened, not being made to say it in a shaming way.
 */
@Composable
fun RelapseConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.relapse_confirm_title)) },
        text = { Text(stringResource(R.string.relapse_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.relapse_confirm_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
