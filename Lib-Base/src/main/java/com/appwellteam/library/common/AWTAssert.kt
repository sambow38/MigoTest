package com.appwellteam.library.common

/**
 * Created by Sambow on 16/6/1.
 */
@Suppress("unused")
object AWTAssert {
    fun checkEmpty(string: String?) {
        if (string?.isEmpty() != false) {
            throw AssertionError("str empty")
        }
    }

    fun checkNull(string: String?) {
        string ?: throw AssertionError("str null")
    }

    fun checkNotNull(string: String?) {
        if (string != null) {
            throw AssertionError("str not null - $string")
        }
    }

    fun checkNull(obj: Any?) {
        obj ?: throw AssertionError("obj null")
    }

    fun checkNotNull(obj: Any?) {
        if (obj != null) {
            throw AssertionError("obj not null - $obj")
        }
    }

    /**
     *
     * @param byte
     * @param op '='（"=="）、'>'、'<'
     * @param value
     */
    fun checkValue(byte: Byte, op: Char, value: Byte) {
        when (op) {
            '>' -> if (byte > value) {
                throw AssertionError("byte var > value")
            }
            '=' -> if (byte == value) {
                throw AssertionError("byte var == value")
            }
            '<' -> if (byte < value) {
                throw AssertionError("byte var < value")
            }
        }
    }

    /**
     *
     * @param `byte`
     * @param op "<="、">="、"!="、"=="、">"、"<"
     * @param value
     */
    fun checkValue(byte: Byte, op: String, value: Byte) {
        val intern = op.intern()
        if (">=" === intern) {
            if (byte >= value) {
                throw AssertionError("byte byte >= value")
            }
        } else if ("<=" === intern) {
            if (byte <= value) {
                throw AssertionError("byte byte <= value")
            }
        } else if ("!=" === intern) {
            if (byte != value) {
                throw AssertionError("byte byte != value")
            }
        } else if ("==" === intern) {
            if (byte == value) {
                throw AssertionError("byte byte == value")
            }
        } else if (">" === intern) {
            if (byte > value) {
                throw AssertionError("byte byte > value")
            }
        } else if ("<" === intern) {
            if (byte < value) {
                throw AssertionError("byte byte < value")
            }
        }
    }

    /**
     *
     * @param short
     * @param op '='（"=="）、'>'、'<'
     * @param value
     */
    fun checkValue(short: Short, op: Char, value: Short) {
        when (op) {
            '>' -> if (short > value) {
                throw AssertionError("short var > value")
            }
            '=' -> if (short == value) {
                throw AssertionError("short var == value")
            }
            '<' -> if (short < value) {
                throw AssertionError("short var < value")
            }
        }
    }

    /**
     *
     * @param short
     * @param op "<="、">="、"!="、">"、"<"
     * @param value
     */
    fun checkValue(short: Short, op: String, value: Short) {
        val intern = op.intern()
        if (">=" === intern) {
            if (short >= value) {
                throw AssertionError("short var >= value")
            }
        } else if ("<=" === intern) {
            if (short <= value) {
                throw AssertionError("short var <= value")
            }
        } else if ("!=" === intern) {
            if (short != value) {
                throw AssertionError("short var != value")
            }
        } else if ("==" === intern) {
            if (short == value) {
                throw AssertionError("short var == value")
            }
        } else if (">" === intern) {
            if (short > value) {
                throw AssertionError("short var > value")
            }
        } else if ("<" === intern) {
            if (short < value) {
                throw AssertionError("short var < value")
            }
        }
    }

    /**
     *
     * @param int
     * @param op '='（"=="）、'>'、'<'
     * @param value
     */
    fun checkValue(int: Int, op: Char, value: Int) {
        when (op) {
            '>' -> if (int > value) {
                throw AssertionError("int var > value")
            }
            '=' -> if (int == value) {
                throw AssertionError("int var == value")
            }
            '<' -> if (int < value) {
                throw AssertionError("int var < value")
            }
        }
    }

