import com.demo.security.spring.model.TransactionType
import com.github.javafaker.Faker

import java.util.concurrent.TimeUnit

final String OUTPUT_FILE = "./src/main/resources/example-users.json"

new Generator().execute().join("\n")

// TODO: Make this script able to be triggered by a maven profile

class Generator {
    private final Faker faker = new Faker()

    List<String> execute() {
        return [
                """{
    "username": "user",
    "password": "password",
    "userType": "external",
    "email": "user@demo.com",
    "userRole": "STANDARD",
    "enabled": true,
    "authorities": [
      {
        "authority": "ROLE_USER"
      }
    ],
    "account" : ${randomAccount()}
  },
""",
                """{
    "username": "admin",
    "email": "admin@demo.com",
    "password": "password",
    "userType": "internal",
    "userRole": "ADMIN",
    "enabled": true,
    "authorities": [
      {
        "authority": "ROLE_ADMIN"
      }
    ],
    "account" : ${randomAccount()}
  },
""",
                """{
    "username": "otherUser",
    "email": "otherUser@demo.com",
    "password": "password",
    "userType": "external",
    "userRole": "STANDARD",
    "enabled": true,
    "authorities": [
      {
        "authority": "ROLE_USER"
      }
    ],
    "account" : ${randomAccount()}
  },
""",
                """{
    "username": "otherAdmin",
    "email": "otherAdmin@demo.com",
    "password": "password",
    "userType": "internal",
    "userRole": "ADMIN",
    "enabled": true,
    "authorities": [
      {
        "authority": "ROLE_ADMIN"
      },
      {
        "authority": "ROLE_USER"
      }
    ],
    "account" : ${randomAccount()}
  },
""",
                """{
    "username": "userDisabled",
    "email": "userDisabled@demo.com",
    "password": "password",
    "userType": "external",
    "userRole": "STANDARD",
    "enabled": false,
    "authorities": [
      {
        "authority": "ROLE_USER"
      }
    ],
    "account" : ${randomAccount()}
  },
""",
                """{
    "username": "adminDisabled",
    "email": "adminDisabled@demo.com",
    "password": "password",
    "userType": "internal",
    "userRole": "ADMIN",
    "enabled": false,
    "authorities": [
      {
        "authority": "ROLE_ADMIN"
      }
    ],
    "account" : ${randomAccount()}
  },
""",
                randomUser() + ",",
                randomUser() + ",",
                randomUser() + ",",
                randomUser() + ",",
                randomUser()
        ]
    }

    private String randomUser() {
        final String username = faker.starTrek().character()

        return """{
    "username": "${username}",
    "email": "${username.replaceAll("\\s", "")}@demo.com",
    "password": "${faker.internet().password()}",
    "userType": "external",
    "userRole": "STANDARD",
    "enabled": false,
    "authorities": [
      {
        "authority": "ROLE_USER"
      }
    ],
    "account" : ${randomAccount()}
  }
"""
    }

    private String randomAccount() {
        return """
    {
      "accountType": "Savings",
      "branchAddress": "${faker.address().fullAddress()}",
      "createdDate": "${faker.date().past(100, 5, TimeUnit.DAYS)}",
      "accountTransactions": [
        ${randomAccountTransaction()},
        ${randomAccountTransaction()},
        ${randomAccountTransaction()},
        ${randomAccountTransaction()},
        ${randomAccountTransaction()}
      ]
    }
"""
    }

    private String randomAccountTransaction() {
        return """{
          "transactionDate": "${faker.date().past(100, 5, TimeUnit.DAYS)}",
          "transactionSummary": "${faker.commerce().productName()}",
          "transactionType": "${faker.random().nextInt(2) % 2 == 0 ? TransactionType.Withdrawal.name() : TransactionType.Deposit.name()}",
          "transactionAmount": ${Math.abs(faker.random().nextDouble())},
          "closingBalance": ${Math.abs(faker.random().nextDouble())},
          "createdDate": "${faker.date().past(100, 0, TimeUnit.DAYS).toInstant().toEpochMilli()}"
        }"""
    }
}