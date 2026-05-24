package com.akurbanoff.linkage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.akurbanoff.linkage.ui.theme.LinkageTheme

sealed interface DL

@LinkageDeepLink("app://person/{id}")
data class PersonDeepLink(val id: Int) : DL

@LinkageDeepLink("app://person/{name}")
data class PersonNameDeepLink(val name: String) : DL

class MainActivity : ComponentActivity() {
    private val linkageParser = provideLinkageParser()
    private val linkageUriConverter = provideLinkageUriConverter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val person = linkageParser.parse<DL>("app://person/artem")
            LinkageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        when (person) {
                            is PersonDeepLink -> {
                                Greeting(
                                    name = person.id.toString(),
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }

                            is PersonNameDeepLink -> {
                                Greeting(
                                    name = person.name,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }

                            else -> {
                                Greeting(
                                    name = "Android",
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                        val uri = linkageUriConverter.toUri(person)
                        Greeting(
                            name = uri?.toString() ?: "Android Uri",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}