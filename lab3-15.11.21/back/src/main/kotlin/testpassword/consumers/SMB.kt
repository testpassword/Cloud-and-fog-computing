package testpassword.consumers

import com.microsoft.azure.storage.CloudStorageAccount
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

object SMB {

    private fun parseCreds(creds: String): String {
        val (address, key) = creds.split(";")
        val name = address.split(".").first().split("//")[1]
        return "DefaultEndpointsProtocol=https;AccountName=${name};AccountKey=${key}"
    }

    operator fun invoke(creds: String, productPath: Path) =
        CloudStorageAccount
            .parse(parseCreds(creds))
            .createCloudFileClient()
            .getShareReference("optreports")
            .also { it.createIfNotExists() }
            .rootDirectoryReference
            .getFileReference(productPath.name)
            .uploadFromFile(productPath.absolutePathString())
}