package com.bandlab.metro.station.sample.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.bandlab.metro.station.MetroStation
import com.bandlab.metro.station.sample.profile.ProfileActivity
import com.bandlab.metro.station.sample.ui.theme.AndroidAppTheme
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory

@MetroStation //TODO: Currently no-op
class MainActivity : ComponentActivity() {

    @Inject
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: Injection point will be generated in IR
        createGraphFactory<MainActivityGraph.Factory>()
            .create(this)
            .inject(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Text(
                            text = "Hello from Main screen!",
                        )

                        Button(onClick = ::openProfile) {
                            Text(text = "Open Profile")
                        }
                    }
                }
            }
        }

        viewModel.showToast()
    }

    private fun openProfile() {
        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
        startActivity(intent)
    }
}