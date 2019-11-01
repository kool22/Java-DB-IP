package co.santainside.dbip

import `in`.ankushs.dbip.api.GeoEntity
import `in`.ankushs.dbip.lookup.GeoEntityLookupService
import `in`.ankushs.dbip.lookup.GeoEntityLookupServiceImpl
import `in`.ankushs.dbip.repository.SmallMapDBDbIpRepositoryImpl
import com.google.common.net.InetAddresses.forString
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileOutputStream
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/")
class IpController(
        private val resourceLoader: ResourceLoader
) {
    lateinit var service: GeoEntityLookupService

    @PostConstruct
    fun init() {

        val dbInputStream = resourceLoader.getResource("classpath:dbip.db").inputStream
        val userPath = System.getProperty("user.dir")
        val dbPath = "$userPath/dbip.db"
        val output = FileOutputStream(dbPath)
        dbInputStream.use { input ->
            output.use { fileOut -> input.copyTo(fileOut) }
        }
        service = GeoEntityLookupServiceImpl(
                SmallMapDBDbIpRepositoryImpl(File(dbPath)))
    }

    @RequestMapping("/{ip}")
    fun ip(@PathVariable ip: String): GeoEntity {
        return service.lookup(forString(ip))
    }

    @ExceptionHandler(Throwable::class)
    fun handleError(req: HttpServletRequest, ex: Throwable): ResponseEntity<Map<String, Any?>> {
        return ResponseEntity(mapOf(
                "url" to req.requestURL,
                "message" to ex.message,
                "code" to HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST)
    }
}
