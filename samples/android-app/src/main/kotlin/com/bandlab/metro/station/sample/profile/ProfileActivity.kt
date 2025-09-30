package com.bandlab.metro.station.sample.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bandlab.metro.station.StationEntry
import com.bandlab.metro.station.sample.HasStationEntry
import com.bandlab.metro.station.sample.ui.theme.AndroidAppTheme
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@StationEntry(parentScope = AppScope::class) //TODO: Currently no-op
class ProfileActivity : ComponentActivity() {

    @Inject
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: Injection point will be generated in IR
        (applicationContext as HasStationEntry)
            .nowArriving<ProfileGraphExtension.Factory>()
            .create(this)
            .inject(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(
                                text = "Hello from Profile screen!",
                            )

                            Button(onClick = { finish() }) {
                                Text(text = "Go back")
                            }
                        }
                    }
                }
            }
        }

        viewModel.showToast()
    }
}