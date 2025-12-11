package sasipca.models

enum class SnackbarType {
    SUCCESS, ERROR, WARNING
}

data class SnackbarMessage(
    val message: String,
    val type: SnackbarType = SnackbarType.SUCCESS
)
