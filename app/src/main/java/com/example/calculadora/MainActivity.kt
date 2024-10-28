package com.example.calculadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    var tvRes: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvRes = findViewById(R.id.tvRes)
    }

    fun calcular(view: View) {
        val boton = view as Button
        val textoBoton = boton.text.toString()
        var pantalla = tvRes?.text.toString()

        if (textoBoton in listOf("+", "-", "*", "/")) {
            if (pantalla.isNotEmpty() && pantalla.last().toString() in listOf("+", "-", "*", "/")) {
                return
            }
        }

        if (textoBoton == ".") {
            val lastOperatorIndex = pantalla.lastIndexOfAny(charArrayOf('+', '-', '*', '/'))
            val lastNumber = if (lastOperatorIndex != -1) pantalla.substring(lastOperatorIndex + 1) else pantalla
            if (lastNumber.contains(".")) {
                return
            }
        }

        var nuevaPantalla = pantalla + textoBoton

        if (textoBoton == "=") {
            try {
                val resultado = eval(pantalla)
                tvRes?.text = if (resultado % 1 == 0.0) resultado.toInt().toString() else resultado.toString()
            } catch (e: Exception) {
                tvRes?.text = "Error: ${e.message}"
            }
        } else if (textoBoton == "RESET") {
            tvRes?.text = "0"
        } else {
            tvRes?.text = quitarCerosIzquierda(nuevaPantalla)
        }
    }

    fun quitarCerosIzquierda(str: String): String {
        var i = 0
        while (i < str.length && str[i] == '0' && (i + 1 < str.length && str[i + 1] != '.')) i++
        return str.substring(i)
    }

    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].toInt() else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Carácter inesperado: '${ch.toChar()}'")
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    x = when {
                        eat('+'.toInt()) -> x + parseTerm()
                        eat('-'.toInt()) -> x - parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    x = when {
                        eat('*'.toInt()) -> x * parseFactor()
                        eat('/'.toInt()) -> x / parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor()
                if (eat('-'.toInt())) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.toInt())) {
                    x = parseExpression()
                    if (!eat(')'.toInt())) throw RuntimeException("Falta paréntesis de cierre")
                } else if (ch in '0'.toInt()..'9'.toInt() || ch == '.'.toInt()) {
                    while (ch in '0'.toInt()..'9'.toInt() || ch == '.'.toInt()) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Carácter inesperado: '${ch.toChar()}'")
                }
                return x
            }
        }.parse()
    }
}
