package com.lightdarktools.passcrypt.common

import kotlin.random.Random

object PasswordGenerator {
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    fun generatePassword(
        length: Int = 16,
        useUppercase: Boolean = true,
        useLowercase: Boolean = true,
        useNumbers: Boolean = true,
        useSymbols: Boolean = true
    ): String {
        val charPool = StringBuilder()
        if (useUppercase) charPool.append(UPPERCASE)
        if (useLowercase) charPool.append(LOWERCASE)
        if (useNumbers) charPool.append(NUMBERS)
        if (useSymbols) charPool.append(SYMBOLS)

        if (charPool.isEmpty()) return ""

        val password = StringBuilder()
        val pool = charPool.toString()

        // Ensure at least one character from each selected set is included
        if (useUppercase) password.append(UPPERCASE.random())
        if (useLowercase) password.append(LOWERCASE.random())
        if (useNumbers) password.append(NUMBERS.random())
        if (useSymbols) password.append(SYMBOLS.random())

        // Fill the rest
        repeat(length - password.length) {
            password.append(pool[Random.nextInt(pool.length)])
        }

        // Shuffle the result to prevent predictable patterns
        val shuffled = password.toString().toCharArray()
        shuffled.shuffle()
        return String(shuffled)
    }
}
