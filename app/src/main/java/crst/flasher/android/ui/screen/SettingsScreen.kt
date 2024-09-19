package crst.flasher.android.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import crst.flasher.android.BaseApplication
import crst.flasher.android.data.Constant

@Composable
fun SettingsScreen() {
    var remoteServerUrl by remember {
        mutableStateOf(
            BaseApplication.globalSharedPreference()
                .getString(Constant.SP_KEY_REMOTE_SERVER_URL, "").toString()
        )
    }
    var alertMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = remoteServerUrl,
                onValueChange = {
                    remoteServerUrl = it
                    alertMessage = if (!(it.startsWith("http") || it.startsWith("https"))) {
                        "只接受HTTP或HTTPS协议"
                    } else {
                        ""
                    }
                },
                label = { Text("远程服务器地址") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                if (!(remoteServerUrl.startsWith("http://") || remoteServerUrl.startsWith("https://"))) {
                    alertMessage = "只接受HTTP或HTTPS协议"
                } else {
                    BaseApplication.globalSharedPreference().edit {
                        putString(Constant.SP_KEY_REMOTE_SERVER_URL, remoteServerUrl)
                    }
                    Toast.makeText(BaseApplication.context, "保存成功", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("保存")
            }
        }
        AnimatedVisibility(visible = alertMessage.isNotEmpty()) {
            Text(text = alertMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}