package contacts

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import java.time.LocalDateTime
import kotlin.system.exitProcess
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// Designing serializable hierarchy:
// https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#designing-serializable-hierarchy

// If the class has a primary constructor, the base must be initialized using the parameters of the primary constructor
@Serializable
@Polymorphic
sealed class Record {
    abstract var name: String
    abstract var number: String
    abstract var createdDate: String
    abstract var lastEditDate: String

    // Abstract method to get a list of valid properties that can be changed
    abstract fun getValidProperties(): Set<String>

    // Abstract methods to set and get property values
    abstract fun setProperty(property: String, value: String)
    abstract fun getProperty(property: String): String
}

@Serializable
@SerialName("person")
class Person(
    override var name: String,
    var surname: String,
    var birthDate: String,
    var gender: String,
    override var number: String,
    override var createdDate: String,
    override var lastEditDate: String
) : Record() {

    // Polymorphism - Derived class implements an abstract method of its superclass with a different execution
    override fun getValidProperties(): Set<String> {
        return setOf("name", "surname", "number", "birthDate", "gender")
    }

    override fun setProperty(property: String, value: String) {
        if (property in getValidProperties()) {
            when (property) {
                "name" -> name = value
                "surname" -> surname = value
                "number" -> number = value
                "birthDate" -> birthDate = value
                "gender" -> gender = value
            }
        } else println("Invalid property: $property")
    }

    override fun getProperty(property: String): String {
        return when (property) {
            "name" -> name
            "surname" -> surname
            "number" -> surname
            "birthDate" -> birthDate
            "gender" -> gender
            else -> "Invalid property"
        }
    }

    override fun toString(): String {
        if (birthDate.isEmpty()) birthDate = "[no data]"
        if (gender.isEmpty()) gender = "[no data]"
        if (number.isEmpty()) number = "[no data]"
        return "Name: $name\n" +
                "Surname: $surname\n" +
                "Birth date: $birthDate\n" +
                "Gender: $gender\n" +
                "Number: $number\n" +
                "Time created: $createdDate\n" +
                "Time last edit: $lastEditDate\n"
    }
}

@Serializable
@SerialName("organization")
class Organization(
    override var name: String,
    var address: String,
    override var number: String,
    override var createdDate: String,
    override var lastEditDate: String
) : Record() {

    // Polymorphism - Derived class implements an abstract method of its superclass with a different execution
    override fun getValidProperties(): Set<String> {
        return setOf("name", "address", "number")
    }

    override fun setProperty(property: String, value: String) {
        if (property in getValidProperties()) {
            when (property) {
                "name" -> name = value
                "address" -> address = value
                "number" -> number = value
            }
        } else println("Invalid property: $property")
    }

    override fun getProperty(property: String): String {
        return when (property) {
            "name" -> name
            "address" -> address
            "number" -> number
            else -> "Invalid property"
        }
    }

    override fun toString(): String {
        if (address.isEmpty()) address = "[no data]"
        if (number.isEmpty()) number = "[no data]"
        return "Organization name: $name\n" +
                "Address: $address\n" +
                "Number: $number\n" +
                "Time created: $createdDate\n" +
                "Time last edit: $lastEditDate\n"
    }
}


class Contacts {
    private var recordsList: MutableList<Record> = mutableListOf()
    private val currentDateTime = LocalDateTime.now()
    private val formattedDateTimeNow = currentDateTime.withSecond(0).withNano(0).toString()

    fun menu() {
        val jsonFile = File("records.json")
        if (jsonFile.exists()) {
            recordsList = readRecordsFromJsonFile(jsonFile)
        }

        while (true) {
            println("Enter action (add, remove, edit, count, list, search, exit): ")
            val input = readln()

            when (input) {
                "add" -> addRecord()
                "count" -> println("The phonebook has ${recordsList.count()} records.")
                "list" -> printList()
                "search" -> search()
                "exit" -> {
                    //writeRecordsToJsonFile(recordsList, jsonFile)
                    exitProcess(0)
                }
            }
        }
    }

    // function which can accept a function as parameter or can return a function is called Higher-Order function
    private fun addRecord() {
        println("Enter the type (person, organization):")
        val type = readln()
        when (type) {
            "person" -> addPerson()
            "organization" -> addOrganization()
            else -> {
                println("Unknown type \n"); menu()
            }
        }
    }

    private fun addPerson() {
        println("Enter the name of the person:")
        val name = readln()

        println("Enter the surname of the person:")
        val surname = readln()

        println("Enter the birthdate of the person:")
        val birthDate = readln()

        println("Enter the gender (M, F):")
        val gender = readln()

        println("Enter the number:")
        var number = readln()
        if (!isValidPhoneNumber(number)) {
            println("Wrong number format!")
            number = ""
        }

        val person = Person(name, surname, birthDate, gender, number, formattedDateTimeNow, formattedDateTimeNow)
        recordsList.add(person)
        println("The record added.\n")
    }

    private fun addOrganization() {
        println("Enter the organization name:")
        val name = readln()

        println("Enter the address:")
        val address = readln()

        println("Enter the number:")
        var number = readln()
        if (!isValidPhoneNumber(number)) {
            println("Wrong number format!")
            number = ""
        }
        val organization = Organization(name, address, number, formattedDateTimeNow, formattedDateTimeNow)
        recordsList.add(organization)
        println("The record added.\n")
    }