    /**
     *
     * @param int
     * @param op "<="、">="、"!="、">"、"<"
     * @param value
     */
    fun checkValue(int: Int, op: String, value: Int) {
        val intern = op.intern()
        if (">=" === intern) {
            if (int >= value) {
                throw AssertionError("int var >= value")
            }
        } else if ("<=" === intern) {
            if (int <= value) {
                throw AssertionError("int var <= value")
            }
        } else if ("!=" === intern) {
            if (int != value) {
                throw AssertionError("int var != value")
            }
        } else if ("==" === intern) {
            if (int == value) {
                throw AssertionError("int var == value")
            }
        } else if (">" === intern) {
            if (int > value) {
                throw AssertionError("int var > value")
            }
        } else if ("<" === intern) {
            if (int < value) {
                throw AssertionError("int var < value")
            }
        }
    }

    /**
     *
     * @param long
     * @param op '='（"=="）、'>'、'<'
     * @param value
     */
    fun checkValue(long: Long, op: Char, value: Long) {
        when (op) {
            '>' -> if (long > value) {
                throw AssertionError("long var > value")
            }
            '=' -> if (long == value) {
                throw AssertionError("long var == value")
            }
            '<' -> if (long < value) {
                throw AssertionError("long var < value")
            }
        }
    }

    /**
     *
     * @param long
     * @param op "<="、">="、"!="、"=="、">"、"<"
     * @param value
     */
    fun checkValue(long: Long, op: String, value: Long) {
        val intern = op.intern()
        if (">=" === intern) {
            if (long >= value) {
                throw AssertionError("long var >= value")
            }
        } else if ("<=" === intern) {
            if (long <= value) {
                throw AssertionError("long var <= value")
            }
        } else if ("!=" === intern) {
            if (long != value) {
                throw AssertionError("long var != value")
            }
        } else if ("==" === intern) {
            if (long == value) {
                throw AssertionError("long var == value")
            }
        } else if (">" === intern) {
            if (long > value) {
                throw AssertionError("long var > value")
            }
        } else if ("<" === intern) {
            if (long < value) {
                throw AssertionError("long var < value")
            }
        }
    }

    /**
     *
     * @param float
     * @param op '='（"=="）、'>'、'<'
     * @param value
     */
    fun checkValue(float: Float, op: Char, value: Float) {
        when (op) {
            '>' -> if (float > value) {
                throw AssertionError("float var > value")
            }
            '=' -> if (float == value) {
                throw AssertionError("float var == value")
            }
            '<' -> if (float < value) {
                throw AssertionError("float var < value")
            }
        }
    }

    /**
     *
     * @param float
     * @param op "<="、">="、"!="、"=="、">"、"<"
     * @param value
     */
    fun checkValue(float: Float, op: String, value: Float) {
        val intern = op.intern()
        if (">=" === intern) {
            if (float >= value) {
                throw AssertionError("float var >= value")
            }
        } else if ("<=" === intern) {
            if (float <= value) {
                throw AssertionError("float var <= value")
            }
        } else if ("!=" === intern) {
            if (float != value) {
                throw AssertionError("float var != value")
            }
        } else if ("==" === intern) {
            if (float == value) {
                throw AssertionError("float var == value")
            }
        } else if (">" === intern) {
            if (float > value) {
                throw AssertionError("float var > value")
            }
        } else if ("<" === intern) {
            if (float < value) {
                throw AssertionError("float var < value")
            }
        }
    }

    /**
     *
     * @param double
     * @param op '='（"=="）、'>'、'<'
     * @param value
     */
    fun checkValue(double: Double, op: Char, value: Double) {
        when (op) {
            '>' -> if (double > value) {
                throw AssertionError("double var > value")
            }
            '=' -> if (double == value) {
                throw AssertionError("double var == value")
            }
            '<' -> if (double < value) {
                throw AssertionError("double var < value")
            }
        }
    }

    /**
     *
     * @param double
     * @param op "<="、">="、"!="、"=="、">"、"<"
     * @param value
     */
    fun checkValue(double: Double, op: String, value: Double) {
        val intern = op.intern()
        if (">=" === intern) {
            if (double >= value) {
                throw AssertionError("double var >= value")
            }
        } else if ("<=" === intern) {
            if (double <= value) {
                throw AssertionError("double var <= value")
            }
        } else if ("!=" === intern) {
            if (double != value) {
                throw AssertionError("double var != value")
            }
        } else if ("==" === intern) {
            if (double == value) {
                throw AssertionError("double var == value")
            }
        } else if (">" === intern) {
            if (double > value) {
                throw AssertionError("double var > value")
            }
        } else if ("<" === intern) {
            if (double < value) {
                throw AssertionError("double var < value")
            }
        }
    }
}
