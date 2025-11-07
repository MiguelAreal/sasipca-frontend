package sasipca.utils

enum class SnackbarType {
    SUCCESS, ERROR, WARNING
}

data class SnackbarMessage(
    val message: String,
    val type: SnackbarType = SnackbarType.SUCCESS
)
