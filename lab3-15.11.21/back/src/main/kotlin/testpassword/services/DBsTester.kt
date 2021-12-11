package testpassword.services

import testpassword.models.TestResult

// tableName-columnNane node
typealias IndexedUnit = Pair<String, String>

object DBsTester {

    fun findUnits(): Set<IndexedUnit> {
        /* todo:
            отловить все таблицы,
            проверить есть ли алисы
            найти все тестируемые поля
        */
        return emptySet()
    }

    fun testUnit(query: String): TestResult {
        return TestResult("d", 1.0, 3.0, 4.0)
    }

    fun createIndexes(unit: IndexedUnit) {}
}