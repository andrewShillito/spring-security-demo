import com.demo.security.spring.generate.ContactMessagesFileGenerator
import com.demo.security.spring.generate.NoticeDetailsFileGenerator
import com.demo.security.spring.generate.UserFileGenerator

/**
 * A groovy script to use for generating random dev environment data.
 * Generates the following example data into three different files for seeding into db:
 *   - users with authorities, accounts & associated accountTransactions, cards, and loans
 *   - contact messages
 *   - noticeDetails
 * Can be used manually in a groovy console.
 *
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

new UserFileGenerator("example-users.json").write()
new ContactMessagesFileGenerator("example-contact-messages.json").write()
new NoticeDetailsFileGenerator("example-notice-details.json").write()