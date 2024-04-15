package ionic.mayazuc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import ionic.mayazuc.ui.theme.MayazucLiteTheme

object UiUtilities {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ElementWithPlainTooltip(tooltip: String, content: @Composable () -> Unit) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(20.dp),
            tooltip = { PlainTooltip { Text(tooltip) } },
            state = rememberTooltipState()
        ) {
            content();
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MayazucScaffold(
        topBar: @Composable () -> Unit,
        bottomBar: @Composable () -> Unit,
        content: @Composable () -> Unit,
        scrollBehavior: TopAppBarScrollBehavior
    ) {
        MayazucLiteTheme {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = topBar,
                bottomBar = bottomBar
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    content();
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun textInputDialog(
        openDialog: MutableState<Boolean> = remember { mutableStateOf(true) },
        text: MutableState<String> = remember { mutableStateOf("Hello") },
        label: String = "Label",
        onOkCallback: () -> Unit
    ) {

        if (openDialog.value) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    // Dismiss the dialog when the user clicks outside the dialog or on the back
                    // button. If you want to disable that functionality, simply use an empty
                    // onDismissRequest.
                    openDialog.value = false
                }
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        //... AlertDialog content
                        OutlinedTextField(
                            value = text.value,
                            onValueChange = { text.value = it },
                            label = { Text(label) },
                        )

                       Button(onClick = {
                           onOkCallback();
                           openDialog.value = false  }) {
                           Text("OK");
                       }
                    }
                }
            }
        }
    }
}