    private fun editRecord(recordIndex: Int) {
        val record = recordsList[recordIndex]
        println(record.toString())

        println("[record] Enter action (edit, delete, menu):")
        when (val input = readln()) {
            "edit" -> {
                if (record is Person) {
                    editPerson(record)
                } else if (record is Organization) {
                    editOrganization(record)
                }
            }
            "delete" -> removeRecord(record)
            "menu" -> menu()
            else -> editRecord(input.toInt())
        }
    }

    private fun editPerson(person: Person) {
        println("Select a field (name, surname, birth, gender, number): ")
        val field = readlnOrNull() ?: return

        // Check if the selected field exists as a property of the data class
        if (field in person.getValidProperties()) {
            val newValue = readlnOrNull() ?: return

            when (field) {
                "name" -> person.name = newValue
                "surname" -> person.surname = newValue
                "birth" -> person.birthDate = newValue
                "gender" -> person.gender = newValue
                "number" -> {
                    if (isValidPhoneNumber(newValue)) {
                        person.number = newValue
                    } else {
                        println("Wrong number format")
                        person.number = "[no number]"
                    }
                }
            }
            person.lastEditDate = formattedDateTimeNow
            println("Person updated successfully.\n")
        } else {
            println("Invalid field. It must be one of: name, surname, number.")
        }
    }

    private fun editOrganization(organization: Organization) {
        println("Select a field (address, number): ")
        val field = readlnOrNull() ?: return

        // Check if the selected field exists as a property of the data class
        if (field in organization.getValidProperties()) {
            val newValue = readlnOrNull() ?: return

            when (field) {
                "address" -> organization.address = newValue
                "number" -> {
                    if (isValidPhoneNumber(newValue)) {
                        organization.number = newValue
                    } else {
                        println("Wrong number format")
                        organization.number = "[no number]"
                    }
                }
            }
            organization.lastEditDate = formattedDateTimeNow
            println("Organization updated successfully.\n")
        } else {
            println("Invalid field. It must be one of: name, surname, number.")
        }
    }

    private fun removeRecord(record: Record) {
            recordsList.remove(record)
            println("Record ${record.name} removed successfully.\n")
    }

    private fun printAllRecords() {
        recordsList.forEachIndexed { index, record ->
            println("${index + 1}." + record.name)
        }
        println()
    }

    private fun printList() {
        printAllRecords()
        println("[list] Enter action ([number], back):")
        when (val input = readln()) {
            "back" -> menu()
            else -> {
                val recordIndex = input.toIntOrNull()
                if (recordIndex != null && recordIndex in 1..recordsList.size) {
                    val record = recordsList[recordIndex - 1]
                    println(record.toString())
                    editRecord(recordIndex - 1)
                }
            }
        }
    }

    private fun search() {
        println("Enter search query:")
        val query = readln()

        val searchResults = mutableListOf<Record>()

        for (record in recordsList) {
            if (recordContainsQuery(record, query)) {
                searchResults.add(record)
            }
        }

        if (searchResults.isNotEmpty()) {
            println("Found ${searchResults.count()} results:")
            for ((index, result) in searchResults.withIndex()) {
                println("${index + 1}. " + if (result is Person) {
                    "${result.name} ${result.surname}"
                } else result.name
                )
            }
            println()
        } else {
            println("No matching records found.")
            menu()
        }


        println("[search] Enter action ([number], back, again):")
        when (val input = readln()) {
            "again" -> search()
            "back" -> menu()
            else -> editRecord(input.toInt())
        }

    }

    private fun recordContainsQuery(record: Record, query: String): Boolean {
        val lowercaseQuery = query.lowercase()

        // Check if the query matches any of the valid property values
        if (record.getValidProperties().any { property ->
                val propertyValue = record.getProperty(property).lowercase()
                propertyValue.contains(lowercaseQuery)
            }) {
            return true
        }

        // Check if the query matches the phone number for both persons and organizations
        val phoneNumber = record.number.lowercase()
        return phoneNumber.contains(lowercaseQuery)
    }


    private fun isValidPhoneNumber(number: String): Boolean {
        val firstBracket = "(\\(\\w+\\)([- ]\\w{2,})*)"
        val secondBracket = "(\\w+[- ]\\(\\w{2,}\\)([- ]\\w{2,})*)"
        val noBracket = "(\\w+[- ]\\w{2,}([- ]\\w{2,})*)"
        val phoneNumberRegex = Regex("\\+?(\\w+|$firstBracket|$secondBracket|$noBracket)")
        return number.matches(phoneNumberRegex)
    }

    private fun writeRecordsToJsonFile(recordsList: MutableList<Record>, file: File) {
        val json = Json {
            encodeDefaults = true
            prettyPrint = true
        }
        val jsonString = json.encodeToString(recordsList)

        // Write the JSON string to a file
        file.writeText(jsonString)
    }

    private fun readRecordsFromJsonFile(file: File): MutableList<Record> {
        val json = Json {
            encodeDefaults = true
            prettyPrint = true
        }

        // Read the JSON data from the file
        val jsonString = file.readText()

        // Deserialize the JSON data into a list of records
        return json.decodeFromString<MutableList<Record>>(jsonString)
    }
}

fun main() {
    val contacts = Contacts()
    contacts.menu()
}