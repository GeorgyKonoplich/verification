import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.InputStream

fun main(args: Array<String>) {
    val automatonFile = args[0]
    val formula = args[1]

    val automaton = Automaton.readAutomatonFromFile(automatonFile)
    val buchiAutomaton = BuchiAutomaton(automaton)

    val inputStream: InputStream = File(formula).inputStream()
    val lineList = mutableListOf<String>()

    inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }

    for (item in lineList){
        val ltlFormula = LtlFormula.Not(parseLtlFormula(item))

        val generalizedBuchiAutomatonByLtl = GeneralizedLabeledBuchiAutomaton(ltlFormula, buchiAutomaton.atomPropositions)
        val buchiAutomatonByLtl = BuchiAutomaton(generalizedBuchiAutomatonByLtl)


        val answer = findPath(buchiAutomaton, buchiAutomatonByLtl)
        if (answer.holds) {
            println("Formula $item is true")
        } else {
            println("Formula $item is false")
            println("Counter-example path:")
            for ((i, label) in answer.path!!.withIndex()) {
                assert(label.min == label.max)
                println("$i :\t${label.min}")
            }
            println("Back to ${answer.cycleStartIndex!!} and it's a cycle")
        }
        println("---------------------------------------------------------")
        println()

    }
}

fun parseLtlFormula(input: String): LtlFormula {
    val lexer = LtlLexer(ANTLRInputStream(input))
    val parser = LtlParser(CommonTokenStream(lexer))
    val formula = parser.formula()
    return LtlFormulaBuilder().visit(formula)
}
