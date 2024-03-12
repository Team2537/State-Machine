import kotlin.math.abs

fun Double.near(target: Double, error: Double): Boolean{
    return abs(this - target) < error
}