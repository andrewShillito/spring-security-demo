import com.demo.security.spring.model.TransactionType
import com.github.javafaker.Faker

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream

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





/*

INSERT INTO `accounts` (`customer_id`, `account_number`, `account_type`, `branch_address`, `create_dt`)
 VALUES (1, 1865764534, 'Savings', '123 Main Street, New York', CURDATE());



INSERT INTO `account_transactions` (`transaction_id`, `account_number`, `customer_id`, `transaction_dt`, `transaction_summary`, `transaction_type`,`transaction_amt`,
`closing_balance`, `create_dt`)  VALUES (UUID(), 1865764534, 1, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 'Coffee Shop', 'Withdrawal', 30,34500,DATE_SUB(CURDATE(), INTERVAL 7 DAY));

INSERT INTO `account_transactions` (`transaction_id`, `account_number`, `customer_id`, `transaction_dt`, `transaction_summary`, `transaction_type`,`transaction_amt`,
`closing_balance`, `create_dt`)  VALUES (UUID(), 1865764534, 1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'Uber', 'Withdrawal', 100,34400,DATE_SUB(CURDATE(), INTERVAL 6 DAY));

INSERT INTO `account_transactions` (`transaction_id`, `account_number`, `customer_id`, `transaction_dt`, `transaction_summary`, `transaction_type`,`transaction_amt`,
`closing_balance`, `create_dt`)  VALUES (UUID(), 1865764534, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'Self Deposit', 'Deposit', 500,34900,DATE_SUB(CURDATE(), INTERVAL 5 DAY));

INSERT INTO `account_transactions` (`transaction_id`, `account_number`, `customer_id`, `transaction_dt`, `transaction_summary`, `transaction_type`,`transaction_amt`,
`closing_balance`, `create_dt`)  VALUES (UUID(), 1865764534, 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'Ebay', 'Withdrawal', 600,34300,DATE_SUB(CURDATE(), INTERVAL 4 DAY));

INSERT INTO `account_transactions` (`transaction_id`, `account_number`, `customer_id`, `transaction_dt`, `transaction_summary`, `transaction_type`,`transaction_amt`,
`closing_balance`, `create_dt`)  VALUES (UUID(), 1865764534, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'OnlineTransfer', 'Deposit', 700,35000,DATE_SUB(CURDATE(), INTERVAL 2 DAY));

INSERT INTO `account_transactions` (`transaction_id`, `account_number`, `customer_id`, `transaction_dt`, `transaction_summary`, `transaction_type`,`transaction_amt`,
`closing_balance`, `create_dt`)  VALUES (UUID(), 1865764534, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Amazon.com', 'Withdrawal', 100,34900,DATE_SUB(CURDATE(), INTERVAL 1 DAY));

*
*/