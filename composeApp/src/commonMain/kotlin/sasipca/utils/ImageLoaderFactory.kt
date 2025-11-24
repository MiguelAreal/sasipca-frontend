package sasipca.utils

import coil3.ImageLoader
import coil3.PlatformContext

// Declaramos que esperamos uma função com esta assinatura em cada plataforma
expect fun getAsyncImageLoader(context: PlatformContext): ImageLoader