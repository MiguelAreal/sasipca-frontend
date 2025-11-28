package sasipca.utils
import android.content.Context
import java.lang.ref.WeakReference

object AndroidContext {
    var contextRef: WeakReference<Context>? = null

    fun set(context: Context) {
        contextRef = WeakReference(context)
    }

    fun get(): Context? = contextRef?.get()
}