package calculator

import java.util.*
import java.math.BigInteger

var vars = mutableMapOf<String, BigInteger>()
fun main() {
    val scanner = Scanner(System.`in`)
    var input: String
    input@ while (true) {
        input = scanner.nextLine()
        if (input == "") continue@input
        if (input.first() == '/') {
            input = input.drop(1)
            when (input) {
                "exit" -> break@input
                "help" -> {
                    println("Complex calculator, can use variables, and processes +, -, *, /, (), and unary minus")
                    continue@input
                }
                else -> {
                    println("Unknown command")
                    continue@input
                }
            }
        }
        input = input.replace(" ", "")
        if (input.contains("=")) {
            if (!input.substringBefore("=").matches(Regex("[a-zA-Z]+"))) {
                println("Invalid assignment")
                continue@input
            }
            val result = parseExpression(input.substringAfter("="))
            try {
                vars[input.substringBefore("=")] = result.toBigInteger()
                continue@input
            } catch (_: NumberFormatException) {
                println(result)
                continue@input
            }
        } else {
            val result = parseExpression(input)
            try {
                val output = result.toBigInteger()
                println(output)
                continue@input
            } catch (_: NumberFormatException) {
                println(result)
                continue@input
            }
        }
    }
    println("Bye!")
}

fun parseExpression(expression: String): String {
    var expr = expression
    do {
        val inPrev = expr
        expr = expr.replace("--", "+")
        expr = expr.replace("++", "+")
        expr.replace("-+", "-")
        expr.replace("+-", "-")
    } while (inPrev != expr)
    expr = expr.replace("-(", "-1*(")
    if (expr.contains(Regex("[*/+^-][*/^]")) || expr.contains("[*/^][*/+^]")) return "Invalid expression"
    if (expr.contains("""[\d][a-zA-Z]""".toRegex())) return "Invalid assignment" //Comment this line for implicit multiplication (not tested)
    if (expr.contains("=")) return "Invalid assignment"
    val variables = Regex("""[a-zA-Z]+""").findAll(expr)
    for (variable in variables) {
        if (vars.contains(variable.value)) {
            expr = expr.replace(variable.value, "$" + vars[variable.value].toString())
        } else return "Unknown variable"
    }
    val exprList = mutableListOf("+")
    var sign = 1
    var lastChar = "+"
    for (char in expr) {
        when {
            char == '-' -> {
                if (!Regex("[+*/^]").matches(exprList.last())) exprList.add("+")
                sign = -1
            }
            Regex("[+*/()^]").matches(char.toString()) -> {
                exprList.add(char.toString())
                sign = 1
            }
            char == '$' -> {
                if (exprList.last().contains(Regex("""\d"""))) {
                    exprList.add("*")
                }
            }
            char.isDigit() -> {
                if (Regex("""[\D]""").matches(lastChar)) exprList.add((sign * char.toString().toInt()).toString()) else {
                    exprList[exprList.lastIndex] += char.toString()
                }
            }
        }
        lastChar = char.toString()
    }
    exprList.removeAt(0)
    val postfix = toPostfix(exprList)
    if (postfix[0] == "Invalid expression") return "Invalid expression"
    return toResult(postfix)
}

fun toPostfix(infix: MutableList<String>): MutableList<String> {
    val stack = Stack<Char>()
    val postfix = mutableListOf<String>()
    next@for (symbol in infix) {
        if (symbol.contains(Regex("""[\d]"""))) {
            postfix.add(symbol)
            continue@next
        }
        if (symbol == "(") {
            stack.push(symbol[0])
            continue@next
        }
        if (symbol == ")") {
            while (stack.peek() != '(') {
                postfix.add(stack.pop().toString())
                if (stack.empty()) return mutableListOf("Invalid expression")
            }
            stack.pop()
            continue@next
        }
        while (true) {
            if (stack.empty() || priority(stack.peek()) < priority(symbol[0])) {
                stack.push(symbol[0])
                continue@next
            }
            postfix.add(stack.pop().toString())
        }
    }
    while (!stack.empty()) {
        if (stack.peek() == '(') return mutableListOf("Invalid expression") else postfix.add(stack.pop().toString())
    }
    return postfix
}

fun toResult(postfix: MutableList<String>): String {
    val stack = Stack<String>()
    next@for (symbol in postfix) {
        if (symbol.contains(Regex("""[\d]"""))) {
            stack.push(symbol)
            continue@next
        }
        stack.push(when (symbol) {
            "+" -> (stack.pop().toBigInteger() + stack.pop().toBigInteger()).toString()
            "*" -> (stack.pop().toBigInteger() * stack.pop().toBigInteger()).toString()
            "/" -> {
                val a = stack.pop().toBigInteger()
                val b = stack.pop().toBigInteger()
                (b / a).toString()
            }
            "^" -> {
                val a = stack.pop().toInt()
                val b = stack.pop().toBigInteger()
                b.pow(a).toInt().toString()
            }
            else -> return "Invalid expression"
        })
    }
    return stack.peek()
}

fun priority(char: Char): Int {
    return when (char) {
        '+' -> 1
        '*' -> 2
        '/' -> 2
        '^' -> 3
        '(' -> 0
        else -> 0
    }
}