package sasipca.utils

/**
 * Normaliza unidades do OpenFoodFacts para formato interno consistente.
 * Exemplo:
 *  - "kg" → "g", multiplica ×1000
 *  - "l"  → "ml", multiplica ×1000
 *  - "cl" → "ml", multiplica ×10
 *  - "oz" → "g", multiplica ×28.35
 *  - "units", "un", "uni" → "un"
 */
object UnitConverter {

    data class NormalizedQuantity(val value: Double, val unit: String)

    fun normalize(quantity: Double?, unit: String?): NormalizedQuantity? {
        if (quantity == null || unit.isNullOrBlank()) return null

        val normalized = when (unit.lowercase()) {
            "kg", "kgs", "kilogram", "kilograms" -> NormalizedQuantity(quantity * 1000, "g")
            "g", "gram", "grams" -> NormalizedQuantity(quantity, "g")

            "l", "litre", "litres", "liter", "liters" -> NormalizedQuantity(quantity * 1000, "ml")
            "ml", "millilitre", "millilitres", "milliliter", "milliliters" -> NormalizedQuantity(quantity, "ml")
            "cl" -> NormalizedQuantity(quantity * 10, "ml")

            "oz" -> NormalizedQuantity(quantity * 28.35, "g")
            "unit", "units", "un", "uni" -> NormalizedQuantity(quantity, "un")

            else -> NormalizedQuantity(quantity, unit.lowercase()) // fallback
        }

        return normalized
    }
}
