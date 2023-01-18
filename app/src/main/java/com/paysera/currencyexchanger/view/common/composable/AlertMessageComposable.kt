import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.paysera.currencyexchanger.R

@Composable
fun AlertDialogComposable(
    openDialog: MutableState<Boolean>,
    title: String,
    message: String
) {
    if (openDialog.value) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                    }) {
                    Text(stringResource(R.string.popup_convert_button))
                }
            }
        )
    }
}