@file:Suppress("unused")
package com.appwellteam.library.extension

import android.database.Cursor
import java.io.Closeable
import java.io.Flushable
import java.io.IOException

fun Closeable.closeStream()
{
    try {
        if (this is Flushable) this.flush()
        if (this is Cursor)
        {
            if (!this.isClosed)
            {
                this.close()
            }
        }
        else
        {
            this.close()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}