package com.ibrahim.novacalc

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.drawable.GradientDrawable
import java.text.DecimalFormat
import java.math.BigDecimal

class MainActivity : Activity() {

    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView

    private var current = "0"
    private var stored = 0.0
    private var operation = ""
    private var startNew = false

    private val blue = Color.rgb(0, 125, 255)
    private val white = Color.rgb(245, 247, 255)
    private val background = Color.rgb(5, 8, 15)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = background
        window.navigationBarColor = background
        window.decorView.systemUiVisibility = 0
        buildUI()
    }

    private fun rounded(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 28f
            setStroke(2, Color.rgb(43, 51, 68))
        }
    }

    private fun buildUI() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 20)
            setBackgroundColor(background)
        }

        val header = TextView(this).apply {
            text = "NOVA CALC"
            textSize = 30f
            setTextColor(blue)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        root.addView(header, LinearLayout.LayoutParams(-1, 65))

        val subtitle = TextView(this).apply {
            text = "آلة حاسبة ذكية"
            textSize = 17f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        root.addView(subtitle, LinearLayout.LayoutParams(-1, 35))

        val display = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
            setPadding(20, 20, 20, 20)
            setBackground(rounded(Color.rgb(11, 17, 29)))
        }

        expressionView = TextView(this).apply {
            text = ""
            textSize = 23f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.END
            maxLines = 1
        }

        resultView = TextView(this).apply {
            text = "0"
            textSize = 46f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.END
            maxLines = 1
        }

        display.addView(expressionView, LinearLayout.LayoutParams(-1, 45))
        display.addView(resultView, LinearLayout.LayoutParams(-1, 75))
        root.addView(display, LinearLayout.LayoutParams(-1, 150).apply {
            setMargins(0, 22, 0, 25)
        })

        val grid = GridLayout(this).apply {
            columnCount = 4
            rowCount = 5
        }

        val keys = arrayOf(
            "AC", "±", "%", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "−",
            "1", "2", "3", "+",
            "0", ".", "⌫", "="
        )

        keys.forEachIndexed { index, key ->
            val button = Button(this).apply {
                text = key
                textSize = if (key.length > 1) 22f else 27f
                isAllCaps = false
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(
                    when (key) {
                        "AC" -> blue
                        "+", "−", "×", "÷", "=" -> blue
                        else -> white
                    }
                )
                background = rounded(
                    if (key == "=") blue
                    else Color.rgb(20, 26, 39)
                )
                if (key == "=") setTextColor(Color.WHITE)
                setPadding(0, 0, 0, 0)
                stateListAnimator = null
                setOnClickListener { press(key) }
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(index % 4, 1f)
                rowSpec = GridLayout.spec(index / 4, 1f)
                setMargins(4, 5, 4, 5)
            }

            if (key == "0") {
                params.columnSpec = GridLayout.spec(index % 4, 2, 2f)
            }

            grid.addView(button, params)
        }

        root.addView(grid, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun press(key: String) {
        when (key) {
            "AC" -> {
                current = "0"
                stored = 0.0
                operation = ""
                startNew = false
                expressionView.text = ""
            }

            "⌫" -> {
                if (!startNew && current.isNotEmpty()) {
                    current = if (current.length > 1)
                        current.dropLast(1) else "0"
                }
            }

            "." -> {
                if (startNew) {
                    current = "0"
                    startNew = false
                }
                if (!current.contains(".")) current += "."
            }

            "±" -> {
                if (current != "0") {
                    current = if (current.startsWith("-"))
                        current.drop(1) else "-$current"
                }
            }

            "%" -> {
                current = format(current.toDoubleOrNull()?.div(100) ?: 0.0)
            }

            "+", "−", "×", "÷" -> {
                stored = current.toDoubleOrNull() ?: 0.0
                operation = key
                expressionView.text = "${current} $key"
                startNew = true
            }

            "=" -> calculate()

            else -> {
                if (startNew) {
                    current = key
                    startNew = false
                } else {
                    current = if (current == "0") key else current + key
                }
            }
        }

        resultView.text = current
    }

    private fun calculate() {
        val second = current.toDoubleOrNull() ?: 0.0

        val answer = when (operation) {
            "+" -> stored + second
            "−" -> stored - second
            "×" -> stored * second
            "÷" -> {
                if (second == 0.0) {
                    current = "خطأ"
                    resultView.text = current
                    expressionView.text = "لا يمكن القسمة على صفر"
                    operation = ""
                    startNew = true
                    return
                }
                stored / second
            }
            else -> second
        }

        expressionView.text = "$stored $operation $second ="
        current = format(answer)
        operation = ""
        startNew = true
    }

    private fun format(value: Double): String {
        if (!value.isFinite()) return "خطأ"
        return BigDecimal.valueOf(value)
            .stripTrailingZeros()
            .toPlainString()
    }
}