import com.demo.security.spring.model.Account
import com.demo.security.spring.model.AccountTransaction
import com.demo.security.spring.model.EntityControlDates
import com.demo.security.spring.model.EntityCreatedDate
import com.demo.security.spring.model.SecurityAuthority
import com.demo.security.spring.model.SecurityUser
import com.demo.security.spring.model.TransactionType
import com.demo.security.spring.model.UserType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.common.base.Preconditions
import net.datafaker.Faker
import org.apache.commons.lang3.StringUtils

import java.math.RoundingMode
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Steps to use:
 * - in intelliJ, go to Tools -> Groovy Console
 * - copy paste this script into the Groovy Console
 * - execute the script:
 *   - either using the play button OR
 *   - press ctrl + a to select all and then press ctrl + enter to execute it
 *   - select primary/only module as the classpath to use if necessary
 *   - the output file example-users.json should be updated
 *
 * NOTE:
 * Some versions of intelliJ on some platforms have a bug where executions of groovy consoles fail after the first run.
 * A workaround to that issue is to kill the existing groovy process between executions of the script.
 */

new Generator().execute()

class Generator {

    private static final String DEFAULT_OUTPUT_FILE_PATH = "./src/main/resources/seed/example-users.json"
    private static final BigDecimal DEFAULT_STARTING_BALANCE = BigDecimal.valueOf(500.00)
    private static final String DEFAULT_TESTING_PASSWORD = "password"
    private static final int RANDOM_USER_COUNT = 20

