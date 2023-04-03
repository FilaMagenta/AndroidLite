package com.arnyminerz.filamagenta.core.utils

import com.arnyminerz.filamagenta.core.database.data.woo.AvailablePayment

/**
 * Divides the given amount of money into the least amount possible of packages given.
 * @param amount The amount to divide
 * @param availablePackages The packages of money available to fit.
 * @param maxCycles The maximum amount of cycles to make. Helps on avoiding Stack Overflow Exceptions.
 * @return A map with the ids of [availablePackages] as keys, and the amount of packages required
 * as values.
 */
fun divideMoney(
    amount: Double,
    availablePackages: List<AvailablePayment>,
    maxCycles: Int = availablePackages.size * 100,
): Map<Long, Int> {
    // Sort the available packages from greatest to lowest
    val packages = availablePackages.sortedByDescending { it.price }
    // Create a map in which all the counts will be stored
    val map = mutableMapOf<Long, Int>()
    // Create a counter that will store the remaining money to sort
    // Note that we cut amount to two decimals
    var remaining = amount.roundTo(2)
    // We count the amount of cycles made for not receiving StackOverflow exceptions if packages don't fit
    var cycles = 0
    // Start dividing
    while (remaining > 0 && cycles < maxCycles) {
        cycles++
        for (pack in packages) {
            // If package doesn't fit, go to the next smallest one
            if (pack.price > remaining) continue
            // Get the number of packages we can make
            val packagesCount = (remaining / pack.price).toInt()
            // Update the amount of packages to return
            map[pack.id] = map.getOrDefault(pack.id, 0) + packagesCount
            // Subtract the amount already counted
            remaining -= pack.price * packagesCount
        }
    }
    return map
}
