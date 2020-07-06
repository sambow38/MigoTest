package com.appwellteam.library.common

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorInt
import com.appwellteam.library.AWTApplication
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.regex.Pattern

/**
 * Created by Sambow on 15/10/9.
 */
@Suppress("unused")
object AWTCommon {
    private val EMAIL_PATTERN = Pattern.compile("^[_a-z0-9-]+([._a-z0-9-]+)*@[a-z0-9-]+([.a-z0-9-]+)*$")

    private fun getContext(context: Context? = null): Context
    {
        return context ?: AWTApplication.app ?: throw RuntimeException("context is null")
    }

    fun hideKeyboard() {
        val view: View = AWTApplication.app?.activity?.findViewById(android.R.id.content) ?: return
        val imm = AWTApplication.app?.activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                ?: return
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun isSocialSecurityNumber(id: String): Boolean {
        if (!id.matches("[a-zA-Z][1-2][0-9]{8}".toRegex())) {
            return false
        }
        val newId = id.toUpperCase()

        //身分證第一碼代表數值
        val headNum = intArrayOf(1, 10, 19, 28, 37, 46, 55, 64, 39, 73, 82, 2, 11, 20, 48, 29, 38, 47, 56, 65, 74, 83, 21, 3, 12, 30)

        val headCharUpper = charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')

        val index = Arrays.binarySearch(headCharUpper, newId[0])

        var base = 8
        var total = 0
        for (i in 1..9) {
            val tmp = Integer.parseInt(Character.toString(newId[i])) * base
            total += tmp
            base--
        }

        total += headNum[index]
        val remain = total % 10
        val checkNum = (10 - remain) % 10
        return Integer.parseInt(Character.toString(newId[9])) == checkNum
    }

    fun isEmail(email: String): Boolean {
        val matcher = EMAIL_PATTERN.matcher(email.toLowerCase())
        return matcher.matches()
    }

    fun isTWPhone(phone: String?): Array<String>? {
        phone ?: return null
        if (phone.length < 9) return null

        val number = phone.replace("(", "").replace(")", "").replace(" ", "").replace("-", "")
        val arr = IntArray(number.length)
        for (ii in 0 until number.length) {
            try {
                arr[ii] = Integer.valueOf(number.substring(ii, ii + 1))
            } catch (e: Exception) {
                return null
            }

        }

        return if (arr[0] == 0) {
            if (arr[1] == 2) {
                if (arr.size == 10) {
                    arrayOf(number.substring(0, 2), number.substring(2, 10))
                } else {
                    null
                }
            } else if (arr[1] == 3) {
                if (arr.size == 9) {
                    if (arr[2] == 7) {
                        if (arr[3] == 0 || arr[3] == 1) {
                            null
                        } else {
                            arrayOf(number.substring(0, 2), number.substring(2, 9))
                        }
                    } else if (arr[2] == 0 || arr[2] == 1) {
                        null
                    } else {
                        arrayOf(number.substring(0, 2), number.substring(2, 9))
                    }
                } else {
                    null
                }
            } else if (arr[1] == 4) {
                if (arr.size == 9) {
                    if (arr[2] == 7 || arr[2] == 8) {
                        arrayOf(number.substring(0, 2), number.substring(2, 9))
                    } else {
                        null
                    }
                } else if (arr.size == 10) {
                    if (arr[2] == 9) {
                        if (arr[3] == 2 || arr[3] == 5 || arr[3] == 6 || arr[3] == 7) {
                            arrayOf(number.substring(0, 3), number.substring(3, 10))
                        } else {
                            null
                        }
                    } else if (arr[2] == 2 || arr[2] == 3) {
                        arrayOf(number.substring(0, 2), number.substring(2, 10))
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else if (arr[1] == 5) {
                if (arr.size == 9) {
                    if (arr[2] == 2 || arr[2] == 3 || arr[2] == 5 || arr[2] == 6 || arr[2] == 7) {
                        arrayOf(number.substring(0, 2), number.substring(2, 9))
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else if (arr[1] == 6) {
                if (arr.size == 9) {
                    if (arr[2] == 0 || arr[2] == 1 || arr[2] == 8) {
                        null
                    } else {
                        arrayOf(number.substring(0, 2), number.substring(2, 9))
                    }
                } else {
                    null
                }
            } else if (arr[1] == 7) {
                if (arr.size == 9) {
                    arrayOf(number.substring(0, 2), number.substring(2, 9))
                } else {
                    null
                }
            } else if (arr[1] == 8) {
                if (arr.size == 9) {
                    if (arr[2] == 2) {
                        if (arr[3] == 6) {
                            arrayOf(number.substring(0, 4), number.substring(4, 9))
                        } else {
                            arrayOf(number.substring(0, 3), number.substring(3, 9))
                        }
                    } else if (arr[2] == 3) {
                        if (arr[3] == 6) {
                            arrayOf(number.substring(0, 4), number.substring(4, 9))
                        } else {
                            null
                        }
                    } else if (arr[2] == 9) {
                        arrayOf(number.substring(0, 3), number.substring(3, 9))
                    } else if (arr[2] == 7 || arr[2] == 8) {
                        arrayOf(number.substring(0, 2), number.substring(2, 9))
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else if (arr[1] == 9) {
                if (arr.size == 10) {
                    arrayOf("", number)
                } else {
                    null
                }
            } else {
                null
            }
        } else {
            null
        }
    }

    fun randomStr(): String {
        return UUID.randomUUID().toString()
    }

    fun toastExceptionStack(e: Throwable, rowCount: Int) {
        val stringBuilder = StringBuilder()
        stringBuilder.append(e.toString())
        val stackTraceElements = e.stackTrace
        var count = 0
        for (stackTraceElement in stackTraceElements) {
            if (count > rowCount) break
            stringBuilder.append("\n")
            stringBuilder.append(stackTraceElement.className)
            stringBuilder.append(" - ")
            stringBuilder.append(stackTraceElement.methodName)
            stringBuilder.append(" - ")
            stringBuilder.append(stackTraceElement.lineNumber)
            count++
        }
        showToast(stringBuilder.toString(), isLong = true)
    }


    @Suppress("MemberVisibilityCanBePrivate")
    @JvmOverloads fun showToast(msg: String, gravity: Int = Gravity.CENTER, isLong: Boolean = false, context: Context? = null) {
        val toast = Toast.makeText(context ?: AWTApplication.app ?: return, msg, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        toast.setGravity(gravity, 0, 0)
        toast.show()
    }

    fun createBaseInstance(target: Class<*>): Any? {
        var instance: Any? = null
        try {
            val c = target.getConstructor()
            instance = c.newInstance()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        AWTAssert.checkNull(instance)
        return instance
    }

    fun setStatusBarColor(@ColorInt color: Int) {
        val window = AWTApplication.app?.activity?.window ?: return

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }

    fun setStatusBarTextLight(isLight: Boolean) {
        val act = AWTApplication.app?.activity ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = act.window.decorView.systemUiVisibility
            if (!isLight) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            //			flags |= isLight ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            act.window.decorView.systemUiVisibility = flags
        }
    }
}
