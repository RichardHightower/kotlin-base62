
@file:JvmName("main.Base62Encoder")
package main

import kotlin.math.abs

import main.Base62Encoder.convertToEncodedString
import main.Base62Encoder.convertToLong


fun main() {
    val id = 12345678910L
    val strId = convertToEncodedString(id)
    val newId = convertToLong(strId)
    System.out.printf("%d %s %d\n", id, strId, newId)

    val longUrl = "https://www.somewebiste.com/dp/0201616165/?_encoding=UTF8&pd_rd_w=vwEcs&content-id=amzn1.sym.8cf3b8ef-6a74-45dc-9f0d-6409eb523603&pf_rd_p=8cf3b8ef-6a74-45dc-9f0d-6409eb523603&pf_rd_r=BQ0KD40K57XG761DBNBA&pd_rd_wg=DtkHk&pd_rd_r=f94b60b7-9080-4065-b77f-6377ec854d17&ref_=pd_gw_ci_mcx_mi";
    val urlId = abs(longUrl.hashCode()).toLong()
    val shortHandle = convertToEncodedString(urlId)
    println("url id $urlId short handle $shortHandle ${convertToLong(shortHandle)}")
}

object Base62Encoder {


    private val DIGITS = charArrayOf(
        //0    1    2    3    4    5    6    7    8    9
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        //10    11   12   13  14   15   16   17    18    19   20  21
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
        //22    23   24   25  26   27   28   29    30    31   32  33    34  35
        'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        // 36  37  38   39   40   41    42    43   44  45   46    47
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',  //Easy to add more characters if not using lookup tables.
        // 48  49   50   51   52   53   54   55  56   57   58  59   60   61   // 62   63, 64
        'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')

    fun convertToEncodedString(id: Long): String  {
        val builder = StringBuilder()
        val placeHolder = findStartBucket(id)
        val results = accumulateDigits(placeHolder, id, builder)
        val bucketValue = powDigitsBase(1)
        val digitIndex: Int = (results.second / bucketValue).toInt()
        val acc = results.second - (bucketValue * digitIndex)
        appendSafe(builder, digitIndex)
        //Put the remainder in the ones column
        val place1DigitIndex = (acc % bucketValue).toInt()
        appendSafe(builder, place1DigitIndex)
        return builder.toString()
    }

    private fun accumulateDigits(placeHolder: Int, acc: Long, builder: StringBuilder):
            Triple<Int, Long, StringBuilder>  {
        if (placeHolder <= 1) {
            return Triple(placeHolder, acc, builder)
        }
        val bucketValue = powDigitsBase(placeHolder.toLong())
        val digitIndex = (acc / bucketValue).toInt()
        return accumulateDigits(placeHolder - 1,
            acc - (bucketValue * digitIndex), appendSafe(builder, digitIndex))
    }


    fun convertToLong(strId: String): Long {
        return doConvertToLong(strId.toCharArray())
    }

    private fun doConvertToLong(chars: CharArray): Long {
        chars.reverse()
        val (acc, _) = chars.fold(Pair(0L, 0)) { pos : Pair<Long, Int>, ch: Char ->
            val (acc, position) = pos
            val value = computeValue(ch, position)
            Pair(acc + value, position + 1)
        }
        return acc
    }

    private fun findIndexOfDigitInTable(c: Char): Int  {
        val i = DIGITS.indexOf(c)
        if (i == -1) {
            throw  IllegalStateException("Unknown char #$c#")
        }
        return i
    }

    private fun computeValue(c: Char, position: Int): Long {
        val digitIndex = findIndexOfDigitInTable(c)
        val multiplier = powDigitsBase(position.toLong())
        return digitIndex * multiplier
    }

    private fun appendSafe(builder: StringBuilder, digitIndex: Int) : StringBuilder {
        if (digitIndex != 0) {
            builder.append(DIGITS[digitIndex])
        } else {
            if (builder.isNotEmpty()) {
                builder.append(DIGITS[digitIndex])
            }
        }
        return builder
    }

    private fun powDigitsBase(exponent: Long): Long {
        return doPow(exponent, DIGITS.size)
    }

    private fun doPow(exponent: Long, base: Int): Long  {
        if (exponent == 0L) return 1L
        return doPow(exponent - 1, base) * base
    }

    private fun findStartBucket(value: Long): Int {
        val index : Int? =  (0..14).find { i -> value < powDigitsBase(i.toLong()) }
        return index ?: 0
    }

}
