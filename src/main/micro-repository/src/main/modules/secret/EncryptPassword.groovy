
import org.mx.oauth.client.PWSec
import org.quartz.JobExecutionException

try {
    if (args.length == 0) {
        throw new JobExecutionException("Password parameter is required")
    }

    def password = args[0]
    def encryptPassword = (new PWSec()).encrypt(password)

    println " "
    println "Password encrypted : $encryptPassword"
} catch (Throwable t) {
    t.printStackTrace()
}