    private final Faker faker = new Faker()
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())

    private final boolean overwriteFile
    private final String outputFilePath

    Generator() {
        this(DEFAULT_OUTPUT_FILE_PATH)
    }

    Generator(String outputFilePath) {
        this(outputFilePath, true)
    }

    Generator(String outputFilePath, boolean overwriteFile) {
        Preconditions.checkArgument(StringUtils.isNotBlank(outputFilePath), "File path cannot be empty")
        this.overwriteFile = overwriteFile
        this.outputFilePath = outputFilePath
    }

    void execute() {
        final File outputFile = new File((String) outputFilePath)
        if (!outputFile.exists() || overwriteFile) {
            outputFile.createNewFile()
        }
        Preconditions.checkArgument(outputFile.exists(), "Output file for user generation does not exist")
        Preconditions.checkArgument(outputFile.canWrite(), "Write permissions to file are required in order to run user generation")
        println("Starting user generation")
        final List<SecurityUser> generatedUsers = generateUsers()
        println("Generated ${generatedUsers.size()} users")
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, generatedUsers)
        println("Finished writing generated users to file ${outputFilePath}")
    }

    List<SecurityUser> generateUsers() {
        final List<SecurityUser> users = [
                generateUser("user", false),
                generateUser("admin", true),
                generateUser("otherUser", false),
                generateUser("otherAdmin", true, List.of("ROLE_ADMIN", "ROLE_USER")),
                disable(generateUser("userDisabled", false)),
                disable(generateUser("adminDisabled", true))
        ]
        for (int i = 0; i < RANDOM_USER_COUNT; i++) {
            final String username = faker.internet().username()
            users.add(faker.random().nextBoolean() ? generateRandomActiveUser(username) : generateRandomUser(username))
        }
        return users
    }

    private SecurityUser generateRandomActiveUser(String username) {
        final String type = faker.random().nextBoolean() ? "external" : "internal"
        return generateUser(
                username,
                faker.internet().password(),
                type,
                type == "external" ? [ "ROLE_USER" ] : [ "ROLE_ADMIN" ],
                true,
                false,
                false,
                false
        )
    }

    private SecurityUser generateRandomUser(String username) {
        final String type = faker.random().nextBoolean() ? "external" : "internal"
        return generateUser(
                username,
                faker.internet().password(),
                type,
                type == "external" ? [ "ROLE_USER" ] : [ "ROLE_ADMIN" ],
                faker.random().nextBoolean(),
                faker.random().nextBoolean(),
                faker.random().nextBoolean(),
                faker.random().nextBoolean()
        )
    }

    private SecurityUser generateUser(String username, boolean isInternal) {
        return isInternal ? generateExternalUser(username, DEFAULT_TESTING_PASSWORD) : generateInternalUser(username, DEFAULT_TESTING_PASSWORD)
    }

    private SecurityUser generateUser(String username, boolean isInternal, List<String> roles) {
        final SecurityUser user = isInternal ? generateExternalUser(username, DEFAULT_TESTING_PASSWORD) : generateInternalUser(username, DEFAULT_TESTING_PASSWORD)
        user.setAuthorities(toAuthorities(roles))
        return user
    }

    private SecurityUser generateExternalUser(String username, String password) {
        return generateUser(username, password, "external", List.of("ROLE_USER"), true, false, false, false)
    }

    private SecurityUser generateInternalUser(String username, String password) {
        return generateUser(username, password, "internal", List.of("ROLE_ADMIN"), true, false, false, false)
    }

    private SecurityUser generateUser(
            String username,
            String password,
            String type,
            List<String> roles,
            boolean enabled,
            boolean accountExpired,
            boolean passwordExpired,
            boolean isLocked
    ) {
        final SecurityUser user = new SecurityUser()
        user.setUsername(username)
        user.setPassword(password)
        user.setEnabled(enabled)
        user.setAccountExpired(accountExpired)
        user.setAccountExpiredDate(accountExpired ? randomPastDate() : null)
        user.setPasswordExpired(passwordExpired)
        user.setPasswordExpiredDate(passwordExpired ? randomPastDate() : null)
        user.setLocked(isLocked)
        user.setLockedDate(isLocked ? randomPastDate() : null)
        // user type and role will be replaced in the future with better role-based structures
        user.setUserType(UserType.valueOf(type))
        user.setUserRole(user.getUserType() == UserType.internal ? "ADMIN" : "STANDARD")
        user.setAuthorities(toAuthorities(roles))
        user.setEmail(username + "@demo.com")
        user.setAccount(generateAccount())
        user.setControlDates(randomEntityControlDates())
        return user
    }

    private SecurityUser expireAccount(final SecurityUser user) {
        user.setAccountExpired(true)
        user.setAccountExpiredDate(randomPastDate())
        return user
    }

    private SecurityUser expirePassword(final SecurityUser user) {
        user.setPasswordExpired(true)
        user.setPasswordExpiredDate(randomPastDate())
        return user
    }

    private SecurityUser disable(final SecurityUser user) {
        user.setEnabled(false)
        return user
    }

    private SecurityUser lock(final SecurityUser user) {
        user.setLocked(true)
        user.setLockedDate(randomPastDate())
        return user
    }

    private List<SecurityAuthority> toAuthorities(List<String> roles) {
        roles.collect { role ->
            SecurityAuthority authority = new SecurityAuthority()
            authority.setAuthority(role)
            return authority
        }
    }

    private Account generateAccount() {
        final Account account = new Account()
        account.setAccountType(faker.random().nextBoolean() ? "Savings" : "Checking")
        account.setBranchAddress(faker.address().fullAddress())
        account.setCreatedDate(randomEntityCreatedDate());
        final int upperBoundInclusive = faker.random().nextInt(5, 29)
        final List<AccountTransaction> transactions = new ArrayList<>()
        BigDecimal previousClosingBalance = DEFAULT_STARTING_BALANCE
        for (int i = 0; i < upperBoundInclusive; i++) {
            transactions.add(generateAccountTransaction(previousClosingBalance))
            previousClosingBalance = transactions.getLast().getClosingBalance()
        }
        account.setAccountTransactions(transactions)
        return account
    }

    private AccountTransaction generateAccountTransaction(BigDecimal previousClosingBalance) {
        final AccountTransaction accountTransaction = new AccountTransaction()
        accountTransaction.setCreatedDate(randomEntityCreatedDate())
        accountTransaction.setTransactionDate(randomPastDate())
        accountTransaction.setTransactionSummary(faker.commerce().productName())
        // add other transaction types as needed
        accountTransaction.setTransactionType(faker.random().nextBoolean() ? TransactionType.Withdrawal : TransactionType.Deposit)
        accountTransaction.setTransactionAmount(BigDecimal.valueOf(Math.abs(faker.random().nextDouble())).setScale(2, RoundingMode.HALF_EVEN))
        if (accountTransaction.getTransactionType() == TransactionType.Withdrawal) {
            accountTransaction.setClosingBalance(previousClosingBalance.subtract(accountTransaction.getTransactionAmount()).setScale(2, RoundingMode.HALF_EVEN));
        } else {
            accountTransaction.setClosingBalance(previousClosingBalance.add(accountTransaction.getTransactionAmount()).setScale(2, RoundingMode.HALF_EVEN));
        }
        return accountTransaction
    }

    private ZonedDateTime randomPastDate() {
        return randomPastDate(100, TimeUnit.DAYS)
    }

    private ZonedDateTime randomPastDate(int atMost, TimeUnit timeUnit) {
        return ZonedDateTime.ofInstant(faker.date().past(atMost, timeUnit).toInstant(), ZoneId.systemDefault())
    }

    private ZonedDateTime randomPastDate(int atMost, int min, TimeUnit timeUnit) {
        return ZonedDateTime.ofInstant(faker.date().past(atMost, min, timeUnit).toInstant(), ZoneId.systemDefault())
    }

    private EntityCreatedDate randomEntityCreatedDate() {
        final EntityCreatedDate entityCreatedDate = new EntityCreatedDate()
        entityCreatedDate.setCreated(randomPastDate())
        return entityCreatedDate
    }

    private EntityControlDates randomEntityControlDates() {
        final EntityControlDates entityControlDates = new EntityControlDates()
        entityControlDates.setCreated(randomPastDate())
        entityControlDates.setLastUpdated(ZonedDateTime.now())
        return entityControlDates
    }
}