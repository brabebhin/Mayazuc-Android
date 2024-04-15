package ionic.mayazuc

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ComposeCheckBoxWithTitle(
    caption: String,
    title: String,
    initialValue: () -> Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    val checkedState =
        remember { mutableStateOf(initialValue()) }
    Column() {
        Text(
            text = title,
            modifier = Modifier.clickable { checkedState.value = !checkedState.value })
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = checkedState.value,
                onCheckedChange = { checkedState.value = it; onCheckedChanged(it) },
            )
            Text(
                text = caption,
                modifier = Modifier.clickable { checkedState.value = !checkedState.value })
        }
    }
}

@Composable
fun ComposeButtonWithInnerText(innerText: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .background(Color.Transparent)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = innerText)
        }
    }
}

@Composable
fun RadioButtonsGroup(
    title: String,
    radioOptions: List<String>,
    onOptionSelected: (String) -> Unit,
    initialItem: String
) {

    val selectedOption = remember { mutableStateOf(initialItem) }
    Column {
        Text(text = title)
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (text == selectedOption.value),
                        onClick = {
                            selectedOption.value = text
                            onOptionSelected(text)
                        }
                    )
                    .padding(horizontal = 16.dp)
            ) {
                RadioButton(
                    selected = (text == selectedOption.value),
                    onClick =
                    {
                        selectedOption.value = text;
                        onOptionSelected(text)
                    }
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
